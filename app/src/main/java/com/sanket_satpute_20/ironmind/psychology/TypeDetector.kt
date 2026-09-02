package com.sanket_satpute_20.ironmind.psychology

object TypeDetector {

    // ── Question 1: Core Motivation ─────────────────────
    val q1Answers = listOf(
        DiagnosticAnswer(
            text        = "Proving to myself what I'm truly capable of",
            emoji       = "🔥",
            typeScores  = mapOf(UserType.ACHIEVER to 5, UserType.BROKEN_STRIVER to 2)
        ),
        DiagnosticAnswer(
            text        = "Finally living up to the identity I know I have inside",
            emoji       = "🧬",
            typeScores  = mapOf(UserType.IDENTITY_SEEKER to 5, UserType.BROKEN_STRIVER to 3)
        ),
        DiagnosticAnswer(
            text        = "Building an unbreakable system that guarantees success",
            emoji       = "⚙️",
            typeScores  = mapOf(UserType.SYSTEMS_PERSON to 5, UserType.PERFECTIONIST to 2)
        ),
        DiagnosticAnswer(
            text        = "Just surviving and finding peace without falling apart",
            emoji       = "🕊️",
            typeScores  = mapOf(UserType.BURNED_OUT to 5)
        )
    )

    // ── Question 2: Deepest Fear ──────────────────────────
    val q2Answers = listOf(
        DiagnosticAnswer(
            text        = "Looking back in 10 years and realizing I wasted my potential",
            emoji       = "⏳",
            typeScores  = mapOf(UserType.ACHIEVER to 4, UserType.PROCRASTINATOR to 4)
        ),
        DiagnosticAnswer(
            text        = "Doing it wrong or producing something mediocre",
            emoji       = "📏",
            typeScores  = mapOf(UserType.PERFECTIONIST to 5)
        ),
        DiagnosticAnswer(
            text        = "Losing control to distractions and algorithms",
            emoji       = "📱",
            typeScores  = mapOf(UserType.DOOMSCROLLER to 5, UserType.SYSTEMS_PERSON to 2)
        ),
        DiagnosticAnswer(
            text        = "Burning out completely and giving up",
            emoji       = "🔥",
            typeScores  = mapOf(UserType.BURNED_OUT to 5, UserType.BROKEN_STRIVER to 2)
        )
    )

    // ── Question 3: Legacy ────────────────────────────────
    val q3Answers = listOf(
        DiagnosticAnswer(
            text        = "As someone who outworked everyone else",
            emoji       = "💪",
            typeScores  = mapOf(UserType.ACHIEVER to 5)
        ),
        DiagnosticAnswer(
            text        = "As someone who stayed true to their core values",
            emoji       = "🧭",
            typeScores  = mapOf(UserType.IDENTITY_SEEKER to 5)
        ),
        DiagnosticAnswer(
            text        = "As someone who built things that lasted",
            emoji       = "🏗️",
            typeScores  = mapOf(UserType.SYSTEMS_PERSON to 5, UserType.PERFECTIONIST to 2)
        ),
        DiagnosticAnswer(
            text        = "I just want to be happy and at peace",
            emoji       = "🌅",
            typeScores  = mapOf(UserType.BURNED_OUT to 5)
        )
    )

    // ── Question 4: The Ideal Day ─────────────────────────
    val q4Answers = listOf(
        DiagnosticAnswer(
            text        = "A brutally hard day where I conquer every obstacle",
            emoji       = "⚔️",
            typeScores  = mapOf(UserType.ACHIEVER to 5)
        ),
        DiagnosticAnswer(
            text        = "A perfectly structured day with zero friction",
            emoji       = "🗓️",
            typeScores  = mapOf(UserType.SYSTEMS_PERSON to 5, UserType.PERFECTIONIST to 3)
        ),
        DiagnosticAnswer(
            text        = "A day where I just start the thing I've been avoiding",
            emoji       = "🚀",
            typeScores  = mapOf(UserType.PROCRASTINATOR to 5, UserType.BROKEN_STRIVER to 3)
        ),
        DiagnosticAnswer(
            text        = "A day completely unplugged from the digital world",
            emoji       = "🌲",
            typeScores  = mapOf(UserType.DOOMSCROLLER to 5, UserType.BURNED_OUT to 2)
        )
    )

    fun detectType(answers: List<DiagnosticAnswer>): UserProfile {
        val scores = mutableMapOf<UserType, Int>().withDefault { 0 }

        answers.forEach { answer ->
            answer.typeScores.forEach { (type, score) ->
                scores[type] = scores.getValue(type) + score
            }
        }

        val sortedScores = scores.entries.sortedByDescending { it.value }
        
        val primaryType = sortedScores.firstOrNull()?.key ?: UserType.ACHIEVER
        val secondaryType = if (sortedScores.size > 1 && sortedScores[1].value > 2) {
            sortedScores[1].key
        } else {
            null
        }

        // For the aspirational quiz, we assign a temporary mode. 
        // The BehavioralProfilerWorker will override this later.
        val assignedMode = when (primaryType) {
            UserType.ACHIEVER         -> AppMode.IRON
            UserType.BROKEN_STRIVER   -> AppMode.BUILD
            UserType.PERFECTIONIST    -> AppMode.EXPERIMENT
            UserType.BURNED_OUT       -> AppMode.RECOVERY
            UserType.IDENTITY_SEEKER  -> AppMode.BUILD
            UserType.SYSTEMS_PERSON   -> AppMode.EXPERIMENT
            UserType.DOOMSCROLLER     -> AppMode.IRON
            UserType.PROCRASTINATOR   -> AppMode.BUILD
        }

        return UserProfile(
            primaryType = primaryType, 
            secondaryType = secondaryType, 
            assignedMode = assignedMode,
            typeScores = scores
        )
    }
}
data class DiagnosticAnswer(
    val text: String,
    val emoji: String,
    val typeScores: Map<UserType, Int>
)

data class DiagnosticQuestion(
    val id: Int,
    val question: String,
    val subtext: String,
    val answers: List<DiagnosticAnswer>
)


