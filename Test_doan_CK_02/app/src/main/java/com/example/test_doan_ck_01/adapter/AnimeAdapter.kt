package com.example.test_doan_ck_01.adapter


import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.example.test_doan_ck_01.R
import com.example.test_doan_ck_01.model.Result

//Nhận listener từ Activity
class AnimeAdapter(private val result: List<Result>, private val anime_listener: OnItemClickListener): RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {

    interface OnItemClickListener {

        fun onItemClick(result: Result)
    }

    inner class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        //bind được gọi -> mỗi item sẽ được set xử lý event.
        //object lắng nghe event gọi onItemClick (override bên MainActivity).
        fun bind(result: Result){

            itemView.setOnClickListener{

                anime_listener.onItemClick(result)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.result_item, parent, false)

        return AnimeViewHolder(view)
    }

    fun formatTime(seconds: Double): String{

        val hours = (seconds / 3600).toInt()
        val minutes = ((seconds % 3600) / 60).toInt()
        val secs = (seconds % 60).toInt()

        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {

        holder.itemView.apply {

            //romaji
            val anime_name = findViewById<TextView>(R.id.txt_romaji)
            anime_name.text = result[position].anilist.title.romaji

            //episode
            val anime_episode = findViewById<TextView>(R.id.txt_episode)
            anime_episode.text = "Episode: ${result[position].episode}"

            //from
            val anime_from = findViewById<TextView>(R.id.txt_from)
            anime_from.text = "From: ${formatTime(result[position].from)}"

            //to
            val anime_to = findViewById<TextView>(R.id.txt_to)
            anime_to.text = "To: ${formatTime(result[position].to)}"

            //similarity
            val anime_similarity = findViewById<TextView>(R.id.txt_similarity)
            anime_similarity.text = "Similarity: ${"%.2f".format(result[position].similarity)}"

            //video
            val videoUri = Uri.parse(result[position].video)

            val anime_video = findViewById<VideoView>(R.id.vv_video)
            anime_video.setVideoURI(videoUri)

            anime_video.setOnPreparedListener{ mediaPlayer ->

                //Tự động phát video
                mediaPlayer.start()
            }
        }

        //Lấy result thứ i truyền vào bind.
        holder.bind(result[position])
    }

    override fun getItemCount(): Int {

        return result.size
    }
}