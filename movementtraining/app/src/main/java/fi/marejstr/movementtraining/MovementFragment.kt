package fi.marejstr.movementtraining

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import fi.marejstr.movementtraining.adapters.MovementAdapter
//import fi.marejstr.movementtraining.adapters.MovementInfoClickListener
import fi.marejstr.movementtraining.adapters.MovementRecordClickListener
import fi.marejstr.movementtraining.databinding.FragmentMovementBinding


class MovementFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = DataBindingUtil.inflate<FragmentMovementBinding>(
            inflater, R.layout.fragment_movement, container, false
        )

        setHasOptionsMenu(true)

        val movementAdapter = MovementAdapter(
            /*
            MovementInfoClickListener {
                mainViewModel.currentMovement.value = it
                findNavController().navigate(R.id.action_movementFragment_to_recordFragment)
            },
             */
            MovementRecordClickListener {
                mainViewModel.currentMovement.value = it
                //mainViewModel.resetRecordFragmentVariables()
                findNavController().navigate(R.id.action_movementFragment_to_recordFragment)

                //mainViewModel.recordMovement(it)
                /*
                val text = it.movementName
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(requireContext(), text, duration)
                toast.show()
                */
            }
        )
        movementAdapter.movementList = mainViewModel.movements.value!!
        binding.movementRecyclerView.adapter = movementAdapter

        mainViewModel.movements.observe(viewLifecycleOwner) {
            it?.let {
                movementAdapter.movementList = it
            }
        }

        // handle permission request callback
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    mainViewModel.saveFile()
                }
                else {
                    // TODO toast
                }
            }

        mainViewModel.saveButtonEnabled.observe(viewLifecycleOwner) {
            it?.let {
                binding.saveButton.isEnabled = it
            }
        }

        binding.saveButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mainViewModel.saveFile()
            } else {
                requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }


        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //return NavigationUI.onNavDestinationSelected(item!!, requireView().findNavController()) || super.onOptionsItemSelected(item)
        return when (item.itemId) {
            R.id.action_help -> {
                findNavController().navigate(R.id.action_movementFragment_to_helpFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }
}
