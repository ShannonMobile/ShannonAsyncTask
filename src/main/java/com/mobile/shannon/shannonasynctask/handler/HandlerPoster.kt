/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mobile.shannon.shannonasynctask.handler

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock

/**
 * 使用Handler处理事件，实际目前只用来调度到主线程
 */
public class HandlerPoster(
    looper: Looper
) : Handler(looper), Poster {

    private var handlerActive: Boolean = false
    // 需要调度到目标线程的任务队列
    private val queue: PendingPostQueue = PendingPostQueue()
    // handleMessage中循环处理队列的最大时间
    private val maxMillisInsideHandleMessage = 10

    override fun enqueue(dispatched: () -> Any?) {
        val pendingPost = PendingPost.obtainPendingPost(dispatched)
        synchronized(this) {
            queue.enqueue(pendingPost)
            if (!handlerActive) {
                handlerActive = true
                if (!sendMessage(obtainMessage())) {
                    throw RuntimeException("Could not send handler message")
                }
            }
        }
    }

    override fun handleMessage(msg: Message) {
        var rescheduled = false
        try {
            val started = SystemClock.uptimeMillis()
            // 在一次handleMessage中循环处理队列中的任务，
            // 避免频繁向主线程sendMessage()
            while (true) {
                var pendingPost: PendingPost? = queue.poll()
                if (pendingPost == null) {
                    synchronized(this) {
                        // Check again, this time in synchronized
                        pendingPost = queue.poll()
                        if (pendingPost == null) {
                            handlerActive = false
                            return
                        }
                    }
                }
                pendingPost!!.block?.invoke()
                PendingPost.releasePendingPost(pendingPost!!)
                val timeInMethod = SystemClock.uptimeMillis() - started
                // 由于一次处理了多个任务，这里判断是否大于间隔时间
                // 若是则让出主线程，避免长时间阻塞主线程
                if (timeInMethod >= maxMillisInsideHandleMessage) {
                    if (!sendMessage(obtainMessage())) {
                        throw RuntimeException("Could not send handler message")
                    }
                    rescheduled = true
                    return
                }
            }
        } finally {
            handlerActive = rescheduled
        }
    }
}