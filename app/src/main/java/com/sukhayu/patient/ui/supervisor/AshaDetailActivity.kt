package com.sukhayu.patient.ui.supervisor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R

/**
 * TEMPORARY simplified AshaDetail screen.
 *
 * The original file depended on supervisor survey/delete APIs and models
 * that broke after the merge. This minimal version just loads the layout
 * so the app can compile and other features (patient, general survey, TB)
 * continue to work.
 *
 * Later, you can restore the full implementation on a new branch.
 */
class AshaDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ⚠️ If your old layout had a different name, change this line only.
        setContentView(R.layout.activity_asha_detail)

        supportActionBar?.apply {
            title = "ASHA Details"
            setDisplayHomeAsUpEnabled(true)
        }

        // You can still read intent extras here if needed, e.g. asha id/name,
        // but we keep it empty for now to avoid any extra dependencies.
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
