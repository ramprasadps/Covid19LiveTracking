package com.ram.covid19livetracking.fragment

import android.os.Bundle
import androidx.leanback.app.DetailsFragment
import androidx.leanback.app.DetailsFragmentBackgroundController
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter

import com.google.gson.Gson
import com.ram.covid19livetracking.model.StateDataModel
import com.ram.covid19livetracking.presenter.DetailCardPresenter
import org.json.JSONObject

class DetailsFragment : DetailsFragment() {


    private lateinit var mDetailsBackground: DetailsFragmentBackgroundController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getIntentData("state")
        mDetailsBackground = DetailsFragmentBackgroundController(this)
        var districtDataMap: HashMap<String, StateDataModel.DistrictDataModel> =
            Gson().fromJson(JSONObject(getIntentData("districtData")).toString(), HashMap::class.java) as HashMap<String, StateDataModel.DistrictDataModel>
        showDistrictData(getIntentData("district"), districtDataMap)
    }

    private fun showDistrictData(state: String, districtDataMap: HashMap<String, StateDataModel.DistrictDataModel>){
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = DetailCardPresenter()
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            for (districtData in districtDataMap) {
                listRowAdapter.add(districtData)
            }
            val header = HeaderItem(0, state)
            rowsAdapter.add(ListRow(header, listRowAdapter))
        adapter = rowsAdapter
    }

    private fun getIntentData(key: String): String{
        return activity?.intent?.getSerializableExtra(key).toString()
    }

}