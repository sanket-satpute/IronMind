package com.sanket_satpute_20.ironmind.bossmode

data class BossModeUpgrade(
    val upgradedTaskName: String,
    val originalEndTime: String,
    val newEndTime: String,
    val newPunishmentSeconds: Long,
    val weekNumber: Int
)
