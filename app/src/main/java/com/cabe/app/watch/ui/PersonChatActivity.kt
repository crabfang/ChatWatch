package com.cabe.app.watch.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.cabe.app.watch.R
import com.cabe.app.watch.db.DBHelper
import com.cabe.app.watch.db.WatchChatInfo
import com.cabe.app.watch.widget.WaitingDialog
import com.cabe.app.watch.widget.toast
import kotlinx.android.synthetic.main.activity_chat_list.*
import kotlinx.android.synthetic.main.chat_person_item_layout.view.*
import java.text.SimpleDateFormat
import java.util.*

const val EXTRA_KEY_CHAT_NAME = "extraKeyChatName"
class PersonChatActivity: BaseActivity() {
    lateinit var chatType: String
    lateinit var chatName: String
    override fun onCreate(savedInstanceState: Bundle?) {
        chatType = intent.getStringExtra(EXTRA_KEY_CHAT_TYPE)
        chatName = intent.getStringExtra(EXTRA_KEY_CHAT_NAME)
        super.onCreate(savedInstanceState)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        title = chatName
        setContentView(R.layout.activity_chat_person)

        val adapter = MyAdapter()
        activity_chat_recycler.adapter = adapter

        DBHelper.queryChatListByName(chatType, chatName).observe(this, Observer {
            adapter.dataList = it
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chat_list_option, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_chat_list_clear -> {
                AlertDialog.Builder(this).apply {
                    setTitle("温馨提示")
                    setMessage("确定要清空记录吗")
                    setPositiveButton("清空") { dialog, _ ->
                        dialog.dismiss()
                        actionClear()
                    }
                    setNegativeButton("取消") { dialog, _ ->
                        dialog.dismiss()
                    }
                }.create().show()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun actionClear() {
        val waiting = WaitingDialog(this)
        waiting.show()
        DBHelper.deleteChatByName(chatType, chatName) {
            waiting.dismiss()

            AlertDialog.Builder(this).apply {
                setMessage("清空成功！")
                setPositiveButton("确定") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
            }.create().show()
        }
    }

    inner class MyAdapter: RecyclerView.Adapter<MyViewHolder>() {
        var dataList: List<WatchChatInfo>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }
        private fun getItemData(position: Int): WatchChatInfo? {
            return dataList?.get(position)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder.create(
                parent
            )
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
        private val format = SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss", Locale.getDefault())
        companion object {
            fun create(parent: ViewGroup): MyViewHolder {
                return MyViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.chat_person_item_layout,
                        parent,
                        false
                    )
                )
            }
        }
        fun bind(chatInfo: WatchChatInfo) {
            itemView.apply {
                chat_list_item_time.text = format.format(chatInfo.timestamp)
                chat_list_item_content.text = chatInfo.content
                itemView.setOnLongClickListener {
                    AlertDialog.Builder(it.context).apply {
                        setTitle("温馨提示")
                        setMessage("确定要删除该记录吗")
                        setPositiveButton("删除") { dialog, _ ->
                            dialog.dismiss()
                            DBHelper.deleteChat(chatInfo) {
                                toast("删除成功")
                            }
                        }
                        setNegativeButton("取消") { dialog, _ ->
                            dialog.dismiss()
                        }
                    }.create().show()
                    true
                }
            }
        }
    }
}