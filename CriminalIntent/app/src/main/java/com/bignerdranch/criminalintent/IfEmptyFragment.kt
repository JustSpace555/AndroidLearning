package com.bignerdranch.criminalintent

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class IfEmptyFragment : Fragment() {

    private lateinit var button: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.empty_fragment_crime_list, container, false)

        button = view.findViewById(R.id.if_empty_button)

        return view
    }
}