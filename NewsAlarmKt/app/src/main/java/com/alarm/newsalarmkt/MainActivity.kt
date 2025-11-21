package com.alarm.newsalarmkt

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alarm.newsalarmkt.ui.MainView
import com.alarm.newsalarmkt.ui.theme.NewsAlarmKtTheme

class MainActivity : BaseActivity("MainActivity") {

    private var backKeyReleasedTime = -1L
    private var backKeyPressedTime = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsAlarmKtTheme {
                MainView()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        backKeyPressedTime = System.currentTimeMillis()
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val curBackKeyReleased = System.currentTimeMillis()
            if (isBackKeyPressedOverTwoSeconds(curBackKeyReleased)) {
                return true
            }
            if (isBackKeyReleasedTwoTimesWithinTwoSeconds(curBackKeyReleased)) {
                finish()
                return true
            }
            Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르시면 종료합니다.", Toast.LENGTH_SHORT).show()
            backKeyReleasedTime = curBackKeyReleased
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun isBackKeyPressedOverTwoSeconds(curBackKeyReleased: Long): Boolean {
        return curBackKeyReleased - backKeyPressedTime > BACK_KEY_GAP
    }

    private fun isBackKeyReleasedTwoTimesWithinTwoSeconds(curBackKeyReleased: Long): Boolean {
        return curBackKeyReleased - backKeyReleasedTime < BACK_KEY_GAP
    }

    companion object {
        const val BACK_KEY_GAP = 2000
    }
}
