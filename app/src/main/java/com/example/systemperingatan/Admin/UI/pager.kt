package com.example.systemperingatan.Admin.UI

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.systemperingatan.Admin.UI.Fragment.AreaFragment
import com.example.systemperingatan.Admin.UI.Fragment.ZonaEvakuasiFragment

class pager(fm: FragmentManager, private val context: Context) : FragmentPagerAdapter(fm) {

    private val pages = listOf(
            AreaFragment(),
            ZonaEvakuasiFragment()
    )

    override fun getItem(position: Int): Fragment {
        return pages[position]
    }

    override fun getCount(): Int {
        return pages.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0    -> "Area"
            else -> "Zona Evakuasi"
        }
    }
}