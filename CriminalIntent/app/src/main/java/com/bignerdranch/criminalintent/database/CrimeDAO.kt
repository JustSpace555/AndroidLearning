package com.bignerdranch.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.bignerdranch.criminalintent.Crime
import java.util.UUID

@Dao
interface CrimeDAO {

	@Query("SELECT * FROM crime")
	fun getCrimes(): LiveData<List<Crime>>

	@Query("SELECT * FROM crime WHERE id=(:id)")
	fun getCrime(id: UUID):  LiveData<Crime?>
}