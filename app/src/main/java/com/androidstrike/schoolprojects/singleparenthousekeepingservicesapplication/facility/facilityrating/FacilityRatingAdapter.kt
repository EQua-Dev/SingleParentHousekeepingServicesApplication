package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.facility.facilityrating

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.iarcuschin.simpleratingbar.SimpleRatingBar

class FacilityRatingAdapter(itemView: View): RecyclerView.ViewHolder(itemView) {

    var dateCreated: TextView
    var timeCreated: TextView
    var clientName: TextView
    var serviceName: TextView
    var serviceRating: SimpleRatingBar
    var serviceRatingText: TextView
    //var rateServiceButton: Button


    init {
        dateCreated = itemView.findViewById(R.id.txt_facility_rated_date)
        timeCreated = itemView.findViewById(R.id.txt_facility_rated_time)
        clientName = itemView.findViewById(R.id.txt_facility_rated_client_name)
        serviceName = itemView.findViewById(R.id.txt_facility_rated_service_name)
        serviceRating = itemView.findViewById(R.id.txt_client_service_rating)
        serviceRatingText = itemView.findViewById(R.id.txt_facility_rated_text)
        //rateServiceButton = itemView.findViewById(R.id.btn_client_rate_invoice_generated)










    }
}