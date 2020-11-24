package com.borzg.nearbyrestaurants

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.borzg.nearbyrestaurants.database.AppDatabase
import com.borzg.nearbyrestaurants.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        const val UPDATE_INTERVAL = 100_000L
        const val FASTEST_UPDATE_INTERVAL = 80_000L
    }

    lateinit var binding: ActivityMainBinding

    private val viewModel: MainActivityViewModel by viewModels()

    val onItemClickListener: (SearchResponseQuery.Business) -> Unit = {
        val intent = Intent(this, DetailBusinessActivity::class.java)
        intent.putExtra(DetailBusinessActivity.BUSINESS_ID_KEY, it.id)
        intent.putExtra(DetailBusinessActivity.BUSINESS_DISTANCE_KEY, it.distance)
        startActivity(intent)
    }

    private val businessAdapter: BusinessAdapter by lazy {
        BusinessAdapter(onItemClickListener)
    }

    private var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindBusinessesList()
        bindRefresherLayout()

        initializeLocationUpdates()

        viewModel.businessDao = AppDatabase.getInstance(this).businessDao()
        // Starts a new search when location data updates
        viewModel.coordinateLiveData.distinctUntilChanged().observe(this) { startSearch() }
    }

    private fun initializeLocationUpdates() {
        ensurePermissionGranted()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Updates location data with a certain interval
        val locationRequest =
            LocationRequest.create()?.apply {
                interval = UPDATE_INTERVAL
                fastestInterval = FASTEST_UPDATE_INTERVAL
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

        val callback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult?) {
                    super.onLocationResult(p0)
                    val latitude = p0?.lastLocation?.latitude
                    val longitude = p0?.lastLocation?.longitude
                    if (latitude != null && longitude != null) {
                        viewModel.newCoordinates(Coordinates(latitude, longitude))
                    }
                }
            }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient!!.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )
    }

    private fun ensurePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 101)
            }
        }
    }

    // Starts a new search when user swipe refresher layout
    private fun bindRefresherLayout() {
        binding.refresherLayout.setOnRefreshListener { startSearch() }
    }

    private fun bindBusinessesList() {
        with(binding.businessesList) {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            adapter = businessAdapter
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        }
        businessAdapter.loadStateFlow.asLiveData().observe(this) { loadStates ->
            handleLoadStates(loadStates)
        }
    }

    private fun startSearch() {
        if (fusedLocationClient == null) {
            initializeLocationUpdates()
            setRefreshState(false)
            return
        }
        if (viewModel.coordinateLiveData.value == null) {
            showToastMessage("Location not yet initialized")
            return
        }
        lifecycleScope.launch {
            viewModel.searchNearbyBusinesses(isConnected()).collectLatest { pagingData ->
                businessAdapter.submitData(pagingData)
            }
        }
    }

    private fun isConnected(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo()!!.isConnected()
    }

    private fun handleLoadStates(loadStates: CombinedLoadStates) {
        val state = loadStates.refresh
        setRefreshState(state is LoadState.Loading)
        if (state is LoadState.Loading) return
        if (state is LoadState.Error) {
            showToastMessage(state.error.message)
        } else if (state is LoadState.NotLoading && loadStates.append.endOfPaginationReached && binding.businessesList.adapter?.itemCount == 0)
            showEmptyListLayout()
        else showBusinessesList()
    }

    private fun setRefreshState(isRefreshing: Boolean) {
        binding.refresherLayout.isRefreshing = isRefreshing
    }

    private fun showBusinessesList() {
        with(binding) {
            businessesList.visibility = View.VISIBLE
            emptyListTv.visibility = View.GONE
        }
    }

    private fun showEmptyListLayout() {
        with(binding) {
            emptyListTv.visibility = View.VISIBLE
            businessesList.visibility = View.GONE
        }
    }

    private fun showToastMessage(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}