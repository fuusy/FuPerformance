package com.fuusy.fuperformance.appstart.task

import com.fuusy.fuperformance.appstart.dispatcher.Task
import com.tencent.bugly.Bugly

/**
 * @date：2021/8/11
 * @author fushiyao
 * @instruction：
 */
class BuglyTask :Task() {
    override fun run() {
        Bugly.init(mContext, "12324334", false)
    }
}