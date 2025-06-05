package fi.marejstr.movementtraining.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.polidea.rxandroidble2.scan.ScanResult
import fi.marejstr.movementtraining.databinding.TextRowItemBinding

class ScanResultAdapter(val clickListener: DeviceListener) :
    RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    var dataSet = listOf<ScanResult>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = TextRowItemBinding.inflate(layoutInflater, parent, false)
        //val view = layoutInflater.inflate(R.layout.text_row_item, parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        with(dataSet[position]) {
            viewHolder.binding.clickListener = clickListener
            viewHolder.binding.macVariable = String.format("%s", bleDevice.macAddress)
            viewHolder.device.text = String.format("%s", bleDevice.name)
            viewHolder.rssi.text = String.format("RSSI: %d", rssi)
         }
    }

    class ViewHolder(val binding: TextRowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val device: TextView = binding.deviceTextView
        //val mac: TextView = binding.macTextView
        val rssi: TextView = binding.rssiTextView
    }

}

class DeviceListener(val clickListener: (macAdd: String) -> Unit) {
    fun onClick(mac: String) = clickListener(mac)
}
