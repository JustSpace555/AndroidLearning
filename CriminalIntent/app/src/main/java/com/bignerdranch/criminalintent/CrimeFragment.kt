package com.bignerdranch.criminalintent

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CI.CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

	private lateinit var crime: Crime
	private lateinit var titleField: EditText
	private lateinit var dateButton: Button
	private lateinit var solvedCheckBox: CheckBox
	private lateinit var addCrimeButton: Button
	private lateinit var timeButton: Button
	private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
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
		Log.d(TAG, "On create")
		crimeDetailViewModel.loadCrime(crimeId)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_crime, container, false)

		Log.d(TAG, "On create view")

		titleField = view.findViewById(R.id.crime_title) as EditText
		dateButton = view.findViewById(R.id.crime_date) as Button
		timeButton = view.findViewById(R.id.crime_time) as Button
		solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
		addCrimeButton = view.findViewById(R.id.add_crime_button) as Button

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
//				activity?.supportFragmentManager?.popBackStack()
			}
		}

		return view
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		Log.d(TAG, "On view created")

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

		Log.d(TAG, "On start")

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
	}

	override fun onDestroy() {
		super.onDestroy()
		Log.d(TAG, "On detach")
		if (crime.title.isBlank() || crime.title.isEmpty()) {
			Log.d(TAG, "Empty crime's title")
			crime.title = "Unnamed crime #${++crimeDetailViewModel.unnamedCrimes}"
			crimeDetailViewModel.saveCrime(crime)
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
}