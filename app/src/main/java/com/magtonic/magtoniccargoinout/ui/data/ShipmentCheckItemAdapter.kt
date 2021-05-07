package com.magtonic.magtoniccargoinout.ui.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.magtonic.magtoniccargoinout.R
import java.util.ArrayList

class ShipmentCheckItemAdapter(context: Context?, resource: Int, objects: ArrayList<ShipmentCheckItem>)  :
    ArrayAdapter<ShipmentCheckItem>(context as Context, resource, objects) {
    //private val mTAG = ShipmentCheckItemAdapter::class.java.name
    private val layoutResourceId: Int = resource

    private var inflater : LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val items: ArrayList<ShipmentCheckItem> = objects
    //private val mContext = context

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): ShipmentCheckItem {
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


        val shipmentCheckItem = items[position]
        //if (receiptDetailItem != null) {
        if (shipmentCheckItem.getStatus() == "0") {
            holder.itemStatus.text = "成功"
            holder.itemIcon.setImageResource(R.drawable.circle_green)
        } else {
            holder.itemStatus.text = "錯誤"
            holder.itemIcon.setImageResource(R.drawable.cross_red)
        }

        holder.itemReason.text = shipmentCheckItem.getReason()


        return view
    }

    class ViewHolder (view: View) {

        var itemStatus: TextView = view.findViewById(R.id.shipmentCheckHeader)
        var itemIcon: ImageView = view.findViewById(R.id.shipmentStatusIcon)
        var itemReason: TextView = view.findViewById(R.id.shipmentCheckContent)

    }
}