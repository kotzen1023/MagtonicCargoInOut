package com.magtonic.magtoniccargoinout.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.magtonic.magtoniccargoinout.R
import com.magtonic.magtoniccargoinout.persistence.History
import java.util.ArrayList

class HistoryAdapter (context: Context?, resource: Int, objects: ArrayList<History>) :
    ArrayAdapter<History>(context as Context, resource, objects) {

    private val layoutResourceId: Int = resource

    private var inflater : LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val items: ArrayList<History> = objects
    //private val mContext = context


    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): History? {
        return items[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        //Log.e(mTAG, "getView = "+ position);
        val view: View
        val holder: ViewHolder
        if (convertView == null || convertView.tag == null) {
            //Log.e(mTAG, "convertView = null");
            /*view = inflater.inflate(layoutResourceId, null);
            holder = new ViewHolder(view);
            view.setTag(holder);*/

            //LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(layoutResourceId, null)
            holder = ViewHolder(view)
            //holder.checkbox.setVisibility(View.INVISIBLE);
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        //holder.fileicon = (ImageView) view.findViewById(R.id.fd_Icon1);
        //holder.filename = (TextView) view.findViewById(R.id.fileChooseFileName);
        //holder.checkbox = (CheckBox) view.findViewById(R.id.checkBoxInRow);


        val history = items[position]
        //if (receiptDetailItem != null) {
        var stateCount = 0
        if (history.getWorkOrderState()!!.isNotEmpty()) {
            stateCount += history.getWorkOrderState()!!.toInt()
        }
        if (history.getShipmentState()!!.isNotEmpty()) {
            stateCount += history.getShipmentState()!!.toInt()
        }


        if (stateCount == 0) {
            holder.itemIcon.setImageResource(R.drawable.circle_green)
        } else {
            holder.itemIcon.setImageResource(R.drawable.cross_red)
        }

        holder.itemHeader.text = history.getBarcode()

        val content = history.getWorkOrderDesc()+"\n"+history.getShipmentDesc()
        holder.itemContent.text = content

        holder.itemTime.text = history.getDatetime()






        return view
    }

    class ViewHolder (view: View) {
        var itemIcon: ImageView = view.findViewById(R.id.success_or_fail_icon)
        var itemHeader: TextView = view.findViewById(R.id.historyHeader)
        var itemContent: TextView = view.findViewById(R.id.historyContent)
        var itemTime: TextView = view.findViewById(R.id.historyTime)
    }
}