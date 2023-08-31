package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientinvoice

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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.databinding.FragmentInvoicePaymentBinding
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.facility.facilitynotification.FacilityCustomerRequestResponseBottomSheet
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.AcceptedRequestInvoice
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.BookService
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Client
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Facility
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Service
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.hideProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.showProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class InvoicePayment : Fragment() {

    private var clientInvoiceNotificationAdapter: FirestoreRecyclerAdapter<AcceptedRequestInvoice, ClientInvoiceNotificationAdapter>? =
        null

    private var _binding: FragmentInvoicePaymentBinding? = null
    private val binding get() = _binding!!
    lateinit var requestingClient: Client
    lateinit var servingFacility: Facility
    lateinit var scheduledService: Service

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentInvoicePaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestingClient = Client()
        servingFacility = Facility()

        getRealtimeInvoice()


        with(binding) {
            val layoutManager = LinearLayoutManager(requireContext())
            rvClientInvoiceNotification.layoutManager = layoutManager
            rvClientInvoiceNotification.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    layoutManager.orientation
                )
            )
        }


    }

    private fun getRealtimeInvoice() {
        val mAuth = FirebaseAuth.getInstance()

        val clientGeneratedInvoice =
            Common.acceptedRequestInvoiceCollectionRef.whereEqualTo("customerID", mAuth.uid)

        val options = FirestoreRecyclerOptions.Builder<AcceptedRequestInvoice>()
            .setQuery(clientGeneratedInvoice, AcceptedRequestInvoice::class.java).build()
        try {
            clientInvoiceNotificationAdapter = object :
                FirestoreRecyclerAdapter<AcceptedRequestInvoice, ClientInvoiceNotificationAdapter>(
                    options
                ) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): ClientInvoiceNotificationAdapter {
                    val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.client_custom_invoice_notification_layout, parent, false)
                    return ClientInvoiceNotificationAdapter(itemView)
                }

                override fun onBindViewHolder(
                    holder: ClientInvoiceNotificationAdapter,
                    position: Int,
                    model: AcceptedRequestInvoice
                ) {
                    holder.serviceName.text =
                        getService(model.organisationProfileServiceID)!!.organisationOfferedServiceName
                    holder.facilityName.text =
                        getOrganisation(model.organisationID)!!.organisationName

                    holder.dateCreated.text = model.dateCreated
                    holder.timeCreated.text = model.timeCreated
                    holder.btnViewNotificationDetails.setOnClickListener {
                        //launch invoice payment bottom sheet
                        val requestResponseSheetFragment =
                            ClientInvoiceDetailsBottomSheet.newInstance(model)
                        requestResponseSheetFragment.show(
                            requireFragmentManager(),
                            ClientInvoiceDetailsBottomSheet::class.java.simpleName
                        )
                    }
                }

            }

        } catch (e: Exception) {
            requireActivity().toast(e.message.toString())
        }
        clientInvoiceNotificationAdapter?.startListening()
        binding.rvClientInvoiceNotification.adapter = clientInvoiceNotificationAdapter
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


    private fun getClientDetails(clientId: String, clientName: TextView) = CoroutineScope(
        Dispatchers.IO
    ).launch {
        Common.clientCollectionRef
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                for (document in querySnapshot.documents) {
                    val item = document.toObject(Client::class.java)
                    if (item?.userId == clientId) {
                        requestingClient = item
                    }
                }
                clientName.text =
                    "${requestingClient.customerFirstName}, ${requestingClient.customerLastName}"
            }
    }

    private fun getFacilityDetails(
        facilityId: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        Common.facilityCollectionRef
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                for (document in querySnapshot.documents) {
                    val item = document.toObject(Facility::class.java)
                    if (item?.organisationID == facilityId) {
                        servingFacility = item
                    }
                }
//                tvFacilityName.text = servingFacility.facilityName
//                tvFacilityAddress.text = servingFacility.facilityAddress
//                tvFacilityEmail.text = servingFacility.facilityEmail
//                tvFacilityPhone.text = servingFacility.facilityPhoneNumber
            }
    }

    private fun getServiceDetails(facilityId: String, serviceId: String) = CoroutineScope(
        Dispatchers.IO
    ).launch {
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}