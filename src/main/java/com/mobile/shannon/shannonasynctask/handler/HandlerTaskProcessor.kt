package com.mobile.shannon.shannonasynctask.handler

import android.os.Handler
import com.mobile.shannon.shannonasynctask.ITaskProcessor
import com.mobile.shannon.shannonasynctask.ThreadContext

/**
 * 参考EventBus3.1.1的线程调度实现
 * https://github.com/greenrobot/EventBus
 * 主线程由[MainThreadSupport]调度，传递主线程looper至[HandlerPoster]的handler
 * 工作线程在[AsyncPoster]的线程池中
 */
public class HandlerTaskProcessor<T>(private val mHeavyFunction: () -> T) : ITaskProcessor<T> {

    companion object {
        @JvmField
        val UI: ThreadContext = ThreadContext.MAIN
        @JvmField
        val BG: ThreadContext = ThreadContext.BACKGROUND

        internal val mainThreadSupport = MainThreadSupport.AndroidMainThreadSupport()
        internal val mainThreadPoster = mainThreadSupport.createPoster()
        internal var asyncPoster: Poster = AsyncPoster()
    }

    private var mOnError: ((error: Throwable?) -> Unit)? = null
    private var mOnResponse: ((response: T?) -> Unit)? = null
    private var mErrorContext: ThreadContext = UI
    private var mResponseContext: ThreadContext = UI
    private var mRunContext: ThreadContext = BG

    fun setAsyncPoster(poster: Poster) {
        asyncPoster = poster
    }

    //设置异常结果所执行的线程
    override fun errorOn(threadContext: ThreadContext) {
        mErrorContext = threadContext
    }

    //设置响应结果所执行的线程
    override fun responseOn(threadContext: ThreadContext) {
        mResponseContext = threadContext
    }

    //设置响应结果所执行在主线程
    override fun responseOnUI() {
        mResponseContext = UI
    }

    //设置响应结果所执行在工作线程
    override fun responseOnBG() {
        mResponseContext = BG
    }

    //设置任务执行所在线程
    override fun on(threadContext: ThreadContext) {
        mRunContext = threadContext
    }

    //设置任务执行在工作线程
    override fun onBG() {
        mRunContext = BG
    }

    //设置任务执行在主线程
    override fun onUI() {
        mRunContext = UI
    }

    //设置异常执行逻辑
    override fun onError(onError: (error: Throwable?) -> Unit) {
        mOnError = onError
    }

    //设置响应执行逻辑
    override fun onResponse(onResponse: (response: T?) -> Unit) {
        mOnResponse = onResponse
    }

    override fun run(delay: Long) {
        try {
            dispatch(mRunContext) {
                try {
                    val result = mHeavyFunction()
                    mOnResponse?.apply {
                        dispatch(mResponseContext) { invoke(result) }
                    }
                } catch (e: Exception) {
                    mOnError?.apply {
                        dispatch(mErrorContext) { invoke(e) }
                    } ?: dispatch(UI) { throw e }
                }
            }
        } catch (e: Exception) {
            mOnError?.apply {
                dispatch(mErrorContext) { invoke(e) }
            } ?: dispatch(UI) { throw e }
        }
    }

    override fun cancel() {

    }

    private fun dispatch(threadContext: ThreadContext, block: () -> Unit) {
        when (threadContext) {
            ThreadContext.MAIN -> {
                // 若已在主线程，直接执行
                if (mainThreadSupport.isMainThread()) {
                    block()
                } else {
                    mainThreadPoster.enqueue { block() }
                }
            }
            ThreadContext.BACKGROUND -> {
                asyncPoster.enqueue { block() }
            }
        }
    }

}
