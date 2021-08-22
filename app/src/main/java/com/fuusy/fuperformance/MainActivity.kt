package com.fuusy.fuperformance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.fuusy.fuperformance.memory.MemoryActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.bt_memory).setOnClickListener {
            Intent(this, MemoryActivity::class.java).apply {
                startActivity(this)
            }
        }
    }
}