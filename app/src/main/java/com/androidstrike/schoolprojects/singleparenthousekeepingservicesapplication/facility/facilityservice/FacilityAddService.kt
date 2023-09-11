package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.facility.facilityservice

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.databinding.FragmentFacilityAddServiceBinding
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Service
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.ServiceCategory
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.ServiceType
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.auth
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.serviceCategoryCollectionRef
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.serviceTypeCollectionRef
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.hideProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.serviceSnackBar
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.showProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.showProgressDialog
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.snackBar
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.toast
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FacilityAddService : Fragment() {

    private var _binding: FragmentFacilityAddServiceBinding? = null
    private val binding get() = _binding!!

    private lateinit var servicePrice: String
    private lateinit var serviceName: String
    private lateinit var serviceDetails: String
    private lateinit var serviceTypeID: String
    private lateinit var serviceCategoryID: String
    private lateinit var serviceDiscountedPrice: String
    private lateinit var serviceAvailablePlacesOption: String
    private lateinit var serviceFrequency: String

    private var serviceFrequency1 = ""
    private var serviceFrequency2 = ""
    private var serviceFrequency3 = ""
    private var serviceFrequency4 = ""
    private var serviceFrequency5 = ""

    private var serviceFrequencies = mutableListOf<String>()

    private var serviceTypesList: MutableList<ServiceType> = mutableListOf()
    private var serviceTypesNamesList: MutableList<String> = mutableListOf()
    private var serviceCategoriesList: MutableList<ServiceCategory> = mutableListOf()
    private var serviceCategoriesNamesList: MutableList<String> = mutableListOf()

    private val calendar = Calendar.getInstance()

    private var progressDialog: Dialog? = null

    private val TAG = "FacilityAddService"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentFacilityAddServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getServiceCategories()

    }

    private fun loadView() {
        with(binding) {

//            val newServiceCategoryArray =
//                listOf("Cleaning", "Deliveries")
            val newServiceCategoryArrayAdapter =
                ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_item,
                    serviceCategoriesNamesList
                )

            facilityAddNewServiceCategory.setAdapter(newServiceCategoryArrayAdapter)

//
            facilityAddNewServiceCategory.setOnItemClickListener { _, _, position, _ ->
                serviceTypesNamesList.clear()
                var selectedCategoryId = ""
                for (category in serviceCategoriesList) {
                    if (facilityAddNewServiceCategory.text.toString() == category.serviceCategoryName) {
                        selectedCategoryId = category.serviceCategoryID
                    }
                }

                Log.d(TAG, "loadView: $selectedCategoryId")
                for (serviceType in serviceTypesList)
                    if (serviceType.serviceCategoryID == selectedCategoryId)
                        serviceTypesNamesList.add(serviceType.serviceTypeName)

                Log.d(TAG, "loadView: $serviceTypesList")
                Log.d(TAG, "loadView: $serviceTypesNamesList")
                val newServiceTypesArrayAdapter =
                    ArrayAdapter(
                        requireContext(),
                        R.layout.drop_down_item,
                        serviceTypesNamesList
                    )
                facilityAddServiceType.setAdapter(newServiceTypesArrayAdapter)

            }

            handleCheckBox(facilityAddServiceFrequencyCb1)
            handleCheckBox(facilityAddServiceFrequencyCb2)
            handleCheckBox(facilityAddServiceFrequencyCb3)
            handleCheckBox(facilityAddServiceFrequencyCb4)
            handleCheckBox(facilityAddServiceFrequencyCb5)

            val newServiceAvailablePlacesArray =
                resources.getStringArray(R.array.service_available_places_option)
            val newServiceAvailablePlacesArrayAdapter =
                ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_item,
                    newServiceAvailablePlacesArray
                )
            facilityAddServiceAvailablePlaces.setAdapter(newServiceAvailablePlacesArrayAdapter)


            facilityAddServiceSubmitButton.setOnClickListener {
                for (category in serviceCategoriesList)
                    if (facilityAddNewServiceCategory.text.toString()
                            .trim() == category.serviceCategoryName
                    )
                        serviceCategoryID = category.serviceCategoryID
                for (type in serviceTypesList)
                    if (facilityAddServiceType.text.toString().trim() == type.serviceTypeName)
                        serviceTypeID = type.serviceTypeID


                servicePrice = facilityAddServicePrice.text.toString().trim().ifEmpty { "0" }
                serviceName = facilityAddServiceName.text.toString().trim()
                serviceDiscountedPrice =
                    facilityAddServiceDiscountedPrice.text.toString().trim().ifEmpty { "0" }
                serviceAvailablePlacesOption =
                    facilityAddServiceAvailablePlaces.text.toString().trim().ifEmpty { "No" }
                serviceDetails = facilityAddServiceDetails.text.toString().trim()

                validateInputs()
            }

            facilityAddServiceNextServiceButton.setOnClickListener {
                facilityAddNewServiceCategory.text.clear()
                facilityAddServiceType.text!!.clear()
                facilityAddServiceName.text!!.clear()
                facilityAddServicePrice.text!!.clear()
                facilityAddServiceDiscountedPrice.text!!.clear()
                facilityAddServiceAvailablePlaces.text.clear()
                facilityAddServiceFrequencyCb1.isChecked = false
                facilityAddServiceFrequencyCb2.isChecked = false
                facilityAddServiceFrequencyCb3.isChecked = false
                facilityAddServiceFrequencyCb4.isChecked = false
                facilityAddServiceFrequencyCb5.isChecked = false
                facilityAddServiceDetails.text!!.clear()

                requireContext().toast("Enter new service details")
            }


        }

    }

    private fun validateInputs() {
        with(binding) {
            textInputLayoutFacilityAddNewServiceCategory.error = null
            textInputLayoutFacilityAddServiceName.error = null
            textInputLayoutFacilityAddServiceDetails.error = null
            textInputLayoutFacilityAddServiceAvailablePlaces.error = null

            if (serviceTypeID.isEmpty()) {
                textInputLayoutFacilityAddNewServiceCategory.error = "Select Service Type"
                facilityAddNewServiceCategory.requestFocus()
                return
            }
//            if (servicePrice.isEmpty()) {
//                textInputLayoutFacilityAddServicePrice.error = "Enter Service Price"
//                facilityAddServicePrice.requestFocus()
//            }
            if (serviceName.isEmpty()) {
                textInputLayoutFacilityAddServiceName.error = "Enter Service Description"
                facilityAddServiceName.requestFocus()
                return
            }
            if (serviceDetails.isEmpty()) {
                textInputLayoutFacilityAddServiceDetails.error =
                    "Enter Service Details"
                facilityAddServiceDetails.requestFocus()
                return
            }
            if (serviceAvailablePlacesOption.isEmpty()) {
                textInputLayoutFacilityAddServiceAvailablePlaces.error =
                    "Select Service Available Places Option"
                facilityAddServiceAvailablePlaces.requestFocus()
                return
            } else {
                textInputLayoutFacilityAddNewServiceCategory.error = null
                textInputLayoutFacilityAddServiceName.error = null
                textInputLayoutFacilityAddServiceDetails.error = null
                textInputLayoutFacilityAddServiceAvailablePlaces.error = null

                val newService = Service(

                    organisationProfileServiceID = System.currentTimeMillis().toString(),
                    organisationID = auth.uid!!,
                    categoryOfServiceID = serviceCategoryID,
                    organisationOfferedServiceName = serviceName,
                    typeOfServiceID = serviceTypeID,
                    organisationOfferedServiceDetails = serviceDetails,
                    serviceDiscountedPrice = serviceDiscountedPrice,
                    organisationOfferedServicePrice = servicePrice,
                    serviceAvailability = serviceAvailablePlacesOption,
                    organisationServiceFrequency = serviceFrequencies,
                )
                createService(newService)
            }

        }
    }
    private fun handleCheckBox(checkBox: MaterialCheckBox){
        var checkedItem = ""
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            checkedItem = checkBox.text.toString()
            if (isChecked) {
                serviceFrequencies.add(checkedItem)
            } else {
                serviceFrequencies.remove(checkedItem)

            }
        }
    }
    private fun createService(newService: Service) {
        requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            try {

                //val appointmentQuery = Common.facilityCollectionRef.whereEqualTo("facilityId", facility.facilityId).get().await()
                Common.servicesCollectionRef.document(newService.organisationProfileServiceID).set(newService).await()

                withContext(Dispatchers.Main) {
                    with(binding) {
                        facilityAddNewServiceCategory.text.clear()
                        facilityAddServiceName.text!!.clear()
                        facilityAddServiceDetails.text!!.clear()
                        facilityAddServicePrice.text!!.clear()
                        facilityAddServiceType.text!!.clear()
                        facilityAddServiceDiscountedPrice.text!!.clear()
                        facilityAddServiceAvailablePlaces.text.clear()
                        facilityAddServiceFrequencyCb1.isChecked = false
                        facilityAddServiceFrequencyCb2.isChecked = false
                        facilityAddServiceFrequencyCb3.isChecked = false
                        facilityAddServiceFrequencyCb4.isChecked = false
                        facilityAddServiceFrequencyCb5.isChecked = false                    }
                    hideProgress()
                    requireView().serviceSnackBar("Service Added")
                }
                //dismiss bottom sheet

            } catch (e: Exception) {
                requireContext().toast(e.message.toString())
            }
        }
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
                val serviceScheduleStartDate = view as TextInputEditText
                serviceScheduleStartDate.setText(formattedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun getServiceCategories() {
        requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            serviceCategoryCollectionRef.get().addOnSuccessListener { doc: QuerySnapshot ->
                hideProgress()
                if (doc.isEmpty) {
                    requireContext().toast("No Service Types in Database")
                } else {

                    for (item in doc.documents) {
                        val serviceCategory = item.toObject(ServiceCategory::class.java)
                        serviceCategoriesList.add(serviceCategory!!)
                    }
                    for (serviceCategory in serviceCategoriesList)
                        serviceCategoriesNamesList.add(serviceCategory.serviceCategoryName)
                }
                getServiceType()

            }
        }
    }

    private fun getServiceType() {
        requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            serviceTypeCollectionRef.get().addOnSuccessListener { doc: QuerySnapshot ->
                hideProgress()
                if (doc.isEmpty) {
                    requireContext().toast("No Service Types in Database")
                } else {

                    for (item in doc.documents) {
                        val serviceType = item.toObject(ServiceType::class.java)
                        serviceTypesList.add(serviceType!!)
                        Log.d(TAG, "getServiceType: $serviceType")
                    }
//                    for (serviceType in serviceTypesList)
//                        serviceTypesNamesList.add(serviceType.serviceTypeName)
                }
                loadView()

            }
        }
    }


}