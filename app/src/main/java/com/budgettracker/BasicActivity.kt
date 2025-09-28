package com.budgettracker

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.widget.LinearLayout
import android.graphics.Color
import android.view.Gravity

/**
 * Basic activity without Compose to test if the issue is Compose-related
 */
class BasicActivity : Activity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Create a simple LinearLayout programmatically
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setBackgroundColor(Color.parseColor("#667eea"))
                setPadding(50, 50, 50, 50)
            }
            
            // Add title text
            val titleText = TextView(this).apply {
                text = "Budget Tracker"
                textSize = 32f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            }
            layout.addView(titleText)
            
            // Add status text
            val statusText = TextView(this).apply {
                text = "App is working! 🎉"
                textSize = 18f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            }
            layout.addView(statusText)
            
            setContentView(layout)
            
        } catch (e: Exception) {
            // If anything fails, just show a simple text
            val simpleText = TextView(this).apply {
                text = "Budget Tracker - Basic Mode"
                textSize = 24f
                gravity = Gravity.CENTER
            }
            setContentView(simpleText)
        }
    }
}


