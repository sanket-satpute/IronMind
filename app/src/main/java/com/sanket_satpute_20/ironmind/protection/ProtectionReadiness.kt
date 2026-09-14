package com.sanket_satpute_20.ironmind.protection


sealed interface ProtectionReadiness {
    data class Ready(val capabilities: Map<ProtectionCapability, CapabilityStatus>) : ProtectionReadiness
    data class NotReady(
        val missingRequired: Set<ProtectionCapability>,
        val capabilities: Map<ProtectionCapability, CapabilityStatus>
    ) : ProtectionReadiness
}

data class CapabilityStatus(
    val required: Boolean,
    val enabled: Boolean,
    val reason: String
)
