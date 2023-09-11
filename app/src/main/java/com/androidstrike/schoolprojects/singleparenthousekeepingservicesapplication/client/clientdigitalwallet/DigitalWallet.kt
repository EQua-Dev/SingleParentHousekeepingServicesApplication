package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientdigitalwallet

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.WalletData
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model.WalletHistory
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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
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


    private var walletHistoryAdapter: FirestoreRecyclerAdapter<WalletHistory, WalletHistoryAdapter>? =
        null

    private val TAG = "DigitalWallet"


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

        with(binding) {
            walletLayout.visible(false)
            createDigitalWallet.visible(false)
            //noWalletLayout.visible(true)
            val layoutManager = LinearLayoutManager(requireContext())
            rvWalletHistory.layoutManager = layoutManager
            rvWalletHistory.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    layoutManager.orientation
                )
            )
        }
        requireContext().showProgress()
        getWalletDetails()
        hideProgress()

        //loadWalletInfo()


    }

    private fun getWalletDetails() {
        Log.d(TAG, "getWalletDetails: ")
        val user = getUser(auth.uid!!)!!
        //requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {

            if (user.wallet.isEmpty()){
                //hideProgress()
                binding.createDigitalWallet.visible(true)
                binding.createDigitalWallet.setOnClickListener {
                    val newWallet = WalletData(
                        walletId = hashString(
                            "${auth.uid}${
                                System.currentTimeMillis().toString()
                            }"
                        ),
                        walletOwner = auth.uid!!,
                        walletBalance = "0.0"
                    )
                    createWallet(newWallet)
                    //create wallet
                }
            }else{
                //hideProgress()
                //binding.createDigitalWallet.visible(false)

                walletCollectionRef.document(user.wallet).get().addOnSuccessListener { doc ->
                    val walletInfo = doc.toObject(WalletData::class.java)
                    binding.walletLayout.visible(true)
                    binding.walletBalance.text = resources.getString(R.string.euro_money_text, walletInfo!!.walletBalance)
                    binding.fundDigitalWallet.setOnClickListener {
                        launchFundWalletDialog()
//                        val bottomSheetFragment = FundWalletBottomSheet.newInstance()
//                        bottomSheetFragment.setListener(this@DigitalWallet) // Pass the listener to the BottomSheet
//                        bottomSheetFragment.show(childFragmentManager, "bottomSheetTag")
//                        //fetchWalletDetails(walletId)
                    }
                    getWalletHistory(walletInfo.walletId)
                }
            }
//            walletCollectionRef//.document(user!!.wallet)
//                .get()
//                .addOnSuccessListener { wallets: QuerySnapshot ->
//                    hideProgress()
//                    if (wallets.isEmpty) {
//                        //no wallets
//                        //show to create new
//                        binding.createDigitalWallet.visible(true)
//                        binding.createDigitalWallet.setOnClickListener {
//                            val newWallet = WalletData(
//                                walletId = hashString(
//                                    "${auth.uid}${
//                                        System.currentTimeMillis().toString()
//                                    }"
//                                ),
//                                walletOwner = auth.uid!!,
//                                walletBalance = "0.0"
//                            )
//                            launchCreateWalletDialog(newWallet)
//                            //create wallet
//                            //createWallet(newWallet, dialog)
//                        }
//
//                    } else {
//                        //binding.noWalletLayout.visible(false)
//
//                        for (doc in wallets.documents) {
//                            val wallet = doc.toObject(WalletData::class.java)
//                            if (wallet?.walletOwner == auth.uid!!) {
//                                hasWallet = true
//
//                                binding.walletLayout.visible(true)
//                                Log.d(TAG, "getWalletDetails: $hasWallet")
//                                //binding.noWalletLayout.visible(false)
//                                fetchWalletDetails(walletId = wallet.walletId)
//                                return@addOnSuccessListener
//                            } else {
//                                //binding.noWalletLayout.visible(true)
//                                hasWallet = false
//                            }
//                        }
//                        Log.d(TAG, "getWalletDetails: $hasWallet")
//                        binding.createDigitalWallet.visible(!hasWallet)
//                        binding.createDigitalWallet.setOnClickListener {
//                            val newWallet = WalletData(
//                                walletId = hashString(
//                                    "${auth.uid}${
//                                        System.currentTimeMillis().toString()
//                                    }"
//                                ),
//                                walletOwner = auth.uid!!,
//                                walletBalance = "0.0"
//                            )
//                            launchCreateWalletDialog(newWallet)
//                            //create wallet
//                        }
//                    }
//                }
        }
    }
    private fun getWalletHistory(walletId: String) {


        val walletHistory =
            walletCollectionRef.document(walletId).collection(WALLET_HISTORY_REF).orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<WalletHistory>()
            .setQuery(walletHistory, WalletHistory::class.java).build()
        try {
            walletHistoryAdapter = object :
                FirestoreRecyclerAdapter<WalletHistory, WalletHistoryAdapter>(options) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): WalletHistoryAdapter {
                    val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_wallet_history_layout, parent, false)
                    return WalletHistoryAdapter(itemView)
                }

                override fun onBindViewHolder(
                    holder: WalletHistoryAdapter,
                    position: Int,
                    model: WalletHistory
                ) {
                    //val timeToDeliver = model.supposedTime
                    holder.walletTransactionDate.text = model.transactionDate
                    holder.walletTransactionType.text = model.transactionType
                    holder.walletTransactionReason.text = model.transactionReason
                    holder.walletTransactionAmount.text = model.transactionAmount
                }
            }

        } catch (e: Exception) {
            requireActivity().toast(e.message.toString())
        }
        walletHistoryAdapter?.startListening()
        binding.rvWalletHistory.adapter = walletHistoryAdapter


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
                        binding.createDigitalWallet.visible(false)
                        getWalletDetails()
                        //fetchWalletDetails(newWallet.walletId)

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
                        getWalletDetails()
                        dialog.dismiss()

                    }

                }
        }

    }
    private fun getUser(userId: String): Client? {
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