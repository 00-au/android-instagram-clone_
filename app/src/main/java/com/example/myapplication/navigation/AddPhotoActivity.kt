package com.example.myapplication.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.navigation.model.contentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*



class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM=0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //스토리지 초기화

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        //액티비티 화면이 열리자마자 앨범을 오픈해주는것
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)

        //버튼 이벤트
        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){ //앨범선택
            if(resultCode == Activity.RESULT_OK) //사진을 선택 했을 때, OK일 때 이미지 경로가 넘어오게 됨
            {
                photoUri = data?.data //포토uri에 경로를 담아준다.
                addphoto_image.setImageURI(photoUri) //선택한 이미지에 넣음
            }
            else{   //취소버튼을 눌렀을 때, 작동하는 부분
                finish()
            }
        }
    }
    fun contentUpload(){
        //파일 이름을 만듬
        var timestamp = SimpleDateFormat("yyyyMMdd__HHmmss").format(Date()) //중복금지시킴
        var imageFileName = "IMAGE_"+ timestamp+"_.png" //파일이름만들기

        var storageRef = storage?.reference?.child("images")?.child(imageFileName) //스토리지에 저장

        //파일 업로드 (promise 방식)
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: com.google.android.gms.tasks.Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { url ->

            var contentDTO = contentDTO()

            //다운로드 url을 이미지에 넣어줌
            contentDTO.imageUrl = url.toString()

            //유저에 넣어줌
            contentDTO.uid = auth?.currentUser?.uid

            //유저 아이디를 넣어줌
            contentDTO.userId = auth?.currentUser?.email

            //설명을 넣어줌
            contentDTO.explain = addphoto_edit_explain.text.toString()

            //시간을 넣어줌
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)

            //이미지 컬렉션 안에다가 데이터를 넣어줌 ^)

            setResult(Activity.RESULT_OK) //업로드 완료

            finish()

        }
        //파일 업로드 (callback 방식)

    }
}