package com.sanket_satpute_20.ironmind.apps

enum class AppCategory(
    val wireValue: String,
    val displayName: String,
    val missionTreatment: String
) {
    VOID(
        wireValue = "VOID",
        displayName = "Void",
        missionTreatment = "Always block during missions"
    ),
    SIGNAL(
        wireValue = "SIGNAL",
        displayName = "Signal",
        missionTreatment = "Block communication during missions"
    ),
    TOOL(
        wireValue = "TOOL",
        displayName = "Tool",
        missionTreatment = "Always allow"
    ),
    CONTEXT(
        wireValue = "CONTEXT",
        displayName = "Context",
        missionTreatment = "Ask per mission"
    );

    companion object {
        fun fromWireValue(value: String): AppCategory {
            return entries.firstOrNull { it.wireValue == value } ?: CONTEXT
        }
    }
}

enum class QuickProtectMode(
    val label: String,
    val subtitle: String
) {
    BLOCKED(
        label = "Blocked",
        subtitle = "Block during missions"
    ),
    ASK(
        label = "Ask",
        subtitle = "Ask each mission"
    ),
    ALLOWED(
        label = "Allowed",
        subtitle = "Always allow"
    );

    companion object {
        fun fromCategory(category: AppCategory): QuickProtectMode {
            return when (category) {
                AppCategory.VOID, AppCategory.SIGNAL -> BLOCKED
                AppCategory.CONTEXT -> ASK
                AppCategory.TOOL -> ALLOWED
            }
        }
    }
}

enum class AppClassificationSource(val wireValue: String) {
    SYSTEM_DEFAULT("SYSTEM_DEFAULT"),
    USER_OVERRIDE("USER_OVERRIDE"),
    USER_CONFIRMED("USER_CONFIRMED"),
    UNKNOWN_PENDING("UNKNOWN_PENDING");

    companion object {
        fun fromWireValue(value: String): AppClassificationSource {
            return entries.firstOrNull { it.wireValue == value } ?: UNKNOWN_PENDING
        }
    }
}

enum class AppClassificationConfidence(val wireValue: String) {
    HIGH("HIGH"),
    MEDIUM("MEDIUM"),
    LOW("LOW");

    companion object {
        fun fromWireValue(value: String): AppClassificationConfidence {
            return entries.firstOrNull { it.wireValue == value } ?: LOW
        }
    }
}
