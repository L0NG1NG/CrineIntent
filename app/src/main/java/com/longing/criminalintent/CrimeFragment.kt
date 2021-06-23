package com.longing.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.util.*

const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DATE_FORMAT = "EEE,MMM,dd"

class CrimeFragment : Fragment() {

    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var activityResult: ActivityResultLauncher<Intent>
    private lateinit var takePhotoResult: ActivityResultLauncher<Intent>

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)

        //联系人选择forResult(这样写好像还更复杂)
        activityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when {
                    it.resultCode != Activity.RESULT_OK -> return@registerForActivityResult
                    it != null -> {
                        val contractUri: Uri? = it.data?.data
                        contractUri?.let { uri ->
                            val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                            val cursor = requireActivity().contentResolver.query(
                                uri,
                                queryFields,
                                null,
                                null,
                                null

                            )

                            cursor?.use { cursor ->
                                if (cursor.count == 0) {
                                    return@registerForActivityResult
                                }
                                cursor.moveToFirst()
                                val suspect = cursor.getString(0)
                                crime.suspect = suspect
                                crimeDetailViewModel.saveCrime(crime)
                                suspectButton.text = crime.suspect

                            }
                        }

                    }

                }

            }

        takePhotoResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when {
                    it.resultCode != Activity.RESULT_OK -> return@registerForActivityResult
                    it != null -> {
                        requireActivity().revokeUriPermission(photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        updatePhotoView()

                    }
                }
            }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title)
        dateButton = view.findViewById(R.id.crime_date)
        solvedCheckBox = view.findViewById(R.id.crime_solved)
        reportButton = view.findViewById(R.id.crime_report)
        suspectButton = view.findViewById(R.id.crime_suspect)
        photoButton = view.findViewById(R.id.crime_camera)
        photoView = view.findViewById(R.id.crime_photo)

        //setTargetFragment被废弃了,试试这个
        parentFragmentManager.setFragmentResultListener(
            REQUEST_DATE,
            viewLifecycleOwner
        ) { requestKey: String, result: Bundle ->
            if (requestKey == REQUEST_DATE) {
                val date: Date = DatePickerFragment.getResultDate(result)
                crime.date = date
                updateUI()

            }
        }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveDate
            .observe(viewLifecycleOwner) { crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.longing.criminalintent.fileprovider",
                        photoFile
                    )
                    updateUI()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        }

        solvedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            crime.isSolved = isChecked

        }

        titleField.addTextChangedListener(titleWatcher)

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }

        }
        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject)
                )
            }.also { intent: Intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                activityResult.launch(pickContactIntent)

            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolveActivity: ResolveInfo? =
                packageManager.resolveActivity(
                    captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
            if (resolveActivity == null) {
                isEnabled = false
            }
            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(
                    captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY
                )

                for (cameraActivity in cameraActivities) {

                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                takePhotoResult.launch(captureImage)


            }

        }

    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)

    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    override fun onDestroy() {
        super.onDestroy()
        activityResult.unregister()
        takePhotoResult.unregister()
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        //   solvedCheckBox.isChecked = crime.isSolved
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }
        updatePhotoView()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireContext())
            photoView.setImageBitmap(bitmap)

        } else {
            photoView.setImageBitmap(null)
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_no_suspect)
        }

        return getString(
            R.string.crime_report,
            crime.title, dateString, solvedString, suspect
        )

    }


    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)

            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }


}