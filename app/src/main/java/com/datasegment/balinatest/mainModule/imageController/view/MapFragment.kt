package com.datasegment.balinatest.mainModule.imageController.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.datasegment.balinatest.R
import com.datasegment.balinatest.mainModule.database.AppDatabase
import com.datasegment.balinatest.mainModule.database.model.Photo
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragment : Fragment() {
    private var db: AppDatabase? = null
    private var mapView: MapView? = null
    private lateinit var googleMap: GoogleMap
    private lateinit var photoList: List<Photo>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = Room.databaseBuilder(
            requireContext().applicationContext,
            AppDatabase::class.java, "PhotoDB"
        ).build()
        mapView = view.findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { googleMap ->
            onMapReady(googleMap)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    private fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        GlobalScope.launch {
            photoList = db?.photoDao()?.getAll()!!
            withContext(Dispatchers.Main) {
                for (photo in photoList) {
                    val photoLocation = LatLng(photo.lat, photo.lng)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(photoLocation)
                            .title("Photo ${photo.id}")
                    )
                }

                if (photoList.isNotEmpty()) {
                    val firstPhotoLocation = LatLng(photoList[0].lat, photoList[0].lng)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPhotoLocation, 10f))
                }
            }
        }
    }
}

