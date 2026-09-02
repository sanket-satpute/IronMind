package com.sanket_satpute_20.ironmind.crucible

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sanket_satpute_20.ironmind.MainActivity
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme

class CrucibleFailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val breachItem = intent.getStringExtra("BREACH_ITEM") ?: "Distraction"
        val prefs = PrefManager.getInstance(this)
        val crucibleTitle = intent.getStringExtra("CRUCIBLE_TITLE")
            ?: prefs.crucibleTaskName
            .ifEmpty { breachItem }
        
        setContent {
            IronMindTheme {
                CrucibleFailedScreen(
                    crucibleTitle = crucibleTitle,
                    onContinue = {
                        val mainIntent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        startActivity(mainIntent)
                        finish()
                    }
                )
            }
        }
    }
}
