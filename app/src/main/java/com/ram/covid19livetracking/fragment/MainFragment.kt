/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ram.covid19livetracking.fragment

import java.util.Timer

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.core.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.google.gson.Gson
import com.ram.covid19livetracking.R
import com.ram.covid19livetracking.activity.DetailsActivity
import com.ram.covid19livetracking.model.StateDataModel
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

/**
 * Loads a grid of cards with movies to browse.
 */
class MainFragment : BrowseFragment() {

    private lateinit var mBackgroundManager: BackgroundManager
    private var mDefaultBackground: Drawable? = null
    private lateinit var mMetrics: DisplayMetrics
    private var mBackgroundTimer: Timer? = null

    private val client = OkHttpClient()

    private var districtMap = HashMap<String, StateDataModel.DistrictDataModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onActivityCreated(savedInstanceState)

        prepareBackgroundManager()

        setupUIElements()

        updateLiveData()

        setupEventListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: " + mBackgroundTimer?.toString())
        mBackgroundTimer?.cancel()
    }

    private fun prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(activity)
        mBackgroundManager.attach(activity.window)
        mDefaultBackground = ContextCompat.getDrawable(activity,
            R.drawable.default_background
        )
        mMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(mMetrics)
    }

    private fun setupUIElements() {
        title = getString(R.string.browse_title)
        // over title
        headersState = BrowseFragment.HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // set fastLane (or headers) background coloronItemViewClickedListener
        brandColor = ContextCompat.getColor(activity,
            R.color.fastlane_background
        )
        // set search icon color
        searchAffordanceColor = ContextCompat.getColor(activity,
            R.color.search_opaque
        )
    }

    private fun updateLiveData(){
        val request = Request.Builder()
            .url("https://api.covid19india.org/v2/state_district_wise.json")
            .build()

        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response){
                val resultData = JSONArray(response.body()?.string())
                val cardPresenter = GridItemPresenter()
                activity.runOnUiThread(Runnable {
                    for (stateIndex in 1 until resultData.length()) {
                        var stateDataModel = Gson().fromJson(resultData.getJSONObject(stateIndex).toString(), StateDataModel::class.java)
                        val listRowAdapter = ArrayObjectAdapter(cardPresenter)
                        var districtDataList = stateDataModel.districtData
                        for (districtData in districtDataList) {
                            districtMap[districtData.district] = districtData
                            listRowAdapter.add(districtData.district)
                        }
                        val header = HeaderItem(stateIndex.toLong(), stateDataModel.state)
                        rowsAdapter.add(ListRow(header, listRowAdapter))
                    }
                })
            }
        })
        adapter = rowsAdapter
    }

    private fun setupEventListeners() {
        setOnSearchClickedListener {
            Toast.makeText(activity, "Implement your own in-app search", Toast.LENGTH_LONG)
                    .show()
        }
        onItemViewClickedListener = ItemViewClickedListener()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
                itemViewHolder: Presenter.ViewHolder,
                item: Any,
                rowViewHolder: RowPresenter.ViewHolder,
                row: Row) {

            val intent = Intent(activity, DetailsActivity::class.java)
            intent.putExtra("state", row.headerItem.name)
            intent.putExtra("district", item.toString())
            intent.putExtra("districtData", Gson().toJson(districtMap[item]))
            startActivity(intent)
        }
    }

    private fun updateBackground(uri: String?) {
        val width = mMetrics.widthPixels
        val height = mMetrics.heightPixels
        Glide.with(activity)
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into<SimpleTarget<GlideDrawable>>(
                        object : SimpleTarget<GlideDrawable>(width, height) {
                            override fun onResourceReady(resource: GlideDrawable,
                                                         glideAnimation: GlideAnimation<in GlideDrawable>) {
                                mBackgroundManager.drawable = resource
                            }
                        })
        mBackgroundTimer?.cancel()
    }

    private inner class GridItemPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
            val view = TextView(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(
                GRID_ITEM_WIDTH,
                GRID_ITEM_HEIGHT
            )
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.setBackgroundColor(ContextCompat.getColor(activity,
                R.color.default_background
            ))
            view.setTextColor(Color.WHITE)
            view.gravity = Gravity.CENTER
            return Presenter.ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
            (viewHolder.view as TextView).text = item as String
        }

        override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {}
    }

    companion object {
        private const val TAG = "MainFragment"

        private const val GRID_ITEM_WIDTH = 200
        private const val GRID_ITEM_HEIGHT = 200
    }
}
