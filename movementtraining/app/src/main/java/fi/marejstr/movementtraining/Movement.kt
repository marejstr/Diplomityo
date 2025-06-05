package fi.marejstr.movementtraining

class Movement(var movementName: String, var movementDrawable: Int) {
    var recordingCompleted = false

    private var sensorStringBuilders : List<StringBuilder> = listOf(
        StringBuilder(), StringBuilder(), StringBuilder()
    )

    fun resetStringBuilders(){
       sensorStringBuilders = listOf(
            StringBuilder(), StringBuilder(), StringBuilder())
    }

    fun appendLine(sensorNum : Int, line: String?) {
        sensorStringBuilders[sensorNum].append(line).append("\n")
    }

    fun getStrings(): String {
       return "${sensorStringBuilders[0]}${sensorStringBuilders[1]}${sensorStringBuilders[2]}"
    }

}