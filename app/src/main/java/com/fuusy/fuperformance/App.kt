package com.fuusy.fuperformance

import android.app.Application
import android.graphics.Bitmap
import android.widget.ImageView
import com.alibaba.android.arouter.launcher.ARouter
import com.fuusy.fuperformance.appstart.TimeMonitorManager
import com.fuusy.fuperformance.appstart.dispatcher.TaskDispatcher
import com.fuusy.fuperformance.appstart.task.*
import com.fuusy.fuperformance.memory.bitmap.BitmapARTHook
import com.kingja.loadsir.core.LoadSir


import com.tencent.bugly.Bugly
import de.robv.android.xposed.DexposedBridge
import de.robv.android.xposed.XC_MethodHook
import java.util.*
import java.util.concurrent.CountDownLatch

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
        //Debug.startMethodTracing("onCreate")
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

        initBitmapHook()
    }

    /**
     * 初始化Bitmap大小监控。
     */
    private fun initBitmapHook() {

        DexposedBridge.hookAllConstructors(ImageView::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)
                DexposedBridge.findAndHookMethod(
                    ImageView::class.java, "setImageBitmap",
                    Bitmap::class.java, BitmapARTHook()
                )
            }
        })
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

    private fun Delay() {
        object : Thread() {
            override fun run() {
                super.run()
                sleep(3000) //休眠3秒
                /**
                 * 要执行的操作
                 */
            }
        }.start()

        val task: TimerTask = object : TimerTask() {
            override fun run() {
                /**
                 * 要执行的操作
                 */
            }
        }
        val timer = Timer()
        timer.schedule(task, 3000) //3秒后执行TimeTask的run方法

    }
}