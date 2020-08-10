package com.example.myapplication.navigation.model

data class AlarmDTO(
    var destinationUid : String? = null,
    var userId : String? = null,
    var uid : String? = null,
    //kind는 알람을 구분해주는 find값 comment 1 like 0 follower 2
    var kind : Int? = null,
    var message :  String? = null,
    var timestamp: Long? = null
)