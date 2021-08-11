package com.fuusy.fuperformance.appstart.dispatcher.utils

import android.util.Log

object DispatcherLog {
    var isDebug = true
    fun i(msg: String?) {
        if (!isDebug) {
            return
        }
        Log.i("task", msg!!)
    }
}