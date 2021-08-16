package com.fuusy.fuperformance.appstart.delay

import android.os.Looper
import android.os.MessageQueue.IdleHandler
import com.fuusy.fuperformance.appstart.dispatcher.DispatchRunnable
import com.fuusy.fuperformance.appstart.dispatcher.Task
import java.util.*

/**
 * @date：2021/8/16
 * @author fushiyao
 * @instruction：延时初始化，IdleHandler启动器
 */
class DelayDispatcher {
    private val mDelayTasks: Queue<Task> = LinkedList<Task>()

    private val mIdleHandler = IdleHandler {
        if (mDelayTasks.size > 0) {
            val task: Task = mDelayTasks.poll()
            DispatchRunnable(task).run()
        }
        !mDelayTasks.isEmpty()
    }

    /**
     * 添加延时任务
     */
    fun addTask(task: Task): DelayDispatcher? {
        mDelayTasks.add(task)
        return this
    }

    fun start() {
        Looper.myQueue().addIdleHandler(mIdleHandler)
    }
}