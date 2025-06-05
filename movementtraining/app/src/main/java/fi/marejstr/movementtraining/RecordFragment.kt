package fi.marejstr.movementtraining

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices
import fi.marejstr.movementtraining.databinding.FragmentRecordBinding

class RecordFragment : Fragment() {

    // TODO stop subscription when user navigates away

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<FragmentRecordBinding>(
            inflater, R.layout.fragment_record, container, false)

        binding.mainViewModel = mainViewModel
        binding.lifecycleOwner = this

        binding.movementNameText.text = mainViewModel.currentMovement.value?.movementName ?: "Error"

        binding.imageView.setImageResource(mainViewModel.currentMovement.value?.movementDrawable ?: R.drawable.ic_launcher_foreground);

        mainViewModel.recordButtonText.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.recordButton.text = it
            }
        })

        mainViewModel.timestampText[0].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.timestamp0Text.text = it
            }
        })

        mainViewModel.timestampText[1].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.timestamp1Text.text = it
            }
        })

        /*
        mainViewModel.timestampText[2].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.timestamp2Text.text = it
            }
        })

         */


        /*
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

        binding.getTimeButton.setOnClickListener {
            //mainViewModel.getTimes()
            mainViewModel.getTimeDetails()
        }

        binding.setTimeButton.setOnClickListener {
            mainViewModel.setSensorTimes()
        }

        mainViewModel.toggleButtonText.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.toggleSubscriptionButton.text = it
            }
        })
        mainViewModel.accelerometerText[0].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.accData1Text.text = it
            }
        })
        mainViewModel.gyrometerText[0].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.gyroData1Text.text = it
            }
        })
        mainViewModel.magnetometerText[0].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.magData1Text.text = it
            }
        })
        mainViewModel.accelerometerText[1].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.accData2Text.text = it
            }
        })
        mainViewModel.gyrometerText[1].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.gyroData2Text.text = it
            }
        })
        mainViewModel.magnetometerText[1].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.magData2Text.text = it
            }
        })
        mainViewModel.accelerometerText[2].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.accData3Text.text = it
            }
        })
        mainViewModel.gyrometerText[2].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.gyroData3Text.text = it
            }
        })
        mainViewModel.magnetometerText[2].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.magData3Text.text = it
            }
        })

        mainViewModel.timeText[0].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.time1Text.text = it
            }
        })

        mainViewModel.timeText[1].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.time2Text.text = it
            }
        })

        mainViewModel.timeText[2].observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.time3Text.text = it
            }
        })
         */

        return binding.root
    }
}