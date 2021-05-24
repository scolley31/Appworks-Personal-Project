package com.example.betterlife

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.betterlife.data.Plan
import com.example.betterlife.home.item.HomeDoneAdapter
import com.example.betterlife.home.item.HomeItemAdapter
import com.example.betterlife.other.OtherAdapter

@BindingAdapter("plans")
fun bindRecyclerViewWithPlan(recyclerView: RecyclerView, plan: List<Plan>?) {
    plan?.let {
        recyclerView.adapter?.apply {
            when (this) {
                        is HomeDoneAdapter -> {
                            notifyDataSetChanged()
                            submitList(it)
                        }
                        is HomeItemAdapter -> {
                            notifyDataSetChanged()
                            submitList(it)
                        }
                        is OtherAdapter -> {
                            notifyDataSetChanged()
                            submitList(it)
                        }
                    }
                }
            }
    }


