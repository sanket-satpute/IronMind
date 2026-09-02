package com.sanket_satpute_20.ironmind.chat

import java.time.LocalDate

object DailyMessages {

    private val promptThemes = listOf(
        "Roll call: what did 5AM buy you today before the world woke up?",
        "Keep it sharp: one sentence on what held when the alarm hit.",
        "Quiet flex: what part of your routine is starting to feel automatic now?",
        "Pressure check: what almost broke today, and what kept you aligned?",
        "Identity check: what proof did this morning give you about who you are becoming?",
        "Weekend standard: how are you protecting the habit when structure gets loose?",
        "Room prompt: what would Day 1 you need to hear from the version of you here now?"
    )

    val messages = mapOf(
        1 to "Day 1. Most people are still asleep. You are not most people. Today you prove the alarm was not a mistake.",
        2 to "Day 2. Yesterday was not a fluke. Your body remembers. Do it again.",
        3 to "Day 3. Three days is when it stops feeling like a decision and starts feeling like a direction.",
        4 to "Day 4. 96 hours of early mornings. The version of you that sleeps in is getting weaker.",
        5 to "Day 5. One working week of 5AM starts. Most people will never know what this feels like.",
        6 to "Day 6. The weekend tests whether this is a habit or a performance. Show up anyway.",
        7 to "Day 7. One week. You have earned reader access to this room. Now earn the right to speak in it as a full member.",
        8 to "Day 8. The second week is where amateurs quit. They got their win at Day 7 and stopped.",
        9 to "Day 9. Your sleep is restructuring itself around this habit now. Trust the process.",
        10 to "Day 10. One third of the way. The compound interest of discipline is starting to show.",
        11 to "Day 11. Eleven consecutive victories over the version of you that makes excuses.",
        12 to "Day 12. If someone asked what kind of person wakes up at 5AM twelve days in a row — that is a description of you now.",
        13 to "Day 13. Unlucky for lazy people. Another ordinary morning for you.",
        14 to "Day 14. Two weeks. This is no longer a challenge. This is becoming your identity.",
        15 to "Day 15. Halfway. Most people who started with you are gone. You are still here.",
        16 to "Day 16. The people who quit at Day 15 told themselves they would restart Monday. You did not need Monday.",
        17 to "Day 17. Tell one person today what you are doing. Say it out loud. Own it.",
        18 to "Day 18. Your mornings are now more productive than most people's entire days.",
        19 to "Day 19. Nineteen days of choosing the hard thing first. Your standard has permanently risen.",
        20 to "Day 20. Two thirds done. The Iron Status is visible from here.",
        21 to "Day 21. Three weeks. Neuroscience says habits form around 21 days. You just wrote one into your brain.",
        22 to "Day 22. The alarm no longer feels like a punishment. That is the shift. That is the whole game.",
        23 to "Day 23. Seven days left. Do not calculate. Do not count down. Just show up tomorrow.",
        24 to "Day 24. The person who set this alarm 24 days ago was hoping you would get this far. You did not disappoint them.",
        25 to "Day 25. Five days. The finish line is close enough to see and far enough to trip you up. Do not trip.",
        26 to "Day 26. Four days. Think about who you were on Day 1. Think about the distance between then and now.",
        27 to "Day 27. Three days. Do not do anything different. The routine that got you here is the routine that finishes it.",
        28 to "Day 28. Two days. Most people have a story about almost finishing something. You will not be most people.",
        29 to "Day 29. Tomorrow is Day 30. Sleep early tonight. You have earned the best morning of the challenge.",
        30 to "Day 30. You did it. Thirty consecutive mornings. Iron Status is yours. Full member voice is yours. Welcome to the other side."
    )

    fun getMessageForDay(dayNumber: Int): String {
        return messages[dayNumber.coerceIn(1, 30)]
            ?: "Another morning. Another proof. Keep going."
    }

    fun getDailyPrompt(dayNumber: Int): String {
        return promptThemes[(dayNumber - 1).mod(promptThemes.size)]
    }

    fun getReaderUnlockMessage(dayNumber: Int): String {
        return "Reader access unlocked at Day $dayNumber. You earned entry into the room. Read the standard, absorb the tone, and keep climbing toward full member voice."
    }

    fun getFullUnlockMessage(dayNumber: Int): String {
        return "Full member access unlocked at Day $dayNumber. The room is no longer read-only for you. Speak with the weight of a finished run."
    }
}
