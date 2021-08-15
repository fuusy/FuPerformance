package com.fuusy.fuperformance.appstart.task

import com.fuusy.fuperformance.appstart.dispatcher.Task

/**
 * @date：2021/8/15
 * @author fushiyao
 * @instruction：初始化微信支付
 */
class WeChatPayTask :Task(){

    /**
     * 微信支付依赖AppId
     */
    override fun dependsOn(): List<Class<out Task?>?>? {
        val task = mutableListOf<Class<out Task?>>()
        task.add(LoadAppIdTask::class.java)
        return task
    }

    override fun run() {
        //初始化微信支付
    }
}