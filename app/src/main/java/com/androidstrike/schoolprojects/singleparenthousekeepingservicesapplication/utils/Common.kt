package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils

import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object Common {

    const val REQUEST_PERMISSION = 100

    var facilityName = "Facility"
    var clientName = "Client"
    const val SERVICE_CATEGORY = "Category of Service"
    const val SERVICE_TYPE = "Service Type Within a Category"
    const val SERVICE = "Organisation's Profile Service Offered"
    private const val CUSTOMER_REQUEST_FORMS = "Customer's Service Request Form"
    private const val REQUEST_RESPONSE_NOTIFICATION_REF = "Notification of Customer's Service Request Form Acceptance or Rejection"
    private const val ACCEPTED_REQUEST_PAYMENT_INVOICE_REF = "Invoice for Payment of Accepted Customer's Service Request"
    private const val CUSTOMER_INVOICE_PAYMENT_REF = "Payment for Invoice of Accepted Customer's Service Request"



    private const val WALLET_REF = "Customer Digital Wallets"
    const val WALLET_HISTORY_REF = "Wallet History"
    const val SPECIALISTS = "Specialists"


    const val REASON_ACCOUNT_FUND = "Account Fund"
    const val ACCEPTED_TEXT = "Accepted"
    const val REJECTED_TEXT = "Rejected"
    const val PROCESSED_TEXT = "Processed"
    const val PAID_TEXT = "Processed"


    const val DATE_FORMAT_LONG = "EEE, dd MMM, yyyy | hh:mm a"



    var serviceToRate = ""

    //lateinit var mAuth: FirebaseAuth// = FirebaseAuth.getInstance()
    val auth = FirebaseAuth.getInstance()
    val clientCollectionRef = Firebase.firestore.collection("Single Parent Customer")
    val facilityCollectionRef = Firebase.firestore.collection("Housekeeping and Delivery Organisation")
    val servicesCollectionRef = Firebase.firestore.collection(SERVICE)
    val serviceTypeCollectionRef = Firebase.firestore.collection(SERVICE_TYPE)
    val serviceCategoryCollectionRef = Firebase.firestore.collection(SERVICE_CATEGORY)
    val appointmentsCollectionRef = Firebase.firestore.collection(CUSTOMER_REQUEST_FORMS)
    val requestResponseNotificationCollectionRef = Firebase.firestore.collection(REQUEST_RESPONSE_NOTIFICATION_REF)
    val acceptedRequestInvoiceCollectionRef = Firebase.firestore.collection(ACCEPTED_REQUEST_PAYMENT_INVOICE_REF)
    val invoicePaymentCollectionRef = Firebase.firestore.collection(CUSTOMER_INVOICE_PAYMENT_REF)
    val feedbackCollectionRef = Firebase.firestore.collection("Feedbacks")
    val walletCollectionRef = Firebase.firestore.collection(WALLET_REF)
    fun convertTimeStampToDate(time: Long): Date {
        return Date(Timestamp(time).time)
    }

    fun getDateFormatted(date: Date): String? {
        return SimpleDateFormat("dd-MM-yyyy HH:mm").format(date).toString()
    }


}