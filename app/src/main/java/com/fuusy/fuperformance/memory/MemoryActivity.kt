package com.fuusy.fuperformance.memory

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.fuusy.fuperformance.R
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

        val imageView = findViewById<ImageView>(R.id.iv_bitmap)
        BitmapFactory.decodeResource(resources, R.mipmap.bitmap1).apply {
            imageView.setImageBitmap(this)
        }

    }

}

