package com.zpw.myplayground.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zpw.myplayground.R

class NotificationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: ConstraintLayout = inflater.inflate(R.layout.fragment_notifications, container, false) as ConstraintLayout
        root.findViewById<Button>(R.id.button_notifications).setOnClickListener {
            findNavController().navigate(R.id.action_NotificationsFragment_to_HomeFragment)
        }
        return root
    }
}