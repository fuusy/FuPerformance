package com.fuusy.fuperformance

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter
import com.fuusy.fuperformance.appstart.TimeMonitorManager
import com.fuusy.fuperformance.appstart.dispatcher.TaskDispatcher
import com.fuusy.fuperformance.appstart.task.*
import com.kingja.loadsir.core.LoadSir
import com.tencent.bugly.Bugly
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class App : Application() {
    //CountDownLatch等待子线程完成后操作
    private val countDownLatch: CountDownLatch = CountDownLatch(1)


    companion object {
        private const val TAG = "App"
        private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
        private val CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4))
    }

    override fun onCreate() {
        super.onCreate()
        //Debug.startMethodTracing("App")
        //TraceCompat.beginSection("onCreate")

        TimeMonitorManager.instance?.startMonitor()
        //异步方法一、创建线程池
        /*
        val newFixedThreadPool = Executors.newFixedThreadPool(CORE_POOL_SIZE)
        newFixedThreadPool.submit {
            initRouter()
        }
        newFixedThreadPool.submit {
            initBugly()
            countDownLatch.countDown()
        }
        newFixedThreadPool.submit {
            initLoadSir()
        }

         */

        //方式二、启动器
        TaskDispatcher.init(this)
        TaskDispatcher.newInstance()
            .addTask(RouterTask())
            .addTask(LoadSirTask())
            .addTask(BuglyTask())
            .addTask(LoadAppIdTask())
            .addTask(WeChatPayTask())

//        initRouter()
//        initBugly()
//        initLoadSir()
        //countDownLatch.await()
        TimeMonitorManager.instance?.endMonitor("APP onCreate")
        //Debug.stopMethodTracing()
        //TraceCompat.endSection()
    }

    private fun initLoadSir() {

        LoadSir.beginBuilder()
            .commit()
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