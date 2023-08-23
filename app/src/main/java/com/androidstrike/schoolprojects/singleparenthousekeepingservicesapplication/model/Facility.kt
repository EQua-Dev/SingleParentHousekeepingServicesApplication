package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Facility(
    val organisationName: String = "",
    val organisationEmail: String = "",
    val organisationPhysicalAddress: String = "",
    val organisationLongitude: String = "",
    val organisationLatitude: String = "",
    val organisationContactNumber: String = "",
    val organisationID: String = "",
    val dateJoined: String = "",
    val role: String = "facility",
): Parcelable
