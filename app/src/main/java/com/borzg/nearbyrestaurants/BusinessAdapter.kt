package com.borzg.nearbyrestaurants

import SearchResponseQuery.Business
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.borzg.nearbyrestaurants.databinding.LiBusinessBinding
import com.borzg.nearbyrestaurants.utils.GlideApp
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.lang.StringBuilder

class BusinessAdapter(val onItemClickListener: (Business) -> Unit) :
    PagingDataAdapter<Business, BusinessAdapter.BusinessViewHolder>(BusinessItemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return BusinessViewHolder(LiBusinessBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it, onItemClickListener) }
    }

    inner class BusinessViewHolder(private val binding: LiBusinessBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(business: Business, onItemClickListener: (Business) -> Unit) {
            with(binding) {
                name.text = business.name ?: "No name"
                distance.text = business.distance?.toMeters() ?: "Unknown"
                business.photos?.get(0)?.let { photoUrl ->
                    GlideApp.with(photo)
                        .load(actualPhotoUrl(photoUrl))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(photo)
                }
                root.setOnClickListener {
                    onItemClickListener.invoke(business)
                }
            }
        }
    }

    /**
     * If the link previously pointed to a very large image (o) then it converts it into a link which points to a smaller image (ls)
     */
    fun actualPhotoUrl(photoUrl: String): String {
        val builder = StringBuilder()
        val list = photoUrl.split("/").toMutableList()
        if (list[list.size - 1] == "o.jpg") list[list.size - 1] = "ls.jpg"
        else return photoUrl
        repeat(list.size - 1) { builder.append(list[it]).append("/") }
        builder.append(list.last())
        return builder.toString()
    }

    object BusinessItemDiffCallback : DiffUtil.ItemCallback<Business>() {

        override fun areItemsTheSame(oldItem: Business, newItem: Business): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Business, newItem: Business): Boolean {
            return oldItem == newItem
        }
    }
}

