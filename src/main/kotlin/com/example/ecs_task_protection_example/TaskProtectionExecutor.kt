package com.example.ecs_task_protection_example

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.util.concurrent.atomic.AtomicInteger

@Service
class TaskProtectionExecutor(
    private val transactionalExecutor: TransactionalExecutor,
    private val asyncProcessHistoryRepository: AsyncProcessHistoryRepository,
) {
    companion object {
        private val coroutineCount = AtomicInteger(0)
    }

    private val logger = LoggerFactory.getLogger(TaskProtectionExecutor::class.java)

    fun execute(
        name: String,
        action: () -> Unit,
    ) {
        val asyncProcessHistory = AsyncProcessHistory.createStartedJob(name, getContainerId())

        try {
            logger.info(
                """
                Start job!
                id: ${asyncProcessHistory.id}, container_id: ${asyncProcessHistory.containerId}, name: ${asyncProcessHistory.name}
                """.trimIndent()
            )

            protectTask()
            transactionalExecutor.execute {
                asyncProcessHistoryRepository.insert(asyncProcessHistory)
            }

            return action()
        } catch (e: Throwable) {
            logger.error(
                """
                Error occurred in job!
                id: ${asyncProcessHistory.id}, container_id: ${asyncProcessHistory.containerId}, name: ${asyncProcessHistory.name}
                """.trimIndent(),
                e,
            )
            throw e
        } finally {
            logger.info(
                """
                Complete job!
                id: ${asyncProcessHistory.id}, container_id: ${asyncProcessHistory.containerId}, name: ${asyncProcessHistory.name}
                """.trimIndent(),
            )

            unprotectTask()
            transactionalExecutor.execute {
                asyncProcessHistoryRepository.update(asyncProcessHistory.complete())
            }
        }
    }

    private fun protectTask() {
        val count = coroutineCount.incrementAndGet()
        logger.info("Current Coroutine count: $count")
        setProtectionEnabled(true)
        logger.info("Protection enabled")
    }

    private fun unprotectTask() {
        val count = coroutineCount.decrementAndGet()
        logger.info("Current Coroutine count: $count")
        if (count <= 0) {
            setProtectionEnabled(false)
            logger.info("Protection disabled")
        }
    }

    private fun setProtectionEnabled(enabled: Boolean) {
        val ecsAgentUri = System.getenv("ECS_AGENT_URI")
        if (ecsAgentUri.isNullOrBlank()) {
            // NOTE: 環境変数がセットされていない場合はローカルで実行しているとみなし、何もしない
            return
        }

        val response = RestClient
            .create()
            .post()
            .uri("$ecsAgentUri/task-protection/v1/state")
            .contentType(MediaType("application/json"))
            .body("{\"ProtectionEnabled\":$enabled,\"ExpiresInMinutes\":1440}")
            .retrieve()
            .body(String::class.java)

        logger.info(
            """
            Set task protection
            Response: $response
            """.trimIndent(),
        )
    }

    private fun getContainerId(): String {
        // NOTE: ECS_AGENT_URIは、以下のような形式で取得できる
        // http://169.254.170.2/api/b194c578e01b40db82f0a5c7b9b717c7-1622016141

        val ecsAgentUri = System.getenv("ECS_AGENT_URI")
        if (ecsAgentUri.isNullOrBlank()) {
            return ""
        }

        // NOTE: ECS_AGENT_URIの最後のパスがコンテナIDになる
        return ecsAgentUri.split("/").last()
    }
}
