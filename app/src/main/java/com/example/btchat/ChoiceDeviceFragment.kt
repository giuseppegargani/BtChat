package com.example.btchat

/* LA PARTE DI DATI DOVREBBE STARE DENTRO AL VIEWMODEL!!! e limitare al fragment la parte relativa alla View!!!!
    vediamo come fare

    LA REGISTRAZIONE DEI DUE BROADCAST RECEIVERS E' STATA FATTA CON REQUIREACTIVITY ED OCCORRE CAMBIARLA
    PER PROGRAMMAZIONE ASINCRONA  (con un blocco di codice)

    NB: RIPULIRE TUTTI GLI ELEMENTI INN GRIGIO SCURO non usati (variabili, funzioni, importazioni) di ogni file

    AGGIUSTARE VIEW BINDING!!!
 */

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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.btchat.databinding.FragmentChoiceDeviceBinding
import kotlin.system.exitProcess

class ChoiceDeviceFragment : Fragment(), DevicesRecyclerViewAdapter.ItemClickListener {

    private val REQUEST_ENABLE_BT = 123
    private val TAG = javaClass.simpleName
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewPaired: RecyclerView
    private val mDeviceList = arrayListOf<DeviceData>()
    private lateinit var devicesAdapter: DevicesRecyclerViewAdapter
    private var mBtAdapter: BluetoothAdapter? = null
    private val PERMISSION_REQUEST_LOCATION = 123
    private val PERMISSION_REQUEST_LOCATION_KEY = "PERMISSION_REQUEST_LOCATION"
    private var alreadyAskedForPermission = false
    private lateinit var headerLabel: TextView
    private lateinit var headerLabelPaired: TextView
    private lateinit var headerLabelContainer: LinearLayout
    private lateinit var status: TextView
    private lateinit var connectionDot: ImageView
    private lateinit var  mConnectedDeviceName: String
    private var connected: Boolean = false


    //private var mChatService: BluetoothChatService? = null
    //private lateinit var chatFragment: ChatFragment

    /*Cosa ci aspettiamo di inizializzare in onCreate?

    la logica relativa al bluetoothm abilitato oppure acceso

    BINDING IN FRAGMENT DEVE ESSERE FATTO DENTRO ONCREATEVIEW!!!!
    Differenza tra onCreate e onCreateView e DA FARE IN SERVIZI

    Dove va messo RecyclerView.LayoutManager? dentro RecyclerViewAdapter o no?
    https://stackoverflow.com/questions/34706399/how-to-use-data-binding-with-fragment
    PENSO CHE VADA MESSO STATICO DENTRO XML!!!

    SI METTE UN LISTENER SU ITEM DI RECYCLERVIEW ED APRE LA CHAT!!!!


    IL NUCLEO CENTRALE E' ONREQUESTPERMISSION LISTENER!!!!! e quello da cui muove tutto
    cambia la variabile e fà delle cose!!!


     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //la parte di deviceAdapter per recycler e broadcast receiver e bt getDefaultAdapter
        devicesAdapter = DevicesRecyclerViewAdapter(context = this, mDeviceList = mDeviceList)
        recyclerView.adapter = devicesAdapter
        devicesAdapter.setItemClickListener(this)

        // Register for broadcasts when a device is discovered.
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        requireActivity().registerReceiver(mReceiver, filter)

        // Register for broadcasts when discovery has finished
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        requireActivity().registerReceiver(mReceiver, filter)

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
                status.text = getString(R.string.not_connected)
            }

            // Get a set of currently paired devices
            val pairedDevices = mBtAdapter?.bondedDevices
            val mPairedDeviceList = arrayListOf<DeviceData>()

            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices?.size ?: 0 > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (device in pairedDevices!!) {
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    mPairedDeviceList.add(DeviceData(deviceName,deviceHardwareAddress))
                }

                val devicesAdapter = DevicesRecyclerViewAdapter(context = this, mDeviceList = mPairedDeviceList)
                recyclerViewPaired.adapter = devicesAdapter
                devicesAdapter.setItemClickListener(this)
                headerLabelPaired.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentChoiceDeviceBinding>(inflater, R.layout.fragment_choice_device, container, false)
        return binding.root

        findDevices()
    }

    //rende visibile l'apparecchio
    private fun makeVisible() {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivity(discoverableIntent)
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
            startDiscovery()
        }
    }

    //la cosa interessante è che si può chiudere una app se non c'è il bluetooth
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

    private fun findDevices() {

        checkPermissions()
    }

    private fun startDiscovery() {

        //headerLabelContainer.visibility = View.VISIBLE
        //progressBar.visibility = View.VISIBLE
        //headerLabel.text = getString(R.string.searching)
        mDeviceList.clear()

        // If we're already discovering, stop it
        if (mBtAdapter?.isDiscovering ?: false)
            mBtAdapter?.cancelDiscovery()

        // Request discover from BluetoothAdapter
        mBtAdapter?.startDiscovery()
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val mReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action

            if (BluetoothDevice.ACTION_FOUND == action) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device!!.name
                val deviceHardwareAddress = device.address // MAC address

                val deviceData = DeviceData(deviceName, deviceHardwareAddress)
                mDeviceList.add(deviceData)

                //aggiunto
                requireActivity().findViewById<TextView>(R.id.scoperte).append(" $deviceName")

                val setList = HashSet<DeviceData>(mDeviceList)
                mDeviceList.clear()
                mDeviceList.addAll(setList)

                //Questo è l'adapter del recyclerView del devices che modifica
                //devicesAdapter.notifyDataSetChanged()
            }

            //da rimettere con elemento visuale
            /*
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                progressBar.visibility = View.INVISIBLE
                headerLabel.text = getString(R.string.found)
            }*/
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //giuseppe e da rimettere
        //progressBar.visibility = View.INVISIBLE

        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            //Bluetooth is now connected.
                //da rimettere
            //status.text = getString(R.string.not_connected)

            // Get a set of currently paired devices
            val pairedDevices = mBtAdapter?.bondedDevices
            val mPairedDeviceList = arrayListOf<DeviceData>()

            mPairedDeviceList.clear()

            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices?.size ?: 0 > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (device in pairedDevices!!) {
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    mPairedDeviceList.add(DeviceData(deviceName,deviceHardwareAddress))
                }
                /*
                val devicesAdapter = DevicesRecyclerViewAdapter(context = this, mDeviceList = mPairedDeviceList)
                recyclerViewPaired.adapter = devicesAdapter
                devicesAdapter.setItemClickListener(this)
                headerLabelPaired.visibility = View.VISIBLE*/
            }

        }
        //label.setText("Bluetooth is now enabled.")
    }

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
                    startDiscovery()
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

    override fun itemClicked(deviceData: DeviceData) {
        connectDevice(deviceData)
    }

    //da aggiustare
    private fun connectDevice(deviceData: DeviceData) {

        // Cancel discovery because it's costly and we're about to connect
        mBtAdapter?.cancelDiscovery()
        val deviceAddress = deviceData.deviceHardwareAddress

        val device = mBtAdapter?.getRemoteDevice(deviceAddress)

        //status.text = getString(R.string.connecting)
        //connectionDot.setImageDrawable(getDrawable(R.drawable.ic_circle_connecting))

        // Attempt to connect to the device
       // mChatService?.connect(device, true)

    }

    /*override fun onResume() {
        super.onResume()
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService?.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService?.start()
            }
        }

        if(connected)
            showChatFragment()

    }*/

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(mReceiver)
    }

}
