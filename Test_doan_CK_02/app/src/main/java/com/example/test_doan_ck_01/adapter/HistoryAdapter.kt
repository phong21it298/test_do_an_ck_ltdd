package com.example.test_doan_ck_01.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test_doan_ck_01.R
import com.example.test_doan_ck_01.database.AnimeDatabaseModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(private val history: ArrayList<AnimeDatabaseModel>, private val history_listener: OnItemClickListenerHistory): RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    interface OnItemClickListenerHistory {

        fun onItemClickHistory(history: AnimeDatabaseModel)
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        //bind được gọi -> mỗi item sẽ được set xử lý event.
        //object lắng nghe event gọi onItemClick (override bên MainActivity).
        fun bind(history: AnimeDatabaseModel){

            itemView.setOnClickListener{

                history_listener.onItemClickHistory(history)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)

        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {

        holder.itemView.apply {

            //history_name
            val history_name = findViewById<TextView>(R.id.txt_history_name)
            history_name.text = history[position].animeName

            //history_image
            val history_image = findViewById<ImageView>(R.id.iv_history_image)
            val imageUrl = history[position].animeImage

            if (!imageUrl.isNullOrEmpty()) {

                Glide.with(context) //Dùng context để load ảnh
                    .load(imageUrl) //Đường dẫn ảnh
                    .placeholder(R.drawable.ic_placeholder_image) //Ảnh hiển thị khi đang tải
                    .error(R.drawable.ic_error_image) //Ảnh hiển thị khi lỗi
                    .into(history_image) //Load vào ImageView
            }

            //history_datetime
            val history_datetime = findViewById<TextView>(R.id.txt_history_datetime)
            val timestamp = history[position].animeDateTime

            if (timestamp != null) {

                val date = Date(timestamp)

                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

                history_datetime.text = sdf.format(date)
            } else {

                history_datetime.text = "N/A"
            }
        }

        //Lấy result thứ i truyền vào bind.
        holder.bind(history[position])
    }

    override fun getItemCount(): Int {

        return history.size
    }
}