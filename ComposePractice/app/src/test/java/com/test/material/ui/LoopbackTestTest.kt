package com.test.material.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.RadioGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.size
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

import com.test.material.R
import com.test.material.ui.LoopbackTestTest.*

import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowContextCompat::class, ShadowActivityCompat::class])
class LoopbackTestTest {

    companion object {
        var isGranted: Boolean = true
    }

    @Implements(ContextCompat::class)
    class ShadowContextCompat {

        companion object {

            @Implementation
            @JvmStatic
            fun checkSelfPermission(context: Context, permission: String): Int {
                if (isGranted) return PackageManager.PERMISSION_GRANTED

                return PackageManager.PERMISSION_DENIED
            }
        }
    }

    @Implements(ActivityCompat::class)
    class ShadowActivityCompat {

        companion object {

            @Implementation
            @JvmStatic
            fun requestPermissions(
                activity: Activity,
                permissions: Array<String>,
                requestCode: Int
            ) {
                isGranted = true
            }
        }
    }

    private lateinit var controller: ActivityController<LoopbackTest>

    @Before
    fun setUp() {
        mockkStatic(
            ContextCompat::class,
            ActivityCompat::class
        )

        controller = Robolectric.buildActivity(LoopbackTest::class.java)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should request record permission if not granted`() {
        isGranted = false
        val testActivity = controller.setup().get()

        verify(exactly = 1) {
            ActivityCompat.checkSelfPermission(testActivity, Manifest.permission.RECORD_AUDIO)
            ActivityCompat.requestPermissions(
                testActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        }
    }

    @Test
    fun `should not request record permission if already granted`() {
        val testActivity = controller.setup().get()

        verify(exactly = 1) {
            ActivityCompat.checkSelfPermission(testActivity, Manifest.permission.RECORD_AUDIO)
        }
        verify(exactly = 0) {
            ActivityCompat.requestPermissions(
                testActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        }
    }

    @Test
    fun `Loopback RadioButtons should be checked in Stop Loopback! Button`() {
        val testActivity = controller.setup().get()
        val radioGroup = testActivity.findViewById<RadioGroup>(R.id.radioGroup)

        assert(radioGroup.size == 2)
        assert(radioGroup.getChildAt(0).id == R.id.bt_start)
        assert(radioGroup.getChildAt(1).id == R.id.bt_stop)
        assert(radioGroup.checkedRadioButtonId == R.id.bt_stop)
    }

    @Test
    fun `start & stop LoopbackTest Task by checking Start & Stop Loopback! Button`() {
        val testActivity = controller.setup().get()
        val radioGroup = testActivity.findViewById<RadioGroup>(R.id.radioGroup)

        radioGroup.check(R.id.bt_start)

        assert(testActivity.isRecording)
        assert(testActivity.loopbackTaskThread!!.isAlive)

        radioGroup.check(R.id.bt_stop)

        assert(!testActivity.isRecording)
        assert(testActivity.loopbackTaskThread?.isInterrupted ?: true)
    }
}
