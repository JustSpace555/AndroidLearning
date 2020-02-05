package com.example.geoquiz

import androidx.lifecycle.ViewModel

class QuizViewModel : ViewModel() {

	var currentIndex = 0
	var amountOfCheats = 3

	private val questionBank = listOf(
		Question(R.string.question_australia, true),
		Question(R.string.question_oceans, true),
		Question(R.string.question_mideast, false),
		Question(R.string.question_africa, false),
		Question(R.string.question_americas, true),
		Question(R.string.question_asia, true)
	)

	val currentQuestion : Question
		get() = questionBank[currentIndex]

	val currentQuestionAnswer: Boolean
		get() = currentQuestion.answer

	val size: Int
		get() = questionBank.size

	fun moveToNext() {
		currentIndex = (currentIndex + 1) % questionBank.size
	}

	fun moveToPrev() {
		currentIndex = if (currentIndex - 1 < 0)
			questionBank.size - 1
		else
			currentIndex - 1
	}
}