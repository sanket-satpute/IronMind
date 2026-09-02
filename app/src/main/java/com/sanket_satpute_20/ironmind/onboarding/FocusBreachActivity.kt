package com.sanket_satpute_20.ironmind.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme

class FocusBreachActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowFlags()

        val blockedItem = intent.getStringExtra("BLOCKED_ITEM") ?: "Distraction"
        val breachMode = intent.getStringExtra(EXTRA_BREACH_MODE).orEmpty()
        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE).orEmpty()
        val prefs = PrefManager.getInstance(this)
        val blockedAppName = if (blockedPackage.isNotBlank()) {
            runCatching {
                val info = packageManager.getApplicationInfo(blockedPackage, 0)
                packageManager.getApplicationLabel(info).toString()
            }.getOrDefault(blockedPackage.substringAfterLast('.'))
        } else {
            blockedItem
        }

        // The "No Escape" Back Button Trap
        onBackPressedDispatcher.addCallback(this) {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
            finish()
        }

        setContent {
            IronMindTheme {
                if (breachMode == MODE_EARNED_UNLOCK) {
                    EarnedUnlockBreachOverlay(
                        blockedPackage = blockedPackage,
                        blockedAppName = blockedAppName,
                        remainingCount = prefs.earnedUnlockRemainingCount,
                        completedCount = prefs.earnedUnlockCompletedCount,
                        totalCount = prefs.earnedUnlockTotalCount,
                        onReturn = {
                            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(homeIntent)
                            finish()
                        }
                    )
                } else {
                    FocusBreachOverlay(
                        blockedItem = blockedItem,
                        onReturn = {
                            // Also go home on the explicit return button
                            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(homeIntent)
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun setupWindowFlags() {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    }

    companion object {
        const val EXTRA_BREACH_MODE = "BREACH_MODE"
        const val EXTRA_BLOCKED_PACKAGE = "BLOCKED_PACKAGE"
        const val MODE_EARNED_UNLOCK = "EARNED_UNLOCK"

        fun createEarnedUnlockIntent(context: android.content.Context, blockedPackage: String): Intent {
            return Intent(context, FocusBreachActivity::class.java).apply {
                putExtra(EXTRA_BREACH_MODE, MODE_EARNED_UNLOCK)
                putExtra(EXTRA_BLOCKED_PACKAGE, blockedPackage)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            }
        }
    }
}
