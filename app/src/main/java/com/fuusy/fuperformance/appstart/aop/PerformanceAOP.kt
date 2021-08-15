package com.fuusy.fuperformance.appstart.aop

import android.util.Log
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect

/**
 * @date：2021/8/10
 * @author fuusy
 * @instruction： AOP监听每个方法的执行时间，使用aspectj。
 */
@Aspect
class PerformanceAOP {

    companion object {
        private const val TAG = "PerformanceAOP"
    }

    @Around("call(* com.fuusy.fuperformance.App.**(..))")
    fun getMethodTime(joinPoint: ProceedingJoinPoint) {
        val signature = joinPoint.signature
        val time: Long = System.currentTimeMillis()
        joinPoint.proceed()
        Log.d(TAG, "${signature.toShortString()} speed time = ${System.currentTimeMillis() - time}")
    }
}