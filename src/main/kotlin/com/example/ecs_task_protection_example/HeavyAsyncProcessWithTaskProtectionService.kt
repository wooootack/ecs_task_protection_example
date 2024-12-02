package com.example.ecs_task_protection_example

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class HeavyAsyncProcessWithTaskProtectionService(
    private val asyncProcessEventRepository: AsyncProcessEventRepository,
    private val transactionalExecutor: TransactionalExecutor,
    private val taskProtectionExecutor: TaskProtectionExecutor,
) {
    private val logger = LoggerFactory.getLogger(HeavyAsyncProcessWithTaskProtectionService::class.java)

    fun execute() {
        logger.info("""
            Start heavy process with task protection
        """.trimIndent())

        val event = asyncProcessEventRegister()
        taskProtectionExecutor.execute("HeavyAsyncProcess") { runAsyncProcess(event) }

        logger.info("""
            End heavy process with task protection
        """.trimIndent())
    }

    /**
     * 非同期処理が始まる前に別トランザクションでイベントを登録する
     */
    fun asyncProcessEventRegister(): HeavyAsyncProcessEvent {
        return transactionalExecutor.execute {
            val asyncProcessEvent = asyncProcessEventRepository.findRunningEvent()
            if (asyncProcessEvent != null) {
                throw Exception("既に処理中のイベントが存在します")
            }

            val runningEvent = HeavyAsyncProcessEvent.createRunningEvent()
            asyncProcessEventRepository.insert(runningEvent)

            runningEvent
        }
    }

    /**
     * イベント登録後に別トランザクションを切って実行する
     * これは非同期処理なのですぐにレスポンスを返す
     *
     * このメソッドが中断されたりそもそも呼び出されなかった場合、イベントのステータスが更新されない
     */
    @Async
    fun runAsyncProcess(event: HeavyAsyncProcessEvent) {
        try {
            transactionalExecutor.execute {
                Thread.sleep(30000) // 重い処理を模擬
                event.success()
                asyncProcessEventRepository.update(event)
            }
        } catch (e: Throwable) {
            transactionalExecutor.execute {
                // この後に再スローするためトランザクションを切ってステータスを変更する
                event.failure()
                asyncProcessEventRepository.update(event)
            }
            throw e
        }
    }
}
