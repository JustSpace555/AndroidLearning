package com.bignerdranch.criminalintent

import androidx.lifecycle.ViewModel

class CrimeListViewModel: ViewModel() {

	val crimes = mutableListOf<Crime>()

	init {
		for (i in 0 until 100) {
			crimes += Crime().apply {
				title = "Crime #$i"
				isSolved = i % 2 == 0
				requiresPolice = isSolved
			}
		}
	}
}