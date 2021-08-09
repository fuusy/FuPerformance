package com.fuusy.fuperformance

import android.app.Application
import android.hardware.Camera
import androidx.core.os.TraceCompat
import com.alibaba.android.arouter.launcher.ARouter
import com.fuusy.fuperformance.appstart.TimeMonitorManager
import com.kingja.loadsir.core.LoadSir
import com.tencent.bugly.Bugly

class App : Application() {

    companion object {
        private const val TAG = "App"
    }

    override fun onCreate() {
        super.onCreate()
        //Debug.startMethodTracing("App")
        //TraceCompat.beginSection("onCreate")
        TimeMonitorManager.instance?.startMonitor()
        initRouter()
        TimeMonitorManager.instance?.endMonitor("initRouter")

        TimeMonitorManager.instance?.startMonitor()
        initBugly()
        TimeMonitorManager.instance?.endMonitor("initBugly")

        TimeMonitorManager.instance?.startMonitor()
        initLoadSir()
        TimeMonitorManager.instance?.endMonitor("initLoadSir")

        //Debug.stopMethodTracing()
        //TraceCompat.endSection()
    }

    private fun initLoadSir() {

        LoadSir.beginBuilder()
            .commit();
    }

    private fun initRouter() {
        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this)
    }

    private fun initBugly() {
        Bugly.init(this, "12324334", false)
    }
}