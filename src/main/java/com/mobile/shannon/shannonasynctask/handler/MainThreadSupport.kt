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

import android.os.Looper


internal interface MainThreadSupport {
    fun isMainThread(): Boolean
    fun createPoster(): Poster

    class AndroidMainThreadSupport : MainThreadSupport {

        private val looper = Looper.getMainLooper()

        override fun isMainThread(): Boolean {
            return looper == Looper.myLooper()
        }

        override fun createPoster(): Poster {
            return HandlerPoster(looper)
        }
    }
}