package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.facility.facilitynotification

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.BookService
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Client
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
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.visible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class FacilityCustomerRequestDetailBottomSheet : BottomSheetDialogFragment() {

    private lateinit var requestedService: BookService

    private val TAG =  "FacilityCustomerRequestDetailBottomSheet"

    private var progressDialog: Dialog? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.facility_custom_view_request_detail_dialog_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requestedService = arguments?.getParcelable<BookService>(ARG_REQUESTED_SERVICE_DATA)!!

        val tvClientName =
            requireView().findViewById<TextView>(R.id.facility_request_customer_name)
        val tvClientContactName =
            requireView().findViewById<TextView>(R.id.facility_request_customer_contact_number)
        val tvClientEmail =
            requireView().findViewById<TextView>(R.id.facility_request_customer_email)
        val tvChosenServiceName =
            requireView().findViewById<TextView>(R.id.facility_request_chosen_service_name)
        val tvChosenServiceType =
            requireView().findViewById<TextView>(R.id.facility_request_chosen_service_type)
        val tvRequestStartDate =
            requireView().findViewById<TextView>(R.id.facility_request_start_date)
        val tvRequestServicePrice =
            requireView().findViewById<TextView>(R.id.facility_request_service_price)
        val tvRequestServiceDiscountedPrice =
            requireView().findViewById<TextView>(R.id.facility_request_service_discounted_price)
        val tvRequestText =
            requireView().findViewById<TextView>(R.id.facility_request_text)
        val llDeliveryAddress =
            requireView().findViewById<LinearLayout>(R.id.layout_delivery_address)
        val tvDeliveryStreet =
            requireView().findViewById<TextView>(R.id.request_delivery_address_street)
        val tvDeliveryCity =
            requireView().findViewById<TextView>(R.id.request_delivery_address_city)
        val tvDeliveryEirCode =
            requireView().findViewById<TextView>(R.id.request_delivery_address_eir_code)
//        val tvRequestedDateCreated =
//            requireView().findViewById<TextView>(R.id.facility_request_date_created)
//        val tvRequestedTimeCreated =
//            requireView().findViewById<TextView>(R.id.facility_request_time_created)
        val btnAcceptRequest =
            requireView().findViewById<Button>(R.id.btn_facility_accept_customer_request)
        val btnRejectRequest =
            requireView().findViewById<TextView>(R.id.btn_facility_reject_customer_request)

        val client = getUser(requestedService.customerID)!!
        val service = getService(requestedService.organisationProfileServiceID)!!
        val serviceCategory = getServiceCategory(requestedService.categoryOfServiceID)!!

        if (serviceCategory.serviceCategoryName == "Delivery"){
            llDeliveryAddress.visible(true)
            tvDeliveryStreet.text = resources.getString(R.string.request_delivery_address_street, requestedService.deliveryStreet)
            tvDeliveryCity.text = resources.getString(R.string.request_delivery_address_city, requestedService.deliveryCity)
            tvDeliveryEirCode.text = resources.getString(R.string.request_delivery_address_eir_code, requestedService.deliveryEirCode)
        }else{
            llDeliveryAddress.visible(false)
        }

        tvClientName.text = resources.getString(
            R.string.requesting_client_name,
            client.customerFirstName,
            client.customerLastName
        )
        tvClientContactName.text =
            resources.getString(
                R.string.requesting_client_contact_number,
                client.customerMobileNumber
            )
        tvClientEmail.text =
            resources.getString(R.string.requesting_client_email, client.customerEmail)
        tvChosenServiceName.text =
            resources.getString(R.string.requesting_chosen_service_name, service.organisationOfferedServiceName)
        tvChosenServiceType.text = resources.getString(
            R.string.requesting_chosen_service_type,
            serviceCategory.serviceCategoryName
        )
        tvRequestStartDate.text =
            resources.getString(R.string.requesting_start_date, requestedService.requestedStartDate)
        tvRequestServicePrice.text =
            resources.getString(R.string.requesting_service_price, service.organisationOfferedServicePrice)
        tvRequestServiceDiscountedPrice.text = resources.getString(
            R.string.requesting_service_discounted_price,
            service.serviceDiscountedPrice
        )
        tvRequestText.text = requestedService.requestFormText
//        tvRequestedDateCreated.text =
//            resources.getString(R.string.requesting_date_created, requestedService.dateCreated)
//        tvRequestedTimeCreated.text =
//            resources.getString(R.string.requesting_time_created, requestedService.timeCreated)
        btnAcceptRequest.setOnClickListener {
            val currentDate = getDate(System.currentTimeMillis(), "dd-MM-yyyy")
            val currentTime = getDate(System.currentTimeMillis(), "hh:mm a")
            //add to db
            val facilityRequestResponse = FacilityRequestResponse(
                notificationID = System.currentTimeMillis().toString(),
                requestFormID = requestedService.requestFormId,
                customerID = requestedService.customerID,
                organisationID = requestedService.organisationID,
                organisationProfileServiceID = requestedService.organisationProfileServiceID,
                typeOfServiceID = requestedService.typeOfServiceID,
                categoryOfServiceID = requestedService.categoryOfServiceID,
                requestFormStatus = ACCEPTED_TEXT,
                notificationText = resources.getString(
                    R.string.notification_text,
                    client.customerFirstName,
                    service.organisationOfferedServiceName,
                    ACCEPTED_TEXT,
                    currentDate,
                    currentTime
                ),
                dateCreated = currentDate,
                timeCreated = currentTime
            )
            requireContext().showProgress()
            Common.appointmentsCollectionRef.document(requestedService.requestFormId)
                .update("requestStatus", ACCEPTED_TEXT).addOnSuccessListener {
                    hideProgress()
                    createResponseNotification(facilityRequestResponse)
                }.addOnFailureListener { e ->
                    hideProgress()
                    requireContext().toast(e.message.toString())
                }


        }
        btnRejectRequest.setOnClickListener {
            //add to db
            requireContext().showProgress()
            Common.appointmentsCollectionRef.document(requestedService.requestFormId)
                .update("requestStatus", REJECTED_TEXT).addOnSuccessListener {
                    hideProgress()
                    dismiss()
                }.addOnFailureListener { e ->
                    requireContext().toast(e.message.toString())
                }
        }
    }


    private fun createResponseNotification(
        facilityRequestResponse: FacilityRequestResponse,
        //model: BookService,
        //dialog: AlertDialog
    ) {
        requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            requestResponseNotificationCollectionRef.document(facilityRequestResponse.notificationID)
                .set(facilityRequestResponse).addOnSuccessListener {
                    hideProgress()
//                    dismiss()
                    val requestResponseSheetFragment = FacilityCustomerRequestResponseBottomSheet.newInstance(facilityRequestResponse)
                    requestResponseSheetFragment.show(requireFragmentManager(), "requestDetailsSheetTag")
                    dismiss()

                    //launchInvoiceDialog(model, facilityRequestResponse)
                }.addOnFailureListener { e ->
                    hideProgress()
                    requireContext().toast(e.message.toString())
                }
        }
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
        private const val ARG_REQUESTED_SERVICE_DATA = "arg_requested_service_data"

        fun newInstance(requestedService: BookService): FacilityCustomerRequestDetailBottomSheet {
            val fragment = FacilityCustomerRequestDetailBottomSheet()
            val args = Bundle().apply {
                putParcelable(ARG_REQUESTED_SERVICE_DATA, requestedService)

            }
            fragment.arguments = args
            return fragment
        }
    }
}