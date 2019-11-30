package com.example.systemperingatan.Admin.UI

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.systemperingatan.Admin.UI.Fragment.LihatDataAmanFragment
import com.example.systemperingatan.Admin.UI.Fragment.LihatDataEnterFragment
import com.example.systemperingatan.Admin.UI.Fragment.LihatDataExitFragment

class pagerDataUser(fragmentManager: FragmentManager, private val context: Context) : FragmentPagerAdapter(fragmentManager) {

    private val page = listOf(LihatDataEnterFragment(),
            LihatDataExitFragment(),LihatDataAmanFragment())

    override fun getItem(position: Int): Fragment {
        return page[position]
    }

    override fun getCount(): Int {
        return page.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0 -> "Masuk"
            1 -> "Keluar"
            else -> "Aman"

        }
    }
}