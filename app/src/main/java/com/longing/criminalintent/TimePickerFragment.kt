package com.longing.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import java.util.*


private const val ARG_TIME = "time"

const val REQUEST_TIME = "RequestTime"

/**
 * like {@link #DatePickerFragment} 怎么生成超链接啊？？？？？？？？？
 */
class TimePickerFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = arguments?.getSerializable(ARG_TIME) as Date
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = date

        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
             calendar.apply {
                 set(Calendar.HOUR_OF_DAY,hourOfDay)
                 set(Calendar.MINUTE, minute)
             }

            val result = Bundle().apply {
                putSerializable(ARG_TIME, calendar.time)
            }

            parentFragmentManager.setFragmentResult(REQUEST_TIME, result)

        }


        return TimePickerDialog(
            requireContext(),
            timeSetListener,
            initialHour, initialMinute, false
        )
    }


    companion object {
        fun newInstance(date: Date): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_TIME, date)
            }
            val fragment = TimePickerFragment()
            fragment.arguments = args
            return fragment
        }

        fun getResultTime(result: Bundle): Date {
            return result.getSerializable(ARG_TIME) as Date
        }
    }
}