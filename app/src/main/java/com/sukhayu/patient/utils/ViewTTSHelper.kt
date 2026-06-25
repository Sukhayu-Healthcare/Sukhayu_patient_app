package com.sukhayu.patient.utils

import android.view.View
import android.view.ViewGroup
import android.widget.TextView

object ViewTtsHelper {

    fun attachToAllTextViews(
        root: View,
        ttsHelper: TtsHelper
    ) {

        when (root) {

            is TextView -> {

                root.setOnLongClickListener {

                    val text = root.text.toString().trim()

                    if (text.isNotEmpty()) {
                        ttsHelper.speak(text)
                    }

                    true
                }
            }

            is ViewGroup -> {

                for (i in 0 until root.childCount) {
                    attachToAllTextViews(
                        root.getChildAt(i),
                        ttsHelper
                    )
                }
            }
        }
    }
}