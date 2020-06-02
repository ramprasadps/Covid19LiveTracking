package com.ram.covid19livetracking.model

class StateDataModel {
    lateinit var state: String
    lateinit var statecode: String
    var districtData: List<DistrictDataModel> = emptyList()

    class DistrictDataModel {
        var district: String = ""
        var active: Int = 0
        var confirmed: Int = 0
        var deceased: Int = 0
        var recovered: Int = 0
    }
}