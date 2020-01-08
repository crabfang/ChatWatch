package com.cabe.app.watch.widget

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import com.cabe.app.watch.R

class WaitingDialog(context: Context): AlertDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_waiting_layout)
    }
}