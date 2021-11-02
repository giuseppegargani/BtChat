package com.example.btchat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.btchat.databinding.FragmentStartBinding

class StartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentStartBinding>(inflater, R.layout.fragment_start, container, false)

        binding.buttonChoice.setOnClickListener { view: View ->
            view.findNavController().navigate(StartFragmentDirections.actionStartFragmentToChoiceDeviceFragment())
        }

        return binding.root
    }
}