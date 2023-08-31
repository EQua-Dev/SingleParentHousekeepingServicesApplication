package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientfeedback

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.BookService
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Client
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.ClientFeedBack
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Facility
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Service
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.ServiceCategory
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.ServiceType
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.appointmentsCollectionRef
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.feedbackCollectionRef
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.RatingBottomSheetListener
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.getDate
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.hideProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.showProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.snackBar
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ClientRatingBottomSheet : BottomSheetDialogFragment() {

    private lateinit var ratedService: BookService

    private val TAG =  "FacilityCustomerRequestDetailBottomSheet"

    private var progressDialog: Dialog? = null

    private var ratingBottomSheetListener: RatingBottomSheetListener? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.custom_client_rate_service_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ratedService = arguments?.getParcelable<BookService>(ARG_RATED_SERVICE_DATA)!!



        val tvFacilityName =
                requireView().findViewById<TextView>(R.id.client_rating_facility_name)
            val tvFacilityAddress =
                requireView().findViewById<TextView>(R.id.client_rating_facility_address)
            val tvFacilityEmail =
                requireView().findViewById<TextView>(R.id.client_rating_facility_email)
            val tvFacilityPhoneNumber =
                requireView().findViewById<TextView>(R.id.client_rating_facility_phone_number)
            val tvClientName =
                requireView().findViewById<TextView>(R.id.client_rating_client_name)
            val clientRatingBar =
                requireView().findViewById<SimpleRatingBar>(R.id.client_rating_bar)
            val etFeedbackText =
                requireView().findViewById<TextInputEditText>(R.id.client_rating_feedback_text)
            val tilFeedbackText =
                requireView().findViewById<TextInputLayout>(R.id.text_input_layout_client_rating_feedback_text)
            val tvDateCreated =
                requireView().findViewById<TextView>(R.id.rating_date_created)
            val tvTimeCreated =
                requireView().findViewById<TextView>(R.id.rating_time_created)
            val btnSubmitRating =
                requireView().findViewById<Button>(R.id.client_rating_submit_button)


            val user = getCustomer(ratedService.customerID)!!
            val organisation = getOrganisation(ratedService.organisationID)!!

            tvFacilityName.text = resources.getString(
                R.string.facility_generate_request_invoice_company_name,
                organisation.organisationName
            )
            tvFacilityAddress.text = resources.getString(
                R.string.facility_generate_request_invoice_company_physical_address,
                organisation.organisationPhysicalAddress
            )
            tvFacilityEmail.text = resources.getString(
                R.string.facility_generate_request_invoice_company_email,
                organisation.organisationEmail
            )
            tvFacilityPhoneNumber.text = resources.getString(
                R.string.facility_generate_request_invoice_company_contact_number,
                organisation.organisationContactNumber
            )
            tvClientName.text = resources.getString(
                R.string.facility_generate_request_invoice_customer_name,
                user.customerFirstName,
                user.customerLastName
            )
            tvDateCreated.text = resources.getString(
                R.string.requesting_date_created,
                getDate(System.currentTimeMillis(), "dd-MM-yyyy")
            )
            tvTimeCreated.text = resources.getString(
                R.string.requesting_time_created,
                getDate(System.currentTimeMillis(), "hh:mm a")
            )

            var customerRating: Float = 0.0F



            clientRatingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                // Handle the rating change event
                if (rating.isNaN()) {
                    requireView().snackBar("Give at least one star")
                } else {
                    customerRating = rating
                }
                // `rating` represents the selected rating value
            }

            btnSubmitRating.setOnClickListener {
                if (customerRating == 0.0F)
                    requireView().snackBar("Give at least one star")
                if (etFeedbackText.text.toString().trim().isEmpty())
                    tilFeedbackText.error = "Please enter a comment"
                else {
                    val clientFeedBack = ClientFeedBack(
                        feedbackAndRatingsID = System.currentTimeMillis().toString(),
                        organisationID = ratedService.organisationID,
                        customerID = ratedService.customerID,
                        organisationProfileService = ratedService.organisationProfileServiceID,
                        serviceType = ratedService.typeOfServiceID,
                        feedBackText = etFeedbackText.text.toString().trim(),
                        ratings = customerRating.toString(),
                        dateCreated = getDate(System.currentTimeMillis(), "dd-MM-yyyy"),
                        timeCreated = getDate(System.currentTimeMillis(), "hh:mm a"),
                    )
                    submitRating(clientFeedBack, ratedService)
                }
            }

    }
    fun setListener(listener: RatingBottomSheetListener) {
        ratingBottomSheetListener = listener
    }


    private fun submitRating(
        clientFeedBack: ClientFeedBack,
        model: BookService
    ) {

        requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                feedbackCollectionRef.document(clientFeedBack.feedbackAndRatingsID)
                    .set(clientFeedBack).addOnSuccessListener {
                        appointmentsCollectionRef.document(model.requestFormId).update("isRated", true)
                            .addOnSuccessListener {
                                requireContext().toast("Feedback Sent")
                                getRealtimeRatings()
                            }
                        hideProgress()
                    }
            } catch (e: Exception) {
                requireContext().toast(e.message.toString())
            }
        }

    }

    private fun getRealtimeRatings() {
        ratingBottomSheetListener?.refreshRealtimeRatings()
        dismiss()

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
        private const val ARG_RATED_SERVICE_DATA = "arg_rated_service_data"

        fun newInstance(ratedService: BookService): ClientRatingBottomSheet {
            val fragment = ClientRatingBottomSheet()
            val args = Bundle().apply {
                putParcelable(ARG_RATED_SERVICE_DATA, ratedService)

            }
            fragment.arguments = args
            return fragment
        }
    }
}