package com.cabe.app.watch.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.cabe.app.watch.R
import com.cabe.app.watch.db.BChatInfo
import com.cabe.app.watch.db.DBHelper
import kotlinx.android.synthetic.main.activity_remote_list.*
import kotlinx.android.synthetic.main.chat_list_item_layout.view.*
import java.text.SimpleDateFormat
import java.util.*

const val SP_KEY_LAST_TABLE = "spKeyLastTable"
class RemoteListActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        title = "RemoteData"
        setContentView(R.layout.activity_remote_list)

        val adapter = MyAdapter()
        activity_chat_recycler.adapter = adapter
        activity_chat_recycler.setScrollCallback {
            val tableName = remote_config_table.text.toString()
            val chatName = remote_config_filter.text.toString()
            DBHelper.queryRemoteData(tableName, chatName, adapter.itemCount) { dataList ->
                adapter.appendData(dataList)
                if(dataList == null || dataList.isEmpty()) {
                    activity_chat_recycler.setScrollEnd(true)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        val lastTable = SPUtils.getInstance().getString(SP_KEY_LAST_TABLE)
        if(!TextUtils.isEmpty(lastTable)) {
            remote_config_table.setText(lastTable)
        }
        remote_config_btn.setOnClickListener {
            activity_chat_recycler.setScrollEnd(false)
            val tableName = remote_config_table.text.toString()
            val chatName = remote_config_filter.text.toString()
            if(TextUtils.isEmpty(tableName)) {
                ToastUtils.showShort("input str")
            } else {
                SPUtils.getInstance().put(SP_KEY_LAST_TABLE, tableName)
                KeyboardUtils.hideSoftInput(it)
                DBHelper.queryRemoteData(tableName, chatName, 0) { dataList ->
                    adapter.dataList = dataList
                    if(dataList == null || dataList.isEmpty()) {
                        activity_chat_recycler.setScrollEnd(true)
                        adapter.notifyDataSetChanged()
                        ToastUtils.showShort("暂无数据")
                    }
                }
            }
        }
    }

    inner class MyAdapter: RecyclerView.Adapter<MyViewHolder>() {
        var dataList: MutableList<BChatInfo>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }
        fun appendData(data: List<BChatInfo>?) {
            if(data != null && data.isNotEmpty()) {
                val lastPosition = itemCount
                dataList?.addAll(data)
                notifyItemInserted(lastPosition)
            }
        }
        private fun getItemData(position: Int): BChatInfo? {
            return dataList?.get(position)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder.create(parent)
        }
        override fun getItemCount(): Int {
            return dataList?.size ?: 0
        }
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            getItemData(position)?.let { data ->
                holder.bind(data)
            }
        }
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup): MyViewHolder {
                return MyViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.chat_list_item_layout,
                        parent,
                        false
                    )
                )
            }
        }
        private val format = SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss", Locale.getDefault())
        fun bind(chatInfo: BChatInfo) {
            itemView.apply {
                chat_list_item_name.text = chatInfo.chatName
                chat_list_item_content.text = chatInfo.content
                val type = if(chatInfo.type == CHAT_TYPE_WX) "WX" else if(chatInfo.type == CHAT_TYPE_QQ) "QQ" else ""
                chat_list_item_time.text = "${format.format(chatInfo.timestamp)}$type"
                setBackgroundResource(if(chatInfo.type == CHAT_TYPE_QQ) R.drawable.selector_btn_bg_yellow else R.drawable.selector_btn_bg_white)
            }
        }
    }
}