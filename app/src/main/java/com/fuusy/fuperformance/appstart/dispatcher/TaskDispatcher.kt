package com.fuusy.fuperformance.appstart.dispatcher

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.annotation.UiThread
import com.fuusy.fuperformance.appstart.dispatcher.topologicalSort.TaskSortUtil
import com.fuusy.fuperformance.appstart.dispatcher.utils.DispatcherLog
import com.fuusy.fuperformance.appstart.dispatcher.utils.ProcessUtils
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 启动器调用类
 */
class TaskDispatcher private constructor() {
    private var mStartTime: Long = 0
    private val mFutures: MutableList<Future<*>> = ArrayList()
    private var mAllTasks: MutableList<Task?>? = ArrayList()
    private val mClsAllTasks: MutableList<Class<out Task>> = ArrayList()


    private val mMainThreadTasks: MutableList<Task?> = ArrayList()
    private var mCountDownLatch: CountDownLatch? = null
    private val mNeedWaitCount = AtomicInteger() //保存需要Wait的Task的数量
    private val mNeedWaitTasks: MutableList<Task?> = ArrayList() //调用了await的时候还没结束的且需要等待的Task


    private val mFinishedTasks: MutableList<Class<out Task?>?> = ArrayList(100) //已经结束了的Task
    private val mDependedHashMap = HashMap<Class<out Task?>?, ArrayList<Task>?>()
    private val mAnalyseCount = AtomicInteger() //启动器分析的次数，统计下分析的耗时；

    fun addTask(task: Task?): TaskDispatcher {
        if (task != null) {
            collectDepends(task)
            mAllTasks!!.add(task)
            mClsAllTasks.add(task.javaClass)
            // 非主线程且需要wait的，主线程不需要CountDownLatch也是同步的
            if (ifNeedWait(task)) {
                mNeedWaitTasks.add(task)
                mNeedWaitCount.getAndIncrement()
            }
        }
        return this
    }

    private fun collectDepends(task: Task) {
        if (task.dependsOn() != null && task.dependsOn()!!.isNotEmpty()) {
            for (cls in task.dependsOn()!!) {
                if (mDependedHashMap[cls] == null) {
                    mDependedHashMap[cls] = ArrayList()
                }
                mDependedHashMap[cls]!!.add(task)
                if (mFinishedTasks.contains(cls)) {
                    task.satisfy()
                }
            }
        }
    }

    private fun ifNeedWait(task: Task?): Boolean {
        return !task!!.runOnMainThread() && task.needWait()
    }

    @UiThread
    fun start() {
        mStartTime = System.currentTimeMillis()
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw RuntimeException("must be called from UiThread")
        }
        if (mAllTasks!!.size > 0) {
            mAnalyseCount.getAndIncrement()
            printDependedMsg()
            mAllTasks = TaskSortUtil.getSortResult(mAllTasks, mClsAllTasks)
            mCountDownLatch = CountDownLatch(mNeedWaitCount.get())
            sendAndExecuteAsyncTasks()
            DispatcherLog.i("task analyse cost " + (System.currentTimeMillis() - mStartTime) + "  begin main ")
            executeTaskMain()
        }
        DispatcherLog.i("task analyse cost startTime cost " + (System.currentTimeMillis() - mStartTime))
    }

    fun cancel() {
        for (future in mFutures) {
            future.cancel(true)
        }
    }

    private fun executeTaskMain() {
        mStartTime = System.currentTimeMillis()
        for (task in mMainThreadTasks) {
            val time = System.currentTimeMillis()
            DispatchRunnable(task, this).run()
            DispatcherLog.i(
                "real main " + task!!.javaClass.simpleName + " cost   " +
                        (System.currentTimeMillis() - time)
            )
        }
        DispatcherLog.i("maintask cost " + (System.currentTimeMillis() - mStartTime))
    }

    private fun sendAndExecuteAsyncTasks() {
        for (task in mAllTasks!!) {
            if (task!!.onlyInMainProcess() && !isMainProcess) {
                markTaskDone(task)
            } else {
                sendTaskReal(task)
            }
            task.isSend = true
        }
    }

    /**
     * 查看被依赖的信息
     */
    private fun printDependedMsg() {
        DispatcherLog.i("needWait size : " + mNeedWaitCount.get())
        if (false) {
            for (cls in mDependedHashMap.keys) {
                DispatcherLog.i("cls " + cls!!.simpleName + "   " + mDependedHashMap[cls]!!.size)
                for (task in mDependedHashMap[cls]!!) {
                    DispatcherLog.i("cls       " + task.javaClass.simpleName)
                }
            }
        }
    }

    /**
     * 通知Children一个前置任务已完成
     *
     * @param launchTask
     */
    fun satisfyChildren(launchTask: Task?) {
        val arrayList = mDependedHashMap[launchTask!!.javaClass]
        if (arrayList != null && arrayList.size > 0) {
            for (task in arrayList) {
                task.satisfy()
            }
        }
    }

    fun markTaskDone(task: Task?) {
        if (ifNeedWait(task)) {
            mFinishedTasks.add(task!!.javaClass)
            mNeedWaitTasks.remove(task)
            mCountDownLatch!!.countDown()
            mNeedWaitCount.getAndDecrement()
        }
    }

    private fun sendTaskReal(task: Task?) {
        if (task!!.runOnMainThread()) {
            mMainThreadTasks.add(task)
            if (task.needCall()) {
                task.setTaskCallBack(object : TaskCallBack {
                    override fun call() {
                        TaskStat.markTaskDone()
                        task.isFinished = true
                        satisfyChildren(task)
                        markTaskDone(task)
                        DispatcherLog.i(task.javaClass.simpleName + " finish")
                        Log.i("testLog", "call")
                    }

                })

            }
        } else {
            // 直接发，是否执行取决于具体线程池
            val future = task.runOn()!!
                .submit(DispatchRunnable(task, this))
            mFutures.add(future)
        }
    }

    fun executeTask(task: Task) {
        if (ifNeedWait(task)) {
            mNeedWaitCount.getAndIncrement()
        }
        task.runOn()!!.execute(DispatchRunnable(task, this))
    }

    @UiThread
    fun await() {
        try {
            if (DispatcherLog.isDebug) {
                DispatcherLog.i("still has " + mNeedWaitCount.get())
                for (task in mNeedWaitTasks) {
                    DispatcherLog.i("needWait: " + task!!.javaClass.simpleName)
                }
            }
            if (mNeedWaitCount.get() > 0) {
                mCountDownLatch!!.await(WAITTIME.toLong(), TimeUnit.MILLISECONDS)
            }
        } catch (e: InterruptedException) {
        }
    }

    companion object {
        private const val WAITTIME = 10000
        var context: Context? = null
        var isMainProcess = false
            private set

        @Volatile
        private var sHasInit = false
        fun init(context: Context?) {
            if (context != null) {
                Companion.context = context
                sHasInit = true
                isMainProcess = ProcessUtils.isMainProcess(Companion.context)
            }
        }

        /**
         * 注意：每次获取的都是新对象
         *
         * @return
         */
        fun newInstance(): TaskDispatcher {
            if (!sHasInit) {
                throw RuntimeException("must call TaskDispatcher.init first")
            }
            return TaskDispatcher()
        }
    }
}