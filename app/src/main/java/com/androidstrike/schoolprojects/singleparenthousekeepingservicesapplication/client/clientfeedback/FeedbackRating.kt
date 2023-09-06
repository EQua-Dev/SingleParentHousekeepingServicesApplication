package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientfeedback

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.databinding.FragmentFeedbackRatingBinding
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.BookService
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Client
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Facility
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Service
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.RatingBottomSheetListener
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.hideProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.showProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.showProgressDialog
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class FeedbackRating : Fragment(), RatingBottomSheetListener {

    private var _binding: FragmentFeedbackRatingBinding? = null
    private val binding get() = _binding!!

    private var clientRatingFeedbackAdapter: FirestoreRecyclerAdapter<BookService, ClientFeedbackRatingAdapter>? =
        null


//    lateinit var scheduledService: BookService
//    lateinit var respondingFacility: Facility

    private var progressDialog: Dialog? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFeedbackRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            val layoutManager = LinearLayoutManager(requireContext())
            rvClientServiceRatings.layoutManager = layoutManager
            rvClientServiceRatings.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    layoutManager.orientation
                )
            )
        }

        getRealtimeRatings()

//
//        val serviceId = Common.serviceToRate
//
//        var customerRating: Float = 0.0F
//
//        val tvFacilityName = binding.clientRatingFacilityName
//        val tvFacilityEmail = binding.clientRatingFacilityEmail
//        val tvFacilityPhoneNumber = binding.clientRatingFacilityPhoneNumber
//        val tvClientName = binding.clientRatingFacilityName
//        val etFeedbackText = binding.clientRatingFeedbackText
//        val tilFeedbackText = binding.textInputLayoutClientRatingFeedbackText
//
//        getServiceDetails(Common.serviceToRate, tvFacilityName, tvFacilityEmail, tvFacilityPhoneNumber)
//
//        Common.clientCollectionRef.document(auth.uid.toString()).get()
//            .addOnSuccessListener { documentSnapshot ->
//                if (documentSnapshot.exists()) {
//                    val clientFirstName = documentSnapshot.getString("userFirstName")
//                    val clientLastName = documentSnapshot.getString("userLastName")
//                    // Use the value of the field as needed
//                    tvClientName.text = "$clientFirstName $clientLastName"
//                } else {
//                    // Document does not exist
//                }
//            }
//            .addOnFailureListener { exception ->
//                // Handle any errors that occurred
//
//            }
//
//        binding.clientRatingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
//            // Handle the rating change event
//            if (rating.isNaN()) {
//                requireView().snackbar("Give at least one star")
//            } else {
//                customerRating = rating
//            }
//            // `rating` represents the selected rating value
//        }
//
//        binding.clientRatingSubmitButton.setOnClickListener {
//            val feedBackText = etFeedbackText.text.toString().trim()
//            val facilityName = tvFacilityName.text.toString().trim()
//            val facilityEmail = tvFacilityEmail.text.toString().trim()
//            val facilityPhone = tvFacilityPhoneNumber.text.toString().trim()
//            val clientName = tvClientName.text.toString().trim()
//            val service = serviceId
//            if (feedBackText.isEmpty())
//                tilFeedbackText.error = "Please enter a comment"
//            else {
//                val feedBackId = System.currentTimeMillis().toString()
//                submitRating(
//                    feedBackText,
//                    facilityName,
//                    facilityEmail,
//                    facilityPhone,
//                    clientName,
//                    service,
//                    feedBackId
//                )
//            }
//        }

    }

    private fun getRealtimeRatings() {
        val mAuth = FirebaseAuth.getInstance()

        val clientPaidService =
            Common.appointmentsCollectionRef.whereEqualTo("customerID", mAuth.uid)
                .whereEqualTo("requestStatus", "paid").whereEqualTo("rated", false)

        val options = FirestoreRecyclerOptions.Builder<BookService>()
            .setQuery(clientPaidService, BookService::class.java).build()
        try {
            clientRatingFeedbackAdapter = object :
                FirestoreRecyclerAdapter<BookService, ClientFeedbackRatingAdapter>(
                    options
                ) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): ClientFeedbackRatingAdapter {
                    val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.client_custom_ratings_layout, parent, false)
                    return ClientFeedbackRatingAdapter(itemView)
                }

                override fun onBindViewHolder(
                    holder: ClientFeedbackRatingAdapter,
                    position: Int,
                    model: BookService
                ) {

                    Log.d("Here", "onBindViewHolder: ")
                    val organisation = getOrganisation(model.organisationID)!!
                    val service = getService(model.organisationProfileServiceID)!!

//                    if (model.ratings.isEmpty()) {
//                        holder.serviceRating.visible(false)
//                    } else {
//                        holder.serviceRating.text = model.ratings
//                    }

                    holder.dateCreated.text = model.dateCreated
                    holder.timeCreated.text = model.timeCreated
                    holder.facilityName.text = organisation.organisationName
                    holder.serviceName.text = service.organisationOfferedServiceName

                    holder.rateServiceButton.setOnClickListener {
                        val bottomSheetFragment = ClientRatingBottomSheet.newInstance(model)
                        bottomSheetFragment.setListener(this@FeedbackRating) // Pass the listener to the BottomSheet
                        bottomSheetFragment.show(childFragmentManager, "bottomSheetTag")
                        //launchRatingDialog(model)
                    }

                }

            }

        } catch (e: Exception) {
            requireActivity().toast(e.message.toString())
        }
        clientRatingFeedbackAdapter?.startListening()
        binding.rvClientServiceRatings.adapter = clientRatingFeedbackAdapter
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

    override fun refreshRealtimeRatings() {
        getRealtimeRatings()
    }


//    private fun submitRating(
//        feedBackText: String,
//        facilityName: String,
//        facilityEmail: String,
//        facilityPhone: String,
//        clientName: String,
//        service: String,
//        feedBackId: String
//    ) {
//
//        showProgress()
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val addClientFeedBack = ClientFeedBack(
//                    feedBackText = feedBackText,
//                    facilityName = facilityName,
//                    facilityEmail = facilityEmail,
//                    facilityPhone = facilityPhone,
//                    clientName = clientName,
//                    ratedService = service,
//                    feedBackId = feedBackId,
//                    ratedFacility = respondingFacility.facilityId,
//                    ratingCustomer = auth.uid.toString()
//
//                )
//
//                //val appointmentQuery = Common.facilityCollectionRef.whereEqualTo("facilityId", facility.facilityId).get().await()
//                Common.feedbackCollectionRef.document(feedBackId).set(addClientFeedBack)
//
//                withContext(Dispatchers.Main) {
//                    with(binding) {
//                        val clientBaseFragment = parentFragment
//                        val clientTabLayout =
//                            clientBaseFragment!!.view?.findViewById<TabLayout>(R.id.client_base_tab_title)
//                        clientTabLayout?.getTabAt(0)?.select()
//
//
//                    }
//                    hideProgress()
//                    requireView().snackbar("Service Added")
//                }
//                //dismiss bottom sheet
//
//            } catch (e: Exception) {
//                requireContext().toast(e.message.toString())
//            }
//        }
//
//
//    }
//
//
//    private fun getServiceDetails(
//        serviceId: String,
//        tvFacilityName: TextView,
//        tvFacilityEmail: TextView,
//        tvFacilityPhoneNumber: TextView
//    ) = CoroutineScope(
//        Dispatchers.IO
//    ).launch {
//        Common.appointmentsCollectionRef
//            .get()
//            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
//
//                for (document in querySnapshot.documents) {
//                    val item = document.toObject(BookService::class.java)
//                    if (item?.selectedAppointmentServiceID == serviceId) {
//                        scheduledService = item
//                    }
//                }
//                getFacilityDetails(
//                    scheduledService.facilityId,
//                    tvFacilityName,
//                    tvFacilityEmail,
//                    tvFacilityPhoneNumber
//                )
//            }
//    }
//
//
//    private fun getFacilityDetails(
//        facilityId: String,
//        tvFacilityName: TextView,
//        tvFacilityEmail: TextView,
//        tvFacilityPhoneNumber: TextView,
//    ) = CoroutineScope(Dispatchers.IO).launch {
//        Common.facilityCollectionRef
//            .get()
//            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
//
//                Log.d("EQUA", "getRealtimeRehabs: $querySnapshot")
//                for (document in querySnapshot.documents) {
//                    Log.d("EQUA", "getRealtimeRehabs: $document")
//                    val item = document.toObject(Facility::class.java)
//                    if (item?.facilityId == facilityId) {
//                        respondingFacility = item
//                    }
//                }
//                tvFacilityName.text = respondingFacility.facilityName
//                tvFacilityEmail.text = respondingFacility.facilityEmail
//                tvFacilityPhoneNumber.text = respondingFacility.facilityPhoneNumber
//
//
//            }
//    }
//




}