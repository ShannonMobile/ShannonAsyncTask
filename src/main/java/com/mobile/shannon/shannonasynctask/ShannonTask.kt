package com.mobile.shannon.shannonasynctask

import com.mobile.shannon.shannonasynctask.handler.HandlerTaskProcessor


/**
 * 必须调用[run]才会执行
 * 默认在工作线程执行，结果在主线程响应。（可调接口runOn()/responseOn()修改执行与响应的线程）
 * @param mHeavyFunction  需要异步执行的方法
 */
public class ShannonTask<T>(private var mTaskProcessor: ITaskProcessor<T>) {
    companion object {
        public const val LOG_TAG = "ShannonTask"
    }

    constructor(heavyFunction: () -> T): this(HandlerTaskProcessor(heavyFunction))

    fun setTaskProcessor(taskProcessor: ITaskProcessor<T>): ShannonTask<T> {
        mTaskProcessor = taskProcessor
        return this
    }

    //设置异常结果所执行的线程
    fun errorOn(threadContext: ThreadContext): ShannonTask<T> {
        mTaskProcessor.errorOn(threadContext)
        return this
    }

    //设置响应结果所执行的线程
    fun responseOn(threadContext: ThreadContext): ShannonTask<T> {
        mTaskProcessor.responseOn(threadContext)
        return this
    }

    //设置响应结果所执行在主线程
    fun responseOnUI(): ShannonTask<T> {
        mTaskProcessor.responseOnUI()
        return this
    }

    //设置响应结果所执行在工作线程
    fun responseOnBG(): ShannonTask<T> {
        mTaskProcessor.responseOnBG()
        return this
    }

    //设置任务执行所在线程
    fun on(threadContext: ThreadContext): ShannonTask<T> {
        mTaskProcessor.on(threadContext)
        return this
    }

    //设置任务执行在工作线程
    fun onBG(): ShannonTask<T> {
        mTaskProcessor.onBG()
        return this
    }

    //设置任务执行在主线程
    fun onUI(): ShannonTask<T> {
        mTaskProcessor.onUI()
        return this
    }

    //设置异常执行逻辑
    fun onError(onError: (error: Throwable?) -> Unit): ShannonTask<T> {
        mTaskProcessor.onError(onError)
        return this
    }

    //设置响应执行逻辑
    fun onResponse(onResponse: (response: T?) -> Unit): ShannonTask<T> {
        mTaskProcessor.onResponse(onResponse)
        return this
    }

    //执行任务
    fun run(delay: Long = 0): ShannonTask<T>? {
        mTaskProcessor.run(delay)
        return this
    }
}
