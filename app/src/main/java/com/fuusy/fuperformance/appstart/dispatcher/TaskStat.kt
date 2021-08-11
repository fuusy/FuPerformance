package com.fuusy.fuperformance.appstart.dispatcher

import com.fuusy.fuperformance.appstart.dispatcher.utils.DispatcherLog
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

object TaskStat {
    @Volatile
    private var sCurrentSituation = ""
    private val sBeans: MutableList<TaskStatBean> = ArrayList()
    private var sTaskDoneCount = AtomicInteger()
    private const val sOpenLaunchStat = false // 是否开启统计
    var currentSituation: String
        get() = sCurrentSituation
        set(currentSituation) {
            if (!sOpenLaunchStat) {
                return
            }
            DispatcherLog.i("currentSituation   $currentSituation")
            sCurrentSituation = currentSituation
            setLaunchStat()
        }

    fun markTaskDone() {
        sTaskDoneCount.getAndIncrement()
    }

    fun setLaunchStat() {
        val bean = TaskStatBean()
        bean.situation = sCurrentSituation
        bean.count = sTaskDoneCount.get()
        sBeans.add(bean)
        sTaskDoneCount = AtomicInteger(0)
    }
}