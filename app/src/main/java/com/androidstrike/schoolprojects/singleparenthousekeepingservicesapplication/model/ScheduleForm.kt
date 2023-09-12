package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model

data class ScheduleForm(
    val notificationOfAcceptedCustomerRequestScheduleMeetingID: String = "",
    val notificationOfCustomerRequestFormID: String = "",
    val requestFormID: String = "",
    val customerID: String = "",
    val organisationID: String = "",
    val organisationProfileServiceID: String = "",
    val serviceType: String = "",
    val requestFormStatus: String = "",
    val scheduledMeetingDate: String = "",
    val scheduledMeetingTime: String = "",
    val notificationText: String = ""



)
