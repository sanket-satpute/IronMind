package com.sanket_satpute_20.ironmind.confidence

import java.time.LocalDate
import com.sanket_satpute_20.ironmind.data.ConfidenceIgnitionEntry

data class IgnitionLine(
    val id: String,
    val text: String
)

data class IgnitionVoicePrompt(
    val id: String,
    val title: String,
    val cue: String
)

data class IgnitionMovePrompt(
    val id: String,
    val title: String,
    val cue: String
)

data class IgnitionCouragePrompt(
    val id: String,
    val category: IgnitionCourageCategory,
    val title: String,
    val cue: String
)

enum class IgnitionCourageCategory(
    val label: String
) {
    GROUP("Group Speaking"),
    SOCIAL("Social Opening"),
    STRANGER("New People"),
    INTERVIEW("Interview Pressure")
}

data class ConfidenceIgnitionPlan(
    val line: IgnitionLine,
    val voicePrompt: IgnitionVoicePrompt,
    val movePrompt: IgnitionMovePrompt,
    val couragePrompt: IgnitionCouragePrompt
)

object ConfidenceIgnitionLibrary {

    private val lines = listOf(
        IgnitionLine("line_fear_go", "Fear means go."),
        IgnitionLine("line_move_first", "Move before mood."),
        IgnitionLine("line_speak_first", "Speak before comfort."),
        IgnitionLine("line_proof", "Proof over perfection."),
        IgnitionLine("line_steady", "Calm is a skill. Train it."),
        IgnitionLine("line_edge", "Do the edge thing today."),
        IgnitionLine("line_hide", "Do not hide from the room."),
        IgnitionLine("line_act", "Action is self-respect.")
    )

    private val voicePrompts = listOf(
        IgnitionVoicePrompt("voice_plan", "Set the plan", "Say what you will do today and what you will not avoid."),
        IgnitionVoicePrompt("voice_reason", "State your reason", "Say one reason you can handle today well."),
        IgnitionVoicePrompt("voice_project", "Explain your work", "Explain one thing you built or understood recently."),
        IgnitionVoicePrompt("voice_interview", "Interview rep", "Answer: tell me about yourself in a calm clear way."),
        IgnitionVoicePrompt("voice_edge", "Name the edge", "Say the one thing that scares you most today.")
    )

    private val movePrompts = listOf(
        IgnitionMovePrompt("move_push", "10 pushups", "Wake the body and stand taller before the world gets you."),
        IgnitionMovePrompt("move_stretch", "30-second stretch", "Open your shoulders and drop the sleepy posture."),
        IgnitionMovePrompt("move_stance", "20-second power stance", "Chin level, shoulders back, breathe slow."),
        IgnitionMovePrompt("move_walk", "1-minute walk", "Move your feet and get blood into the day.")
    )

    private val couragePrompts = listOf(
        IgnitionCouragePrompt("courage_group", IgnitionCourageCategory.GROUP, "Speak once in a group", "One sentence is enough. Do not stay silent all day."),
        IgnitionCouragePrompt("courage_question", IgnitionCourageCategory.SOCIAL, "Ask one question", "Ask before overthinking. Your job is to move first."),
        IgnitionCouragePrompt("courage_message", IgnitionCourageCategory.SOCIAL, "Send one message first", "Start one connection instead of waiting to be chosen."),
        IgnitionCouragePrompt("courage_stranger", IgnitionCourageCategory.STRANGER, "Talk to one new person", "Keep it simple. Calm beats impressive."),
        IgnitionCouragePrompt("courage_interview", IgnitionCourageCategory.INTERVIEW, "Answer one hard prompt aloud", "Treat pressure as practice, not a threat.")
    )

    fun planFor(date: LocalDate): ConfidenceIgnitionPlan {
        return ConfidenceIgnitionPlan(
            line = pick(lines, date, salt = 3),
            voicePrompt = pick(voicePrompts, date, salt = 7),
            movePrompt = pick(movePrompts, date, salt = 11),
            couragePrompt = pick(couragePrompts, date, salt = 17)
        )
    }

    fun adaptivePlanFor(
        date: LocalDate,
        recentEntries: List<ConfidenceIgnitionEntry>
    ): ConfidenceIgnitionPlan {
        val basePlan = planFor(date)
        val targetCategory = mostNeededCourageCategory(recentEntries) ?: basePlan.couragePrompt.category
        val targetPrompts = couragePrompts.filter { it.category == targetCategory }
        return basePlan.copy(
            couragePrompt = pick(targetPrompts.ifEmpty { couragePrompts }, date, salt = 17)
        )
    }

    fun lineById(id: String): IgnitionLine = lines.firstOrNull { it.id == id } ?: lines.first()

    fun voicePromptById(id: String): IgnitionVoicePrompt =
        voicePrompts.firstOrNull { it.id == id } ?: voicePrompts.first()

    fun movePromptById(id: String): IgnitionMovePrompt =
        movePrompts.firstOrNull { it.id == id } ?: movePrompts.first()

    fun couragePromptById(id: String): IgnitionCouragePrompt =
        couragePrompts.firstOrNull { it.id == id } ?: couragePrompts.first()

    fun courageCategoryByPromptId(id: String): IgnitionCourageCategory =
        couragePromptById(id).category

    fun mostNeededCourageCategory(recentEntries: List<ConfidenceIgnitionEntry>): IgnitionCourageCategory? {
        if (recentEntries.isEmpty()) return null
        val scores = IgnitionCourageCategory.entries.associateWith { 0 }.toMutableMap()
        recentEntries.take(21).forEachIndexed { index, entry ->
            val category = courageCategoryByPromptId(entry.couragePromptId)
            val recencyWeight = (recentEntries.size - index).coerceAtLeast(1)
            val delta = when {
                entry.completed -> -2
                entry.courageAccepted -> 1
                else -> 3
            }
            scores[category] = (scores[category] ?: 0) + (delta * recencyWeight)
        }
        return scores.maxByOrNull { it.value }?.key
    }

    private fun <T> pick(items: List<T>, date: LocalDate, salt: Int): T {
        val index = ((date.toEpochDay() + salt) % items.size).toInt().let {
            if (it < 0) it + items.size else it
        }
        return items[index]
    }
}
