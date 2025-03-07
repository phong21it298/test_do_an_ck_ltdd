package com.example.test_doan_ck_01

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.test_doan_ck_01.adapter.HistoryAdapter
import com.example.test_doan_ck_01.database.AnimeDatabaseModel
import com.example.test_doan_ck_01.databinding.ActivityHistoryBinding
import com.example.test_doan_ck_01.databinding.ItemHistoryDialogBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private lateinit var binding: ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var history_adt: HistoryAdapter
    private lateinit var history: ArrayList<AnimeDatabaseModel>
    private lateinit var dbRef: DatabaseReference
    private lateinit var item_history_dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Khởi tạo viewbinding
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        history = arrayListOf<AnimeDatabaseModel>()

        getDataHistory()
    }

    private fun getDataHistory() {

        dbRef = FirebaseDatabase.getInstance().getReference("AnimeSeach")

        dbRef.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                history.clear()

                //snapshot sẽ xem coi table trong database của ta có phần tử ko
                //có thì nó return về true
                if(snapshot.exists()){

                    for(itemSnap in snapshot.children){

                        val itemData = itemSnap.getValue(AnimeDatabaseModel::class.java)

                        if (itemData != null) {

                            Log.d("FirebaseImage", "Image URL: ${itemData.animeImage}") // Thêm dòng này để debug

                            history.add(itemData)
                        }
                    }

                    //Đưa list data vào EmpAdapter để dán lên RecyclerView
                    history_adt = HistoryAdapter(history, object: HistoryAdapter.OnItemClickListenerHistory{

                        override fun onItemClickHistory(history: AnimeDatabaseModel) {

                            //Để đây đã, hiện lên cái button để có xóa đi hay không thôi.
                            Toast.makeText(this@HistoryActivity, "Click History Item", Toast.LENGTH_SHORT).show()
                            showDialogHistoryBinding(history)
                        }
                    })

                    binding.rvHistory.adapter = history_adt

                    binding.rvHistory.layoutManager = LinearLayoutManager(

                        this@HistoryActivity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )

                    binding.txtLoading.visibility = View.GONE
                    binding.rvHistory.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun showDialogHistoryBinding(history: AnimeDatabaseModel){

        val build = AlertDialog.Builder(this@HistoryActivity)

        val dialogBinding = ItemHistoryDialogBinding.inflate(LayoutInflater.from(this@HistoryActivity))

        build.setView(dialogBinding.root)

        //Xử lý data gán lên dialog
        //Dùng Glide để load ảnh từ URL
        Glide.with(this@HistoryActivity)
            .load(history.animeImage)
            .placeholder(R.drawable.ic_placeholder_image)  //Ảnh tạm khi đang tải
            .error(R.drawable.ic_error_image)  //Ảnh hiển thị khi lỗi
            .into(dialogBinding.ivItemHistory)

        //Delete history
        dialogBinding.btnDelete.setOnClickListener {

            deleteHistory(history.animeId.toString())

            item_history_dialog.dismiss()
        }

        //Update history
        dialogBinding.btnUpdate.setOnClickListener {

            updateHistory(history)

            item_history_dialog.dismiss()
        }

        item_history_dialog = build.create()

        item_history_dialog.show()
    }

    private fun deleteHistory(animeId: String){

        dbRef = FirebaseDatabase.getInstance().getReference("AnimeSeach").child(animeId)

        val rmTask = dbRef.removeValue()

        rmTask.addOnSuccessListener {

            Toast.makeText(this@HistoryActivity, "Remove completed", Toast.LENGTH_SHORT).show()

            getDataHistory()
        }.addOnFailureListener {err -> //Nếu xóa không thành công

            Toast.makeText(this@HistoryActivity, "Delete error: ${err.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateHistory(history: AnimeDatabaseModel){

        val build = AlertDialog.Builder(this@HistoryActivity)

        val inflater = layoutInflater

        val mDialogView = inflater.inflate(R.layout.update_history_dialog, null)

        build.setView(mDialogView)
        build.setTitle("Updating ${history.animeName} record")

        val alertDialog = build.create()
        alertDialog.show()

        val btnUpdateData = mDialogView.findViewById<Button>(R.id.btn_update_history)
        val editNameHistory = mDialogView.findViewById<EditText>(R.id.et_name_history)

        editNameHistory.setText(history.animeName)

        btnUpdateData.setOnClickListener{

            val newName = editNameHistory.text.toString()

            if(newName.isNotEmpty()){

                dbRef = FirebaseDatabase.getInstance().getReference("AnimeSeach").child(history.animeId.toString())

                val oUpdate = AnimeDatabaseModel(history.animeId, newName, history.animeImage, history.animeDateTime)

                dbRef.setValue(oUpdate).addOnSuccessListener{

                    Toast.makeText(applicationContext, "Update completed", Toast.LENGTH_SHORT).show()

                    getDataHistory()

                    alertDialog.dismiss() //Đóng hộp thoại sau khi cập nhật thành công
                }.addOnFailureListener{err ->

                    Toast.makeText(applicationContext, "Update error: ${err.message}", Toast.LENGTH_SHORT).show()
                }
            }else{

                Toast.makeText(applicationContext, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}