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
import androidx.appcompat.app.AlertDialog
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.AcceptedRequestInvoice
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Client
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Facility
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.FacilityRequestResponse
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Service
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.ServiceCategory
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.ServiceType
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.PROCESSED_TEXT
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.getDate
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.hideProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.showProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlin.properties.Delegates

class FacilityRecordInvoiceBottomSheet : BottomSheetDialogFragment() {

    private lateinit var facilityRequestResponse: FacilityRequestResponse
    private var totalServicePrice: Double by Delegates.notNull<Double>()

    private val TAG = "FacilityCustomerRequestDetailBottomSheet"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.facility_custom_new_invoice_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        facilityRequestResponse =
            arguments?.getParcelable<FacilityRequestResponse>(ARG_FACILITY_RESPONSE_DATA)!!
        totalServicePrice = arguments?.getDouble(ARG_SERVICE_TOTAL_PRICE_DATA)!!


        var invoiceBankName: String = ""
        var invoiceAccountIBAN: String = ""
        var invoiceAccountName: String = ""

        val tilBankName =
            requireView().findViewById<TextInputLayout>(R.id.text_input_layout_facility_new_record_bank_name)
        val etBankName =
            requireView().findViewById<TextInputEditText>(R.id.facility_new_record_bank_name)
        val tilAccountIBAN =
            requireView().findViewById<TextInputLayout>(R.id.text_input_layout_facility_new_record_account_iban)
        val etAccountIBAN =
            requireView().findViewById<TextInputEditText>(R.id.facility_new_record_account_iban)
        val tilAccountHolder =
            requireView().findViewById<TextInputLayout>(R.id.text_input_layout_facility_new_record_account_holder_name)
        val etAccountHolder =
            requireView().findViewById<TextInputEditText>(R.id.facility_new_record_account_holder_name)

        val btnConfirmInvoiceRecord =
            requireView().findViewById<Button>(R.id.facility_new_record_confirm_btn)
        val btnCancelInvoiceRecord =
            requireView().findViewById<Button>(R.id.facility_new_record_cancel_btn)


        btnConfirmInvoiceRecord.setOnClickListener {
            tilBankName.error = null
            tilAccountIBAN.error = null
            tilAccountHolder.error = null

            invoiceBankName = etBankName.text.toString().trim()
            invoiceAccountIBAN = etAccountIBAN.text.toString().trim()
            invoiceAccountName = etAccountHolder.text.toString().trim()

            if (invoiceBankName.isEmpty()) {
                tilBankName.error = "Bank name required"
                etBankName.requestFocus()
            }
            if (invoiceAccountIBAN.isEmpty()) {
                tilAccountIBAN.error = "Account IBAN required"
                etAccountIBAN.requestFocus()
            }
            if (invoiceAccountName.isEmpty()) {
                tilAccountHolder.error = "Account holder name required"
            } else {
                tilBankName.error = null
                tilAccountIBAN.error = null
                tilAccountHolder.error = null

                val client = getUser(facilityRequestResponse.customerID)!!
                val service = getService(facilityRequestResponse.organisationProfileServiceID)!!

                val currentDate = getDate(System.currentTimeMillis(), "dd-MM-yyyy")
                val currentTime = getDate(System.currentTimeMillis(), "hh:mm a")

                val acceptedRequestInvoice = AcceptedRequestInvoice(
                    invoiceID = System.currentTimeMillis().toString(),
                    notificationID = facilityRequestResponse.notificationID,
                    customerRequestFormID = facilityRequestResponse.requestFormID,
                    customerID = facilityRequestResponse.customerID,
                    organisationID = facilityRequestResponse.organisationID,
                    organisationProfileServiceID = facilityRequestResponse.organisationProfileServiceID,
                    typeOfServiceID = facilityRequestResponse.typeOfServiceID,
                    categoryOfServiceID = facilityRequestResponse.categoryOfServiceID,
                    invoiceAmount = totalServicePrice.toString(),
                    bankName = invoiceBankName,
                    bankAccountIBAN = invoiceAccountIBAN,
                    bankAccountHolderName = invoiceAccountName,
                    invoiceText = resources.getString(
                        R.string.invoice_text,
                        client.customerFirstName,
                        service.organisationOfferedServiceName,
                        PROCESSED_TEXT,
                        invoiceBankName,
                        invoiceAccountIBAN,
                        invoiceAccountName,
                        currentDate,
                        currentTime
                    ),
                    dateCreated = currentDate,
                    timeCreated = currentTime
                )
                uploadInvoiceRecord(
                    acceptedRequestInvoice
                )
            }

        }

        btnCancelInvoiceRecord.setOnClickListener {
            etBankName.setText("")
            etAccountIBAN.setText("")
            etAccountHolder.setText("")
        }

    }

    private fun uploadInvoiceRecord(
        acceptedRequestInvoice: AcceptedRequestInvoice,
    ) {
        requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            Common.acceptedRequestInvoiceCollectionRef.document(acceptedRequestInvoice.invoiceID)
                .set(acceptedRequestInvoice).addOnSuccessListener {
                    hideProgress()
                    requireContext().toast("Invoice Generated")
                    val facilityCustomerRequestResponseBottomSheet = FacilityCustomerRequestResponseBottomSheet
                    val facilityCustomerRequestDetailBottomSheet = FacilityCustomerRequestDetailBottomSheet
                    //facilityCustomerRequestDetailBottomSheet.
                    dismiss()
                }
                .addOnFailureListener { e ->
                    // Handle error
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
                val snapshot =
                    Common.serviceCategoryCollectionRef.document(serviceCategoryId).get().await()
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
        private const val ARG_SERVICE_TOTAL_PRICE_DATA = "arg_service_total_price_data"

        fun newInstance(
            facilityResponse: FacilityRequestResponse,
            totalServicePrice: Double
        ): FacilityRecordInvoiceBottomSheet {
            val fragment = FacilityRecordInvoiceBottomSheet()
            val args = Bundle().apply {
                putParcelable(ARG_FACILITY_RESPONSE_DATA, facilityResponse)
                putDouble(ARG_SERVICE_TOTAL_PRICE_DATA, totalServicePrice)

            }
            fragment.arguments = args
            return fragment
        }
    }
}