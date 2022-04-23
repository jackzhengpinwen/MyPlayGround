package com.zpw.myplayground.drawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zpw.myplayground.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: ConstraintLayout = inflater.inflate(R.layout.fragment_home, container, false) as ConstraintLayout
        root.findViewById<Button>(R.id.button_home).setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_DashboardFragment)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}