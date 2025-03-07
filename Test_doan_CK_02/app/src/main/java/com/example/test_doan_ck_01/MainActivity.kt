package com.example.test_doan_ck_01

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.test_doan_ck_01.adapter.AnimeAdapter
import com.example.test_doan_ck_01.api.RetrofitClient
import com.example.test_doan_ck_01.api.RetrofitImgBB
import com.example.test_doan_ck_01.database.AnimeDatabaseModel
import com.example.test_doan_ck_01.databinding.ActivityMainBinding
import com.example.test_doan_ck_01.databinding.ItemAnimeDialogBinding
import com.example.test_doan_ck_01.model.AnimeResponse
import com.example.test_doan_ck_01.model.ImgBBResponse
import com.example.test_doan_ck_01.model.Result
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var anime_adt: AnimeAdapter
    private var anime_result: List<Result> = emptyList()
    private lateinit var item_dialog: AlertDialog
    private lateinit var dbRef: DatabaseReference

    private var imageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        //Nếu chọn ảnh.
        if(result.resultCode == Activity.RESULT_OK && result.data != null){

            //Lấy path URI của ảnh.
            imageUri = result.data?.data

            if(imageUri == null){

                Toast.makeText(this, "Không thể lấy ảnh từ thư viện ảnh.", Toast.LENGTH_SHORT).show()

                //Không làm các bước tiếp theo.
                return@registerForActivityResult
            }

            binding.imgImageSearch.setImageURI(imageUri)

            Toast.makeText(this, "Lấy ảnh thành công từ thư viện ảnh.", Toast.LENGTH_SHORT).show()

            //Gọi hàm upload ảnh lên Imgbb.
            uploadToImageBB(imageUri!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Khởi tạo viewbinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Table database
        dbRef = FirebaseDatabase.getInstance().getReference("AnimeSeach")

        //code cho button ib_image_fold
        binding.ibImageFold.setOnClickListener {

            openGallery()
        }

        binding.svImageLink.setOnClickListener {

            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val clipData = clipboard.primaryClip

            if(clipData != null && clipData.itemCount > 0){

                val pastedText = clipData.getItemAt(0).text.toString()

                // Tự động dán URL vào SearchView
                binding.svImageLink.setQuery(pastedText, false)
            }
        }

        binding.svImageLink.setOnQueryTextListener(object: SearchView.OnQueryTextListener{

            // Xử lý khi người dùng nhấn Enter/tìm kiếm
            override fun onQueryTextSubmit(query: String?): Boolean {

                if (query != null) {

                    searchAnime(query)

                    //Dùng Glide để load ảnh từ URL
                    Glide.with(this@MainActivity)
                        .load(query)
                        .placeholder(R.drawable.ic_placeholder_image)  //Ảnh tạm khi đang tải
                        .error(R.drawable.ic_error_image)  //Ảnh hiển thị khi lỗi
                        .into(binding.imgImageSearch)
                }

                return true
            }

            // Xử lý khi người dùng thay đổi nội dung tìm kiếm
            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })

        //Lắng nghe event từ menu
        binding.nvLeftMenu.setNavigationItemSelectedListener{

            when(it.itemId){

                R.id.nav_history -> showHistory()
                R.id.nav_exit -> finish()
            }
            true
        }
    }

    private fun openGallery() {

        //Chọn tài liệu/file từ bộ nhớ.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

            //Hiển thị các file có thể mở.
            addCategory(Intent.CATEGORY_OPENABLE)

            type = "image/*"

            //Chỉ mở ảnh từ thiết bị, bộ nhớ trong của máy.
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)

            //Cấp quyền đọc ảnh.
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        //pickImage kích hoạt intent này.
        pickImage.launch(intent)
    }

    private fun uploadToImageBB(uri: Uri) {

        val apiKey = "a47f4fe12b597abbf1924c5105c10d24"

        //Chuyển API key thành RequestBody.
        val apiKeyPart = apiKey.toRequestBody("text/plain".toMediaTypeOrNull())

        //Lấy absolute path của ảnh từ URI.
        val file = File(getPathFromUri(uri) ?: return)

        //Tạo request body từ file (nội dung file).
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())

        //Data cần gửi cho API.
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val apiService = RetrofitImgBB.instance_imgBB

        apiService.uploadImage(apiKeyPart, imagePart).enqueue(object : Callback<ImgBBResponse> {

            override fun onResponse(call: Call<ImgBBResponse>, response: Response<ImgBBResponse>) {

                if(response.isSuccessful){

                    val imageUrl = response.body()?.data?.url?: return

                    runOnUiThread{

                        Toast.makeText(this@MainActivity, "Lấy URL của ảnh thành công.", Toast.LENGTH_LONG).show()

                        binding.svImageLink.setQuery(imageUrl, false)

                        searchAnime(imageUrl)
                    }
                }else{

                    runOnUiThread{

                        Toast.makeText(this@MainActivity, "Lỗi API ImgBB trả về: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ImgBBResponse>, t: Throwable) {

                runOnUiThread {

                    Toast.makeText(this@MainActivity, "Lỗi không kết nối được với API ImgBB: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getPathFromUri(uri: Uri): String?{

        //Mở luồng để đọc data từ URI.
        val inputStream = contentResolver.openInputStream(uri) ?: return null

        //Tạo 1 file tạm trong thư mục cache riêng của app này.
        val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")

        file.outputStream().use{ outputStream ->

            //Sao chép nội dung từ inputStream sang file tạm.
            inputStream.copyTo(outputStream)
        }

        //Trả về đường dẫn tuyệt đối của file tạm.
        return file.absolutePath
    }

    private fun searchAnime(imageUrl: String) {

        val apiService = RetrofitClient.instance

        apiService.searchAnimeByUrl(imageUrl = imageUrl).enqueue(object: Callback<AnimeResponse>{

            override fun onResponse(call: Call<AnimeResponse>, response: Response<AnimeResponse>) {

                if(response.isSuccessful){

                    val anime = response.body()

                    if(anime != null && anime.result.isNotEmpty()){

                        Toast.makeText(this@MainActivity, "Result upload completed!", Toast.LENGTH_SHORT).show()

                        //Hiện số kết quả tìm kiếm
                        binding.txtFrameCount.text = "Total search results: ${anime.frameCount}"

                        binding.txtFrameCount.visibility = View.VISIBLE

                        //Gán data lên RecyclerView
                        anime_result = anime.result

                        anime_adt = AnimeAdapter(anime_result, object: AnimeAdapter.OnItemClickListener{

                            override fun onItemClick(result: Result) {

                                showDialogBinding(result)
                            }
                        })

                        binding.rvResult.adapter = anime_adt

                        binding.rvResult.layoutManager = LinearLayoutManager(

                            this@MainActivity,
                            LinearLayoutManager.VERTICAL,
                            false
                        )

                        binding.rvResult.visibility = View.VISIBLE
                    }else{

                        Toast.makeText(this@MainActivity, "Không tìm thấy anime!!!", Toast.LENGTH_SHORT).show()
                    }
                }else{

                    Log.e("API_ERROR", "Lỗi response2: ${response.code()} - ${response.errorBody()?.string()}")

                    Toast.makeText(this@MainActivity, "Lỗi API trace.moe trả về: ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AnimeResponse>, t: Throwable) {

                Toast.makeText(this@MainActivity, "Lỗi không kết nối được API trace.moe: ", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDialogBinding(result: Result) {

        val build = AlertDialog.Builder(this@MainActivity)

        val dialogBinding = ItemAnimeDialogBinding.inflate(LayoutInflater.from(this@MainActivity))

        build.setView(dialogBinding.root)

        //Xử lý data gán lên dialog
        //Dùng Glide để load ảnh từ URL
        Glide.with(this@MainActivity)
            .load(result.image)
            .placeholder(R.drawable.ic_placeholder_image)  //Ảnh tạm khi đang tải
            .error(R.drawable.ic_error_image)  //Ảnh hiển thị khi lỗi
            .into(dialogBinding.ivItemImage)

        dialogBinding.txtItemName.text = result.anilist.title.romaji

        //Xử lý event
        dialogBinding.btnCancel.setOnClickListener {

            item_dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {

            //Insert data của dialog vào database
            saveAnimeDatabase(result)

            item_dialog.dismiss()
        }

        item_dialog = build.create()

        item_dialog.show()
    }

    private fun saveAnimeDatabase(result: Result) {

        val anime_name = result.anilist.title.romaji
        val anime_image = result.image
        val anime_datetime = System.currentTimeMillis() //Lưu timestamp (milliseconds từ 01/01/1970 UTC).

        //Đẩy data vừa lấy được lên database.
        //Tạo thêm 1 data cho cột id để phân biệt cho từng row trong table ấy.
        val anime_id = dbRef.push().key!!

        //Data cần đẩy
        val anime_insert = AnimeDatabaseModel(anime_id, anime_name, anime_image, anime_datetime)

        dbRef.child(anime_id).setValue(anime_insert)
            .addOnCompleteListener {//Khi chèn thành công

                Toast.makeText(this, "Data insert Completed", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {err ->//Khi chèn thất bại

                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showHistory() {

        //Chuyển qua 1 activity history
        val intent  = Intent(this, HistoryActivity::class.java)

        startActivity(intent)
    }
}