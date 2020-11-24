package com.borzg.nearbyrestaurants

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.borzg.nearbyrestaurants.database.AppDatabase
import com.borzg.nearbyrestaurants.databinding.ActivityDetailBusinessBinding
import com.borzg.nearbyrestaurants.databinding.ActivityMainBinding
import com.borzg.nearbyrestaurants.utils.GlideApp
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.security.Permission

class DetailBusinessActivity : AppCompatActivity() {

    companion object {
        const val BUSINESS_ID_KEY = "BUSINESS_ID"
        const val BUSINESS_DISTANCE_KEY = "BUSINESS_DISTANCE"
    }

    lateinit var binding: ActivityDetailBusinessBinding
    val viewModel: DetailBusinessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBusinessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ensurePermissionGranted()

        viewModel.businessDao = AppDatabase.getInstance(this).businessDao()

        viewModel.business.observe(this) {
            bindBusinessDetails(it)
            bindCallButton(it)
        }

        val id = intent.getStringExtra(BUSINESS_ID_KEY)
        if (id != null) requestForDetails(id)
        binding.distance.text = intent.getDoubleExtra(BUSINESS_DISTANCE_KEY, 0.0).toMeters()
    }

    private fun requestForDetails(id: String) {
        viewModel.getBusinessDetails(isConnected(), id)
    }

    private fun isConnected(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo()!!.isConnected()
    }

    private fun bindBusinessDetails(business: BusinessDetailsQuery.Business) {
        with(binding) {
            name.text = business.name
            rating.rating = business.rating?.toFloat() ?: 0f
            isClosed.text = getString(R.string.is_open, if (business.is_closed == false) "open" else "closed")
            GlideApp.with(photo)
                .load(business.photos?.get(0))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(photo)
        }
    }

    private fun bindCallButton(business: BusinessDetailsQuery.Business) {
        binding.buttonCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL)
            val uri = "tel:" + business.phone
            intent.data = Uri.parse(uri)
            startActivity(intent)
        }
    }

    private fun ensurePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 102)
            }
        }
    }
}