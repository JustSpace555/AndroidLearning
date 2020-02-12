package com.bignerdranch.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

private const val TAG = "CI.IfEmptyFragment"

class IfEmptyFragment : Fragment() {

    private lateinit var button: Button
    private var callbacks: Callbacks? = null

    interface Callbacks {
        fun onButtonPressed()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "On attach")
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "On detach")
        callbacks = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.empty_fragment_crime_list, container, false)

        Log.d(TAG, "On create view")

        button = view.findViewById(R.id.if_empty_button)

        button.setOnClickListener {
            callbacks?.onButtonPressed()
        }

        return view
    }
}