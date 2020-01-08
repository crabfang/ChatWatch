package com.cabe.app.watch.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

abstract class BaseActivity: AppCompatActivity() {
    var canBack = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(canBack) {
            supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR)
            supportActionBar?.apply {
                setDisplayShowTitleEnabled(true)
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(false)
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}