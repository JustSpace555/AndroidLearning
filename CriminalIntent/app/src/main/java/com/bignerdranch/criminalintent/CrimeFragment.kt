package com.bignerdranch.criminalintent

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CI.CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1
private const val DATE_FORMAT = "E, MMM d, yyyy, hh:mm"
private const val REQUEST_CONTACT = 3

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

	private lateinit var crime:				Crime
	private lateinit var addCrimeButton:	Button
	private lateinit var timeButton:		Button
	private lateinit var reportButton:		Button
	private lateinit var dateButton:		Button
	private lateinit var suspectButton:		Button
	private lateinit var callButton:		Button
	private lateinit var titleField:		EditText
	private lateinit var solvedCheckBox:	CheckBox
	private val crimeDetailViewModel:		CrimeDetailViewModel by lazy {
		ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
	}

	/*
		Initialization
	*/

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



	/*
		Fragment lifecycle functions
	*/

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		crime = Crime()
		val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
		crimeDetailViewModel.loadCrime(crimeId)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {

		val view = inflater.inflate(R.layout.fragment_crime, container, false)

		titleField = view.findViewById(R.id.crime_title) as EditText
		dateButton = view.findViewById(R.id.crime_date) as Button
		timeButton = view.findViewById(R.id.crime_time) as Button
		solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
		addCrimeButton = view.findViewById(R.id.add_crime_button) as Button
		reportButton = view.findViewById(R.id.crime_report ) as Button
		suspectButton = view.findViewById(R.id.crime_suspect) as Button
		callButton = view.findViewById(R.id.call_suspect_button) as Button

		return view
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		crimeDetailViewModel.crimeLiveData.observe(
			viewLifecycleOwner, Observer { crime ->
				crime?.let {
					this.crime = crime
				}
				updateUI()
			}
		)
	}

	override fun onStart() {
		super.onStart()

		titleField.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(s: CharSequence?,
										   start: Int,
										   count: Int,
										   after: Int) {}
			override fun afterTextChanged(s: Editable?) {}
			override fun onTextChanged(s: CharSequence?,
									   start: Int,
									   before: Int,
									   count: Int) {
				crime.title = s.toString()
			}
		}
		)

		solvedCheckBox.apply {
			setOnCheckedChangeListener { _, isChecked ->
				crime.isSolved = isChecked
			}
		}

		dateButton.setOnClickListener {
			DatePickerFragment.newInstance(crime.date).apply {
				setTargetFragment(this@CrimeFragment, REQUEST_DATE)
				show(this@CrimeFragment.requireActivity().supportFragmentManager, DIALOG_DATE)
			}
		}

		timeButton.setOnClickListener {
			TimePickerFragment.newInstance(crime.date).apply {
				setTargetFragment(this@CrimeFragment, REQUEST_TIME)
				show(this@CrimeFragment.requireActivity().supportFragmentManager, DIALOG_TIME)
			}
		}

		addCrimeButton.setOnClickListener {
			if (crime.title.isEmpty())
				Toast.makeText(context, "Title is empty. Please write something",
					Toast.LENGTH_SHORT).show()
			else {
				crimeDetailViewModel.saveCrime(crime)
				activity?.supportFragmentManager?.popBackStack()
			}
		}

		reportButton.setOnClickListener {
			Intent(Intent.ACTION_SEND).apply {
				type = "text/plain"
				putExtra(Intent.EXTRA_TEXT, getCrimeReport())
				putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
			}.also { intent ->
				startActivity(
					Intent.createChooser(intent, getString(R.string.send_report))
				)
			}
		}

		suspectButton.apply {
			val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
			setOnClickListener {
				if (ContextCompat.checkSelfPermission(
						context, android.Manifest.permission.READ_CONTACTS
					) != PackageManager.PERMISSION_GRANTED) {
					requestPermissions(
						arrayOf(android.Manifest.permission.READ_CONTACTS),
						REQUEST_CONTACT
					)
				}
				else
					startActivityForResult(pickContactIntent, REQUEST_CONTACT)
			}

			val packageManager: PackageManager = requireActivity().packageManager
			val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent,
				PackageManager.MATCH_DEFAULT_ONLY)
			if (resolvedActivity == null)
				isEnabled = false
		}

		callButton.apply {
			val dialContactIntent = Intent(Intent.ACTION_DIAL).setData(
				Uri.parse("tel:${crime.suspectPhone}")
			)
			setOnClickListener {
				startActivity(dialContactIntent)
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		if (crime.title.isBlank() || crime.title.isEmpty()) {
			crime.title = "Unnamed crime"
			crimeDetailViewModel.saveCrime(crime)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		when {
			resultCode != Activity.RESULT_OK -> return
			requestCode == REQUEST_CONTACT && data != null -> {

				val contactUri: Uri = data.data as Uri
				var id = ""
				val queryFields = arrayOf(
					ContactsContract.Contacts.DISPLAY_NAME,
					ContactsContract.Contacts._ID
				)
				val cursor = requireActivity()
					.contentResolver
					.query(contactUri, queryFields, null, null, null)

				cursor?.use {
					if (it.count == 0)
						return
					it.moveToFirst()
					val suspect = it.getString(0)
					id = it.getString(1)
					crime.suspect = suspect
					crimeDetailViewModel.saveCrime(crime)
					suspectButton.text = suspect
				}

				cursor?.close()

				val selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id
				val cursorPhone = requireActivity()
					.contentResolver
					.query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null, selection, null, null)
				cursorPhone?.use {
					if (it.count == 0)
						return
					it.moveToNext()
					crime.suspectPhone = it.getString(
						it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
					)
					crimeDetailViewModel.saveCrime(crime)
				}
			}
		}
	}



	/*
	 *	Additional functions
	 */

	@SuppressLint("SetTextI18n")
	private fun updateUI() {
		val cal = Calendar.getInstance()
		cal.time = crime.date
		titleField.setText(crime.title)
		dateButton.text = SimpleDateFormat("E, MMM d, yyyy", Locale.ENGLISH).format(crime.date)
		timeButton.text = SimpleDateFormat("hh:mm", Locale.ENGLISH).format(crime.date)
		solvedCheckBox.apply {
			isChecked = crime.isSolved
			jumpDrawablesToCurrentState()
		}
		if (crime.suspect.isNotEmpty())
			suspectButton.text = crime.suspect
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			REQUEST_CONTACT -> {
				if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					val pickContactIntent =
						Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
					startActivityForResult(pickContactIntent, REQUEST_CONTACT)
				}
			}
		}
	}


	/*
	 *	Update date
	 */
	override fun onDateSelected(date: Date) {
		crime.date = date
		updateUI()
	}

	override fun onTimeSet(date: Date) {
		crime.date = date
		updateUI()
	}

	private fun getCrimeReport(): String {
		val solvedString = if (crime.isSolved)
			getString(R.string.crime_report_solved)
		else
			getString(R.string.crime_report_unsolved)

		val dateString = android.text.format.DateFormat.format(DATE_FORMAT, crime.date).toString()
		val suspect = if(crime.suspect.isBlank())
			getString(R.string.crime_report_no_suspect)
		else
			getString(R.string.crime_report_suspect, crime.suspect)

		return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
	}
}