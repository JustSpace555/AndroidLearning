package com.bignerdranch.criminalintent

import android.annotation.SuppressLint
import android.app.TimePickerDialog
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import java.util.Date
import java.util.Calendar
import java.util.UUID

private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

	private lateinit var crime: Crime
	private lateinit var titleField: EditText
	private lateinit var dateButton: Button
	private lateinit var solvedCheckBox: CheckBox
	private lateinit var timeButton: Button
	private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
		ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		crime = Crime()
		val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
		Log.d(TAG, "args bundle crime ID: $crimeId")
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

		return view
	}

	@SuppressLint("SetTextI18n")
	private fun updateUI() {
		val cal = Calendar.getInstance()
		cal.time = crime.date
		titleField.setText(crime.title)
		dateButton.text = "${cal.get(Calendar.YEAR)} | ${cal.get(Calendar.MONTH)} | " +
				"${cal.get(Calendar.DAY_OF_WEEK)}"
		timeButton.text = "${cal.get(Calendar.HOUR)} : ${cal.get(Calendar.MINUTE)}"
		solvedCheckBox.apply {
			isChecked = crime.isSolved
			jumpDrawablesToCurrentState()
		}
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

	override fun onDateSelected(date: Date) {
		crime.date = date
		updateUI()
	}

	override fun onTimeSet(date: Date) {
		crime.date = date
		updateUI()
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
	}

	override fun onStop() {
		super.onStop()
		crimeDetailViewModel.saveCrime(crime)
	}
}