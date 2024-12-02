package com.example.ecs_task_protection_example

import org.springframework.stereotype.Service

class HeavyAsyncProcessEvent(
    val id: String,
    val status: String
) {
    fun success(): HeavyAsyncProcessEvent {
        return HeavyAsyncProcessEvent(
            this.id,
            "SUCCESS"
        )
    }

    fun failure(): HeavyAsyncProcessEvent {
        return HeavyAsyncProcessEvent(
            this.id,
            "FAILURE"
        )
    }

    companion object {
        fun createRunningEvent(): HeavyAsyncProcessEvent {
            return HeavyAsyncProcessEvent(
                generateRandomString(26),
                "RUNNING"
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

interface AsyncProcessEventRepository {
    fun findRunningEvent(): HeavyAsyncProcessEvent?
    fun insert(asyncProcessEvent: HeavyAsyncProcessEvent)
    fun update(asyncProcessEvent: HeavyAsyncProcessEvent)
}

/**
 * 本筋ではないので割愛
 */
@Service
class AsyncProcessEventDummyRepository : AsyncProcessEventRepository {
    override fun findRunningEvent(): HeavyAsyncProcessEvent? {
        return null
    }

    override fun insert(asyncProcessEvent: HeavyAsyncProcessEvent) {
        // do nothing
    }

    override fun update(asyncProcessEvent: HeavyAsyncProcessEvent) {
        // do nothing
    }
}
