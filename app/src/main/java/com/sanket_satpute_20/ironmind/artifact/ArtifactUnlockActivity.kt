package com.sanket_satpute_20.ironmind.artifact

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme

class ArtifactUnlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val artifactId = intent.getStringExtra(EXTRA_ARTIFACT_ID)
        if (artifactId.isNullOrBlank() || ArtifactRepository.getById(artifactId) == null) {
            finish()
            return
        }
        setContent {
            IronMindTheme {
                ArtifactUnlockOverlay(artifactId = artifactId, onDismiss = ::finish)
            }
        }
    }

    companion object {
        private const val EXTRA_ARTIFACT_ID = "ARTIFACT_ID"

        fun createIntent(context: Context, artifactId: String): Intent =
            Intent(context, ArtifactUnlockActivity::class.java).apply {
                putExtra(EXTRA_ARTIFACT_ID, artifactId)
            }
    }
}
