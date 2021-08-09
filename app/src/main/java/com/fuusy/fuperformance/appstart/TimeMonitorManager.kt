package com.fuusy.fuperformance.appstart

import android.util.Log
import java.util.*

/**
 * @author fuusy
 * @date：2021/8/9
 * @instruction：
 */
class TimeMonitorManager private constructor() {
    private val mTimeTagMap = HashMap<String, Long?>()
    private var mStartTime: Long = 0

    fun clear() {
        mTimeTagMap.clear()
    }

    /**
     * 开始监听.
     */
    fun startMonitor() {
        if (mTimeTagMap.size > 0) {
            mTimeTagMap.clear()
        }
        mStartTime = System.currentTimeMillis()
    }

    /**
     * 结束监听.
     * @param tag 所要打印的tag.
     */
    fun endMonitor(tag: String) {
        if (mTimeTagMap[tag] != null) {
            mTimeTagMap.remove(tag)
        }
        val time = System.currentTimeMillis() - mStartTime
        mTimeTagMap[tag] = time
        showData()
    }

    private fun showData() {
        if (mTimeTagMap.size <= 0) {
            return
        }
        for (tag in mTimeTagMap.keys) {
            val time = mTimeTagMap[tag]!!
            Log.d(TAG, "$tag: $time")
        }
    }

    companion object {
        private const val TAG = "TimeMonitorManager"

        @Volatile
        private var mMonitorManager: TimeMonitorManager? = null
        val instance: TimeMonitorManager?
            get() {
                if (mMonitorManager == null) {
                    synchronized(TimeMonitorManager::class.java) {
                        if (mMonitorManager == null) {
                            mMonitorManager = TimeMonitorManager()
                        }
                    }
                }
                return mMonitorManager
            }
    }
}