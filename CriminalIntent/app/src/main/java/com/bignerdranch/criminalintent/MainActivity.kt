package com.bignerdranch.criminalintent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment == null)
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, CrimeListFragment.newInstance())
                .commit()
    }

    override fun onCrimeSelected(crimeId: UUID) {
        Log.d(TAG, "MainActivity.onCrimeSelected: $crimeId")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CrimeFragment.newInstance(crimeId))
            .addToBackStack(null)
            .commit()
    }
}
