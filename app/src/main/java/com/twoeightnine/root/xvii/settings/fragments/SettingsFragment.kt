package com.twoeightnine.root.xvii.settings.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.ListView
import butterknife.BindView
import butterknife.ButterKnife
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.PinActivity
import com.twoeightnine.root.xvii.fragments.BaseFragment
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Account
import com.twoeightnine.root.xvii.settings.Item
import com.twoeightnine.root.xvii.settings.adapters.SettingsAdapter
import com.twoeightnine.root.xvii.utils.CacheHelper
import com.twoeightnine.root.xvii.utils.restartApp
import io.realm.Realm

class SettingsFragment: BaseFragment() {

    @BindView(R.id.lvPrefs)
    lateinit var lvPrefs: ListView

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private lateinit var adapter: SettingsAdapter

    override fun bindViews(view: View) {
        ButterKnife.bind(this, view)
        initAdapter()
    }

    override fun getLayout() = R.layout.fragment_settings

    private fun initAdapter() {
        adapter = SettingsAdapter(activity)
        adapter.add(Item(getString(R.string.general), GeneralFragment()))
        adapter.add(Item(getString(R.string.notifications), NotificationsFragment()))
        adapter.add(Item(getString(R.string.appearance), AppearanceFragment()))
        adapter.add(Item(getString(R.string.accounts), AccountsFragment()))
        adapter.add(Item(getString(R.string.pin_settings), { pin() }))
        adapter.add(Item(getString(R.string.about), AboutFragment()))
        adapter.add(Item(getString(R.string.logout), { showLogoutDialog() }))

        lvPrefs.adapter = adapter
        lvPrefs.setOnItemClickListener { _, _, i, _ ->
            val obj = adapter.getItem(i).obj
            if (obj is Fragment) {
                rootActivity.loadFragment(obj)
            } else  {
                (obj as () -> Unit).invoke()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.settings))
    }

    private fun showLogoutDialog() {
        val dialog = AlertDialog.Builder(activity)
                .setMessage(R.string.wanna_logout)
                .setPositiveButton(android.R.string.ok, { _, _ -> logout() })
                .setNegativeButton(android.R.string.cancel, null)
                .create()

        dialog.show()
        Style.forDialog(dialog)
    }

    private fun logout() {
        Session.token = ""
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.where(Account::class.java)
                .equalTo(Account.TOKEN, Session.token)
                .findAll()
                .deleteAllFromRealm()
        realm.commitTransaction()
        CacheHelper.deleteAllMessagesAsync()
        restartApp(getString(R.string.restart_app))
    }

    private fun pin() {
        val pin = Prefs.pin
        if (TextUtils.isEmpty(pin)) {
            PinActivity.launch(context, PinActivity.ACTION_SET)
        } else {
            val dialog = AlertDialog.Builder(activity)
                    .setMessage(R.string.have_pin)
                    .setPositiveButton(R.string.edit) { _, _ ->
                        PinActivity.launch(context, PinActivity.ACTION_EDIT)
                    }
                    .setNegativeButton(R.string.reset) { _, _ ->
                        PinActivity.launch(context, PinActivity.ACTION_RESET)
                    }
                    .create()

            dialog.show()
            Style.forDialog(dialog)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }
}