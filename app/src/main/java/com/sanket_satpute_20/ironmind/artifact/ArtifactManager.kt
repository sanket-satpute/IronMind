package com.sanket_satpute_20.ironmind.artifact

import com.sanket_satpute_20.ironmind.data.PrefManager

/**
 * Persists and retrieves unlocked artifacts.
 */
class ArtifactManager(private val prefs: PrefManager) {

    fun isUnlocked(artifactId: String): Boolean {
        return prefs.unlockedArtifactIds.contains(artifactId)
    }

    fun unlockArtifact(artifactId: String): Boolean {
        if (isUnlocked(artifactId)) return false
        
        val current = prefs.unlockedArtifactIds.toMutableSet()
        current.add(artifactId)
        prefs.unlockedArtifactIds = current
        return true
    }

    fun getUnlockedArtifacts(): List<ArtifactDefinition> {
        return ArtifactRepository.ALL_ARTIFACTS.filter { isUnlocked(it.id) }
    }
}
