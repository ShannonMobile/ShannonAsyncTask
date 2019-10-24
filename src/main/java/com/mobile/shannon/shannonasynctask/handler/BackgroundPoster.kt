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

import android.util.Log

/**
 * 使用线程池复用，但同一时间只会有一个线程执行任务
 */
public class BackgroundPoster : Runnable, Poster {

    private val queue: PendingPostQueue = PendingPostQueue()

    @Volatile
    private var executorRunning: Boolean = false // volatile保证多线程可见性
    private val executorService = JobExecutor

    override fun enqueue(dispatched: () -> Any?) {
        val pendingPost = PendingPost.obtainPendingPost(dispatched)
        synchronized(this) {
            queue.enqueue(pendingPost)
            if (!executorRunning) {
                executorRunning = true
                executorService.execute(this)
            }
        }
    }

    override fun run() {
        try {
            try {
                while (true) {
                    var pendingPost = queue.poll(1000)
                    if (pendingPost == null) {
                        synchronized(this) {
                            // Check again, this time in synchronized
                            pendingPost = queue.poll()
                            if (pendingPost == null) {
                                executorRunning = false
                                return
                            }
                        }
                    }
                    pendingPost!!.block?.invoke()
                    PendingPost.releasePendingPost(pendingPost!!)
                }
            } catch (e: InterruptedException) {
                Log.w("ShannonTask", Thread.currentThread().name + " was interruppted", e)
            }
        } finally {
            executorRunning = false
        }
    }

}
