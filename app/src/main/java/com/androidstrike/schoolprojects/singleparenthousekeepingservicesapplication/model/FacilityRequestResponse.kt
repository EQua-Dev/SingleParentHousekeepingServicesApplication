package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FacilityRequestResponse(
    val notificationID: String = "",
    val requestFormID: String = "",
    val organisationID: String = "",
    val organisationProfileServiceID: String = "",
    val typeOfServiceID: String = "",
    val categoryOfServiceID: String = "",
    val customerID: String = "",
    val requestFormStatus: String = "",
    val notificationText: String = "",
    val dateCreated: String = "",
    val timeCreated: String = "",
    ): Parcelable
