package com.mobile.shannon.shannonasynctask


public interface ITaskProcessor<T> {

    //设置异常结果所执行的线程
    fun errorOn(threadContext: ThreadContext)

    //设置响应结果所执行的线程
    fun responseOn(threadContext: ThreadContext)

    //设置响应结果所执行在主线程
    fun responseOnUI()

    //设置响应结果所执行在工作线程
    fun responseOnBG()

    //设置任务执行所在线程
    fun on(threadContext: ThreadContext)

    //设置任务执行在工作线程
    fun onBG()

    //设置任务执行在主线程
    fun onUI()

    //设置异常执行逻辑
    fun onError(onError: (error: Throwable?) -> Unit)

    //设置响应执行逻辑
    fun onResponse(onResponse: (response: T?) -> Unit)

    fun run(delay: Long = 0)

    fun cancel()

}