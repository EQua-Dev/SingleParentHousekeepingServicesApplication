package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookService(
    val requestFormId: String = "",
    val customerID: String = "",
    val organisationID: String = "",
    val organisationProfileServiceID: String = "",
    val typeOfServiceID: String = "",
    val categoryOfServiceID: String = "",
    val requestedServiceFrequency: String = "",
    val requestDeliveryOfGoodsOptions: String = "",
    var requestedStartDate: String = "",
    var requestFormText: String = "",
    var dateCreated: String = "",
    var timeCreated: String = "",
    var requestStatus: String = "",
    var deliveryStreet: String = "",
    var deliveryCity: String = "",
    var deliveryEirCode: String = "",
    var clientAddress: String = "",
    val isRated: Boolean = false


): Parcelable
