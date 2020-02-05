package com.example.geoquiz

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val REQUEST_CODE_CHEAT = 0
private const val EXTRA_ANSWER_SHOWN = "com.example.geoquiz.answer_is_true"
private const val AMOUNT_OF_CHEATS = "com.example.geoquiz.amount_of_cheats"

class MainActivity : AppCompatActivity() {

	private lateinit var trueButton:		Button
	private lateinit var falseButton:		Button
	private lateinit var nextButton:		Button
	private lateinit var prevButton:		Button
	private lateinit var cheatButton:		Button

	private lateinit var questionTextView:	TextView
	private lateinit var currentQuestion:	Question
	private var amountCorrectAnswers = 0
	private var amountAnswers = 0

	private val quizViewModel : QuizViewModel by lazy {
		ViewModelProvider(this).get(QuizViewModel::class.java)
	}


	private val prevQuestionFunc = {
		quizViewModel.moveToPrev()
		updateQuestion()
	}

	private val nextQuestionFunc = {
		quizViewModel.moveToNext()
		updateQuestion()
	}


	private fun updateButtons() {
		falseButton.isVisible = currentQuestion.ifVisible
		trueButton.isVisible = currentQuestion.ifVisible
	}

	private fun updateQuestion() {
		currentQuestion = quizViewModel.currentQuestion
		questionTextView.setText(currentQuestion.textResId)
		updateButtons()
	}

	private fun checkAnswer(userAnswer: Boolean): Boolean {
		val correctAnswer = quizViewModel.currentQuestionAnswer
		var ifAnswerIsCorrect = false

		val messageResId = when (quizViewModel.currentQuestion.ifCheater) {
			true -> R.string.judgment_toast
			false -> if (userAnswer == correctAnswer) {
						amountCorrectAnswers++
						ifAnswerIsCorrect = true
						R.string.correct_toast
					}
					else
						R.string.incorrect_toast
		}

		amountAnswers++
		if (amountCorrectAnswers >= quizViewModel.size)
			Toast.makeText (
				this, "You've scored ${(amountCorrectAnswers.toDouble() /
						amountAnswers * 100).toInt()}%. Congratulations!", Toast.LENGTH_LONG
			).show()
		else
			Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
		return ifAnswerIsCorrect
	}

	private fun changeButtonVisibility (ifAnswerRight: Boolean) {
		if (!ifAnswerRight)
			return
		currentQuestion.ifVisible = false
		updateButtons()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		quizViewModel.currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0

		trueButton = findViewById(R.id.true_button)
		falseButton = findViewById(R.id.false_button)
		nextButton = findViewById(R.id.next_button)
		prevButton = findViewById(R.id.prev_button)
		cheatButton = findViewById(R.id.cheat_button)
		questionTextView = findViewById(R.id.question_text_view)

		trueButton.setOnClickListener {
			changeButtonVisibility(checkAnswer(true))
		}
		falseButton.setOnClickListener {
			changeButtonVisibility(checkAnswer(false))
		}
		nextButton.setOnClickListener { nextQuestionFunc() }
		prevButton.setOnClickListener { prevQuestionFunc() }
		cheatButton.setOnClickListener { view ->
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				val options = ActivityOptions.makeClipRevealAnimation(
					view, 0, 0, view.width, view.height
				)
				startActivityForResult(
					CheatActivity.newIntent(
						this@MainActivity, quizViewModel.currentQuestionAnswer,
						quizViewModel.amountOfCheats
					), REQUEST_CODE_CHEAT, options.toBundle()
				)
			} else {
				startActivityForResult(
					CheatActivity.newIntent(
						this@MainActivity, quizViewModel.currentQuestionAnswer,
						quizViewModel.amountOfCheats
					), REQUEST_CODE_CHEAT
				)
			}
		}
		updateQuestion()
	}

	override fun onSaveInstanceState(savedInstanceState: Bundle) {
		super.onSaveInstanceState(savedInstanceState)
		Log.i(TAG, "onSavedInstanceState")
		savedInstanceState.putInt(KEY_INDEX, quizViewModel.currentIndex)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		if (resultCode != Activity.RESULT_OK || requestCode != REQUEST_CODE_CHEAT)
			return
		quizViewModel.currentQuestion.ifCheater =
			data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
		if (quizViewModel.currentQuestion.ifCheater)
			quizViewModel.amountOfCheats--
	}
}
