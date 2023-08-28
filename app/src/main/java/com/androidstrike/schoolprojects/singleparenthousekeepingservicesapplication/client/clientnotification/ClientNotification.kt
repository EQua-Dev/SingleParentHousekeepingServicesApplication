package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientnotification

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.databinding.FragmentClientNotificationBinding
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Facility
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.FacilityRequestResponse
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Service
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.ACCEPTED_TEXT
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.hideProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.showProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.snackBar
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ClientNotification : Fragment() {

    var clientBookingResponseScreenAdapter: FirestoreRecyclerAdapter<FacilityRequestResponse, ClientBookingResponseScreenAdapter>? = null

    private var progressDialog: Dialog? = null
    lateinit var respondingFacility : Facility
    lateinit var scheduledService: Service



    private var _binding: FragmentClientNotificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentClientNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        respondingFacility = Facility()
        scheduledService = Service()
        getRealtimeResponses()

        with(binding){
            val layoutManager = LinearLayoutManager(requireContext())
            rvClientBookingResult.layoutManager = layoutManager
            rvClientBookingResult.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    layoutManager.orientation
                )
            )
        }
    }

    private fun getRealtimeResponses() {
        val mAuth = FirebaseAuth.getInstance()

        val facilityResponses =
            Common.requestResponseNotificationCollectionRef.whereEqualTo("customerID",mAuth.uid).orderBy("notificationID", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<FacilityRequestResponse>()
            .setQuery(facilityResponses, FacilityRequestResponse::class.java).build()
        try
        {
            clientBookingResponseScreenAdapter = object : FirestoreRecyclerAdapter<FacilityRequestResponse, ClientBookingResponseScreenAdapter>(options){
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientBookingResponseScreenAdapter {
                    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.client_booking_result_custom_layout, parent, false)
                    return ClientBookingResponseScreenAdapter(itemView)
                }

                override fun onBindViewHolder(
                    holder: ClientBookingResponseScreenAdapter,
                    position: Int,
                    model: FacilityRequestResponse
                ) {


//                    val clientBaseFragment = parentFragment
//                    val clientTabLayout =
//                        clientBaseFragment!!.view?.findViewById<TabLayout>(R.id.client_base_tab_title)


                    Log.d("EQUA", "onBindViewHolder: ${model.customerID}")
                    var responseState: String = ""
                    holder.dateCreated.text = model.dateCreated
                    holder.timeCreated.text = model.timeCreated
                    holder.facilityName.text = getOrganisation(model.organisationID)!!.organisationName
                    holder.serviceName.text = getService(model.organisationProfileServiceID)!!.organisationOfferedServiceName

                    holder.clientBookingResponseViewDetailsButton.setOnClickListener {
                        if(model.requestFormStatus == ACCEPTED_TEXT) {
                            val notificationDetailSheetFragment =
                                ClientNotificationDetailBottomSheet.newInstance(model)
                            notificationDetailSheetFragment.show(
                                childFragmentManager,
                                "requestDetailsSheetTag"
                            )
                        }
                        //launchDetailDialog(model)
                        else
                            requireView().snackBar("Your request was not accepted")
                    }

                }

            }

        }catch (e: Exception){
            requireActivity().toast(e.message.toString())
        }
        clientBookingResponseScreenAdapter?.startListening()
        binding.rvClientBookingResult.adapter = clientBookingResponseScreenAdapter
    }

//    private fun launchDetailDialog(model: FacilityRequestResponse) {
//
//        val builder = layoutInflater.inflate(R.layout.client_notification_detail_dialog_layout, null)
//
//        getServiceDetails(model.organisationID, model.organisationProfileServiceID)
//
//        val tvDateCreated = builder.findViewById<TextView>(R.id.txt_client_booking_response_detail_date_created)
//        val tvDateTimeCreated = builder.findViewById<TextView>(R.id.txt_client_booking_response_detail_time_created)
//        val tvDetailText = builder.findViewById<TextView>(R.id.txt_client_booking_result_detail_description)
//
//        val btnOkay = builder.findViewById<Button>(R.id.btn_client_booking_response_detail_okay)
//
////        tvDateCreated.text = getDate(model.dateResponded.toLong(), "dd MMMM, yyyy")
////        tvDateTimeCreated.text = getDate(model.dateResponded.toLong(), "hh:mm a")
////        tvDetailText.text = "Dear Customer,\n\nThis is to confirm that your request has been accepted for service: ${scheduledService.specificServiceName}\n\nPlease come for the initial meeting at:\n\nDate: ${model.scheduledDate}\nTime: ${model.scheduledTime}\n\nWe will be happy to see you there"
//
//        val dialog = AlertDialog.Builder(requireContext())
//            .setView(builder)
//            .setCancelable(false)
//            .create()
//
//
//        btnOkay.setOnClickListener {
//            dialog.dismiss()
//        }
//
//
//
//        dialog.show()
//
//
//
//    }
//

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
        //Log.d(TAG, "getOrganisation: $organisation")

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


    private fun getServiceDetails(facilityId: String, serviceId: String) = CoroutineScope(Dispatchers.IO).launch {
        Common.servicesCollectionRef
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                for (document in querySnapshot.documents) {
                    val item = document.toObject(Service::class.java)
                    if (item?.organisationProfileServiceID == serviceId) {
                        scheduledService = item
                    }
                }
            }
    }

    private fun getFacilityDetails(
        facilityId: String,
        facilityName: TextView,
        facilityEmail: TextView,
        facilityPhone: TextView
    ) = CoroutineScope(Dispatchers.IO).launch {
        Common.facilityCollectionRef
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                Log.d("EQUA", "getRealtimeRehabs: $querySnapshot")
                for (document in querySnapshot.documents) {
                    Log.d("EQUA", "getRealtimeRehabs: $document")
                    val item = document.toObject(Facility::class.java)
                    if (item?.organisationID == facilityId) {
                        respondingFacility = item
                    }
                }
                facilityName.text = respondingFacility.organisationName
                facilityEmail.text = respondingFacility.organisationEmail
                facilityPhone.text = respondingFacility.organisationContactNumber
            }
    }
}