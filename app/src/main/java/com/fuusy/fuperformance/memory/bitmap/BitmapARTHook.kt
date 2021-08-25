package com.fuusy.fuperformance.memory.bitmap

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnPreDrawListener
import android.widget.ImageView
import android.widget.Toast
import com.blankj.utilcode.util.ToastUtils
import de.robv.android.xposed.XC_MethodHook


/**
 * @date：2021/8/24
 * @author fuusy
 * @instruction： Bitmap大小监控方案，ARTHook方案,基于Epic框架。
 */
class BitmapARTHook : XC_MethodHook() {
    @Throws(Throwable::class)

    override fun afterHookedMethod(param: MethodHookParam) {
        super.afterHookedMethod(param)

        val imageView = param.thisObject as ImageView
        checkBitmap(imageView, (param.thisObject as ImageView).drawable)
    }

    companion object {
        private const val TAG = "BitmapARTHook"

        //检查Bitmap大小
        private fun checkBitmap(obj: Any, drawable: Drawable) {
            if (drawable is BitmapDrawable && obj is View) {
                val bitmap = drawable.bitmap
                if (bitmap != null) {
                    val view = obj
                    val width = view.width
                    val height = view.height
                    if (width > 0 && height > 0) {
                        // 图标宽高都大于view带下的2倍以上，则发出警告
                        if (bitmap.width >= width shl 1
                            && bitmap.height >= height shl 1
                        ) {
                            warn(
                                bitmap.width,
                                bitmap.height,
                                width,
                                height,
                                RuntimeException("Bitmap size too large")
                            )
                        }
                    } else {
                        val stackTrace: Throwable = RuntimeException()
                        view.viewTreeObserver.addOnPreDrawListener(object :
                            OnPreDrawListener {
                            override fun onPreDraw(): Boolean {
                                val w = view.width
                                val h = view.height
                                if (w > 0 && h > 0) {
                                    if (bitmap.width >= w shl 1
                                        && bitmap.height >= h shl 1
                                    ) {
                                        warn(bitmap.width, bitmap.height, w, h, stackTrace)
                                    }
                                    view.viewTreeObserver.removeOnPreDrawListener(this)
                                }
                                return true
                            }
                        })
                    }
                }
            }
        }

        /**
         * 发出警告，并打印出Bitmap的大小。
         */
        private fun warn(
            bitmapWidth: Int,
            bitmapHeight: Int,
            viewWidth: Int,
            viewHeight: Int,
            t: Throwable
        ) {
            val warnInfo = StringBuilder("Bitmap size too large: ")
                .append("\n real size: (").append(bitmapWidth).append(',').append(bitmapHeight)
                .append(')')
                .append("\n desired size: (").append(viewWidth).append(',').append(viewHeight)
                .append(')')
                .append("\n call stack trace: \n").append(Log.getStackTraceString(t)).append('\n')
                .toString()
            Log.i(TAG, "warn: $warnInfo")
            ToastUtils.showShort(warnInfo)
        }
    }
}