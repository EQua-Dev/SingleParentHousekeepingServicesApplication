/*
 * Copyright (c) 2023.
 * Richard Uzor
 * For Afro Connect Technologies
 * Under the Authority of Devstrike Digital Ltd.
 *
 */

package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientdigitalwallet.DigitalWallet
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientinvoice.InvoicePayment
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientnotification.ClientNotification
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientfeedback.FeedbackRating
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientnotification.ClientNotificationBase
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.landing.MapsFragment

/**
 * Created by Richard Uzor  on 28/01/2023
 */
class ClientLandingPagerAdapter (//var context: FragmentActivity?,
                                 fm: FragmentManager,
                                 //var totalTabs: Int
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return 5
    }

    //when each tab is selected, define the fragment to be implemented
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                MapsFragment()
            }
            1 -> {
                DigitalWallet()
            }
            2 -> {
                ClientNotificationBase()
            }
            3 -> {
                InvoicePayment()
            }
            4 ->{
                FeedbackRating()
            }
            else -> getItem(position)
        }
    }
}