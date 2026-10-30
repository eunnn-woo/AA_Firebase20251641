package com.hew.aa_firebase20251641

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.hew.aa_firebase20251641.LostFoundAdapter
import com.hew.aa_firebase20251641.LostFoundItem
import com.hew.aa_firebase20251641.MyApplication.Companion.auth
import com.hew.aa_firebase20251641.R
import com.hew.aa_firebase20251641.databinding.ActivityMainBinding
import com.hew.aa_firebase20251641.databinding.DialogItemFormBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: LostFoundAdapter

    private var currentUser = "student1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        currentUser = auth.currentUser?.email!!

        adapter = LostFoundAdapter(currentUser, { item -> // 항목 클릭시 호출되는 함수
            showItemFormDialog(item)
        }, {item -> // 항목 길게 클릭시 호출되는 함수
            adapter.deleteItem(item)
        },{ message-> // 어답터 클래스 안에서 토스트 창을 띄울 때 호출되는 함수
            toast(message) })

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        adapter.loadItems()

        binding.btnAdd.setOnClickListener {
            showItemFormDialog(null)
        }

        // 뒤로 가기 버튼 클릭 처리
       

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    } // onCreate

    private fun showItemFormDialog(existingItem: LostFoundItem?) {
        if (existingItem != null && existingItem.ownerId != currentUser) {
            Toast.makeText(baseContext, "작성자만 수정가능", Toast.LENGTH_LONG).show()
            return
        }

        val dialogBinding = DialogItemFormBinding.inflate(layoutInflater)

        dialogBinding.tvFormTitle.text = if(existingItem == null) "등록"
        else "수정"

        if (existingItem == null) {
            dialogBinding.etDate.setText(
                SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Calendar.getInstance().time)
            )
        } else {
            dialogBinding.etItemName.setText(existingItem.name)
            dialogBinding.etPlace.setText(existingItem.place)
            dialogBinding.etDate.setText(existingItem.date)
            dialogBinding.etDescription.setText(existingItem.description)
            dialogBinding.spinnerStatus.setSelection(
                if (existingItem.status == LostFoundItem.STATUS_COMPLETED) 1 else 0
            )
            if (existingItem.type == LostFoundItem.TYPE_FOUND) {
                dialogBinding.rbFound.isChecked = true
            } else {
                dialogBinding.rbLost.isChecked = true
            }
        }

        // custom dialog 생성
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()//

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            saveFromDialog(dialogBinding, existingItem, dialog)
        }

        // dialog 보여주기
        dialog.show()
    }

    private fun saveFromDialog(
        dialogBinding: DialogItemFormBinding,
        existingItem: LostFoundItem?,
        dialog: AlertDialog
    ) {
        val name = dialogBinding.etItemName.text.toString().trim()
        val place = dialogBinding.etPlace.text.toString().trim()
        val date = dialogBinding.etDate.text.toString().trim()
        val description = dialogBinding.etDescription.text.toString().trim()

        // 새로 등록하는 경우
        if (existingItem == null) {
            val now = System.currentTimeMillis()
            val item = LostFoundItem(
                id = now - 30000,
                type = if (dialogBinding.rbLost.isChecked) {
                    LostFoundItem.TYPE_LOST
                } else {
                    LostFoundItem.TYPE_FOUND
                },
                name = name,
                place = place,
                date = date,
                status = if (dialogBinding.spinnerStatus.selectedItemPosition == 1) {
                    LostFoundItem.STATUS_COMPLETED
                } else {
                    LostFoundItem.STATUS_OPEN
                },
                description = description,
                ownerId = currentUser,
                createdAt = now
            )
            adapter.addItem(item)
        } else { // 기존의 항목을 수정하는 경우
            existingItem.type = if (dialogBinding.rbLost.isChecked) {
                LostFoundItem.TYPE_LOST
            } else {
                LostFoundItem.TYPE_FOUND
            }
            existingItem.name = name
            existingItem.place = place
            existingItem.date = date
            existingItem.status = if (dialogBinding.spinnerStatus.selectedItemPosition == 1) {
                LostFoundItem.STATUS_COMPLETED
            } else {
                LostFoundItem.STATUS_OPEN
            }
            existingItem.description = description
            adapter.updateItem(existingItem)

        }
        dialog.dismiss()
    }

    fun toast(message: String, long: Boolean = false) {
        Toast.makeText(baseContext, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_logout -> {
                auth.signOut()
                Toast.makeText(baseContext, "로그아웃되었습니다.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
