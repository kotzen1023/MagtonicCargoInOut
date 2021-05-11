package com.magtonic.magtoniccargoinout.ui.data

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button

import android.widget.TextView

import com.magtonic.magtoniccargoinout.R
import java.util.ArrayList

class GuestDetailItemAdapter(context: Context?, resource: Int, objects: ArrayList<GuestDetailItem>) :
    ArrayAdapter<GuestDetailItem>(context as Context, resource, objects) {
    private val mTAG = GuestDetailItemAdapter::class.java.name
    private val layoutResourceId: Int = resource

    private var inflater : LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val items: ArrayList<GuestDetailItem> = objects
    private val mContext = context

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): GuestDetailItem {
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

        //holder.fileIcon = (ImageView) view.findViewById(R.id.fd_Icon1);
        //holder.filename = (TextView) view.findViewById(R.id.fileChooseFileName);
        //holder.checkbox = (CheckBox) view.findViewById(R.id.checkBoxInRow);


        val guestDetailItem = items[position]
        //if (receiptDetailItem != null) {
        val guestString = guestDetailItem.getData2()+"\n"+guestDetailItem.getData5()
        holder.itemData2.text = guestString

        val date = guestDetailItem.getData3()!!.split(" ")

        val timeString = date[0]+"\n"+guestDetailItem.getData4()

        holder.itemData34.text = timeString
        guestDetailItem.setBtnOk(holder.itemBtnLeave)

        holder.itemBtnLeave.setOnClickListener {
            Log.e(mTAG, "adapter click $position")

            val inDate = items[position].getData3()!!.split(" ")[0]

            val guestInIntent = Intent()
            guestInIntent.action = Constants.ACTION.ACTION_GUEST_SHOW_LEAVE_ACTION
            guestInIntent.putExtra("PLANT", items[position].getData1())
            guestInIntent.putExtra("GUEST_NO", items[position].getData2())
            guestInIntent.putExtra("IN_DATE", inDate)
            guestInIntent.putExtra("IN_TIME", items[position].getData4())
            mContext!!.sendBroadcast(guestInIntent)
        }

        /*holder.itemImageLicencePlate.setOnClickListener {
            Log.e(mTAG, "camera click $position")

            val guestInIntent = Intent(mContext, LicensePlateActivity::class.java)
            guestInIntent.putExtra("GUEST_INDEX", position)
            mContext!!.startActivity(guestInIntent)

        }*/


        return view
    }

    class ViewHolder (view: View) {
        var itemData2: TextView = view.findViewById(R.id.guestItemDetailData2)
        var itemData34: TextView = view.findViewById(R.id.guestItemDetailData34)
        var itemBtnLeave: Button = view.findViewById(R.id.receiptItemDetailBtnLeave)
        //var itemImageLicencePlate: ImageView = view.findViewById(R.id.imageViewCamera)



        /*init {
            this.itemHeader = view.findViewById(R.id.receiptItemDetailHeader)
            this.itemContent = view.findViewById(R.id.receiptItemDetailContent)
            this.itemLayout = view.findViewById(R.id.receiptItemDetailLayout)
            this.itemEdit = view.findViewById(R.id.receiptItemDetailEdit)
            this.itemBtnOk = view.findViewById(R.id.receiptItemDetailBtnOk)
        }*/
    }
}