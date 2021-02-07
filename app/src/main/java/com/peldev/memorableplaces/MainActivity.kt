package com.peldev.memorableplaces

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng

class MainActivity : AppCompatActivity() {
    lateinit var listView: ListView
    companion object {
        val places = ArrayList<String>()
        val locations = ArrayList<LatLng>()
        lateinit var adapter: ArrayAdapter<String>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)

        locations.add(LatLng(0.0, 0.0))
        places.add("Add a new place...")


        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, places)
        listView.adapter = adapter

        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(applicationContext, MapsActivity::class.java)
            intent.putExtra("placeNumber", id)
            startActivity(intent)
        }
    }



}