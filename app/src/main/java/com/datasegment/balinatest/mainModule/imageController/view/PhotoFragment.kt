package com.datasegment.balinatest.mainModule.imageController.view

import com.datasegment.balinatest.mainModule.imageController.viewModel.PhotoAdapter
import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.datasegment.balinatest.R
import com.datasegment.balinatest.authModule.securityController.model.SignInModel
import com.datasegment.balinatest.mainModule.commentController.view.CommentActivity
import com.datasegment.balinatest.mainModule.NetworkManager
import com.datasegment.balinatest.mainModule.database.AppDatabase
import com.datasegment.balinatest.mainModule.database.model.Photo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class PhotoFragment : Fragment(), PhotoAdapter.OnPhotoItemLongClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhotoAdapter
    private var isLoading = false
    private var currentPage = 0
    private val PAGE_SIZE = 7
    private var scrollPosition = 0
    private lateinit var layoutManager: LinearLayoutManager
    private var isLastPage = false
    private lateinit var addPhoto: Button
    private val cameraRequest = 1888
    private val locationRequestCode = 101
    private val networkManager = NetworkManager()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val signInModel = SignInModel()
    private var db: AppDatabase? = null
    private val filePermissionRequestCode = 101
    private val PERMISSION_REQUEST_CODE = 1001
    private var photoIdDel: Int = -1
    private var photoPosDel: Int = -1
    private var userToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo, container, false)
        userToken = arguments?.getString("USER_TOKEN")
        recyclerView = view.findViewById(R.id.photo_recycler_view)
        addPhoto = view.findViewById(R.id.addPhoto)
        addPhoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivityForResult(intent, cameraRequest)
            } else {
            }
        }


        db = Room.databaseBuilder(
            requireContext().applicationContext,
            AppDatabase::class.java, "PhotoDB"
        ).build()

        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (!hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(requireActivity(), permissions, PERMISSION_REQUEST_CODE)
        }

        if (ContextCompat.checkSelfPermission(requireContext().applicationContext, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), cameraRequest)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ContextCompat.checkSelfPermission(requireContext().applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), locationRequestCode)
        } else {
        }

        checkFilePermissions()
        layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.layoutManager = layoutManager
        adapter = PhotoAdapter(requireContext())
        adapter.setOnPhotoItemClickListener(object : PhotoAdapter.OnPhotoItemClickListener {
            override fun onPhotoItemClick(photoId: Int, photoUrl: String?, photoLng: Double, photoLat: Double, photoDate: Int) {
                val intent = Intent(requireContext(), CommentActivity::class.java)
                intent.putExtra("PHOTO_ID", photoId)
                intent.putExtra("PHOTO_URL", photoUrl)
                intent.putExtra("PHOTO_LNG", photoLng)
                intent.putExtra("PHOTO_LAT", photoLat)
                intent.putExtra("PHOTO_DATE", photoDate)
                intent.putExtra("USER_TOKEN", userToken)
                startActivity(intent)
            }

        })
        adapter.setOnPhotoItemLongClickListener(this)
        recyclerView.adapter = adapter

        getPhoto()

        return view
    }

    override fun onPhotoItemLongClick(position: Int) {
        photoIdDel = adapter.getPhotoIdAtPosition(position)
        showDeleteConfirmationDialog(requireContext())
        photoPosDel = position
        Log.d("photoId",photoIdDel.toString())
    }

    private fun showDeleteConfirmationDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Photo")
        builder.setMessage("Are you sure you want to delete this photo?")
        builder.setPositiveButton("Yes") { _, _ ->
            delPhoto()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }


    private fun hasPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            } else {
            }
        }
    }


    private fun setPhotoRecycler() {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraRequest) {
            val photo: Bitmap = data?.extras?.get("data") as Bitmap
            val sendPhotoString = encode(photo)
            val currentTimeMillis: Int = (System.currentTimeMillis() / 1000).toInt()
            getLastLocation(
                onSuccess = { latitude, longitude ->
                    userToken?.let { upLoadPhoto(it, sendPhotoString, currentTimeMillis, latitude, longitude) }
                },
                onFailure = { exception ->
                    Log.e("Location", "Error getting location", exception)
                }
            )
        }
        if(requestCode == locationRequestCode){
            Log.d("location_activate","location_activate")
        }
    }

    private fun getLastLocation(onSuccess: (latitude: Double, longitude: Double) -> Unit, onFailure: (Exception) -> Unit) {
        if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onSuccess(location.latitude, location.longitude)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    private fun checkFilePermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                filePermissionRequestCode
            )
        } else {
        }
    }



    private fun loadMoreData() {
        isLoading = true
        currentPage++
        getPhoto()
    }

    private fun getPhoto() {
        userToken?.let {
            networkManager.getPhoto(it, currentPage) { responseData ->
                if (responseData != null) {
                    val jsonResponse = JSONObject(responseData)
                    Log.d("josnResponsePhoto", jsonResponse.toString())
                    val status = jsonResponse.getInt("status")
                    val dataSTR = jsonResponse.getJSONArray("data")

                    if (status == 200) {
                        if (dataSTR.length() == 0) {
                            isLastPage = true
                        } else {
                            val newPhotos = mutableListOf<Photo>()
                            for (i in 0 until dataSTR.length()) {
                                val data = dataSTR.getJSONObject(i)
                                val photoObj = Photo(
                                    data.getInt("id"),
                                    data.getString("url"),
                                    data.getInt("date"),
                                    data.getDouble("lat"),
                                    data.getDouble("lng")
                                )
                                runInBackground {
                                    db!!.photoDao().insert(photoObj)
                                }
                                newPhotos.add(photoObj)
                            }
                            setPhotoRecycler()
                            requireActivity().runOnUiThread {
                                adapter.setLoading(false)
                                adapter.addPhotos(newPhotos)
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

    private fun delPhoto() {
        userToken?.let {
            networkManager.delPhoto(it, photoIdDel) { responseData ->
                if (responseData != null) {
                    val jsonResponse = JSONObject(responseData)
                    val status = jsonResponse.getInt("status")
                    if (status == 200) {
                        val deferred = GlobalScope.async(Dispatchers.IO) {
                            db!!.photoDao().delete(photoIdDel)
                        }
                        deferred.invokeOnCompletion { throwable ->
                            if (throwable != null) {
                            } else {
                                requireActivity().runOnUiThread {
                                    adapter.removePhotoAtPosition(photoPosDel)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                } else {
                }
            }
        }
        }

    private fun upLoadPhoto(token: String, photo:String, date: Int, lat:Double, lng:Double){
        networkManager.uploadPhoto(token, photo, date, lat, lng) { responseData ->
            if (responseData != null) {
                val jsonResponse = JSONObject(responseData)
                Log.d("josnResponsePhoto", jsonResponse.toString())
                val status = jsonResponse.getInt("status")
                val dataSTR = jsonResponse.getString("data")
                val dataJSON = JSONObject(dataSTR)
                if(status == 200){
                    val newPhotos = mutableListOf<Photo>()
                    val photoId = dataJSON.getInt("id")
                    Log.d("11",jsonResponse.toString())
                    val photoUrl = dataJSON.getString("url")
                    val photoDate = dataJSON.getInt("date")
                    val photoLat = dataJSON.getDouble("lat")
                    val photoLng = dataJSON.getDouble("lng")
                    val photoObj = Photo(
                        dataJSON.getInt("id"),
                        dataJSON.getString("url"),
                        dataJSON.getInt("date"),
                        dataJSON.getDouble("lat"),
                        dataJSON.getDouble("lng")
                    )
                    runInBackground {
                        db?.photoDao()?.insert(Photo(photoId, photoUrl, photoDate, photoLat, photoLng))
                    }
                    val decodePhoto = decode(photo)
                    saveBitmapImage(decodePhoto, photoId)
                    newPhotos.add(photoObj)
                    requireActivity().runOnUiThread {
                        adapter.addPhotos(newPhotos)
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

    private fun showErrorDialog(message: String) {
        requireActivity().runOnUiThread {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Error")
            builder.setMessage(message)
            builder.setPositiveButton("OK", null)
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun runInBackground(runnable: () -> Unit) {
        Thread {
            runnable()
        }.start()
    }


    private fun saveBitmapImage(bitmap: Bitmap, id : Int) {
        val timestamp = System.currentTimeMillis()
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, id)
        values.put(MediaStore.Images.Media.DATE_ADDED, timestamp)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, timestamp)
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + getString(R.string.app_name))
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            val uri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                try {
                    val outputStream = requireContext().contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        try {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.close()
                        } catch (e: Exception) {
                            Log.e("1111", "saveBitmapImage: ", e)
                        }
                    }
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    requireContext().contentResolver.update(uri, values, null, null)
                } catch (e: Exception) {
                    Log.e("2", "saveBitmapImage: ", e)
                }
            }
        } else {
            val imageFileFolder = File(Environment.getExternalStorageDirectory().toString() + '/' + getString(R.string.app_name))
            if (!imageFileFolder.exists()) {
                imageFileFolder.mkdirs()
            }
            val mImageName = "$id.jpeg"
            val imageFile = File(imageFileFolder, mImageName)
            try {
                val outputStream: OutputStream = FileOutputStream(imageFile)
                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    Log.e("3", "saveBitmapImage: ", e)
                }
                values.put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            } catch (e: Exception) {
                Log.e("3", "saveBitmapImage: ", e)
            }
        }
    }

    private fun encode(image: Bitmap): String {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        var imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun decode(imageString: String): Bitmap {
        val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}
