package com.example.btchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/* Questo e' l'adapter per le unità accoppiate e a disposizione
    DENTRO RECYCLER VIEW SI DEVE METTERE UN CLICKLISTENER CHE NAVIGA AL FRAGMENT CORRISPONDENTE CON IL DATO CORRISPONDENTE
    deve cioe' mostrare che si e' cliccato un elemento corrispondente che può essere una lista di messaggi corrispondenti
    vedi Udacity e può prendere i dati storici
    noi vogliamo che rimanga in ascolto
    MA PUO' ESSERE UNA PAGINA CON TUTTI I DATI CHE HA TROVATO SU QUELLA DEVICE!!!!
 */

class DevicesRecyclerViewAdapter(val mDeviceList: List<DeviceData>, val context: ChoiceDeviceFragment) :
    RecyclerView.Adapter<DevicesRecyclerViewAdapter.VH>() {

    private var listener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_single_item, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder?.label?.text = mDeviceList[position].deviceName ?: mDeviceList[position].deviceHardwareAddress
    }

    override fun getItemCount(): Int {
        return mDeviceList.size
    }

    inner class VH(itemView: View?) : RecyclerView.ViewHolder(itemView!!){

        var label: TextView? = itemView?.findViewById(R.id.largeLabel)

        init {
            itemView?.setOnClickListener{
                listener?.itemClicked(mDeviceList[adapterPosition])
            }
        }
    }

    /*fun setItemClickListener(listener: ChoiceDeviceFragment){
        this.listener = listener
    }*/

    interface ItemClickListener{
        fun itemClicked(deviceData: DeviceData)
    }
}