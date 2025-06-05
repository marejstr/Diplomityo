package fi.marejstr.movementtraining

import android.app.Application
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.movesense.mds.*
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices
import com.movesense.mds.internal.connectivity.MovesenseDevice
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import fi.marejstr.movementtraining.gson.ImuDataResponse
import fi.marejstr.movementtraining.gson.TimeResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

// IMPORTANT

// TODO Better save file handling
    // TODO Show toast for file save error or success and pop everything and navigate to main screen
    // TODO Disconnect sensors and go to start

// TODO streamline app usage / improve UI
    // TODO add warning if sensors drop during recording
    // TODO implement person id
    // TODO move hardcoded UI strings to strings file
// TODO fix permissions for all SDK versions

// UI

// TODO better receive data indicator in record movement fragment
// TODO add pictures of movements
// TODO better save file scroll behavior in movement list

// LESS IMPORTANT / LATER

// TODO Improve UI for device connections
// TODO Make it possible to disconnect from sensors
    // TODO Disconnect automatically when saving is complete
    // TODO Disconnect from sensors / dispose subscriptions when you exit app
// TODO Better ble device connection handling
    // TODO what happens if device bluetooth is turned off?
    // TODO Handle disconnect of sensors
// TODO make it possible to change sensor mac

// TODO Keep screen on while app is running?
// TODO Add settings menu
// TODO Add info to help screen

// AndroidViewModel instead of ViewModel because the application context is needed
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // ----- General -----

    private val TAG = "MainViewModel"

    // ----- Setup Fragment -----

    private val rxBleDevices: MutableList<RxBleDevice> = mutableListOf()
    private var mBleClient = RxBleClient.create(getApplication<Application>().applicationContext)
    private var scanDisposable: Disposable? = null
    // init MDS library (interface for communicating with Movesense sensor)
    private var mMds = Mds.builder().build(getApplication<Application>().applicationContext)

    // Live data for main fragment
    private val scanData = MutableLiveData<List<ScanResult>>(listOf())
    val leftLegSensorStatusText = MutableLiveData<String>("Not connected")
    val rightLegSensorStatusText = MutableLiveData<String>("Not connected")
    val waistSensorStatusText = MutableLiveData<String>("Not connected")

    var progressbarHidden = MutableLiveData<Boolean>(false)
    val sensorScanStatusText = MutableLiveData<String>("")
    var continueButtonEnabled = MutableLiveData<Boolean>(true)
    var sensorTimeSetSuccessCount = 0
    var sensorTimeGetSuccessCount = 0
    var allSensorsSetup = false

    private val _rightSensorCheckVisible = MutableLiveData<Boolean>(false)
    val rightSensorCheckVisible: LiveData<Boolean>
        get() = _rightSensorCheckVisible

    private val _leftSensorCheckVisible = MutableLiveData<Boolean>(false)
    val leftSensorCheckVisible: LiveData<Boolean>
        get() = _leftSensorCheckVisible

    private val _waistSensorCheckVisible = MutableLiveData<Boolean>(false)
    val waistSensorCheckVisible: LiveData<Boolean>
        get() = _waistSensorCheckVisible

    private val sensorBootTime: MutableList<Long> = mutableListOf(0, 0, 0)
    private val sensorUtcTime: MutableList<Long> = mutableListOf(0, 0, 0)

    // ----- Movement Fragment -----

    private val movementNameList = listOf(
        Pair("Jalkanosto vasen", R.drawable.ic_launcher_background),
        Pair("Jalkanosto oikea", R.drawable.ic_launcher_background),
        Pair("Polvinosto vasen", R.drawable.ic_launcher_background),
        Pair("Polvinosto oikea", R.drawable.ic_launcher_background),
        Pair("Jalka taakse vasen", R.drawable.ic_launcher_background),
        Pair("Jalka taakse oikea", R.drawable.ic_launcher_background),
        Pair("Jalka sivulle vasen", R.drawable.ic_launcher_background),
        Pair("Jalka sivulle oikea", R.drawable.ic_launcher_background),
        Pair("Varpaille", R.drawable.ic_launcher_background),
        Pair("Seiso ylös", R.drawable.ic_launcher_background),
        Pair("Not movement inactive", R.drawable.ic_launcher_background),
        Pair("Not movement active", R.drawable.ic_launcher_background),
    )

    private fun createMovementList(): List<Movement> {
        val movementList = mutableListOf<Movement>()

        for (movementPair in movementNameList) {
            movementList.add(Movement(movementPair.first, movementPair.second))
        }

        return movementList
    }

    val movements = MutableLiveData(createMovementList())
    var saveButtonEnabled = MutableLiveData<Boolean>(true)

    // ----- Record Fragment -----

    val currentMovement = MutableLiveData<Movement>()

    // Shared for all record fragments
    private val SAMPLE_RATE = 52
    private val URI_IMU9 = "Meas/IMU9/${SAMPLE_RATE}"
    private val URI_EVENTLISTENER = "suunto://MDS/EventListener"

    //private val WAIST_MAC = "0C:8C:DC:35:24:42"
    private val LEFT_LEG_MAC = "0C:8C:DC:36:0D:FE"
    private val RIGHT_LEG_MAC = "0C:8C:DC:38:4E:F1"

    // Specific for each record fragment

    // Resets when record fragment changes
    private val sensorTextLastUpdateTime: MutableList<Long> = mutableListOf(0, 0, 0)
    private var prevNotificationImuData: MutableList<ImuDataResponse?> =
        mutableListOf(null, null, null)
    private var recording = false

    // Subscriptions to movesense sensor data
    private var mdsSubscriptionLeft: MdsSubscription? = null
    private var mdsSubscriptionRight: MdsSubscription? = null
    // private var mdsSubscriptionWaist: MdsSubscription? = null

    private var leftSensorIndex: Int = 0
    private var rightSensorIndex: Int = 0
    // private var waistSensorIndex: Int = 0

    // Has user indicated that there was a pause between movement repetitions
    private var movementStart: Boolean = false

    val recordButtonText = MutableLiveData<String>("Start recording")
    val repCount = MutableLiveData<Int>(0)

    val timestampText: List<MutableLiveData<String>> =
        listOf(MutableLiveData<String>(), MutableLiveData<String>(), MutableLiveData<String>())
    /*
    val gyrometerText: List<MutableLiveData<String>> =
        listOf(MutableLiveData<String>(), MutableLiveData<String>(), MutableLiveData<String>())
    val magnetometerText: List<MutableLiveData<String>> =
        listOf(MutableLiveData<String>(), MutableLiveData<String>(), MutableLiveData<String>())
    val timeText: List<MutableLiveData<String>> =
        listOf(MutableLiveData<String>(), MutableLiveData<String>(), MutableLiveData<String>())

     */
    // Does not need to be reset

    // ----- Setup Fragment -----

    // Called when the scan button is clicked
    fun onScanClick() {
        if (!allSensorsSetup) {
            progressbarHidden.value = false
            continueButtonEnabled.value = false
            sensorScanStatusText.value = "Setting up sensors"
            rightLegSensorStatusText.value = "Scanning..."
            leftLegSensorStatusText.value = "Scanning..."
            waistSensorStatusText.value = "Scanning..."

            scanData.value = mutableListOf()
            scanBleDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { dispose() }
                .subscribe({ addScanResult(it) }, { onScanFailure(it) })
                .let { scanDisposable = it }
        }
    }

    // Settings for ble scan
    private fun scanBleDevices(): Observable<ScanResult> {
        /*
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        val scanFilter = ScanFilter.Builder().build()

        return mBleClient.scanBleDevices(scanSettings, scanFilter)

         */
        val scanSettings = ScanSettings.Builder().build()
        val scanFilter = ScanFilter.Builder().build()
        return mBleClient.scanBleDevices(scanSettings, scanFilter)
    }

    // Called from onScanClick() when a new bleDevice is found
    private fun addScanResult(bleScanResult: ScanResult) {
        val device: RxBleDevice = bleScanResult.bleDevice
        if (device.name?.contains("movesense", ignoreCase = true) == true) {
            if (!rxBleDevices.contains(device)) {
                when (bleScanResult.bleDevice.macAddress) {
                    LEFT_LEG_MAC -> {
                        rxBleDevices.add(bleScanResult.bleDevice)
                        mMds.connect(LEFT_LEG_MAC, mdsConnListener)
                        leftLegSensorStatusText.value = "Connecting..."
                    }
                    RIGHT_LEG_MAC -> {
                        rxBleDevices.add(bleScanResult.bleDevice)
                        mMds.connect(RIGHT_LEG_MAC, mdsConnListener)
                        rightLegSensorStatusText.value = "Connecting..."
                    }
                    /*
                    WAIST_MAC -> {
                        rxBleDevices.add(bleScanResult.bleDevice)
                        mMds.connect(WAIST_MAC, mdsConnListener)
                        waistSensorStatusText.value = "Connecting..."
                    }
                     */
                    else -> {
                        Log.d(TAG, "Not expected Movesense device found with name: ${device.name}")
                    }
                }
            } else {
                Log.d(TAG, "Skipping already connected Movesense device: ${device.name}")
            }
        } else {
            //Log.d(TAG, "Skipping non Movesense device: ${device.name}")
        }
    }

    // Called from onScanClick() when there is an error while scanning for ble devices
    private fun onScanFailure(throwable: Throwable) {
        Log.e("Scanning", "Scan failed")
        // TODO handle error
    }

    // Callback for rxBleDevice connections
    private val mdsConnListener = object : MdsConnectionListener {
        override fun onConnect(macAddress: String?) {
            Log.d(TAG, "onConnect:$macAddress")
        }

        override fun onConnectionComplete(macAddress: String?, serial: String?) {
            Log.d(TAG, "onConnectionComplete:$macAddress")
            //accData.value = "Connected"

            when (macAddress) {
                LEFT_LEG_MAC -> {
                    leftLegSensorStatusText.value = "Connected"
                    _leftSensorCheckVisible.value = true
                    MovesenseConnectedDevices.addRxConnectedDevice(rxBleDevices.find {
                        it.macAddress.equals(
                            macAddress
                        )
                    })
                    MovesenseConnectedDevices.addConnectedDevice(
                        MovesenseDevice(
                            macAddress,
                            "left",
                            serial,
                            "new"
                        )
                    )
                }
                RIGHT_LEG_MAC -> {
                    rightLegSensorStatusText.value = "Connected"
                    _rightSensorCheckVisible.value = true
                    MovesenseConnectedDevices.addRxConnectedDevice(rxBleDevices.find {
                        it.macAddress.equals(
                            macAddress
                        )
                    })
                    MovesenseConnectedDevices.addConnectedDevice(
                        MovesenseDevice(
                            macAddress,
                            "right",
                            serial,
                            "new"
                        )
                    )
                }
                /*
                WAIST_MAC -> {
                    waistSensorStatusText.value = "Connected"
                    _waistSensorCheckVisible.value = true
                    MovesenseConnectedDevices.addRxConnectedDevice(rxBleDevices.find {
                        it.macAddress.equals(
                            macAddress
                        )
                    })
                    MovesenseConnectedDevices.addConnectedDevice(
                        MovesenseDevice(
                            macAddress,
                            "waist",
                            serial,
                            "new"
                        )
                    )
                }
                 */
                else -> {
                    Log.e(TAG, "Connected to ble device that is not a sensor")
                }
            }

            // all 2 devices are connected
            // TODO improve
            if (MovesenseConnectedDevices.getConnectedDevices().size == 2) {
                sensorScanStatusText.value = "Synchronising sensor times"

                val connectedDeviceIterator =
                    MovesenseConnectedDevices.getConnectedDevices().iterator()

                for ((index, connectedDevice) in connectedDeviceIterator.withIndex()) {
                    when (connectedDevice.macAddress) {
                        LEFT_LEG_MAC -> {
                            leftSensorIndex = index
                        }
                        RIGHT_LEG_MAC -> {
                            rightSensorIndex = index
                        }
                        /*
                        WAIST_MAC -> {
                            waistSensorIndex = index
                        }

                         */
                    }
                }

                setSensorTimes()
            }
        }

        override fun onError(e: MdsException?) {
            Log.e(TAG, "onError:$e")
        }

        override fun onDisconnect(macAddress: String?) {
            Log.d(TAG, "onDisconnect: $macAddress")
            // TODO handle disconnects gracefully
        }
    }

    fun dispose() {
        scanDisposable?.dispose()
        //scanDisposable = null
    }

    // Stop scanning when the view model is cleared
    override fun onCleared() {
        super.onCleared()
        dispose()
    }

    fun setSensorTimes() {

        sensorTimeSetSuccessCount = 0
        sensorTimeGetSuccessCount = 0

        setSensorUtcTime(0)
        setSensorUtcTime(1)
        //setSensorUtcTime(2)
    }

    private fun setSensorUtcTime(deviceIndex: Int) {
        val currentTime: Long = (System.currentTimeMillis() * 1000)
        Mds.builder().build(getApplication<Application>().applicationContext).put(
            "suunto://${MovesenseConnectedDevices.getConnectedDevice(deviceIndex).serial}/Time",
            "{\"value\":$currentTime}",
            object : MdsResponseListener {
                override fun onSuccess(data: String) {
                    Log.d(TAG, "onSuccess: $data")
                    sensorTimeSetSuccessCount++
                    if (sensorTimeSetSuccessCount >= 2) {
                        getTimeDetails()
                    }
                }

                override fun onError(error: MdsException) {
                    Log.e(TAG, "onError()", error)
                }
            })
    }

    fun getTimeDetails() {
        getSensorTimeDetails(0)
        getSensorTimeDetails(1)
        //getSensorTimeDetails(2)
    }

    private fun getSensorTimeDetails(deviceIndex: Int) {

        Mds.builder().build(getApplication<Application>().applicationContext).get(
            "suunto://${MovesenseConnectedDevices.getConnectedDevice(deviceIndex).serial}/Time/Detailed",
            null, object : MdsResponseListener {
                override fun onSuccess(s: String) {
                    val timeData: TimeResponse = Gson().fromJson(s, TimeResponse::class.java)

                    // From milliseconds to microseconds
                    sensorBootTime[deviceIndex] = (timeData.content.utctime / 1000) - timeData.content.relativetime + (deviceIndex * 100)
                    sensorUtcTime[deviceIndex] = timeData.content.utctime / 1000

                    sensorTimeGetSuccessCount++
                    if (sensorTimeGetSuccessCount == 2) {

                        // Check that the times are close enough
                        if(abs(sensorUtcTime[0] - sensorUtcTime[1]) < 8) {
                            continueButtonEnabled.value = true
                            progressbarHidden.value = true
                            allSensorsSetup = true
                            sensorScanStatusText.value = "All sensors ready"
                        } else {
                           setSensorTimes()
                        }

                    }
                }

                override fun onError(e: MdsException) {
                    Log.e(TAG, "Info onError: ", e)
                }
            })
    }

    // ----- Movement Fragment -----

    fun saveFile() {

        val sb = StringBuilder()
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val currentDatTime: String = sdf.format(Date())
        sb.append(currentDatTime).append(".csv")

        val state = Environment.getExternalStorageState()

        if (Environment.MEDIA_MOUNTED == state) {
            val externalDirectory =
                getApplication<Application>().applicationContext.getExternalFilesDir(null)
            val appDirectory = File(externalDirectory, "Movesense")
            val logFile = File(appDirectory, sb.toString())

            // create app folder
            if (!appDirectory.exists()) {
                val status = appDirectory.mkdirs()
                Log.e(TAG, "appDirectory created: $status")
            }

            // create log file
            if (!logFile.exists()) {
                var status = false
                try {
                    status = logFile.createNewFile()
                } catch (e: IOException) {
                    Log.e(TAG, "logFile.createNewFile(): ", e)
                    e.printStackTrace()
                }
                Log.e(TAG, "logFile.createNewFile() created: $status")
            }

            try {

                val fileWriter = FileWriter(logFile)

                fileWriter.write("Timestamp,Movement,SensorPlacement,Start,AccX,AccY,AccZ,GyroX,GyroY,GyroZ,MagnX,MagnY,MagnZ\n")

                movements.value?.forEach { movement ->
                    fileWriter.write(movement.getStrings())
                }

                fileWriter.close()

                // TODO reset app when save complete
                saveButtonEnabled.value = false


            } catch (e: IOException) {
                Log.e(TAG, "Error while writing file")
            }

        } else {
            Log.e(TAG, "createFile isExternalStorageWritable Error")
        }

    }

// ----- Record Fragment -----

    fun resetRecordFragmentVariables() {
    }

    // Start and stop IMU9 subscriptions
    fun recordMovement() {
        if (recording) {
            recording = false

            recordButtonText.value = "Start recording"

            mdsSubscriptionLeft?.unsubscribe()
            mdsSubscriptionRight?.unsubscribe()
            //mdsSubscriptionWaist?.unsubscribe()

            // TODO better
            timestampText[0].value = ""
            timestampText[1].value = ""

            prevNotificationImuData = mutableListOf(null, null, null)

            currentMovement.value?.recordingCompleted = true
            repCount.value = 0

        } else {
            recording = true
            recordButtonText.value = "Stop recording"
            currentMovement.value?.resetStringBuilders()

            mdsSubscriptionLeft =
                imuSubscription(leftSensorIndex, "left")
            mdsSubscriptionRight =
                imuSubscription(rightSensorIndex, "right")
            //mdsSubscriptionWaist =
            //    imuSubscription(waistSensorIndex, "waist")
        }
    }
    fun movementRepetitionStart(){
        movementStart = true
        repCount.value = repCount.value?.plus(1)
    }

    // Creation of and callback for IMU9 subscription
    private fun imuSubscription(
        deviceIndex: Int,
        devicePlacement: String,
    ): MdsSubscription {

        val guessMsPerSample: Int = 1000 / Integer.valueOf(SAMPLE_RATE)

        return Mds.builder().build(getApplication<Application>().applicationContext)
            .subscribe(URI_EVENTLISTENER, formatContractToJson(
                MovesenseConnectedDevices.getConnectedDevice(deviceIndex).serial, URI_IMU9
            ), object : MdsNotificationListener {
                override fun onNotification(data: String) {

                    val currentNotificationImuData: ImuDataResponse =
                        Gson().fromJson(data, ImuDataResponse::class.java)

                    val currentTime: Long = System.currentTimeMillis()
                    if (sensorTextLastUpdateTime[deviceIndex] + 4000 < currentTime) {

                        sensorTextLastUpdateTime[deviceIndex] = currentTime

                        timestampText[deviceIndex].value = String.format(
                            "%d",
                            currentNotificationImuData.body.timestamp
                        )
                    }

                    var movementStartIndicator = 0
                    if(devicePlacement == "left"){
                        if(movementStart){
                            movementStart = false
                            movementStartIndicator = 1
                        }
                    }

                    if (prevNotificationImuData[deviceIndex] != null) {
                        // Time since last update in milliseconds
                        var notificationTimeDelta: Long =
                            currentNotificationImuData.body.timestamp - (prevNotificationImuData[deviceIndex]?.body?.timestamp!!)

                        // Use guess if timestamp loops
                        if (notificationTimeDelta < 0) {
                            notificationTimeDelta =
                                (guessMsPerSample * prevNotificationImuData[deviceIndex]?.body?.arrayAcc?.size!!).toLong()
                        }

                        val prevNotificationUtcTimestamp =
                            sensorBootTime[deviceIndex] + prevNotificationImuData[deviceIndex]?.body?.timestamp!!

                        val len: Int = prevNotificationImuData[deviceIndex]?.body?.arrayAcc?.size!!
                        for (i in 0 until len) {
                            // Interpolate timestamp within update
                            val timestamp: Long =
                                prevNotificationUtcTimestamp + i * (notificationTimeDelta / len)

                            currentMovement.value?.appendLine(
                                deviceIndex, String.format(
                                    "%d,%s,%s,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f",
                                    timestamp,
                                    currentMovement.value!!.movementName,
                                    devicePlacement,
                                    movementStartIndicator,
                                    prevNotificationImuData[deviceIndex]?.body?.arrayAcc!![i].getX(),
                                    prevNotificationImuData[deviceIndex]?.body?.arrayAcc!![i].getY(),
                                    prevNotificationImuData[deviceIndex]?.body?.arrayAcc!![i].getZ(),
                                    prevNotificationImuData[deviceIndex]?.body?.arrayGyro!![i].getX(),
                                    prevNotificationImuData[deviceIndex]?.body?.arrayGyro!![i].getY(),
                                    prevNotificationImuData[deviceIndex]?.body?.arrayGyro!![i].getZ(),
                                    prevNotificationImuData[deviceIndex]?.body?.arrayMagnl!![i].getX(),
                                    prevNotificationImuData[deviceIndex]?.body?.arrayMagnl!![i].getY(),
                                    prevNotificationImuData[deviceIndex]?.body?.arrayMagnl!![i].getZ()
                                )
                            )
                            movementStartIndicator = 0
                        }
                    }
                    prevNotificationImuData[deviceIndex] = currentNotificationImuData
                }

                override fun onError(error: MdsException) {
                    Log.e(TAG, "onError(): ", error)
                }
            })
    }

    // Helper for imuSubscription
    private fun formatContractToJson(serial: String?, uri: String?): String? {
        val sb = StringBuilder()
        return sb.append("{\"Uri\": \"").append(serial).append("/").append(uri).append("\"}")
            .toString()
    }


    fun playBeep() {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
    }


}