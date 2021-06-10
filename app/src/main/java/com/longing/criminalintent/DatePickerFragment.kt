package com.longing.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"
const val REQUEST_DATE = "RequestDate"

class DatePickerFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dataPickListener =
            DatePickerDialog.OnDateSetListener { _: DatePicker, year, month, dayOfMonth ->
                val resultDate: Date = GregorianCalendar(year, month, dayOfMonth).time
                val result = Bundle().apply {
                    putSerializable(ARG_DATE, resultDate)
                }
                parentFragmentManager.setFragmentResult(REQUEST_DATE, result)
            }

        val date: Date = arguments?.get(ARG_DATE) as Date
        val calender = Calendar.getInstance()
        calender.time = date
        val initialYear = calender.get(Calendar.YEAR)
        val initialMonth = calender.get(Calendar.MONTH)
        val initialDay = calender.get(Calendar.DAY_OF_MONTH)


        return DatePickerDialog(
            requireContext(),
            dataPickListener,
            initialYear,
            initialMonth,
            initialDay,
        )
    }

    companion object {
        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }

        fun getResultDate(result: Bundle): Date {
            return result.getSerializable(ARG_DATE) as Date
        }
    }
}