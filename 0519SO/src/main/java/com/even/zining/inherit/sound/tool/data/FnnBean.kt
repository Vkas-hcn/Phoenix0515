package com.even.zining.inherit.sound.tool.data

import androidx.annotation.Keep

@Keep
data class FnnBean(
    val config: Config
)

@Keep
data class Config(
    val adLimits: List<Int>,
    val delayRange: String,
    val identifiers: List<Identifier>,
    val internalLink: InternalLink,
    val scheduler: Scheduler,
    val user: User
)

@Keep
data class Identifier(
    val platform: String,
    val tag: String
)
@Keep
data class InternalLink(
    val address: String
)

@Keep
data class Scheduler(
    val displayGap: Int,
    val initialDelay: Int,
    val loopInterval: Int,
    val retryCap: Int
)

@Keep
data class User(
    val isUploader: Int,
    val level: Int
)
