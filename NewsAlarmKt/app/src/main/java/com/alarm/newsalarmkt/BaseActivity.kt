package com.alarm.newsalarmkt

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.alarm.newsalarmkt.utils.LogUtil.logI

open class BaseActivity(protected val className: String) : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        logI(className, "onCreate", "")
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        logI(className, "onStart", "")
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
