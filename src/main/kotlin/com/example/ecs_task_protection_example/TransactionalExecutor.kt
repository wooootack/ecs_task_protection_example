package com.example.ecs_task_protection_example

import org.springframework.stereotype.Service

interface TransactionalExecutor {
    fun <T> execute(action: () -> T): T
}

/**
 * 本来は受け取った関数をトランザクションを張って実行するが、今回の本筋内容ではないので割愛
 */
@Service
class TransactionalDummyExecutor : TransactionalExecutor {
    override fun <T> execute(action: () -> T): T {
        return action()
    }
}