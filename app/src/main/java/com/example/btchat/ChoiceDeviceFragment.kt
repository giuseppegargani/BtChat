package com.example.btchat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.btchat.databinding.FragmentChoiceDeviceBinding

class ChoiceDeviceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentChoiceDeviceBinding>(inflater, R.layout.fragment_choice_device, container, false)
        return binding.root
    }
}