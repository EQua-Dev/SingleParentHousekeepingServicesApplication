package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientnotification

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.BookService
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Client
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Facility
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.FacilityRequestResponse
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Service
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.ServiceCategory
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.ServiceType
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.ACCEPTED_TEXT
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.REJECTED_TEXT
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.requestResponseNotificationCollectionRef
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.getDate
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.hideProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.showProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ClientNotificationDetailBottomSheet : BottomSheetDialogFragment() {

    private lateinit var notificationDetail: FacilityRequestResponse

    private val TAG =  "ClientNotificationDetailBottomSheet"

    private var progressDialog: Dialog? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.client_notification_detail_dialog_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        notificationDetail = arguments?.getParcelable<FacilityRequestResponse>(ARG_CLIENT_NOTIFICATION_DATA)!!


        val tvCompanyName = requireView().findViewById<TextView>(R.id.txt_client_booking_response_detail_company_name)
        val tvCompanyPhysicalAddress = requireView().findViewById<TextView>(R.id.txt_client_booking_response_detail_company_physical_address)
        val tvCompanyEmail = requireView().findViewById<TextView>(R.id.txt_client_booking_response_detail_company_email)
        val tvCompanyContactNumber = requireView().findViewById<TextView>(R.id.txt_client_booking_response_detail_company_contact_number)
        val tvNotificationText = requireView().findViewById<TextView>(R.id.txt_client_booking_result_detail_notification_text)
//        val tvDateCreated = requireView().findViewById<TextView>(R.id.txt_client_booking_response_detail_date_created)
//        val tvTimeCreated = requireView().findViewById<TextView>(R.id.txt_client_booking_response_detail_time_created)

        val btnOkay = requireView().findViewById<Button>(R.id.btn_client_booking_response_detail_okay)

        val organisation = getOrganisation(notificationDetail.organisationID)!!

        tvCompanyName.text = resources.getString(R.string.facility_generate_request_invoice_company_name, organisation.organisationName)
        tvCompanyPhysicalAddress.text = resources.getString(R.string.facility_generate_request_invoice_company_physical_address, organisation.organisationPhysicalAddress)
        tvCompanyEmail.text = resources.getString(R.string.facility_generate_request_invoice_company_email, organisation.organisationEmail)
        tvCompanyContactNumber.text = resources.getString(R.string.facility_generate_request_invoice_company_contact_number, organisation.organisationContactNumber)
        tvNotificationText.text = notificationDetail.notificationText
//        tvDateCreated.text = resources.getString(R.string.txt_client_booking_response_detail_date_created, notificationDetail.dateCreated)
//        tvTimeCreated.text = resources.getString(R.string.txt_client_booking_response_detail_time_created, notificationDetail.timeCreated)

//        tvDateCreated.text = getDate(model.dateResponded.toLong(), "dd MMMM, yyyy")
//        tvDateTimeCreated.text = getDate(model.dateResponded.toLong(), "hh:mm a")
//        tvDetailText.text = "Dear Customer,\n\nThis is to confirm that your request has been accepted for service: ${scheduledService.specificServiceName}\n\nPlease come for the initial meeting at:\n\nDate: ${model.scheduledDate}\nTime: ${model.scheduledTime}\n\nWe will be happy to see you there"


        btnOkay.setOnClickListener {
            dismiss()
        }


    }

    private fun getOrganisation(organisationId: String): Facility? {
        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.facilityCollectionRef.document(organisationId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(Facility::class.java)
                } else {
                    return@async null
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    requireContext().toast(e.message.toString())
                }
                return@async null
            }
        }

        val organisation = runBlocking { deferred.await() }
        hideProgress()
        Log.d(TAG, "getOrganisation: $organisation")

        return organisation
    }




    private fun getUser(userId: String): Client? {

        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.clientCollectionRef.document(userId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(Client::class.java)
                } else {
                    return@async null
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    requireContext().toast(e.message.toString())
                }
                return@async null
            }
        }

        val client = runBlocking { deferred.await() }
        hideProgress()

        return client
    }

    private fun getService(serviceId: String): Service? {

        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.servicesCollectionRef.document(serviceId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(Service::class.java)
                } else {
                    return@async null
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    requireContext().toast(e.message.toString())
                }
                return@async null
            }
        }

        val service = runBlocking { deferred.await() }
        hideProgress()

        return service
    }
    private fun getServiceType(serviceTypeId: String): ServiceType? {

        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.serviceTypeCollectionRef.document(serviceTypeId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(ServiceType::class.java)
                } else {
                    return@async null
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    requireContext().toast(e.message.toString())
                }
                return@async null
            }
        }

        val serviceType = runBlocking { deferred.await() }
        hideProgress()

        return serviceType
    }
    private fun getServiceCategory(serviceCategoryId: String): ServiceCategory? {

        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.serviceCategoryCollectionRef.document(serviceCategoryId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(ServiceCategory::class.java)
                } else {
                    return@async null
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    requireContext().toast(e.message.toString())
                }
                return@async null
            }
        }

        val serviceCategory = runBlocking { deferred.await() }
        hideProgress()

        return serviceCategory
    }

    companion object {
        private const val ARG_CLIENT_NOTIFICATION_DATA = "arg_client_notification_data"

        fun newInstance(notificationDetail: FacilityRequestResponse): ClientNotificationDetailBottomSheet {
            val fragment = ClientNotificationDetailBottomSheet()
            val args = Bundle().apply {
                putParcelable(ARG_CLIENT_NOTIFICATION_DATA, notificationDetail)

            }
            fragment.arguments = args
            return fragment
        }
    }
}