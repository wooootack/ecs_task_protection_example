package com.example.ecs_task_protection_example

import org.springframework.scheduling.annotation.Async
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController(
    private val heavyAsyncProcessWithTaskProtectionService: HeavyAsyncProcessWithTaskProtectionService,
    private val heavyAsyncProcessWithoutTaskProtectionService: HeavyAsyncProcessWithoutTaskProtectionService
) {

    @PostMapping("/with_task_protection")
    @Async
    fun withTaskProtection() {
        heavyAsyncProcessWithTaskProtectionService.execute()
    }

    @PostMapping("/without_task_protection")
    @Async
    fun withoutTaskProtection() {
        heavyAsyncProcessWithoutTaskProtectionService.execute()
    }

}

