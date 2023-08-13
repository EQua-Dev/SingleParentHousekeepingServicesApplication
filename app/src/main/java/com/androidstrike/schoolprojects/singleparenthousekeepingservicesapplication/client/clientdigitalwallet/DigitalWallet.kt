package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.client.clientdigitalwallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.databinding.FragmentDigitalWalletBinding

class DigitalWallet : Fragment() {

    private var _binding: FragmentDigitalWalletBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDigitalWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fundDigitalWallet.setOnClickListener {
            val bottomSheetFragment = FundWalletBottomSheet.newInstance()
            bottomSheetFragment.show(childFragmentManager, "bottomSheetTag")
        }
    }
}