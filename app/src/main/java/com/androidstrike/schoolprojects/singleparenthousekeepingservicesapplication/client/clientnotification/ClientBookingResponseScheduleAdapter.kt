package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientnotification

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R

class ClientBookingResponseScheduleAdapter(itemView: View): RecyclerView.ViewHolder(itemView) {

    var facilityName: TextView
    var serviceName: TextView
    var clientBookingResponseViewDetailsButton: Button

    init {
        facilityName = itemView.findViewById(R.id.txt_client_booking_result_schedule_service_name)
        serviceName = itemView.findViewById(R.id.txt_client_booking_result_schedule_facility_name)
        clientBookingResponseViewDetailsButton = itemView.findViewById(R.id.client_notification_schedule_view_details_btn)


    }
}