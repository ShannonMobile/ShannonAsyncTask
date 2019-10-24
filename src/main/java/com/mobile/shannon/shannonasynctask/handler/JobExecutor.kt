package com.mobile.shannon.shannonasynctask.handler

import android.util.Log
import com.mobile.shannon.shannonasynctask.ShannonTask
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * 自定义的线程池，参考AsyncTask
 * @see android.os.AsyncTask
 */
object JobExecutor : Executor {
    private const val KEEP_ALIVE_SECOND = 3L
    private const val BACKUP_POOL_SIZE = 5
    private const val KEEP_ALIVE_SECONDS = 3
    private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
    private val workQueue = SynchronousQueue<Runnable>()
    private val threadFactory = JobThreadFactory()
    private var sBackupExecutor: ThreadPoolExecutor? = null
    private var sBackupExecutorQueue: LinkedBlockingQueue<Runnable>? = null

    // 线程不足任务被拒绝时的兜底措施
    private val sRunOnSerialPolicy = object : RejectedExecutionHandler {
        override fun rejectedExecution(r: Runnable, e: ThreadPoolExecutor) {
            Log.w(ShannonTask.LOG_TAG, "Exceeded ThreadPoolExecutor pool size")
            // As a last ditch fallback, run it on an executor with an unbounded queue.
            // Create this executor lazily, hopefully almost never.
            synchronized(this) {
                if (sBackupExecutor == null) {
                    sBackupExecutorQueue = LinkedBlockingQueue()
                    sBackupExecutor = ThreadPoolExecutor(
                        BACKUP_POOL_SIZE,
                        BACKUP_POOL_SIZE,
                        KEEP_ALIVE_SECONDS.toLong(),
                        TimeUnit.SECONDS,
                        sBackupExecutorQueue,
                        threadFactory
                    )
                    sBackupExecutor?.allowCoreThreadTimeOut(true)
                }
            }
            sBackupExecutor?.execute(r)
        }
    }

    private val threadPoolExecutor = ThreadPoolExecutor(
        NUMBER_OF_CORES,
        NUMBER_OF_CORES * 2,
        KEEP_ALIVE_SECOND,
        TimeUnit.SECONDS,
        workQueue,
        threadFactory,
        sRunOnSerialPolicy
    )

    override fun execute(command: Runnable) {
        threadPoolExecutor.execute(command)
    }

    class JobThreadFactory : ThreadFactory {
        companion object {
            private const val THREAD_NAME = "shannon_"
            private var counter = AtomicInteger(1)
        }
        override fun newThread(r: Runnable): Thread {
            return Thread(r, THREAD_NAME + counter.getAndIncrement())
        }
    }
}