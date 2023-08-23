package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.facility.facilityservicespecialist

import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.databinding.FragmentFacilityAddServiceSpecialistBinding
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Specialists
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.REQUEST_PERMISSION
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.SPECIALISTS
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.auth
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.snackBar
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FacilityAddServiceSpecialist : Fragment() {

    private var _binding: FragmentFacilityAddServiceSpecialistBinding? = null
    private val binding get() = _binding!!

    private lateinit var serviceSpecialistServiceType: String
    private lateinit var serviceSpecialistName: String
    private lateinit var serviceSpecialistQualification: String
    private lateinit var serviceID: String

    private var fileUri: Uri? = null;

    //val REQUEST_IMAGE_PICK = 1

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentFacilityAddServiceSpecialistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            val newServiceSpecialistCategoryArray =
                resources.getStringArray(R.array.new_service_categories_list)
            val newServiceSpecialistCategoryArrayAdapter =
                ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_item,
                    newServiceSpecialistCategoryArray
                )
            facilityAddServiceSpecialistCategory.setAdapter(newServiceSpecialistCategoryArrayAdapter)

            facilityAddSpecialistImage.setOnClickListener {
                checkPermissionAndOpenPicker()
            }

            imagePickerLauncher =
                registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    if (uri != null) {
                        fileUri = uri
                        // Display the selected image in the ImageView
                        facilityAddSpecialistImage.setImageURI(uri)
                    }
                }

            facilityAddServiceSpecialistSubmitButton.setOnClickListener {
                serviceSpecialistServiceType =
                    facilityAddServiceSpecialistCategory.text.toString().trim()
                serviceSpecialistName = facilityAddServiceSpecialistName.text.toString().trim()
                serviceSpecialistQualification =
                    facilityAddServiceSpecialistQualification.text.toString()


                serviceID = System.currentTimeMillis().toString()

                if (serviceSpecialistServiceType.isEmpty() || serviceSpecialistName.isEmpty() || serviceSpecialistQualification.isEmpty()) {
                    requireContext().toast(resources.getString(R.string.missing_fields))
                } else {
                    val newSpecialist = Specialists(
                        qualification = serviceSpecialistQualification,
                        name = serviceSpecialistName,
                        service = serviceSpecialistServiceType,
                        id = serviceID
                    )
                    //requireContext().toast("uploading...")
                    createServiceSpecialist(newSpecialist)
                }

            }

            facilityAddServiceNextServiceSpecialistButton.setOnClickListener {
                facilityAddServiceSpecialistCategory.text.clear()
                facilityAddServiceSpecialistName.text!!.clear()
                facilityAddServiceSpecialistQualification.text!!.clear()
                facilityAddSpecialistImage.setImageResource(R.drawable.ic_person)


                requireContext().toast("Enter new service details")
            }


        }


    }


    private fun checkPermissionAndOpenPicker() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        } else {

            openImagePicker()
        }
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    @Suppress("DEPRECATION")
    private fun createServiceSpecialist(newSpecialist: Specialists) {

        //showProgress()
        if (fileUri != null) {
            // on below line displaying a progress dialog when uploading an image.
            val progressDialog = ProgressDialog(requireContext())
            // on below line setting title and message for our progress dialog and displaying our progress dialog.
            progressDialog.setTitle("Uploading...")
            progressDialog.setMessage("Uploading specialist info..")
            progressDialog.show()

            // on below line creating a storage refrence for firebase storage and creating a child in it with
            // random uuid.
//            val ref: StorageReference = FirebaseStorage.getInstance().getReference()
//                .child(newSpecialist.id)
            // on below line adding a file to our storage.
//            ref.putFile(fileUri!!).addOnSuccessListener {
                // this method is called when file is uploaded.
                // in this case we are dismissing our progress dialog and displaying a toast message
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        Common.facilityCollectionRef.document(auth.uid!!).collection(SPECIALISTS)
                            .document(newSpecialist.id).set(newSpecialist)
                        withContext(Dispatchers.Main) {
                            with(binding) {
                                facilityAddServiceSpecialistCategory.text.clear()
                                facilityAddServiceSpecialistName.text!!.clear()
                                facilityAddServiceSpecialistQualification.text!!.clear()
                                facilityAddSpecialistImage.setImageResource(R.drawable.ic_person)

                            }
                            //hideProgress()
                            requireView().snackBar("Specialist Added")
                        }

                        //val appointmentQuery = Common.facilityCollectionRef.whereEqualTo("facilityId", facility.facilityId).get().await()

                        //dismiss bottom sheet

                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            requireContext().toast(e.message.toString())
                        }
                    }
                }


                progressDialog.dismiss()
                //Toast.makeText(applicationContext, "Image Uploaded..", Toast.LENGTH_SHORT).show()
//            }.addOnFailureListener {
//                // this method is called when there is failure in file upload.
//                // in this case we are dismissing the dialog and displaying toast message
//                progressDialog.dismiss()
//                requireContext().toast("Fail to Upload Image..")
//            }

        }
    }

//    // on below line creating a function to upload our image.
//    fun uploadImage() {
//        // on below line checking weather our file uri is null or not.
//
//    }

}