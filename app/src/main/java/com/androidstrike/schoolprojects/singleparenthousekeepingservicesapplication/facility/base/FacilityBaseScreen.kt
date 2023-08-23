package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.facility.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.R
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.databinding.FragmentFacilityBaseScreenBinding
import com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.utils.Common

class FacilityBaseScreen : Fragment() {

    private var _binding: FragmentFacilityBaseScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFacilityBaseScreenBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding){

            (activity as AppCompatActivity).setSupportActionBar(binding.facilityToolBar)

            val tabTitles = listOf(
                "Company Home Screen",
                "Upload New Service",
                "View Request / Create Invoice",
                "View Feedback and Ratings"
            )
            facilityToolBarDropDownMenu.setText(tabTitles[0])
            val facilityMenuArrayAdapter =
                ArrayAdapter(requireContext(), R.layout.drop_down_item, tabTitles)
            facilityToolBarDropDownMenu.setAdapter(facilityMenuArrayAdapter)
            var facilityName = ""
            binding.facilityToolBar.title = Common.facilityName



//            customToolbar = landingScreen.toolBar() as Toolbar
//            customToolbar.title = "News"

            val adapter = childFragmentManager.let {
                FacilityLandingPagerAdapter(
                    activity,
                    it,
                    //facilityBaseTabTitle.tabCount
                )
            }
            facilityLandingViewPager.adapter = adapter
            facilityToolBarDropDownMenu.setOnItemClickListener { _, _, position, _ ->
                val selectedItem =
                    facilityMenuArrayAdapter.getItem(position) // Get the selected item
                facilityLandingViewPager.currentItem = position
            }

        }

    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_logout, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout -> {
                Common.auth.signOut()
                val navToStart =
                    FacilityBaseScreenDirections.actionFacilityBaseScreenToLandingFragment()
                findNavController().navigate(navToStart)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}