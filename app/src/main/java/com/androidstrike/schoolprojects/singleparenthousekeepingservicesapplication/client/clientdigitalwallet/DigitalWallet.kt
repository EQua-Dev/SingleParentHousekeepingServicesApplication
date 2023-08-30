package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientdigitalwallet

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.WalletData
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.WalletHistory
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.databinding.FragmentDigitalWalletBinding
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.Client
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.DATE_FORMAT_LONG
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.REASON_ACCOUNT_FUND
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.WALLET_HISTORY_REF
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.auth
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.clientCollectionRef
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common.walletCollectionRef
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.enable
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.getDate
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.hashString
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.hideProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.showProgress
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.toast
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.visible
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DigitalWallet : Fragment() {

    private var _binding: FragmentDigitalWalletBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDigitalWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadWalletInfo()


    }

    private fun loadWalletInfo() {
        //checkWalletAvailability()
        binding.createDigitalWallet.apply {
            visible(getUser(auth.uid!!)!!.wallet.isEmpty())
            setOnClickListener {

                val newWallet = WalletData(
                    walletId = hashString(
                        "${auth.uid}${
                            System.currentTimeMillis()
                        }"
                    ),
                    walletOwner = auth.uid!!,
                    walletBalance = "0.0"
                )
                //create wallet
                createWallet(newWallet)
            }
        }

        binding.fundDigitalWallet.apply {
            visible(getUser(auth.uid!!)!!.wallet.isNotEmpty())
            setOnClickListener {
                launchFundWalletDialog()
//            val bottomSheetFragment = FundWalletBottomSheet.newInstance()
//            bottomSheetFragment.show(childFragmentManager, "bottomSheetTag")
            }
        }

    }

    private fun launchFundWalletDialog() {

        val builder =
            layoutInflater.inflate(R.layout.client_fund_wallet_bottom_layout, null)

        val bottomSheetAmountField =
            builder.findViewById<TextInputEditText>(R.id.fund_digital_wallet_amount)
        val bottomSheetProceedButton = builder.findViewById<Button>(R.id.fund_wallet_proceed)
        val bottomSheetCancelButton = builder.findViewById<Button>(R.id.fund_wallet_cancel)


        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(true)
            .create()

        bottomSheetProceedButton.enable(false)

        bottomSheetAmountField.addTextChangedListener { amount ->
            val fundAmount = amount.toString().trim()
            bottomSheetProceedButton.apply {
                enable(fundAmount.isNotEmpty())
                setOnClickListener {
                    fundWallet(fundAmount, dialog)
                }
            }

        }

        bottomSheetCancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun createWallet(newWallet: WalletData) {
        requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                walletCollectionRef.document(newWallet.walletId).set(newWallet)
                    .addOnSuccessListener {
                        clientCollectionRef.document(auth.uid!!)
                            .update("wallet", newWallet.walletId)
                        hideProgress()
                        loadWalletInfo()
                        //binding.createDigitalWallet.visible(false)
                        //binding.walletLayout.visible(true)
                        //fetchWalletDetails(newWallet.walletId)
                    }//.await()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    requireContext().toast(e.message.toString())
                }
            }
        }
    }

    private fun fundWallet(fundAmount: String, dialog: AlertDialog) {
        val walletId = getUser(auth.uid!!)!!.wallet
        val newBalance =
            getWalletInfo(walletId)!!.walletBalance.toDouble() + fundAmount.toDouble()
        CoroutineScope(Dispatchers.IO).launch {
            val walletReference =
                walletCollectionRef.document(walletId)

            val updates = hashMapOf<String, Any>(
                "walletBalance" to newBalance.toString(),
            )

            walletReference.update(updates)
                .addOnSuccessListener {
                    //hideProgress()
                    //launch dialog to enter picker details
                    //update wallet transaction
                    CoroutineScope(Dispatchers.IO).launch {
                        val walletHistoryReference =
                            walletCollectionRef.document(walletId).collection(
                                WALLET_HISTORY_REF
                            )
                        val walletTransaction = WalletHistory(
                            transactionDate = getDate(
                                System.currentTimeMillis(),
                                DATE_FORMAT_LONG
                            ),
                            transactionType = "CR",
                            transactionAmount = resources.getString(
                                R.string.money_text,
                                fundAmount
                            ),
                            transactionReason = REASON_ACCOUNT_FUND
                        )

                        walletHistoryReference.document(
                            System.currentTimeMillis().toString()
                        ).set(walletTransaction).await()
                        //fetchWalletDetails(walletId)

                        dialog.dismiss()

                    }

                }
        }

    }
    private fun getUser(userId: String): Client? {
        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = clientCollectionRef.document(userId).get().await()
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

        val clientUser = runBlocking { deferred.await() }
        hideProgress()

        return clientUser
    }
    private fun getWalletInfo(walletId: String): WalletData? {

        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = walletCollectionRef.document(walletId).get().await()
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

        val walletInfo = runBlocking { deferred.await() }
        hideProgress()

        return walletInfo
    }


}