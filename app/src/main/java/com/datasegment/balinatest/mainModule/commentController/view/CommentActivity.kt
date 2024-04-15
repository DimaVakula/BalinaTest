package com.datasegment.balinatest.mainModule.commentController.view

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.datasegment.balinatest.R
import com.datasegment.balinatest.authModule.securityController.model.SignInModel
import com.datasegment.balinatest.mainModule.NetworkManager
import com.datasegment.balinatest.mainModule.commentController.model.Comment
import com.datasegment.balinatest.mainModule.commentController.viewModel.CommentAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CommentActivity : AppCompatActivity(), CommentAdapter.OnCommentItemLongClickListener {
    private var photoId = -1
    private var photoURL: String? = null
    private var photoLng : Double = 0.0
    private var photoLat : Double = 0.0
    private var photoDate : Int = 0
    private val networkManager = NetworkManager()
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private var scrollPosition = 0
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: CommentAdapter
    private val PAGE_SIZE = 4
    private var commentIdDel: Int = -1
    private var commentPosDel: Int = -1
    private lateinit var textInputEditText: TextInputEditText
    private lateinit var sendButton: Button
    private var textComment: String = ""
    private lateinit var textInputLayout: TextInputLayout
    private var userToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView = findViewById(R.id.recycler_Comment)

        textInputLayout = findViewById(R.id.textInputLayout)
        textInputEditText = findViewById(R.id.textInputEditText)
        sendButton = findViewById(R.id.sendButton)
        textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                val isEmpty = s.isNullOrEmpty()
                sendButton.isEnabled = !isEmpty
                if(!isEmpty) textComment = textInputEditText.text.toString()
            }
        })

        sendButton.setOnClickListener {
            if (textComment.isNotEmpty()) {
                hideKeyboard()
                textInputEditText.text?.clear()
                upLoadComment()
            }
        }

        layoutManager = GridLayoutManager(this, 1)
        recyclerView.layoutManager = layoutManager
        adapter = CommentAdapter(this)
        adapter.setOnCommentItemLongClickListener(this)
        recyclerView.adapter = adapter

        val intent = intent
        if (intent.hasExtra("PHOTO_ID")) {
            photoId = intent.getIntExtra("PHOTO_ID", -1)
        }
        if (intent.hasExtra("PHOTO_URL")) {
            photoURL = intent.getStringExtra("PHOTO_URL") ?: ""
        }
        if (intent.hasExtra("PHOTO_LNG")) {
            photoLng = intent.getDoubleExtra("PHOTO_LNG", 0.0)
        }
        if (intent.hasExtra("PHOTO_LAT")) {
            photoLat = intent.getDoubleExtra("PHOTO_LAT", 0.0)
        }
        if (intent.hasExtra("PHOTO_DATE")) {
            photoDate = intent.getIntExtra("PHOTO_DATE", 0)
        }
        if (intent.hasExtra("USER_TOKEN")) {
            userToken = intent.getStringExtra("USER_TOKEN")
        }
        val photoImageView = findViewById<ImageView>(R.id.photoImageView)

        Glide.with(this)
            .load(photoURL)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error)
            .centerCrop()
            .into(photoImageView)
        val formattedDateTime = formatDateTime(photoDate.toLong() * 1000 )
        val photoTextView = findViewById<TextView>(R.id.photoText)
        photoTextView.text = formattedDateTime
        getComment()
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(textInputLayout.windowToken, 0)
    }

    override fun onCommentItemLongClick(position: Int) {
        commentIdDel = adapter.getCommentIdAtPosition(position)
        showDeleteConfirmationDialog(this)
        commentPosDel = position
    }

    fun showDeleteConfirmationDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Comment")
        builder.setMessage("Are you sure you want to delete this comment?")
        builder.setPositiveButton("Yes") { _, _ ->
            delComment()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadMoreData() {
        isLoading = true
        currentPage++
        getComment()
    }

    private fun getComment() {
        userToken?.let {
            networkManager.getComment(it, photoId, currentPage) { responseData ->
                if (responseData != null) {
                    val jsonResponse = JSONObject(responseData)
                    Log.d("getComment", jsonResponse.toString())
                    val status = jsonResponse.getInt("status")
                    val dataSTR = jsonResponse.getJSONArray("data")

                    if (status == 200) {
                        if (dataSTR.length() == 0) {
                            isLastPage = true
                        } else {
                            val newComments = mutableListOf<Comment>()
                            for (i in 0 until dataSTR.length()) {
                                val data = dataSTR.getJSONObject(i)
                                val commentObj = Comment(
                                    data.getInt("id"),
                                    data.getString("text"),
                                    data.getInt("date"),
                                )
                                newComments.add(commentObj)
                            }
                            setCommentRecycler()
                            runOnUiThread {
                                adapter.setLoading(false)
                                adapter.addComments(newComments)
                                recyclerView.scrollToPosition(scrollPosition)
                            }
                        }
                    } else {
                    }
                } else {
                }
                isLoading = false
            }
        }
    }

    private fun delComment() {
        userToken?.let {
            networkManager.delComment(
                it, photoId,
                commentIdDel
            ) { responseData ->
                if (responseData != null) {
                    val jsonResponse = JSONObject(responseData)
                    val status = jsonResponse.getInt("status")
                    if (status == 200) {
                        runOnUiThread {
                            adapter.removeCommentAtPosition(commentPosDel)
                            adapter.notifyDataSetChanged()
                        }
                    }

                }
            }
        }
    }

    private fun upLoadComment(){
        userToken?.let {
            networkManager.uploadComment(it, textComment, photoId) { responseData ->
                if (responseData != null) {
                    val jsonResponse = JSONObject(responseData)
                    Log.d("upLoadComment", jsonResponse.toString())
                    val status = jsonResponse.getInt("status")
                    val dataSTR = jsonResponse.getString("data")
                    val dataJSON = JSONObject(dataSTR)
                    if(status == 200){
                        val newComments = mutableListOf<Comment>()
                        Log.d("11",jsonResponse.toString())
                        val photoObj = Comment(
                            dataJSON.getInt("id"),
                            dataJSON.getString("text"),
                            dataJSON.getInt("date"),
                        )
                        newComments.add(photoObj)
                        runOnUiThread {
                            adapter.addComments(newComments)
                        }

                    } else {
                        showErrorDialog("An error occurred while performing the operation.")
                    }
                    println(responseData)
                } else {
                    println("Error occurred while sending registration request.")
                }
            }
        }
    }

    private fun showErrorDialog(message: String) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Error")
            builder.setMessage(message)
            builder.setPositiveButton("OK", null)
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun setCommentRecycler() {
        adapter.setLoadMoreListener {
            loadMoreData()
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dx != 0 || dy != 0) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 1
                        && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE && !isLastPage) {
                        scrollPosition = firstVisibleItemPosition
                        loadMoreData()
                    }
                }
            }
        })
    }

    private fun formatDateTime(dateTime: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateTime

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}