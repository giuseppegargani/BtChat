package com.example.btchat

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btchat.databinding.FragmentChoiceDeviceBinding
import kotlin.system.exitProcess

/* SI DEVE METTERE ANCHE IL SIMBOLO DI BLUETOOTH

RecyclerView
https://www.geeksforgeeks.org/how-to-disable-recyclerview-scrolling-in-android/

recyclerView= activity?.findViewById<RecyclerView>(R.id.recyclerView)!!
                //val myLinearLayoutManager = LinearLayoutManager(activity)
                //recyclerView.layoutManager = myLinearLayoutManager
                //val devicesAdapter = DevicesRecyclerViewAdapter(context = this, mDeviceList = mPairedDeviceList)
                //recyclerView.adapter = devicesAdapter
                //devicesAdapter.setItemClickListener(this)
                //headerLabelPaired.visibility = View.VISIBLE
*/

class ChoiceDeviceFragment : Fragment()/*, DevicesRecyclerViewAdapter.ItemClickListener */ {

    //TODO 1 - aggiunta di variabili
    private lateinit var recyclerView: RecyclerView
    private val mDeviceList = arrayListOf<DeviceData>()
    private lateinit var devicesAdapter: DevicesRecyclerViewAdapter

    private val REQUEST_ENABLE_BT = 123
    private var mBtAdapter: BluetoothAdapter? = null
    private val PERMISSION_REQUEST_LOCATION = 123
    private val PERMISSION_REQUEST_LOCATION_KEY = "PERMISSION_REQUEST_LOCATION"
    private var alreadyAskedForPermission = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentChoiceDeviceBinding>(inflater, R.layout.fragment_choice_device, container, false)

        //TODO creare la lista da adapter

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter()
        //la parte di logica che verifica bt attivo e manda alert ed eventualmente chiude app
        if (mBtAdapter == null)
            showAlertAndExit()
        else {
            if (mBtAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                Toast.makeText(context,"Bluetooth acceso e pronto all'uso",Toast.LENGTH_SHORT).show()
            }
            // Get a set of currently paired devices
            //DA MODIFICARE
            // Get a set of currently paired devices
            val pairedDevices = mBtAdapter?.bondedDevices
            val mPairedDeviceList = arrayListOf<DeviceData>()

            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices?.size ?: 0 > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (device in pairedDevices!!) {
                    val deviceName = device.name ?: "Senza nome"
                    val deviceHardwareAddress = device.address // MAC address
                    mPairedDeviceList.add(DeviceData(deviceName,deviceHardwareAddress))
                    Log.d("devices","nome "+deviceName+" e indirizzo: "+device.address)
                }

                recyclerView= binding.recyclerView
                val devicesAdapter = DevicesRecyclerViewAdapter(context = this, mDeviceList = mPairedDeviceList)
                recyclerView.adapter = devicesAdapter
                //devicesAdapter.setItemClickListener(this)
                //headerLabelPaired.visibility = View.VISIBLE
            }
        }

        binding.chatButton.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_choiceDeviceFragment_to_chatFragment)
        }

        return binding.root
    }

    private fun checkPermissions() {

        if (alreadyAskedForPermission) {
            // don't check again because the dialog is still open
            return
        }

        // Android M Permission check 
        if (context?.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            val builder = context?.let { AlertDialog.Builder(it) }
            builder?.setTitle(getString(R.string.need_loc_access))
            builder?.setMessage(getString(R.string.please_grant_loc_access))
            builder?.setPositiveButton(android.R.string.ok, null)
            builder?.setOnDismissListener {
                // the dialog will be opened so we have to save that
                alreadyAskedForPermission = true
                requestPermissions(arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), PERMISSION_REQUEST_LOCATION)
            }
            builder?.show()

        } else {
            //startDiscovery()
        }
    }

    //it closes the app if there is no bluetooth but signaling it
    private fun showAlertAndExit() {
        //because getContext return a nullable object
        context?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.not_compatible))
                .setMessage(getString(R.string.no_support))
                .setPositiveButton("Exit") { _, _ -> exitProcess(0) }
                .show()
        }
    }

    //it should keep memory that it has already asked for permission
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(PERMISSION_REQUEST_LOCATION_KEY, alreadyAskedForPermission)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            PERMISSION_REQUEST_LOCATION -> {
                // the request returned a result so the dialog is closed
                alreadyAskedForPermission = false
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //Log.d(TAG, "Coarse and fine location permissions granted")
                    //startDiscovery()
                    Toast.makeText(context,"Permesso Concesso!!", Toast.LENGTH_SHORT).show()
                } else {
                    val builder = context?.let { AlertDialog.Builder(it) }
                    builder?.setTitle(getString(R.string.fun_limted))
                    builder?.setMessage(getString(R.string.since_perm_not_granted))
                    builder?.setPositiveButton(android.R.string.ok, null)
                    builder?.show()
                }
            }
        }
    }

}
