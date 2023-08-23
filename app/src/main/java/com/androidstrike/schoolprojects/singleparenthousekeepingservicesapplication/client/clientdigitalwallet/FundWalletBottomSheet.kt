package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientdigitalwallet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*

class FundWalletBottomSheet : BottomSheetDialogFragment() {


    private val calendar = Calendar.getInstance()

    private var progressDialog: Dialog? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.client_fund_wallet_bottom_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val bottomSheetProceedButton = view.findViewById<Button>(R.id.fund_wallet_proceed)
        val bottomSheetCancelButton = view.findViewById<Button>(R.id.fund_wallet_cancel)

        bottomSheetProceedButton.setOnClickListener {
            dismiss()
        }
        bottomSheetCancelButton.setOnClickListener {
            dismiss()
        }
    }


companion object {

    fun newInstance(): FundWalletBottomSheet {
        val fragment = FundWalletBottomSheet()
//        val args = Bundle().apply {
//            putParcelable(ARG_FACILITY_DATA, facility)
//
//        }
//        fragment.arguments = args
        return fragment
    }
}
}