package com.example.perilif

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity


class Helper {
    companion object{
        fun  changeProgressBarColor(bar: ProgressBar, context: AppCompatActivity) {
            var color = R.color.teal_200
            if (bar.progress > 60) {
                color = android.R.color.holo_orange_dark
            }
            if (bar.progress > 90) {
                color = R.color.colorAccent
            }

            val progressDrawable: Drawable = bar.progressDrawable.mutate()
            progressDrawable.setColorFilter(context.resources.getColor(color), PorterDuff.Mode.SRC_IN)
            bar.progressDrawable = progressDrawable
        }
    }
}