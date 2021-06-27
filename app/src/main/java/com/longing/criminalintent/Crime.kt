package com.longing.criminalintent

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Crime(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false
) {
    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Crime>() {
            override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean =
                oldItem.id == newItem.id


            //应该是这样吧
            override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean =
                oldItem == newItem


        }
    }
}
