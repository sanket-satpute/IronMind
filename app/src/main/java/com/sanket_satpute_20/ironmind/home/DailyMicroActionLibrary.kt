package com.sanket_satpute_20.ironmind.home

import com.sanket_satpute_20.ironmind.data.Task
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

const val DAILY_ONE_PERCENT_ORIGIN = "DAILY_ONE_PERCENT"

data class DailyMicroAction(
    val id: String,
    val title: String,
    val cue: String,
    val startTime: LocalTime = LocalTime.of(5, 15),
    val durationMinutes: Int = 10,
    val taskType: String = "OTHER",
    val focusModeEnabled: Boolean = false,
    val focusPreset: String = "CLASSIC_25_5"
)

object DailyMicroActionLibrary {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val actions: List<DailyMicroAction> = listOf(
        DailyMicroAction(
            id = "confidence_voice_rep",
            title = "90s Confidence Voice Rep",
            cue = "Record one unedited voice rep about what you are building today.",
            taskType = "BEHAVIORAL"
        ),
        DailyMicroAction(
            id = "behavioral_answer",
            title = "One Behavioral Answer",
            cue = "Answer one interview question out loud in 90 seconds.",
            taskType = "BEHAVIORAL"
        ),
        DailyMicroAction(
            id = "project_review_rep",
            title = "Project Review Rep",
            cue = "Explain one IronMind decision as if you were in an interview.",
            taskType = "PROJECT_REVIEW"
        ),
        DailyMicroAction(
            id = "concept_explain",
            title = "Explain One Technical Concept",
            cue = "Teach one concept aloud in simple language for two minutes.",
            taskType = "SYSTEM_DESIGN"
        ),
        DailyMicroAction(
            id = "dsa_three",
            title = "Three DSA Problems",
            cue = "Solve three focused DSA reps and move on.",
            durationMinutes = 15,
            taskType = "DSA",
            focusModeEnabled = true
        ),
        DailyMicroAction(
            id = "company_blog",
            title = "Read One Engineering Post",
            cue = "Read one company engineering blog post and note two takeaways.",
            taskType = "READ"
        ),
        DailyMicroAction(
            id = "reconnect_message",
            title = "One Reconnect Message",
            cue = "Send one short message to someone you have not spoken to recently.",
            taskType = "ADMIN"
        ),
        DailyMicroAction(
            id = "word_rep",
            title = "Use One New Word",
            cue = "Learn one word you avoid and use it in a real sentence today.",
            taskType = "READ"
        ),
        DailyMicroAction(
            id = "pushups_rep",
            title = "20 Pushups",
            cue = "Do 20 pushups before the day gets loud.",
            durationMinutes = 5,
            taskType = "PHYSICAL"
        ),
        DailyMicroAction(
            id = "walk_rep",
            title = "10 Minute Walk",
            cue = "Take a short outdoor walk and log it cleanly.",
            taskType = "PHYSICAL"
        ),
        DailyMicroAction(
            id = "project_bullets",
            title = "Three Build Bullet Points",
            cue = "Write three bullet points about what you built and why it matters.",
            taskType = "PROJECT_REVIEW"
        ),
        DailyMicroAction(
            id = "system_design_bullets",
            title = "System Design Three Bullets",
            cue = "Take one design prompt and write three clear approach bullets.",
            taskType = "SYSTEM_DESIGN",
            focusModeEnabled = true
        )
    )

    fun actionForDate(date: LocalDate): DailyMicroAction {
        val index = Math.floorMod(date.dayOfYear + date.year, actions.size)
        return actions[index]
    }

    fun buildTask(action: DailyMicroAction, date: LocalDate): Task {
        val endTime = action.startTime.plusMinutes(action.durationMinutes.toLong())
        return Task(
            name = action.title,
            startTime = action.startTime.format(timeFormatter),
            endTime = endTime.format(timeFormatter),
            date = date.toString(),
            importanceRank = 2,
            isMissionSix = false,
            origin = DAILY_ONE_PERCENT_ORIGIN,
            taskType = action.taskType,
            focusModeEnabled = action.focusModeEnabled,
            focusPreset = action.focusPreset,
            focusNotes = action.cue,
            durationMinutes = action.durationMinutes,
            difficultyMultiplier = 0.85f
        )
    }

    fun ignitionAlignedCue(action: DailyMicroAction, courageCue: String?): String {
        val normalizedCourageCue = courageCue?.trim().orEmpty()
        if (normalizedCourageCue.isBlank()) return action.cue
        return "$normalizedCourageCue\n\n1% rep: ${action.cue}"
    }
}
