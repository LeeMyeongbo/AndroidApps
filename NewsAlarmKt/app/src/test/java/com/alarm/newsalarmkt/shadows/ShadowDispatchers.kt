package com.alarm.newsalarmkt.shadows

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(Dispatchers::class)
class ShadowDispatchers {

    companion object {
        private var ioDispatcher: CoroutineDispatcher? = null

        fun setIoDispatcher(dispatcher: CoroutineDispatcher) {
            this.ioDispatcher = dispatcher
        }

        fun reset() {
            this.ioDispatcher = null
        }

        @JvmStatic
        @Implementation
        fun getIO(): CoroutineDispatcher {
            return ioDispatcher ?: Dispatchers.IO
        }
    }
}
