package vn.edu.hust.activityexamples

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import android.Manifest
import android.provider.Settings
import android.widget.AdapterView

import androidx.appcompat.app.AlertDialog
import java.io.IOException

class MainActivity : AppCompatActivity() {

    lateinit var textResult: TextView
    val items = arrayListOf<String>()

    private lateinit var fileAdapter: FileAdapter
    private lateinit var currentDirectory: File
    private lateinit var recyclerView: RecyclerView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        registerForContextMenu(recyclerView)

        if (Build.VERSION.SDK_INT < 30) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                Log.v("TAG", "Permission Denied")
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1234)
            } else
                Log.v("TAG", "Permission Granted")
        } else {
            if (!Environment.isExternalStorageManager()) {
                Log.v("TAG", "Permission Denied")
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            } else {
                Log.v("TAG", "Permission Granted")
            }
        }



        currentDirectory = Environment.getExternalStorageDirectory()
        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        val root = Environment.getExternalStorageDirectory()
        val files = getFiles(root)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = FileAdapter(files, object : FileAdapter.OnItemClickListener {
            override fun onItemClick(file: File) {
                if (file.isDirectory) {
                    displayFolderContents(file)
                } else {
                }
            }

            override fun onItemLongClick(item: File, view: View): Boolean {
                // Xử lý sự kiện nhấn giữ
                return true
            }
        })
    }

    private fun displayFolderContents(folder: File) {
        val subItems = getFiles(folder)
        (recyclerView.adapter as? FileAdapter)?.updateData(subItems)
    }

    private fun getFiles(directory: File): List<File> {
        val fileList = ArrayList<File>()
        val files = directory.listFiles()

        if (files != null) {
            fileList.addAll(files)
        }

        return fileList
    }

    private fun showRenameDialog(file: File) {
        val editText = EditText(this)
        editText.setText(file.name)

        AlertDialog.Builder(this)
            .setTitle("Rename File")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    fileAdapter.renameFile(this, file, newName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    fun renameFile(oldFile: File, newName: String) {
        val newFile = File(oldFile.parent, newName)
        if (oldFile.renameTo(newFile)) {
//            notifyDataSetChanged()
        } else {
            Toast.makeText(this, "Failed to rename file", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showDeleteConfirmationDialog(file: File) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete_title)
            .setMessage(getString(R.string.confirm_delete_message, file.name))
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                deleteFolder(file)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun deleteFolder(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteFolder(it) }
        }
        file.delete()
        (recyclerView.adapter as? FileAdapter)?.updateData(getFiles(currentDirectory))
    }

    private fun createNewFolder(parentFolder: File, folderName: String) {
        val newFolder = File(parentFolder, folderName)

        if (!newFolder.exists()) {
            if (newFolder.mkdir()) {
                // Thành công: Thư mục mới đã được tạo
                Toast.makeText(this, "Folder created successfully", Toast.LENGTH_SHORT).show()

                // Refresh danh sách thư mục nếu cần
                // Ví dụ: displayFolderContents(currentDirectory)
                displayFolderContents(currentDirectory)
            } else {
                // Lỗi: Không thể tạo thư mục
                Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Thông báo: Thư mục đã tồn tại
            Toast.makeText(this, "Folder already exists", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNewTextFile(parentFolder: File, fileName: String) {
        val newFile = File(parentFolder, fileName)

        if (!newFile.exists()) {
            try {
                if (newFile.createNewFile()) {
                    // Thành công: File mới đã được tạo
                    Toast.makeText(this, "Text file created successfully", Toast.LENGTH_SHORT).show()

                    // Refresh danh sách thư mục nếu cần
                    // Ví dụ: displayFolderContents(currentDirectory)
                    displayFolderContents(currentDirectory)
                } else {
                    // Lỗi: Không thể tạo file mới
                    Toast.makeText(this, "Failed to create text file", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                // Xử lý ngoại lệ nếu có lỗi IOException
                e.printStackTrace()
            }
        } else {
            // Thông báo: File đã tồn tại
            Toast.makeText(this, "Text file already exists", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCreateFolderDialog() {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this)
        builder.setTitle("Tạo thư mục mới")
            .setView(input)
            .setPositiveButton("Tạo") { _, _ ->
                val folderName = input.text.toString().trim()
                if (folderName.isNotEmpty()) {
                    createNewFolder(folderName)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showCreateTextFileDialog() {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this)
        builder.setTitle("Tạo file văn bản mới")
            .setView(input)
            .setPositiveButton("Tạo") { _, _ ->
                val fileName = input.text.toString().trim()
                if (fileName.isNotEmpty()) {
                    createNewTextFile(fileName)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun createNewFolder(folderName: String) {
        val newFolder = File(currentDirectory, folderName)

        if (!newFolder.exists()) {
            if (newFolder.mkdir()) {
                Toast.makeText(this, "Tạo thư mục mới thành công", Toast.LENGTH_SHORT).show()

                displayFolderContents(currentDirectory)
            } else {
                Toast.makeText(this, "Lỗi tạo thư mục ", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Thư mục đã tồn tại", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNewTextFile(fileName: String) {
        val newFile = File(currentDirectory, fileName)

        if (!newFile.exists()) {
            try {
                if (newFile.createNewFile()) {
                    Toast.makeText(this, "Tạo file văn bản thành công", Toast.LENGTH_SHORT).show()

                    displayFolderContents(currentDirectory)
                } else {
                    Toast.makeText(this, "Lỗi tạo file văn bản", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "File văn bản đã tồn tại", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        menuInflater.inflate(R.menu.context_menu, menu)
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val file = (recyclerView.adapter as FileAdapter).getItem(info.position)
        if (item.itemId == R.id.context_menu_rename) {
            showRenameDialog(file)
            return true
        } else if (item.itemId == R.id.context_menu_delete) {
            showDeleteConfirmationDialog(file)
            return true
        }
        return super.onContextItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 123) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data?.getIntExtra("result", 0)
                textResult.text = "Result: $result"
            } else {
                textResult.text = "Failed"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_new_folder) {
            showCreateFolderDialog()
            return true
        } else if (item.itemId == R.id.action_new_text_file) {
            showCreateTextFileDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}