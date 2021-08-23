package com.fuusy.fuperformance.memory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.fuusy.fuperformance.R
import com.fuusy.fuperformance.memory.bean.User
import com.fuusy.fuperformance.memory.view.WaveView

/**
 * @date：2021/8/22
 * @author fuusy
 * @instruction：内存优化
 */
class MemoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory)
        val waveView = findViewById<WaveView>(R.id.wave_view)

        findViewById<Button>(R.id.bt_start).setOnClickListener {

        }
    }
}

