package com.fuusy.fuperformance.appstart.task

import com.fuusy.fuperformance.appstart.dispatcher.Task
import com.kingja.loadsir.core.LoadSir

/**
 * @date：2021/8/11
 * @author fushiyao
 * @instruction：
 */
class LoadSirTask : Task() {
    override fun run() {
        LoadSir.beginBuilder()
            .commit()
    }
}