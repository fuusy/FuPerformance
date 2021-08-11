package com.fuusy.fuperformance.appstart.dispatcher

abstract class MainTask : Task() {
    override fun runOnMainThread(): Boolean {
        return true
    }
}