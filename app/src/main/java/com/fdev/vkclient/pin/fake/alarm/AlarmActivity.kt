package com.fdev.vkclient.pin.fake.alarm

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fdev.vkclient.R
import com.fdev.vkclient.activities.BaseActivity
import com.fdev.vkclient.lg.Lg
import com.fdev.vkclient.utils.goHome
import com.fdev.vkclient.utils.setBottomInsetMargin
import com.fdev.vkclient.utils.setBottomInsetPadding
import com.fdev.vkclient.utils.setTopInsetPadding
import kotlinx.android.synthetic.main.activity_alarms.*
import kotlinx.android.synthetic.main.toolbar.*

class AlarmActivity : BaseActivity() {

    private val alarms = createDefaultAlarms()

    private val adapter by lazy {
        AlarmAdapter(this) { finish() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarms)
        rvAlarms.layoutManager = LinearLayoutManager(this)
        rvAlarms.adapter = adapter
        adapter.update(createDefaultAlarms())
        rvAlarms.addOnScrollListener(FabVisibilityWatcher())

        fabAdd.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                alarms.add(Alarm(hour * 60 + minute, true))
                alarms.sortBy { it.time }
                adapter.update(alarms)
            }, 9, 17, true).show()
        }

        toolbar.setTopInsetPadding(resources.getDimensionPixelSize(R.dimen.toolbar_height))
        rvAlarms.setBottomInsetPadding()
        fabAdd.setBottomInsetMargin(resources.getDimensionPixelSize(R.dimen.accounts_fab_add_margin))
    }

    override fun onBackPressed() {
        try {
            goHome(this)
        } catch (e: Exception) {
            Lg.wtf("[alarm] unable to go home: ${e.message}")
        }
    }

    private fun createDefaultAlarms() = arrayListOf(
            Alarm(450, false, enabled = false),
            Alarm(480, true, enabled = false),
            Alarm(490, true, enabled = false),
            Alarm(500, false, enabled = true),
            Alarm(510, false, enabled = true),
            Alarm(525, true, enabled = true),
            Alarm(540, false, enabled = true),
            Alarm(600, false, enabled = false),
            Alarm(660, false, enabled = false),
            Alarm(800, true, enabled = false)
    )

    companion object {

        fun launch(context: Context?) {
            context?.startActivity(Intent(context, AlarmActivity::class.java))
        }
    }

    private inner class FabVisibilityWatcher : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy < 0) {
                fabAdd.show()
            } else {
                fabAdd.hide()
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }
    }
}