package com.sanket_satpute_20.ironmind.psychology

enum class PersonaNotifyType {
    STARTING_SOON,
    TASK_ACTIVE,
    TASK_SKIPPED
}

object NotificationPersonaEngine {

    fun getTitle(userType: UserType, type: PersonaNotifyType, taskName: String): String {
        return when (userType) {
            UserType.ACHIEVER -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "⚔️ Battle Prep: $taskName"
                PersonaNotifyType.TASK_ACTIVE -> "🔥 Front Lines: $taskName"
                PersonaNotifyType.TASK_SKIPPED -> "💀 Defeat logged."
            }
            UserType.BROKEN_STRIVER -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "🌱 Small Step: $taskName"
                PersonaNotifyType.TASK_ACTIVE -> "🦾 Building: $taskName"
                PersonaNotifyType.TASK_SKIPPED -> "🔄 Compassion required."
            }
            UserType.PERFECTIONIST -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "⚙️ System Check: $taskName"
                PersonaNotifyType.TASK_ACTIVE -> "📏 Standard: $taskName"
                PersonaNotifyType.TASK_SKIPPED -> "⚠️ Protocol breach."
            }
            UserType.BURNED_OUT -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "🔋 Gentle Start: $taskName"
                PersonaNotifyType.TASK_ACTIVE -> "🕯️ Steady Light: $taskName"
                PersonaNotifyType.TASK_SKIPPED -> "🌬️ Rest needed."
            }
            UserType.IDENTITY_SEEKER -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "🧭 Direction: $taskName"
                PersonaNotifyType.TASK_ACTIVE -> "👤 Identity Work: $taskName"
                PersonaNotifyType.TASK_SKIPPED -> "🌫️ Path lost."
            }
            UserType.SYSTEMS_PERSON -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "📊 Schedule Entry: $taskName"
                PersonaNotifyType.TASK_ACTIVE -> "⚡ Process Execution: $taskName"
                PersonaNotifyType.TASK_SKIPPED -> "❌ Error: Skip detected."
            }
            UserType.DOOMSCROLLER -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "📱 Digital Detox: $taskName"
                PersonaNotifyType.TASK_ACTIVE -> "🛑 Focus Locked: $taskName"
                PersonaNotifyType.TASK_SKIPPED -> "📉 Relapse Logged."
            }
            UserType.PROCRASTINATOR -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "⏳ Stop Waiting: $taskName"
                PersonaNotifyType.TASK_ACTIVE -> "⚡ Momentum: $taskName"
                PersonaNotifyType.TASK_SKIPPED -> "⌛ Delay Logged."
            }
        }
    }

    fun getBody(userType: UserType, type: PersonaNotifyType, taskName: String): String {
        return when (userType) {
            UserType.ACHIEVER -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "5 minutes until deployment. Gear up."
                PersonaNotifyType.TASK_ACTIVE -> "The competition is working. Maintain the standard."
                PersonaNotifyType.TASK_SKIPPED -> "You surrendered ground. Zero tolerance tomorrow."
            }
            UserType.BROKEN_STRIVER -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "Ready for a small win? 5 minutes to go."
                PersonaNotifyType.TASK_ACTIVE -> "One brick at a time. You are becoming resilient."
                PersonaNotifyType.TASK_SKIPPED -> "A slip is not a stop. Breathe. Come back soon."
            }
            UserType.PERFECTIONIST -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "Initiating protocol in 5 minutes. Clear the deck."
                PersonaNotifyType.TASK_ACTIVE -> "Accuracy is everything. Do it once, do it right."
                PersonaNotifyType.TASK_SKIPPED -> "Deviation recorded. Analyze the error and reset."
            }
            UserType.BURNED_OUT -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "Almost time. Just show up, that is enough."
                PersonaNotifyType.TASK_ACTIVE -> "Keep it sustainable. You are safe to work now."
                PersonaNotifyType.TASK_SKIPPED -> "Your battery was low. Let it charge. No shame."
            }
            UserType.IDENTITY_SEEKER -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "Finding the path in 5 minutes. Are you ready?"
                PersonaNotifyType.TASK_ACTIVE -> "Every minute here confirms who you really are."
                PersonaNotifyType.TASK_SKIPPED -> "Lost the trail today. Recalibrate tonight."
            }
            UserType.SYSTEMS_PERSON -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "T-minus 5 minutes. Aligning resources."
                PersonaNotifyType.TASK_ACTIVE -> "Optimizing focus duration. Stay in the system."
                PersonaNotifyType.TASK_SKIPPED -> "Input missed. Logging pattern for analysis."
            }
            UserType.DOOMSCROLLER -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "📱 Log off. 5 mins to reality."
                PersonaNotifyType.TASK_ACTIVE -> "🛑 Stop scrolling. Start living."
                PersonaNotifyType.TASK_SKIPPED -> "📉 You let the algorithm win again."
            }
            UserType.PROCRASTINATOR -> when (type) {
                PersonaNotifyType.STARTING_SOON -> "⏳ The clock is ticking. 5 minutes."
                PersonaNotifyType.TASK_ACTIVE -> "⚡ Action cures fear. Keep moving."
                PersonaNotifyType.TASK_SKIPPED -> "⌛ Time slipped away. Again."
            }
        }
    }
}
