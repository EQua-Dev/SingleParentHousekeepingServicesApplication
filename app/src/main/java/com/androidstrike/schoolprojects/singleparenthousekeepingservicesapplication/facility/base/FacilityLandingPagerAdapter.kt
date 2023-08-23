/*
 * Copyright (c) 2023.
 * Richard Uzor
 * For Afro Connect Technologies
 * Under the Authority of Devstrike Digital Ltd.
 *
 */

package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.facility.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.facility.facilitynotification.FacilityNotification
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.facility.facilityprofile.FacilityProfile
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.facility.facilityservice.FacilityAddService
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.landing.FeedbackRating

/**
 * Created by Richard Uzor  on 28/01/2023
 */
class FacilityLandingPagerAdapter (var context: FragmentActivity?,
                                   fm: FragmentManager,
                                   //var totalTabs: Int
) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return 4
    }

    //when each tab is selected, define the fragment to be implemented
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                FacilityProfile()
            }
            1 -> {
                FacilityAddService()
            }
            2 -> {
                FacilityNotification()
            }
            3 -> {
                FeedbackRating()
            }
            else -> getItem(position)
        }
    }
}