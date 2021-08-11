package com.fuusy.fuperformance.appstart.task

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter
import com.fuusy.fuperformance.BuildConfig
import com.fuusy.fuperformance.appstart.dispatcher.Task

/**
 * @date：2021/8/11
 * @author fushiyao
 * @instruction：
 */
class RouterTask() : Task() {
    override fun run() {
        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(mContext as Application?)
    }
}