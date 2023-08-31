package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.landing

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.BookService
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Client
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Facility
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Service
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.ServiceCategory
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.ServiceType
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.auth
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.enable
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.getDate
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.hideProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.showProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.showProgressDialog
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.toast
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.visible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MapFacilityDetailBottomSheet : BottomSheetDialogFragment() {

    private lateinit var selectedAppointmentServiceName: String
    private lateinit var selectedAppointmentServiceID: String
    private lateinit var selectedAppointmentDate: String
    private lateinit var selectedAppointmentTime: String

    //private lateinit var selectedAppointmentDescription: String
    private lateinit var dateBooked: String
    private lateinit var client: String
    private lateinit var facility: Facility

    private val TAG =  "MapFacilityDetailBottomSheet"

    //private lateinit var facilityId: String

    private val calendar = Calendar.getInstance()

    private val facilityServices: MutableList<Service> = mutableListOf()
    private val facilityServicesNames: MutableList<String> = mutableListOf()
    private val facilityServicesCategories: MutableList<ServiceCategory> = mutableListOf()
    private val facilityServicesCategoriesNames: MutableList<String> = mutableListOf()

    //    private val facilitySpecificServices: MutableList<SpecificService> = mutableListOf()
    private val facilitySpecificServicesNames: MutableList<String> = mutableListOf()

    //    private val facilitySpecialists: MutableList<Specialists> = mutableListOf()
    private val facilitySpecialistsNames: MutableList<String> = mutableListOf()


    private var progressDialog: Dialog? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.facility_maps_detail_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        facility = arguments?.getParcelable<Facility>(ARG_FACILITY_DATA)!!

        getFacilityServiceDetails(facility.organisationID)
    }

    private fun loadView() {

        val bottomSheetBookButton = requireView().findViewById<Button>(R.id.book_btn)
        val bottomSheetFacilityName = requireView().findViewById<TextView>(R.id.facility_name)
        val bottomSheetFacilityAddress =
            requireView().findViewById<TextView>(R.id.facility_address)
        val bottomSheetFacilityEmail =
            requireView().findViewById<TextView>(R.id.facility_email)
        val bottomSheetFacilityPhone =
            requireView().findViewById<TextView>(R.id.facility_phone)
        val bottomSheetFacilityPhoneImage =
            requireView().findViewById<ImageView>(R.id.img_map_facility_phone)
        val bottomSheetFacilityDirectionImage =
            requireView().findViewById<ImageView>(R.id.img_map_facility_direction)
        val bottomSheetFacilityEmailImage =
            requireView().findViewById<ImageView>(R.id.img_map_facility_email)
        val bottomSheetServiceCategory =
            requireView().findViewById<AutoCompleteTextView>(R.id.auto_complete_select_service)
        val bottomSheetServiceName =
            requireView().findViewById<AutoCompleteTextView>(R.id.auto_complete_select_service_name)
val bottomSheetServiceFrequency =
            requireView().findViewById<AutoCompleteTextView>(R.id.auto_complete_select_service_frequency)

        bottomSheetFacilityName.text = facility.organisationName
        bottomSheetFacilityAddress.text = facility.organisationPhysicalAddress
        bottomSheetFacilityEmail.text = facility.organisationEmail
        bottomSheetFacilityPhone.text = facility.organisationContactNumber



        Log.d("EQUA", "onViewCreated: $facilityServicesNames")
        Log.d("EQUA", "onViewCreated: $facilityServices")

        val serviceCategoryArrayAdapter =
            ArrayAdapter(requireContext(), R.layout.drop_down_item, facilityServicesCategoriesNames.distinct())
        bottomSheetServiceCategory.setAdapter(serviceCategoryArrayAdapter)

        bottomSheetServiceCategory.setOnItemClickListener { _, _, position, _ ->
            val selectedItem =
                serviceCategoryArrayAdapter.getItem(position) // Get the selected item

            var categoryId = ""

            for (category in facilityServicesCategories)
                if (selectedItem == category.serviceCategoryName) {
                    categoryId = category.serviceCategoryID
                     }
            for (service in facilityServices)
                if (categoryId == service.categoryOfServiceID)
                    facilityServicesNames.add(service.organisationOfferedServiceName)

            //getFacilitySpecificServiceDetails(facility.facilityId, serviceId)
            val facilityServiceNamesArrayAdapter =
                ArrayAdapter(requireContext(), R.layout.drop_down_item, facilityServicesNames.distinct())
            bottomSheetServiceName
        .setAdapter(
            facilityServiceNamesArrayAdapter
            )
            bottomSheetServiceName.setOnItemClickListener { _, _, position, _ ->
                val selectedServiceName = facilityServiceNamesArrayAdapter.getItem(position)
                var selectedServiceId = ""
                for (service in facilityServices)
                    if (selectedServiceName == service.organisationOfferedServiceName)
                        selectedServiceId = service.organisationProfileServiceID

                Log.d(TAG, "loadView: $selectedServiceId")
                Log.d(
                    TAG,
                    "loadView: ${getService(selectedServiceId)!!.organisationServiceFrequency}"
                )
                val facilityServiceFrequenciesArrayAdapter =
                    ArrayAdapter(requireContext(), R.layout.drop_down_item, getService(selectedServiceId)!!.organisationServiceFrequency)
                bottomSheetServiceFrequency
                    .setAdapter(
                        facilityServiceFrequenciesArrayAdapter
                    )
            }

        }


        bottomSheetBookButton.setOnClickListener {
            var selectedServiceId = ""
            var selectedServiceCategoryId = ""
            var selectedServiceTypeId = ""
                var selectedServiceFrequency = ""
            var selectedSpecificServicePrice = ""
            var selectedSpecificServiceDiscountPrice = ""
            var selectedAppointmentServiceAvailablePlaces = ""
            for (service in facilityServices)
                if (bottomSheetServiceName.text.toString() == service.organisationOfferedServiceName){
                    selectedServiceId = service.organisationProfileServiceID
                    selectedServiceCategoryId = service.categoryOfServiceID
                    selectedServiceTypeId = service.typeOfServiceID
                    selectedSpecificServicePrice = service.serviceDiscountedPrice
                    selectedSpecificServiceDiscountPrice =
                        service.organisationOfferedServicePrice
                    selectedAppointmentServiceAvailablePlaces =
                        service.serviceAvailability
                    selectedServiceFrequency = bottomSheetServiceFrequency.text.toString().trim()
                }





//            requestDeliveryOfGoodsOptions

            val bookService = BookService(
                requestFormId = System.currentTimeMillis().toString(),
                customerID = auth.uid!!,
                organisationID = facility.organisationID,
                organisationProfileServiceID = selectedServiceId,
                categoryOfServiceID = selectedServiceCategoryId,
                typeOfServiceID = selectedServiceTypeId,
                requestedServiceFrequency = selectedServiceFrequency
                )
            launchPlaceRequestDialog(bookService)
//
        }

        bottomSheetFacilityPhoneImage.setOnClickListener {
            // navigate to phone call
            val dialIntent = Intent(Intent.ACTION_DIAL)
            //dialIntent.data = Uri.fromParts("tel",phoneNumber,null)
            dialIntent.data = Uri.fromParts("tel", facility.organisationContactNumber, null)
            startActivity(dialIntent)

        }

        bottomSheetFacilityDirectionImage.setOnClickListener {
            // navigate to maps direction
        }

        bottomSheetFacilityEmailImage.setOnClickListener {
            // navigate to email
        }

    }

    private fun launchPlaceRequestDialog(bookService: BookService) {
        val builder =
            layoutInflater.inflate(
                R.layout.custom_place_request_dialog_layout,
                null
            )
        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

//        val tvServiceType =
//            builder.findViewById<TextView>(R.id.tv_service_type)
//        val tvServiceSpecialistName =
//            builder.findViewById<TextView>(R.id.tv_service_specialist_name)
        val tvServiceName =
            builder.findViewById<TextView>(R.id.tv_service_name)
//        val tvServiceDetails =
//            builder.findViewById<TextView>(R.id.tv_service_details)
        val tvServicePrice =
            builder.findViewById<TextView>(R.id.tv_service_price)
        val tvServiceDiscountPrice =
            builder.findViewById<TextView>(R.id.tv_service_discount_price)
        val tvAvailablePlaces =
            builder.findViewById<TextView>(R.id.tv_available_places)
        val etServiceStartDate =
            builder.findViewById<TextView>(R.id.service_start_date)
        val tvRequestText =
            builder.findViewById<TextView>(R.id.tv_request_text)
        val btnSubmitRequest =
            builder.findViewById<TextView>(R.id.submit_request)

        val service = getService(bookService.organisationProfileServiceID)!!

        tvServiceName.text = resources.getString(
            R.string.service_name,
            service.organisationOfferedServiceName
        )
        tvServicePrice.text =
            resources.getString(R.string.service_price, service.organisationOfferedServicePrice)
        tvServiceDiscountPrice.text = resources.getString(
            R.string.service_discount_price,
            service.serviceDiscountedPrice
        )
        tvAvailablePlaces.text = resources.getString(
            R.string.available_places,
            service.serviceAvailability
        )

        etServiceStartDate.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                showDatePicker(view)
            }
        }
        var serviceStartDate = ""


        val client = getUser(auth.uid!!)!!
        Log.d(TAG, "launchPlaceRequestDialog: $client")
        val clientName = "${client!!.customerFirstName} ${client.customerLastName}"
        val clientPhoneNumber = client.customerMobileNumber
        val clientEmailAddress = client.customerEmail
        val dateCreated = getDate(System.currentTimeMillis(), "dd-MM-yyyy")
        val timeCreated = getDate(System.currentTimeMillis(), "hh:mm a")

        etServiceStartDate.addTextChangedListener {
            serviceStartDate = it.toString()
            tvRequestText.visible(serviceStartDate.isNotEmpty())

            tvRequestText.text = resources.getString(
                R.string.request_text,
                clientName,
                clientPhoneNumber,
                clientEmailAddress,
                tvServiceName.text.toString(),
                serviceStartDate,
                bookService.requestedServiceFrequency,
                dateCreated,
                timeCreated
            )

            btnSubmitRequest.apply {
                enable(tvRequestText.isVisible)
                setOnClickListener {
                    bookService.requestStatus = "pending"
                    bookService.requestedStartDate = serviceStartDate
                    bookService.requestFormText = tvRequestText.text.toString()
                    bookService.dateCreated = dateCreated
                    bookService.timeCreated = timeCreated

                    Log.d("EQUA", "launchPlaceRequestDialog: $bookService")
                    bookAppointment(bookService, dialog)
                }
            }
        }

        dialog.show()

    }


    private fun showDatePicker(view: View) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Update the selected date in the calendar instance
                calendar.set(selectedYear, selectedMonth, selectedDay)

                // Perform any desired action with the selected date
                // For example, update a TextView with the selected date
                val formattedDate =
                    SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(calendar.time)
                val bookAppointmentDate = view as TextInputEditText
                bookAppointmentDate.setText(formattedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun showTimePicker(view: View) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog =
            TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                // Update the selected time in the calendar instance
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)

                // Perform any desired action with the selected time
                // For example, update a TextView with the selected time
                val formattedTime =
                    SimpleDateFormat("HH:mm a", Locale.getDefault()).format(calendar.time)
                val bookAppointmentTime = view as TextInputEditText
                bookAppointmentTime.setText(formattedTime)
            }, hour, minute, false)

        timePickerDialog.show()
    }

    private fun bookAppointment(bookService: BookService, dialog: AlertDialog) {
        requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                //val appointmentQuery = Common.facilityCollectionRef.whereEqualTo("facilityId", facility.facilityId).get().await()
                Common.appointmentsCollectionRef.document(bookService.requestFormId)
                    .set(bookService).addOnSuccessListener {
                        hideProgress()
                        dialog.dismiss()
                        requireContext().toast("Service Booked")
                        dismiss()
                    }.addOnFailureListener { e ->
                        hideProgress()
                        requireContext().toast(e.message.toString())
                    }
                //dismiss bottom sheet

            } catch (e: Exception) {
                Log.d(TAG, "bookAppointment: ${e.message.toString()}")
            }
        }
    }

    private fun getFacilityServiceDetails(facilityId: String) {
        requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("EQUA", "getFacilityServiceDetails: $facilityId")

            Common.servicesCollectionRef
                .get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                    for (document in querySnapshot.documents) {
                        val item = document.toObject(Service::class.java)

                        if (item != null && item.organisationID == facilityId) {
                            facilityServices.add(item)
                        }

                        Log.d("EQUA", "getFacilityServiceDetails: $facilityServices")

                    }
                    Log.d("EQUA", "getFacilityServiceDetails: $facilityServices")
                    for (service in facilityServices) {
                        facilityServicesCategories.add(getServiceCategory(service.categoryOfServiceID)!!)
                    }
                    for (category in facilityServicesCategories)
                        facilityServicesCategoriesNames.add(category.serviceCategoryName)
                    //getFacilitySpecialistsDetails(facilityId)
                    loadView()
                    hideProgress()
                    //hideProgress()
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
        Log.d(TAG, "getService: $service")

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
        private const val ARG_FACILITY_DATA = "arg_facility_data"

        fun newInstance(facility: Facility): MapFacilityDetailBottomSheet {
            val fragment = MapFacilityDetailBottomSheet()
            val args = Bundle().apply {
                putParcelable(ARG_FACILITY_DATA, facility)

            }
            fragment.arguments = args
            return fragment
        }
    }
}