package com.example.ecs_task_protection_example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
class EcsTaskProtectionExampleApplication

fun main(args: Array<String>) {
    runApplication<EcsTaskProtectionExampleApplication>(*args)
}

@EnableAsync
@Configuration(proxyBeanMethods = false)
class AsyncConfiguration