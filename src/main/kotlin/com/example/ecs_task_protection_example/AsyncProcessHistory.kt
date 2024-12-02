package com.example.ecs_task_protection_example

import org.springframework.stereotype.Service
import java.util.Date

class AsyncProcessHistory(
    val id: String,
    val name: String,
    val startedAt: Date?,
    val completedAt: Date?,
    val containerId: String,
) {
    fun complete(): AsyncProcessHistory {
        return AsyncProcessHistory(
            this.id,
            this.name,
            this.startedAt,
            Date(),
            this.containerId,
        )
    }

    companion object {
        fun createStartedJob(name: String, containerId: String): AsyncProcessHistory {

            return AsyncProcessHistory(
                generateRandomString(26),
                name,
                Date(),
                null,
                containerId,
            )
        }

        fun generateRandomString(length: Int): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            return (1..length)
                .map { chars.random() }
                .joinToString("")
        }
    }
}

interface AsyncProcessHistoryRepository {
    fun insert(asyncProcessHistory: AsyncProcessHistory)
    fun update(asyncProcessHistory: AsyncProcessHistory)
}

/**
 * 本筋ではないので割愛
 */
@Service
class AsyncProcessHistoryDummyRepository : AsyncProcessHistoryRepository {
    override fun insert(asyncProcessHistory: AsyncProcessHistory) {
        // do nothing
    }

    override fun update(asyncProcessHistory: AsyncProcessHistory) {
        // do nothing
    }
}