package uz.isoft.imessage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_user.view.*
import uz.isoft.imessage.*

class ContactAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var data = ArrayList<Contact>()
    private var callBack: CallBack? = null
    private var temp = ArrayList<Message>()

    fun setCallBack(callBack: CallBack){
        this.callBack = callBack
    }
    fun setData(data: ArrayList<Contact>) {
        this.data = data
        notifyDataSetChanged()
    }

    fun getData() = data

    fun setCount(messages:ArrayList<Message>){
        temp = messages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindItem(data[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var count =0
        fun onBindItem(contact: Contact) {

            if (contact.flag == 1) {

                temp.forEach {
                    if(it.from ==contact.uid){
                        count ++
                    }
                }
                itemView.apply {
                    //                if(contact.image!=""){
//                    Picasso.get()
//                        .load(SERVER_IMAGE_ADDRESS+contact.image)
//                        .placeholder(R.drawable.ic_account)
//                        .into(ivUser)
//                }
                    tvCount.text = count.toString()
                    tvName.text = contact.name
                    tvMessage.text = contact.phone
                    setOnClickListener {
                        callBack?.onItemClick(contact)
                    }
//                tvTime.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(contact.time?: System.currentTimeMillis()))
                }
            }
        }
    }

    interface CallBack{
        fun onItemClick(contactModel: Contact)
    }
}