package com.longing.criminalintent

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DATE_FORMAT = "EEE,MMM,dd"

class CrimeFragment : Fragment() {

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var callSuspectButton: Button
    private lateinit var activityResult: ActivityResultLauncher<Intent>
    private lateinit var permissionRegister: ActivityResultLauncher<String>
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)

        //activityResult for choose suspect
        activityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when {
                    it.resultCode != Activity.RESULT_OK -> return@registerForActivityResult
                    it != null -> {
                        val contractUri: Uri? = it.data?.data
                        if (contractUri != null) {
                            val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                            val cursor = requireActivity().contentResolver.query(
                                contractUri,
                                queryFields,
                                null,
                                null,
                                null

                            )
                            cursor?.use { c ->
                                if (c.count == 0) {
                                    return@registerForActivityResult
                                }
                                c.moveToFirst()
                                val suspect = c.getString(0)
                                crime.suspect = suspect
                                crimeDetailViewModel.saveCrime(crime)
                                suspectButton.text = crime.suspect
                            }
                        }

                    }

                }

            }

        permissionRegister = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (it) {
                queryContacts()
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.cannot_read_contacts_toast,
                    Toast.LENGTH_SHORT
                ).show()
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
        callSuspectButton = view.findViewById(R.id.call_suspect)

        //试试新的 来实现fragment间传递数据
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

        callSuspectButton.setOnClickListener {
            if (!haveContactsPermission()) {
                permissionRegister.launch(Manifest.permission.READ_CONTACTS)
            } else {
                queryContacts()
            }


        }


    }

    private fun queryContacts() {
        val contactCursor = requireActivity().contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts._ID),
            "${ContactsContract.Contacts.DISPLAY_NAME}=?",
            arrayOf(crime.suspect),
            null
        )

        contactCursor?.use { cc ->
            if (cc.count == 0) {
                return
            }
            cc.moveToFirst()
            val contactsId =
                cc.getInt(cc.getColumnIndex(ContactsContract.Contacts._ID))
            val phoneCursor = this.requireContext().contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID}=?",
                arrayOf(contactsId.toString()),
                null
            )

            phoneCursor?.use { pc ->
                if (pc.count == 0) {
                    return
                }
                pc.moveToFirst()
                val number = pc.getString(
                    pc.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                )
                val numberUri = Uri.parse("tel:$number")
                startActivity(Intent(Intent.ACTION_DIAL, numberUri))

            }
        }

    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)

    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        //   solvedCheckBox.isChecked = crime.isSolved
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        suspectButton.text = crime.suspect
        callSuspectButton.isEnabled = crime.suspect.isNotEmpty()
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_no_suspect)
        }

        return getString(
            R.string.crime_report,
            crime.title, dateString, solvedString, suspect
        )

    }

    private fun haveContactsPermission(): Boolean {
        val status = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_CONTACTS
        )
        return status == PackageManager.PERMISSION_GRANTED

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