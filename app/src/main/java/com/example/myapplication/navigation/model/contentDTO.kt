package com.example.myapplication.navigation.model

data class contentDTO(var explain : String? = null,//컨텐츠의 설명을 관리한다.
                        var imageUrl : String? = null, //이미지url 관리
                        var uid : String? = null, //어느 유저가 올렸는지 관리
                        var userId : String? = null, //올린 이미지의 유저아이디를 관리
                        var timestamp: Long? = null,//몇시 몇분에 컨텐츠를 올렸는지 관리
                        var favoritCount : Int = 0,//좋아요를 몇명이 눌렀는지 관리
                        var favorites : MutableMap<String, Boolean> = HashMap())//중복 좋아요를 방지할 수 있도록 누가 눌렀는지 관리
                        {
                            //덧글 관리
                            data class Comment(var uid : String? = null,
                                var userID : String? = null,
                                var comment: String? = null,
                                var timestamp: Long? = null)
                        }