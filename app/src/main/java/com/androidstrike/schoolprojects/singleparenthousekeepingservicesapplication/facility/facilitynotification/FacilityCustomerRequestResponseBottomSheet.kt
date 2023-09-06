package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.facility.facilitynotification

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.BookService
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Client
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Facility
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.FacilityRequestResponse
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Service
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.ServiceCategory
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.ServiceType
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.hideProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.showProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class FacilityCustomerRequestResponseBottomSheet : BottomSheetDialogFragment() {

    private lateinit var facilityRequestResponse: FacilityRequestResponse

    private val TAG =  "FacilityCustomerRequestDetailBottomSheet"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.facility_custom_generate_invoice_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        facilityRequestResponse = arguments?.getParcelable<FacilityRequestResponse>(ARG_FACILITY_RESPONSE_DATA)!!


        val tvFacilityName =
            requireView().findViewById<TextView>(R.id.facility_generated_invoice_facility_name)
        val tvFacilityAddress =
            requireView().findViewById<TextView>(R.id.facility_generated_invoice_facility_address)
        val tvFacilityPhone =
            requireView().findViewById<TextView>(R.id.facility_generated_invoice_facility_phone_number)
        val tvFacilityEmail =
            requireView().findViewById<TextView>(R.id.facility_generated_invoice_facility_email)
        val tvClientName =
            requireView().findViewById<TextView>(R.id.facility_generated_invoice_customer_name)
        val tvClientPhone =
            requireView().findViewById<TextView>(R.id.facility_generated_invoice_customer_phone)
        val tvClientEmail =
            requireView().findViewById<TextView>(R.id.facility_generated_invoice_customer_email)
        val tvServiceName =
            requireView().findViewById<TextView>(R.id.facility_generated_invoice_request_service_name)
        val tvServiceFrequency =
            requireView().findViewById<TextView>(R.id.facility_generated_invoice_request_service_frequency)
        val tvServicePrice =
            requireView().findViewById<TextView>(R.id.facility_generated_invoice_request_service_price)
        val tvServiceDiscountedPrice =
            requireView().findViewById<TextView>(R.id.facility_generated_invoice_request_service_discounted_price)
        val tvServiceTotal =
            requireView().findViewById<TextView>(R.id.facility_generated_invoice_request_service_total_price)

        val btnConfirmInvoice =
            requireView().findViewById<Button>(R.id.facility_generated_invoice_confirm_button)


        val organisation = getOrganisation(facilityRequestResponse.organisationID)!!
        val client = getUser(facilityRequestResponse.customerID)!!
        val service = getService(facilityRequestResponse.organisationProfileServiceID)!!
        val requestForm = getRequestForm(facilityRequestResponse.requestFormID)!!


        tvFacilityName.text = resources.getString(
            R.string.facility_generate_request_invoice_company_name,
            organisation.organisationName
        )
        tvFacilityAddress.text = resources.getString(
            R.string.facility_generate_request_invoice_company_physical_address,
            organisation.organisationPhysicalAddress
        )
        tvFacilityPhone.text = resources.getString(
            R.string.facility_generate_request_invoice_company_contact_number,
            organisation.organisationContactNumber
        )
        tvFacilityEmail.text = resources.getString(
            R.string.facility_generate_request_invoice_company_email,
            organisation.organisationEmail
        )
        tvClientName.text = resources.getString(
            R.string.facility_generate_request_invoice_customer_name,
            client.customerFirstName,
            client.customerLastName
        )
        tvClientPhone.text = resources.getString(
            R.string.facility_generate_request_invoice_customer_contact_number,
            client.customerMobileNumber
        )
        tvClientEmail.text = resources.getString(
            R.string.facility_generate_request_invoice_customer_email_address,
            client.customerEmail
        )
        tvServiceName.text = resources.getString(
            R.string.facility_generate_request_invoice_service_name,
            service.organisationOfferedServiceName
        )
        tvServiceFrequency.text = resources.getString(R.string.facility_generate_request_invoice_service_frequency, requestForm.requestedServiceFrequency)
        tvServicePrice.text = resources.getString(
            R.string.facility_generate_request_invoice_service_price,
            service.organisationOfferedServicePrice
        )
        tvServiceDiscountedPrice.text = resources.getString(
            R.string.facility_generate_request_invoice_service_discounted_price,
            service.serviceDiscountedPrice
        )

//        val servicePrice = scheduledService.servicePrice
//        val serviceDiscountedPrice = scheduledService.serviceDiscountedPrice
        val totalServicePrice = if (service.organisationOfferedServicePrice.toDouble() < service.serviceDiscountedPrice.toDouble())
            service.organisationOfferedServicePrice.toDouble()
        else
            service.serviceDiscountedPrice.toDouble()
        //Log.d(TAG, "onViewCreated: ${service.serviceDiscountedPrice} $service.")
        tvServiceTotal.text = resources.getString(
            R.string.facility_generate_request_invoice_service_total_price,
            totalServicePrice.toString()
        )


        btnConfirmInvoice.setOnClickListener {
            val requestDetailsSheetFragment = FacilityRecordInvoiceBottomSheet.newInstance(facilityRequestResponse, totalServicePrice)
            requestDetailsSheetFragment.show(requireFragmentManager(), "requestDetailsSheetTag")

            dismiss()
            //launchInvoiceRecordDialog(model, totalServicePrice, facilityRequestResponse)
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
    private fun getRequestForm(requestFormIdId: String): BookService? {

        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.appointmentsCollectionRef.document(requestFormIdId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(BookService::class.java)
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

        val requestForm = runBlocking { deferred.await() }
        hideProgress()

        return requestForm
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



    companion object {
        private const val ARG_FACILITY_RESPONSE_DATA = "arg_facility_response_data"

        fun newInstance(facilityResponse: FacilityRequestResponse): FacilityCustomerRequestResponseBottomSheet {
            val fragment = FacilityCustomerRequestResponseBottomSheet()
            val args = Bundle().apply {
                putParcelable(ARG_FACILITY_RESPONSE_DATA, facilityResponse)

            }
            fragment.arguments = args
            return fragment
        }
    }
}