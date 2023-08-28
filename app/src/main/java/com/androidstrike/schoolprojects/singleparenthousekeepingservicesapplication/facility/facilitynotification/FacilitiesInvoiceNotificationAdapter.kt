package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.facility.facilitynotification

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R

class FacilitiesInvoiceNotificationAdapter(itemView: View): RecyclerView.ViewHolder(itemView) {

    var dateCreated: TextView
    var timeCreated: TextView
    var serviceName: TextView
    var viewRequestButton: Button

    init {
        dateCreated = itemView.findViewById(R.id.txt_facility_invoice_date_created)
        timeCreated = itemView.findViewById(R.id.txt_facility_invoice_time_created)
        serviceName = itemView.findViewById(R.id.txt_facility_invoice_service_type)
        viewRequestButton = itemView.findViewById(R.id.btn_view_customer_request)
    }
}