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


internal class PendingPostQueue {
    private var head: PendingPost? = null
    private var tail: PendingPost? = null

    @Synchronized
    fun enqueue(pendingPost: PendingPost?) {
        if (pendingPost == null) {
            throw NullPointerException("null cannot be enqueued")
        }
        when {
            tail != null -> {
                tail!!.next = pendingPost
                tail = pendingPost
            }
            head == null -> {
                tail = pendingPost
                head = tail
            }
            else -> throw IllegalStateException("Head present, but no tail")
        }
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        (this as Object).notifyAll()
    }

    @Synchronized
    fun poll(): PendingPost? {
        val pendingPost = head
        if (head != null) {
            head = head!!.next
            if (head == null) {
                tail = null
            }
        }
        return pendingPost
    }

    @Synchronized
    @Throws(InterruptedException::class)
    fun poll(maxMillisToWait: Int): PendingPost? {
        if (head == null) {
            @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            (this as Object).wait(maxMillisToWait.toLong())
        }
        return poll()
    }

}
