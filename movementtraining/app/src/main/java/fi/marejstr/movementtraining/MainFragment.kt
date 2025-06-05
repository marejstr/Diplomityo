package fi.marejstr.movementtraining

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import fi.marejstr.movementtraining.adapters.DeviceListener
import fi.marejstr.movementtraining.adapters.ScanResultAdapter
import fi.marejstr.movementtraining.databinding.FragmentMainBinding


class MainFragment : Fragment() {

    // TODO check if this is ok way to do get activity AndroidViewModel, possible memory leak?
    private val mainViewModel: MainViewModel by activityViewModels()
    private val TAG = "MainFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Setup data binding
        val binding = DataBindingUtil.inflate<FragmentMainBinding>(
            inflater, R.layout.fragment_main, container, false
        )

        /*
        // Setup ui click listeners and adapters
        binding.guideButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_helpFragment)
        }
        */

        binding.continueButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_setupFragment)
        }

        /*
        val adapter = ScanResultAdapter(DeviceListener {
            //macAdd -> mainViewModel.connectTo(macAdd)
        })
        binding.recyclerView1.adapter = adapter

        // Update sensor card status texts when status changes in main view model
        mainViewModel.scanData.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.dataSet = it
            }
        })
        */


        return binding.root
    }
}