package fi.marejstr.movementtraining.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fi.marejstr.movementtraining.Movement
import fi.marejstr.movementtraining.databinding.MovementCardBinding

class MovementAdapter(
        //private val movementInfoClickListener: MovementInfoClickListener,
        private val movementRecordClickListener: MovementRecordClickListener
    ) : RecyclerView.Adapter<MovementAdapter.ViewHolder>() {

    var movementList = listOf<Movement>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = MovementCardBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = movementList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(movementList[position]){
            holder.binding.movement = this
            holder.binding.movementRecordClickListener = movementRecordClickListener
            holder.binding.movementNameText.text = movementName
            if(recordingCompleted) {
                holder.binding.statusText.text = "Recording completed"
                holder.binding.imageCompleted.visibility = View.VISIBLE
                holder.binding.imageNotCompleted.visibility = View.INVISIBLE
            } else {
                holder.binding.statusText.text = "Not completed"
                holder.binding.imageCompleted.visibility = View.INVISIBLE
                holder.binding.imageNotCompleted.visibility = View.VISIBLE
            }
        }
    }

    class ViewHolder(val binding: MovementCardBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}

/*
class MovementInfoClickListener(val clickListener: (movement: Movement) -> Unit) {
    fun onClick(mov: Movement) = clickListener(mov)
}
 */

class MovementRecordClickListener(val clickListener: (movement: Movement) -> Unit) {
    fun onClick(mov: Movement) = clickListener(mov)
}
