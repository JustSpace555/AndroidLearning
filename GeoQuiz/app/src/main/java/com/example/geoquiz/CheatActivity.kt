package com.example.geoquiz

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_cheat.*

private const val EXTRA_ANSWER_SHOWN = "com.example.geoquiz.answer_is_true"
private const val AMOUNT_OF_CHEATS = "com.example.geoquiz.amount_of_cheats"

class CheatActivity : AppCompatActivity() {

	private var answerIsTrue = false
	private var amountOfCheats = 0
	private lateinit var answerTextView:			TextView
	private lateinit var apiTextView:				TextView
	private lateinit var amountOfCheatsTextView:	TextView
	private lateinit var showAnswerButton:			Button

	private val cheatViewModel by lazy {
		ViewModelProvider(this).get(CheatViewModel::class.java)
	}

	companion object {
		fun newIntent(packageContext: Context, answerIsTrue: Boolean, amountOfCheats: Int): Intent {
			return Intent(packageContext, CheatActivity::class.java).apply {
				putExtra(EXTRA_ANSWER_SHOWN, answerIsTrue)
				putExtra(AMOUNT_OF_CHEATS, amountOfCheats)
			}
		}
	}


	@SuppressLint("SetTextI18n")
	private fun updateAmountOfCheatsTextView() {
		amountOfCheatsTextView.text = "Amount of cheats remaining: $amountOfCheats"
	}

	private fun updateButtonVisibility() {
		if (amountOfCheats <= 0)
			showAnswerButton.isVisible = false
	}

	private fun updateText() {
		if (cheatViewModel.ifButtonPressed)
			answerTextView.setText(
				when {
					answerIsTrue -> R.string.true_button
					else -> R.string.false_button
				}
			)
	}

	private fun setAnswerShowResult() {
		cheatViewModel.ifButtonPressed = true
		amountOfCheats--
		updateAmountOfCheatsTextView()
		updateButtonVisibility()
		updateText()
		val data = Intent().apply {
			putExtra(EXTRA_ANSWER_SHOWN, true)
		}
		setResult(Activity.RESULT_OK, data)
	}

	private fun updateResult() {
		if (cheatViewModel.ifButtonPressed) {
			updateText()
			setAnswerShowResult()
		}
	}

	@SuppressLint("SetTextI18n")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_cheat)

		answerIsTrue = intent.getBooleanExtra(EXTRA_ANSWER_SHOWN, false)
		amountOfCheats = intent.getIntExtra(AMOUNT_OF_CHEATS, 0)
		answerTextView = findViewById(R.id.answer_text_view)
		showAnswerButton = findViewById(R.id.show_answer_button)
		apiTextView = findViewById(R.id.api_level)
		amountOfCheatsTextView = findViewById(R.id.amount_of_cheats)

		updateAmountOfCheatsTextView()
		updateButtonVisibility()
		apiTextView.text = "API Level ${Build.VERSION.SDK_INT}"
		showAnswerButton.setOnClickListener { setAnswerShowResult() }
		updateResult()
	}
}