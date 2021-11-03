package com.example.btchat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.btchat.databinding.FragmentChatBinding
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.text.StringBuilder

class ChatFragment : Fragment() {

    private lateinit var chatInput: EditText
    private lateinit var sendButton: FrameLayout
    private var chatAdapter: ChatAdapter? = null
    private lateinit var recyclerviewChat: RecyclerView
    private val messageList = arrayListOf<Message>()
    //TODO creare un socket BT
    private lateinit var mBluetoothSocket: BluetoothSocket

    //UUIDS for this application
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    companion object {
        fun newInstance(): ChatFragment {
            val myFragment = ChatFragment()
            val args = Bundle()
            myFragment.arguments = args
            return myFragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val args = ChatFragmentArgs.fromBundle(requireArguments())
        val binding = DataBindingUtil.inflate<FragmentChatBinding>(inflater, R.layout.fragment_chat, container, false)
        binding.answerAddressDevice.text = args.deviceAddress

        //TODO si crea Adapter e si invoca la funzione
        var mBtAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        var bluetoothDevice: BluetoothDevice = mBtAdapter.getRemoteDevice(args.deviceAddress)

        connectToServerSocket(bluetoothDevice, uuid)

        return binding.root
    }

    private fun connectToServerSocket(device: BluetoothDevice, uuid: UUID) {

        val sb = StringBuilder()
        //sb.append(a).append(b)

        try {
            val clientSocket = device.createRfcommSocketToServiceRecord(uuid)
            // Block until server connection accepted.
            clientSocket.connect()
            // Add a reference to the socket used to send messages.
            mBluetoothSocket = clientSocket
            // Start listening for messages.
            listenForMessages(clientSocket,sb)
        } catch (e: IOException) {
            Log.e(TAG, "Bluetooth client I/O Exception.", e)
        }
    }

    private fun sendMessage(socket: BluetoothSocket, message: String) {
        val outputStream: OutputStream
        try {
            outputStream = socket.outputStream
            // Add a stop character.
            val byteArray = "$message ".toByteArray()
            byteArray[byteArray.size - 1] = 0
            outputStream.write(byteArray)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to send message: $message", e)
        }
    }

    private var mListening = false
    private fun listenForMessages(socket: BluetoothSocket, incoming: StringBuilder
    ): String? {
        var result = ""
        mListening = true
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        try {
            val instream: InputStream = socket.inputStream
            var bytesRead = -1
            while (mListening) {
                bytesRead = instream.read(buffer)
                if (bytesRead != -1) {
                    while (bytesRead == bufferSize && (buffer[bufferSize - 1].toInt() != 0))
                    {
                        result = result + String(buffer, 0, bytesRead - 1)
                        bytesRead = instream.read(buffer)
                    }
                    result = result + String(buffer, 0, bytesRead - 1)
                    incoming.append(result)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Message receive failed.", e)
        }
        return result
    }

}