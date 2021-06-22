package com.longing.criminalintent

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.io.File

private const val ARG_PICTURE = "picture"

class SuspectPictureFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val photoFile = arguments?.getSerializable(ARG_PICTURE) as File
        val bitmap = getScaledBitmap(photoFile.path, requireContext())

        val imageView = ImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageBitmap(bitmap)
        }

        return AlertDialog.Builder(requireContext())
            .setView(imageView)
            .create()
    }


    companion object {
        fun newInstance(photoFile: File): SuspectPictureFragment {
            val args = Bundle().apply {
                putSerializable(ARG_PICTURE, photoFile)
            }
            return SuspectPictureFragment().apply {
                arguments = args
            }
        }
    }
}