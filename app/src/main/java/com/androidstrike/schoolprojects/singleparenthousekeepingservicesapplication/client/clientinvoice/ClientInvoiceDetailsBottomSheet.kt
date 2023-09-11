package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientinvoice

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.WalletData
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.WalletHistory
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.AcceptedRequestInvoice
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.BookService
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Client
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Facility
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.InvoicePaymentData
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Service
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.DATE_FORMAT_LONG
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.WALLET_HISTORY_REF
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.appointmentsCollectionRef
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.invoicePaymentCollectionRef
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.walletCollectionRef
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

class ClientInvoiceDetailsBottomSheet : BottomSheetDialogFragment() {

    private lateinit var facilityGeneratedInvoice: AcceptedRequestInvoice



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.client_custom_generated_invoice_detail_layout,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        facilityGeneratedInvoice =
            arguments?.getParcelable<AcceptedRequestInvoice>(ClientInvoiceDetailsBottomSheet.ARG_FACILITY_GENERATED_INVOICE_DATA)!!

        val tvFacilityName =
            requireView().findViewById<TextView>(R.id.client_generated_invoice_facility_name)
        val tvFacilityAddress =
            requireView().findViewById<TextView>(R.id.client_generated_invoice_facility_physical_address)
        val tvFacilityPhone =
            requireView().findViewById<TextView>(R.id.client_generated_invoice_facility_phone_number)
        val tvFacilityEmail =
            requireView().findViewById<TextView>(R.id.client_generated_invoice_facility_email)
        val tvInvoiceText = requireView().findViewById<TextView>(R.id.client_generated_invoice_text)
        val tvInvoiceDate =
            requireView().findViewById<TextView>(R.id.client_generated_invoice_date_created)
        val tvInvoiceTime =
            requireView().findViewById<TextView>(R.id.client_generated_invoice_time_created)

        val btnProvidePayment =
            requireView().findViewById<Button>(R.id.client_generated_invoice_payment_button)

        val organisation = getOrganisation(facilityGeneratedInvoice.organisationID)!!
        val customer = getCustomer(facilityGeneratedInvoice.customerID)!!
        val invoice = getInvoice(facilityGeneratedInvoice.invoiceID)!!


        tvFacilityName.text = resources.getString(
            R.string.facility_generate_request_invoice_company_name,
            organisation.organisationName
        )
        tvFacilityPhone.text = resources.getString(
            R.string.facility_generate_request_invoice_company_contact_number,
            organisation.organisationContactNumber
        )
        tvFacilityAddress.text = resources.getString(
            R.string.facility_generate_request_invoice_company_physical_address,
            organisation.organisationPhysicalAddress
        )
        tvFacilityEmail.text = resources.getString(
            R.string.facility_generate_request_invoice_company_email,
            organisation.organisationEmail
        )
        tvInvoiceText.text = facilityGeneratedInvoice.invoiceText
        tvInvoiceDate.text =
            resources.getString(R.string.requesting_date_created, facilityGeneratedInvoice.dateCreated)
        tvInvoiceTime.text =
            resources.getString(R.string.requesting_time_created, facilityGeneratedInvoice.timeCreated)




        val requestForm = getRequestForm(facilityGeneratedInvoice.customerRequestFormID)!!

        btnProvidePayment.apply {
            if (requestForm.requestStatus != "paid") {
                text = resources.getString(R.string.client_generated_invoice_payment_button)
                setOnClickListener {
                    val currentTime = getDate(System.currentTimeMillis(), "hh:mm a")
                    val currentDate = getDate(System.currentTimeMillis(), "dd-MM-yyyy")

                    val invoicePaymentData = InvoicePaymentData(
                        paymentID = System.currentTimeMillis().toString(),
                        invoiceID = facilityGeneratedInvoice.invoiceID,
                        notificationID = facilityGeneratedInvoice.notificationID,
                        customerRequestFormID = facilityGeneratedInvoice.customerRequestFormID,
                        customerID = facilityGeneratedInvoice.customerID,
                        customerDigitalWalletID = customer.wallet,
                        organisationID = facilityGeneratedInvoice.organisationID,
                        organisationProfileServiceID = facilityGeneratedInvoice.organisationProfileServiceID,
                        typeOfServiceID = facilityGeneratedInvoice.typeOfServiceID,
                        paymentAmount = facilityGeneratedInvoice.invoiceAmount,
                        paymentBankIBAN = facilityGeneratedInvoice.bankAccountIBAN,
                        paymentBankName = facilityGeneratedInvoice.bankName,
                        paymentBankAccountHolderName = facilityGeneratedInvoice.bankAccountHolderName,
                        paymentDate = currentDate,
                        paymentTime = currentTime,
                    )
                    launchPaymentDialog(invoicePaymentData)
                }
            } else {
                text = resources.getString(R.string.btn_client_booking_response_detail_okay)
                setOnClickListener {
                    dismiss()
                }
            }
        }


    }

    private fun launchPaymentDialog(
        invoicePaymentData: InvoicePaymentData
    ) {

        val builder =
            layoutInflater.inflate(R.layout.client_custom_make_payment_layout_dialog, null)

        val tvCustomerWalletBalance =
            builder.findViewById<TextView>(R.id.client_make_payment_wallet_balance)
        val tvMakePaymentText = builder.findViewById<TextView>(R.id.client_make_payment_text)
        val btnProceedPayment = builder.findViewById<Button>(R.id.btn_client_make_payment)
        val btnCancelPayment = builder.findViewById<Button>(R.id.btn_client_cancel_payment)

        val customer = getCustomer(invoicePaymentData.customerID)!!
        val wallet = getWallet(customer.wallet)!!
        val service = getService(invoicePaymentData.organisationProfileServiceID)!!
        tvCustomerWalletBalance.text =
            resources.getString(R.string.client_make_payment_wallet_balance, wallet.walletBalance)
        tvMakePaymentText.text = resources.getString(
            R.string.client_make_payment_text,
            invoicePaymentData.paymentAmount,
            service.organisationOfferedServiceName
        )


        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        btnCancelPayment.setOnClickListener {
            dialog.dismiss()
        }
        btnProceedPayment.setOnClickListener {
            //makePayment(invoicePaymentData)
            if (wallet.walletBalance.toDouble() < invoicePaymentData.paymentAmount.toDouble()) {
                requireContext().toast(resources.getString(R.string.insufficient_balance))
            } else {
                requireContext().showProgress()
                CoroutineScope(Dispatchers.IO).launch {

                    invoicePaymentCollectionRef.document(invoicePaymentData.paymentID)
                        .set(invoicePaymentData).addOnSuccessListener {
                            appointmentsCollectionRef.document(invoicePaymentData.customerRequestFormID)
                                .update("requestStatus", "paid").addOnSuccessListener {
                                    val newWalletBalance =
                                        wallet.walletBalance.toDouble() - invoicePaymentData.paymentAmount.toDouble()
                                    walletCollectionRef.document(customer.wallet)
                                        .update("walletBalance", newWalletBalance.toString())
                                        .addOnSuccessListener {
                                            val walletHistory = WalletHistory(
                                                transactionDate = getDate(
                                                    System.currentTimeMillis(),
                                                    DATE_FORMAT_LONG
                                                ),
                                                transactionReason = "Payment for ${service.organisationOfferedServiceName}",
                                                transactionType = "Dr",
                                                transactionAmount = resources.getString(
                                                    R.string.money_text,
                                                    invoicePaymentData.paymentAmount
                                                )
                                            )
                                            walletCollectionRef.document(customer.wallet)
                                                .collection(
                                                    WALLET_HISTORY_REF
                                                ).document(System.currentTimeMillis().toString())
                                                .set(walletHistory)
                                                .addOnSuccessListener {
                                                    hideProgress()
                                                    requireContext().toast("payment made successfully")
                                                    dialog.dismiss()
                                                    dismiss()

                                                }.addOnFailureListener { e ->
                                                    hideProgress()
                                                    requireContext().toast(e.message.toString())
                                                }
                                        }.addOnFailureListener { e ->
                                            hideProgress()
                                            requireContext().toast(e.message.toString())
                                        }
                                }
                        }.addOnFailureListener { e ->
                            hideProgress()
                            requireContext().toast(e.message.toString())
                        }
                }
            }
        }

        dialog.show()

    }

    private fun makePayment(invoicePaymentData: InvoicePaymentData) {


    }

    private fun getWallet(walletId: String): WalletData? {
        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.walletCollectionRef.document(walletId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(WalletData::class.java)
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

        val wallet = runBlocking { deferred.await() }
        hideProgress()

        return wallet
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

        return organisation
    }

    private fun getCustomer(customerId: String): Client? {
        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.clientCollectionRef.document(customerId).get().await()
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

        val organisation = runBlocking { deferred.await() }
        hideProgress()

        return organisation
    }

    private fun getRequestForm(requestFormId: String): BookService? {
        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.appointmentsCollectionRef.document(requestFormId).get().await()
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


    private fun getInvoice(invoiceId: String): AcceptedRequestInvoice? {
        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot =
                    Common.acceptedRequestInvoiceCollectionRef.document(invoiceId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(AcceptedRequestInvoice::class.java)
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

        val invoice = runBlocking { deferred.await() }
        hideProgress()

        return invoice
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



    companion object {
        private const val ARG_FACILITY_GENERATED_INVOICE_DATA = "arg_facility_generated_invoice_data"

        fun newInstance(
            facilityGeneratedInvoice: AcceptedRequestInvoice,
        ): ClientInvoiceDetailsBottomSheet {
            val fragment = ClientInvoiceDetailsBottomSheet()
            val args = Bundle().apply {
                putParcelable(ARG_FACILITY_GENERATED_INVOICE_DATA, facilityGeneratedInvoice)

            }
            fragment.arguments = args
            return fragment
        }
    }
}