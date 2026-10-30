package com.hew.aa_firebase20251641
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import com.hew.aa_firebase20251641.LostFoundItem
import com.hew.aa_firebase20251641.MyApplication.Companion.db
import com.hew.aa_firebase20251641.R
import com.hew.aa_firebase20251641.databinding.ItemLostFoundBinding

// 뷰홀더 클래스
class ItemViewHolder(
    private val binding: ItemLostFoundBinding,
    private val currentUser: String, // 현재 로그인한 사용자
    private val onItemClick: (LostFoundItem) -> Unit, // 항목 클릭시 호출
    private val onItemLongClick: (LostFoundItem) -> Unit // 항목 길게 클릭시 호출
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: LostFoundItem) = with(binding) {
        ivItem.setImageResource(R.drawable.img)
        tvItemName.text = item.name
        tvPlace.text = "장소: ${item.place}"
        tvDate.text = "날짜: ${item.date}"

        if (item.type == LostFoundItem.TYPE_LOST) {
            tvType.text = root.context.getString(R.string.type_lost)
            tvType.setBackgroundResource(R.drawable.bg_badge_lost)
        } else {
            tvType.text = root.context.getString(R.string.type_found)
            tvType.setBackgroundResource(R.drawable.bg_badge_found)
        }

        if (item.status == LostFoundItem.STATUS_COMPLETED) {
            tvStatus.text = root.context.getString(R.string.status_completed)
            tvStatus.setBackgroundResource(R.drawable.bg_badge_done)
        } else {
            tvStatus.text = root.context.getString(R.string.status_open)
            tvStatus.setBackgroundResource(R.drawable.bg_badge_open)
        }

        tvOwner.text = if (item.ownerId == currentUser) {
            root.context.getString(R.string.written_by_me, item.ownerId)
        } else {
            root.context.getString(R.string.written_by, item.ownerId)
        }

        root.setOnClickListener { onItemClick(item) }
        root.setOnLongClickListener {
            onItemLongClick(item)
            return@setOnLongClickListener true
        }
    }
}

// 어답터 클래스
class LostFoundAdapter(
    private val currentUser: String,
    private val onItemClick: (LostFoundItem) -> Unit,
    private val onItemLongClick: (LostFoundItem) -> Unit,
    private val toast: (String) -> Unit // 어답터 클래스 안에서 토스트 창을 띄워야 할 때 호출하는 함수
) : RecyclerView.Adapter<ItemViewHolder>() {

    private var allItems: MutableList<LostFoundItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemLostFoundBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding, currentUser, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(allItems[position])
    }

    override fun getItemCount(): Int {
        return  allItems.size
    }

    fun loadItems() {
        // firestore에서 해당 컬렉션의 모든 document를 가져와서 리사이클러뷰에 반영
        val colref : CollectionReference = db.collection(COLLECTION_ITEMS)
        colref.orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
        val task = colref.get()
        task.addOnSuccessListener {
            val list = mutableListOf<LostFoundItem>()
            for(document in it){
                val item = document.toObject(LostFoundItem::class.java)
                list.add(item)
            }
            allItems = list
            notifyDataSetChanged()
        }
        task.addOnFailureListener {
            toast("읽기 실패")
        }
    }

    fun addItem(item: LostFoundItem) {
        // firestore에 항목 추가
        val colref : CollectionReference = db.collection(COLLECTION_ITEMS)
        val docref: DocumentReference = colref.document("item${item.id}")
        val task = docref.set(item)
        task.addOnSuccessListener {
            toast("항목 추가 성공")
            allItems.add(item)
            notifyDataSetChanged()
        }
        //allItems.add(item)
        //notifyDataSetChanged()
        task.addOnFailureListener{
            toast("항목 추가 실패")
        }
    }
       
    fun updateItem(item: LostFoundItem) {
        if (item.id==-1L) {
            toast("수정할 게시물 ID가 없습니다.")
            return
        }

        // firestore에 수정된 항목 반영
        val colref : CollectionReference = db.collection(COLLECTION_ITEMS)
        val docref: DocumentReference = colref.document("item${item.id}")
        val task = docref.set(item)
        task.addOnSuccessListener {
            toast("항목 수정 성공")
            notifyDataSetChanged()
        }
        //notifyDataSetChanged()
        task.addOnFailureListener{
            toast("항목 수정 실패")
        }
    }

    fun deleteItem(item: LostFoundItem) {
        if (item.id == -1L) return

       // 삭제
        db.collection(COLLECTION_ITEMS)
            .document("item${item.id}")
            .delete()
            .addOnSuccessListener {
                toast("게시물을 삭제했습니다.")
                allItems.remove(item)
                notifyDataSetChanged()
            }
            .addOnFailureListener { error ->
                toast("게시물 삭제에 실패했습니다: ${error.localizedMessage}")
            }

    }

    companion object {
        private const val COLLECTION_ITEMS = "lostFoundItems"
        private const val FIELD_CREATED_AT = "createdAt"
    }
}