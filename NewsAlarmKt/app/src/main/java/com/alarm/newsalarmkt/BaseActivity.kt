package com.alarm.newsalarmkt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alarm.newsalarmkt.utils.LogUtil.logI

open class BaseActivity(protected val className: String) : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        logI(className, "onCreate", "")
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        logI(className, "onStart", "")
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBarInsets = insets
                .getInsets(WindowInsetsCompat.Type.systemBars())
                .toPlatformInsets()
            v.setPadding(
                systemBarInsets.left,
                systemBarInsets.top,
                systemBarInsets.right,
                systemBarInsets.bottom
            )
            insets
        }
        super.onStart()
    }

    override fun onResume() {
        logI(className, "onResume", "")
        super.onResume()
    }

    override fun onPause() {
        logI(className, "onPause", "")
        super.onPause()
    }

    override fun onStop() {
        logI(className, "onStop", "")
        super.onStop()
    }

    override fun onDestroy() {
        logI(className, "onDestroy", "")
        super.onDestroy()
    }
}
