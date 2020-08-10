package com.example.myapplication.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R
import com.example.myapplication.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*

class AlarmFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm,container,false)
        view.alarmfragment_recyclerview.adapter = AlarmRecyclerviewAdapter()
        view.alarmfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class AlarmRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
       //알람을 저장하는 리스트 변수 만들기
        var alarmDTOList : ArrayList<AlarmDTO> = arrayListOf()

        init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                alarmDTOList.clear()
                //array 값을 깨끗하게 지워준다음 값을 담는다.
                if(querySnapshot == null) return@addSnapshotListener

                for(snapshot in querySnapshot.documents){
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            //아이템 디자인을 불러오기
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_comment,p0,false)

            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmDTOList.size

            }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

            //종류에 따라 메세지를 다르게 표시
            var view = p0.itemView

            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[p1].uid!!).get().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val url = task.result!!["image"]
                    Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentviewitem_imageview_profile)
                }
            }

            //스위치와 비슷
            when(alarmDTOList[p1].kind){
                0 -> {
                    //0번일 때 useid를 받아서 알람 좋아요를 눌렀다는 메세지를 띄움
                    val str_0 = alarmDTOList[p1].userId + getString(R.string.alarm_favorite)
                    view.commentviewitem_textview_profile.text = str_0

                }
                1 -> {
                    //1번일 때 useid를 받아서 알람 코멘트 메세지를 띄움
                    val str_1 = alarmDTOList[p1].userId + " " + getString(R.string.alarm_comment)+" of "+alarmDTOList[p1].message
                    view.commentviewitem_textview_profile.text = str_1

                }
                2 -> {
                    //2번일 때 useid를 받아서 알람 팔로우 메세지를 띄움
                    val str_2 = alarmDTOList[p1].userId + " " + getString(R.string.alarm_follow)
                    view.commentviewitem_textview_profile.text = str_2

                }
            }

            view.commentviewitem_textview_comment.visibility = View.INVISIBLE
        }
    }

}