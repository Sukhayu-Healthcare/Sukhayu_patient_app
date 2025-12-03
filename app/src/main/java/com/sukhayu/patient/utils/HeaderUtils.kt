package com.sukhayu.patient.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.sukhayu.patient.R

object HeaderUtils {
    
    fun setupRoleInHeader(activity: Activity) {
        val roleContainer = activity.findViewById<LinearLayout>(R.id.headerRoleContainer)
        val roleText = activity.findViewById<TextView>(R.id.headerRole)
        val divider = activity.findViewById<View>(R.id.headerDivider)
        
        if (roleContainer != null && roleText != null) {
            val sharedPreferences = activity.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val roleDisplay = sharedPreferences.getString("role_display", null)
            
            if (!roleDisplay.isNullOrEmpty()) {
                // Format role text without emoji prefix
                roleText.text = roleDisplay.uppercase()
                roleContainer.visibility = View.VISIBLE
                divider?.visibility = View.VISIBLE
            } else {
                roleContainer.visibility = View.GONE
                divider?.visibility = View.GONE
            }
        }
    }
}
