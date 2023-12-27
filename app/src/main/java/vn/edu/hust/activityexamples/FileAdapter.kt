package vn.edu.hust.activityexamples

import android.content.Context
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileAdapter(private var items: List<File>, private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: File)
        fun onItemLongClick(item: File, view: View): Boolean
    }

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_file, parent, false)
//        view.setOnCreateContextMenuListener { menu, _, _ ->
//            (parent.context as AppCompatActivity).menuInflater.inflate(R.menu.context_menu, menu)
//        }
//        return ViewHolder(view)
//    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)

        holder.itemView.setOnClickListener { onItemClickListener.onItemClick(item) }
        holder.itemView.setOnLongClickListener {
            onItemClickListener.onItemLongClick(item, holder.itemView)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getItem(position: Int): File {
        return items[position]
    }

    fun updateData(newItems: List<File>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun renameFile(context: Context, oldFile: File, newName: String) {
        val newFile = File(oldFile.parent, newName)
        if (oldFile.renameTo(newFile)) {
            notifyDataSetChanged()
        } else {
            Toast.makeText(context, "Failed to rename file", Toast.LENGTH_SHORT).show()
        }
    }

//    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val icon: ImageView = itemView.findViewById(R.id.icon)
//        private val textFileName: TextView = itemView.findViewById(R.id.textFileName)
//
//        init {
//            itemView.setOnCreateContextMenuListener(this)
//        }
//
//        fun bind(item: File) {
//            textFileName.text = item.name
//            if (item.isDirectory) {
//                icon.setImageResource(R.drawable.baseline_folder_24)
//            } else {
//                icon.setImageResource(R.drawable.baseline_folder_24)
//            }
//        }
//    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val textFileName: TextView = itemView.findViewById(R.id.textFileName)

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bind(item: File) {
            textFileName.text = item.name
            if (item.isDirectory) {
                icon.setImageResource(R.drawable.baseline_folder_24)
            } else {
                icon.setImageResource(R.drawable.baseline_folder_24)
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            (itemView.context as? AppCompatActivity)?.menuInflater?.inflate(R.menu.context_menu, menu)

            menu?.setHeaderTitle("Context Menu Title")
            menu?.add(0, R.id.context_menu_rename, 0, "Item 1")?.setOnMenuItemClickListener {
                handleContextMenuItemClick("Item 1")
                true
            }
            menu?.add(0, R.id.context_menu_delete, 1, "Item 2")?.setOnMenuItemClickListener {
                handleContextMenuItemClick("Item 2")
                true
            }
        }

        private fun handleContextMenuItemClick(itemTitle: String) {
            val item = items[adapterPosition]
            when (itemTitle) {
                "Item 1" -> {
                }
                "Item 2" -> {
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }


}