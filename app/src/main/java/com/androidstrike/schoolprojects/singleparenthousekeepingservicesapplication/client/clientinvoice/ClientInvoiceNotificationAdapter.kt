package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientinvoice

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R

class ClientInvoiceNotificationAdapter(itemView: View): RecyclerView.ViewHolder(itemView) {

    var dateCreated: TextView
    var timeCreated: TextView
    var facilityName: TextView
    var serviceName: TextView
    var btnViewNotificationDetails: Button


    init {
        dateCreated = itemView.findViewById(R.id.txt_client_invoice_date_generated)
        timeCreated = itemView.findViewById(R.id.txt_client_invoice_time_generated)
        facilityName = itemView.findViewById(R.id.txt_client_invoice_facility_name)
        serviceName = itemView.findViewById(R.id.txt_client_invoice_service_type)
        btnViewNotificationDetails = itemView.findViewById(R.id.btn_client_view_invoice_generated)


    }
}