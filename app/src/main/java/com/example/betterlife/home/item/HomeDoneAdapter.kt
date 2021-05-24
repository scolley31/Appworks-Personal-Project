package com.example.betterlife.home.item

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.betterlife.data.Plan
import com.example.betterlife.databinding.ItemHomeDoneGridBinding
import com.example.betterlife.databinding.ItemHomeGridBinding
import kotlinx.android.synthetic.main.item_home_grid.view.*

class HomeDoneAdapter(val viewModel: HomeDoneViewModel,val onClickListener: OnClickListener) :
        ListAdapter<Plan, HomeDoneAdapter.PlanViewHolder>(DiffCallback) {

    class PlanViewHolder(private var binding: ItemHomeDoneGridBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: Plan, viewModel: HomeDoneViewModel) {

            binding.plan = plan
            binding.viewModel = viewModel
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Plan>() {
        override fun areItemsTheSame(oldItem: Plan, newItem: Plan): Boolean {
            return (oldItem.name == newItem.name)
        }

        override fun areContentsTheSame(oldItem: Plan, newItem: Plan): Boolean {
            return oldItem.createdTime == newItem.createdTime
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        return PlanViewHolder(ItemHomeDoneGridBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }


    /**
     * Custom listener that handles clicks on [RecyclerView] items.  Passes the [Product]
     * associated with the current item to the [onClick] function.
     * @param clickListener lambda that will be called with the current [Product]
     */
    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = getItem(position)
        holder.bind(getItem(position), viewModel)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(plan)
        }

    }

    class OnClickListener(val clickListener: (plan : Plan) -> Unit) {
        fun onClick(plan : Plan) = clickListener(plan)
    }
}