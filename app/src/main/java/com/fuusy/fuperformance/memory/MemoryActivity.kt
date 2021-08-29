package com.fuusy.fuperformance.memory

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.device.yearclass.YearClass
import com.fuusy.fuperformance.R
import com.fuusy.fuperformance.memory.bitmap.Main
import com.fuusy.fuperformance.memory.view.WaveView
import java.io.File
import java.io.IOException


/**
 * @date：2021/8/22
 * @author fuusy
 * @instruction：内存优化
 */
class MemoryActivity : AppCompatActivity() {

    private var externalReportPath: File? = null
    private var mHandler: Handler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "MemoryActivity"
        private const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory)

        //initPermission()

        val waveView = findViewById<WaveView>(R.id.wave_view)

        val imageView = findViewById<ImageView>(R.id.iv_bitmap)
        val imageView1 = findViewById<ImageView>(R.id.iv_bitmap1)
        BitmapFactory.decodeResource(resources, R.mipmap.bitmap1).apply {
            imageView.setImageBitmap(this)
        }

        BitmapFactory.decodeResource(resources, R.mipmap.bitmap1).apply {
            imageView1.setImageBitmap(this)
        }

        findViewById<Button>(R.id.bt_dump).setOnClickListener {
            dump()
        }

        findViewById<Button>(R.id.bt_analyzer).setOnClickListener {

        }

        //模拟handler内存泄漏，解决方法弱引用以及onDestroy中removeCallbacksAndMessages
        mHandler.postDelayed(Runnable {

        }, 4000)

        val year = YearClass.get(applicationContext)
        Log.d(TAG, "Year: $year")
        when {
            year >= 2013 -> {
                // Do advanced animation
            }
            year > 2010 -> {
                // Do simple animation
            }
            else -> {
                // Phone too slow, don't do any animations
            }
        }
    }

    private fun initPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {
            initExternalReportPath()
        }
    }

    private fun initExternalReportPath() {
        externalReportPath = File(Environment.getExternalStorageDirectory(), "bitmapAnalyzer")
        if (!externalReportPath!!.exists()) {
            externalReportPath!!.mkdirs()
        }
    }

    private fun dump() {
        // 手动触发GC后获取hprof文件
        Runtime.getRuntime().gc()
        try {
            Thread.sleep(100)
        } catch (e: InterruptedException) {
            throw AssertionError()
        }
        System.runFinalization()
        try {
            val file: File = File(externalReportPath?.absolutePath, "dump.hprof")
            if (!file.exists()) {
                file.createNewFile()
            }
            // 生成Hprof文件
            Debug.dumpHprofData(file.absolutePath)
            Toast.makeText(this, "path: " + file.absolutePath, Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}

