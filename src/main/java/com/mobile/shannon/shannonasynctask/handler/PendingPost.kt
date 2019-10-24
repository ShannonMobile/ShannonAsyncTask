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

import java.util.ArrayList

/**
 * 待执行的任务，由一个任务池缓存复用
 */
internal class PendingPost private constructor(var block: (() -> Any?)?) {
    var next: PendingPost? = null

    companion object {
        private val pendingPostPool = ArrayList<PendingPost>()

        fun obtainPendingPost(block: (() -> Any?)?): PendingPost {
            synchronized(pendingPostPool) {
                val size = pendingPostPool.size
                if (size > 0) {
                    val pendingPost = pendingPostPool.removeAt(size - 1)
                    pendingPost.block = block
                    pendingPost.next = null
                    return pendingPost
                }
            }
            return PendingPost(block)
        }

        fun releasePendingPost(pendingPost: PendingPost) {
            pendingPost.block = null
            pendingPost.next = null
            synchronized(pendingPostPool) {
                // Don't let the pool grow indefinitely
                if (pendingPostPool.size < 10000) {
                    pendingPostPool.add(pendingPost)
                }
            }
        }
    }

}