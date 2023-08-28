package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model

data class AcceptedRequestInvoice(
    val invoiceID: String = "",
    val notificationID: String = "",
    val customerRequestFormID: String = "",
    val customerID: String = "",
    val organisationID: String= "",
    val organisationProfileServiceID: String = "",
    val typeOfServiceID: String = "",
    val categoryOfServiceID: String = "",
    val invoiceAmount: String = "",
    val bankName: String = "",
    val bankAccountIBAN: String = "",
    val bankAccountHolderName: String = "",
    val invoiceText: String = "",
    val dateCreated: String = "",
    val timeCreated: String = "",

    )
