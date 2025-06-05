package fi.marejstr.movementtraining

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import fi.marejstr.movementtraining.databinding.FragmentSetupBinding

class SetupFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val TAG = "SetupFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Setup data binding
        val binding = DataBindingUtil.inflate<FragmentSetupBinding>(
            inflater, R.layout.fragment_setup, container, false
        )

        // Pass main view model to the view so that live data can be used directly
        binding.mainViewModel = mainViewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)

        mainViewModel.continueButtonEnabled.observe(viewLifecycleOwner) {
            it?.let {
                binding.contButton.isEnabled = it
            }
        }


        mainViewModel.rightSensorCheckVisible.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it){
                    binding.rightSensorCheckImage.visibility = View.VISIBLE
                    binding.rightSensorCancelImage.visibility = View.INVISIBLE
                } else {
                    binding.rightSensorCheckImage.visibility = View.INVISIBLE
                    binding.rightSensorCancelImage.visibility = View.VISIBLE
                }
            }
        })

        mainViewModel.leftSensorCheckVisible.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it){
                    binding.leftSensorCheckImage.visibility = View.VISIBLE
                    binding.leftSensorCancelImage.visibility = View.INVISIBLE
                } else {
                    binding.leftSensorCheckImage.visibility = View.INVISIBLE
                    binding.leftSensorCancelImage.visibility = View.VISIBLE
                }
            }
        })

       /*
        mainViewModel.waistSensorCheckVisible.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it){
                    binding.waistSensorCheckImage.visibility = View.VISIBLE
                    binding.waistSensorCancelImage.visibility = View.INVISIBLE
                } else {
                    binding.waistSensorCheckImage.visibility = View.INVISIBLE
                    binding.waistSensorCancelImage.visibility = View.VISIBLE
                }
            }
        })
        */

        binding.contButton.setOnClickListener {
            mainViewModel.dispose()
            findNavController().navigate(R.id.action_setupFragment_to_movementFragment)
        }

        mainViewModel.progressbarHidden.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it){
                    binding.linearProgressIndicator.hide()
                } else {
                    binding.linearProgressIndicator.show()
                }
            }
        })

        // Ask for permissions

        /*
        // handle permission request callback
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    mainViewModel.onScanClick()
                } else {
                    // TODO toast
                }
            }
        */

        val requestMultiplePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantedMap ->
                // TODO implement checking
                mainViewModel.onScanClick()
                /*
                if (grantedMap["android.permission.ACCESS_COARSE_LOCATION"] == true && grantedMap["android.permission.CAMERA"] == true){
                    Log.i(TAG, "All permissions granted")
                }else{
                    Log.i(TAG, "Error while granting permissions")
                }
                 */
            }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            mainViewModel.onScanClick()

        } else {
            requestMultiplePermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.overflow_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //return NavigationUI.onNavDestinationSelected(item!!, requireView().findNavController()) || super.onOptionsItemSelected(item)
        return when (item.itemId) {
            R.id.action_help -> {
                findNavController().navigate(R.id.action_setupFragment_to_helpFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }
}