package com.example.myapplication.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.navigation.model.AlarmDTO
import com.example.myapplication.navigation.model.contentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*


class DetailViewFragment : Fragment(){

    var firestore : FirebaseFirestore? = null //전역변수로 firestore 불러오기 DB접근
    var uid : String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)
        firestore = FirebaseFirestore.getInstance() //파이어베이스 초기화
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<contentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init {

            //DB 접근해서 데이터를 받아올 수 있게 쿼리를 만들기

            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, _ ->
                contentDTOs.clear() //데이터베이스를 일단 초기화
                contentUidList.clear() // 초기화 2
                if(querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot!!.documents) //넘어오는 데이터들을 하나하나씩 받아들이기 시작
                {
                    var item = snapshot.toObject(contentDTO::class.java)
                    contentDTOs.add(item!!) //데이터베이스에 담아줌
                    contentUidList.add(snapshot.id)
                }

                notifyDataSetChanged() //새로고침해줌

            }
        }
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_detail,p0,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        //recyclerview 넘겨주기
        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        //서버에서 넘어오는 데이터들을 맵핑시키는 부분
        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHolder).itemView
            //유저 아이디 맵핑
            viewholder.detailviewitem_profile_text.text = contentDTOs!![p1].userId
            //이미지 맵핑
            Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUrl).into(viewholder.detailviewitem_imageview_content)

            //설명글 맵핑
            viewholder.detailviewitem_explain_textview.text = contentDTOs!![p1].explain

            //좋아요 카운터 맵핑
            viewholder.detailviewitem_favorite_counter_textview.text = "Likes" + contentDTOs!![p1].favoritCount

            //좋아요 버튼에 이벤트 달기
            viewholder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(p1)
            }
            //좋아요 카운터와 하트가 색칠 / 비어있는 이벤트 달기
            if(contentDTOs!![p1].favorites.containsKey(uid)){
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)

            }else{
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)

            }

            //프로필 이미지 클릭 시
            viewholder.detailviewitem_profile_image.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs [p1].uid) //유저아이디 받아오기
                bundle.putString("userId", contentDTOs [p1].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }

            //말풍선 클릭 시  commentActivity가 띄워지게
            viewholder.detailviewitem_comment_imageview.setOnClickListener { v ->
                var intent = Intent(v.context,CommentActivity::class.java)
                //contentuid를 넣어줌
                intent.putExtra("contentUid",contentUidList[p1])
                intent.putExtra("destinationUid",contentDTOs[p1].uid)
                startActivity(intent)
            }

            //유저 프로필 이미지 맵핑
            Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUrl).into(viewholder.detailviewitem_profile_image)
        }
        fun favoriteEvent(position : Int) {
            var tsDos = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction{transaction ->
                var contentDTO = transaction.get(tsDos!!).toObject(contentDTO::class.java)

                //좋아요 버튼이 이미 클릭되어 있을 경우, 아닐 경우
                if(contentDTO!!.favorites.containsKey(uid)){
                    //좋아요 버튼 눌림 클릭 -> 클릭 취소
                    contentDTO.favoritCount = contentDTO?.favoritCount -1
                    contentDTO?.favorites.remove(uid)

                }else{
                    //클릭되지 않음 -> 클릭
                    contentDTO?.favoritCount = contentDTO?.favoritCount +1
                    contentDTO?.favorites[uid!!] = true
                    favoriteAlarm(contentDTOs[position].uid!!)

                }

                transaction.set(tsDos,contentDTO)

            }
        }
    }
    //함수에다가 DTO의 값을 설정
    fun favoriteAlarm(destinationUid : String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.kind = 0
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }
}