package fi.marejstr.movementtraining

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FileLogger() {
    private val TAG = "FileLogger"

    //private val movementArray = arrayOfNulls<List<StringBuilder>>(movementCount)

    // TODO make private
    //val movementArray = Array(movementCount) { arrayOfNulls<StringBuilder>(sensorCount) }

    /*
    private val sensorStringBuilders : List<StringBuilder> = listOf(
        StringBuilder(), StringBuilder(), StringBuilder()
    )
     */

    /*
    private var isHeaderExists = false
    fun appendHeader(header: String?) {
        if (!isHeaderExists) {
            sensorStringBuilders[0].append(header).append("\n")
        }
        isHeaderExists = true
    }
     */

    fun createMovementStringBuilders(movementNum : Int){
        /*
        movementArray[movementNum] =  arrayOf(
            StringBuilder(), StringBuilder(), StringBuilder()
        )
         */
    }

    fun appendLine(movementNum : Int, sensorNum : Int, line: String?) {
        /*
        movementArray[movementNum]?.get(sensorNum)?.append(line)?.append("\n")
         */
    }

    fun finishSavingLogs(context: Context?, movement: String?) {
        //fileLogger.appendHeader("Timestamp,Movement,SensorPlacement,AccX,AccY,AccZ,GyroX,GyroY,GyroZ,MagnX,MagnY,MagnZ")

        /*
        val sb = StringBuilder()
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val currentDatTime: String = sdf.format(Date())
        sb.append(currentDatTime).append("_").append(movement).append(".csv")

        val state = Environment.getExternalStorageState()

        if (Environment.MEDIA_MOUNTED == state) {
            val externalDirectory = context?.getExternalFilesDir(null)
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
                // timestamp + device serial + data type,

                val fileWriter = FileWriter(logFile)
                fileWriter.write(sensorStringBuilders[0].toString())
                fileWriter.write(sensorStringBuilders[1].toString())
                fileWriter.write(sensorStringBuilders[2].toString())
                fileWriter.close()

            } catch (e: IOException) {
                Log.e(TAG, "Error while writing file")
            }

        } else {
            Log.e(TAG, "createFile isExternalStorageWritable Error")
        }

         */
    }
}