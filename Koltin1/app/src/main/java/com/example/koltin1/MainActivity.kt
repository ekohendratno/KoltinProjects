package com.example.koltin1

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var dataList = mutableListOf<Notes>()

    var jsonData : String = ""
    private lateinit var empty_template:  TextView


    private lateinit var alertDialog : AlertDialog.Builder

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.getItemId()

        if (id == R.id.refresh) {
            getDataFromManual()
            //getDataFromAPI()
            viewAdapter.notifyDataSetChanged()

            Toast.makeText(this, "Data diperbaharui", Toast.LENGTH_LONG).show()
            return true
        }else if (id == R.id.logout) {

            alertDialog.setMessage("Yakin ingin keluar?")
            alertDialog.setTitle("Konfirmasi")

            alertDialog.setPositiveButton(android.R.string.yes) { dialog, which ->



                val sharedPref =  getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE)
                sharedPref.edit().clear().commit()

                val intent = Intent (this, MainLogin::class.java)
                startActivity(intent)
                finish()


            }

            alertDialog.setNegativeButton(android.R.string.no) { dialog, which ->
                dialog.dismiss()
            }

            alertDialog.show()

            return true
        }
        return super.onOptionsItemSelected(item)

    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        getDataFromManual()
        //getDataFromAPI()

        empty_template = findViewById<TextView>(R.id.empty_template)
        val fab = findViewById<FloatingActionButton>(R.id.fabAction)

        alertDialog = AlertDialog.Builder(this)

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheet_add, null)

        val dialog_title = view.findViewById<TextView>(R.id.dialog_title)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val btnDismiss = view.findViewById<ImageView>(R.id.btnDismiss)

        val et_key = view.findViewById<EditText>(R.id.et_key)
        val et_title = view.findViewById<EditText>(R.id.et_title)
        val et_desc = view.findViewById<EditText>(R.id.et_desc)

        btnDismiss.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setContentView(view)

        btnSubmit.setOnClickListener{

            val key = et_key.text.toString()
            val title = et_title.text.toString()
            val desc = et_desc.text.toString()

            if(title.isEmpty() || desc.isEmpty()){
                Toast.makeText(applicationContext,"Beberapa data masih kosong! harap diisi",Toast.LENGTH_SHORT).show()
            }else{

                if(!key.isEmpty() ){

                    dataList.set(key.toInt(),Notes(title, desc))

                }else{
                    dataList.add(Notes(title, desc))

                }

                dialog.dismiss()
                viewAdapter.notifyDataSetChanged()
                Toast.makeText(applicationContext,"Data telah berhasil disimpan",Toast.LENGTH_SHORT).show()
            }

        }

        // Inisialisasi RecyclerView
        viewManager = LinearLayoutManager(this)
        viewAdapter = DataAdapter(dataList, object : DataAdapter.OnClick{
            override fun edit(position: Int) {
                dialog.show()
                dialog_title.setText("Ubah Notes")

                val data = dataList[position]

                et_key.setText(position.toString())
                et_title.setText(data.title)
                et_desc.setText(data.desc)

                viewAdapter.notifyDataSetChanged()
            }

            override fun delete(position: Int) {

                alertDialog.setTitle("Konfirmasi")
                alertDialog.setMessage("Yakin ingin hapus data?")
                alertDialog.setPositiveButton(android.R.string.yes) { dialog, which ->


                    dataList.removeAt(position)

                    viewAdapter.notifyDataSetChanged()
                    Toast.makeText(applicationContext,"Data telah berhasil dihapus",Toast.LENGTH_SHORT).show()

                }

                alertDialog.setNegativeButton(android.R.string.no) { dialog, which ->
                    dialog.dismiss()
                }

                alertDialog.show()
            }

        })

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)){
                    //function that add new elements to my recycler view
                    fab.hide()
                }else{

                    fab.show()

                }

            }

        })

        fab.setOnClickListener {
            dialog_title.setText("Buat Notes")

            et_key.setText("")
            et_title.setText("")
            et_desc.setText("")

            dialog.show()
        }

    }



    class DataAdapter( private val dataList: List<Notes>, val onClick : OnClick) : RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

        open interface OnClick {
            fun edit(position: Int)
            fun delete(position: Int)
        }

        class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val title: TextView = itemView.findViewById(R.id.title)
            val desc: TextView = itemView.findViewById(R.id.desc)
            val edit: TextView = itemView.findViewById(R.id.btnEdit)
            val delete: TextView = itemView.findViewById(R.id.btnDelete)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.items, parent, false)
            return DataViewHolder(view)
        }

        override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
            val data = dataList[position]
            holder.title.text = data.title
            holder.desc.text = data.desc

            holder.edit.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    onClick.edit(position)
                }
            })


            holder.delete.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    onClick.delete(position)
                }
            })

        }

        override fun getItemCount() = dataList.size

    }

    private fun getDataFromManual(){
        dataList.clear()
        // Mendapatkan data JSON dari sumber tertentu
        jsonData = "[" +
                "{\"title\":\"Isi Title A\",\"desc\":\"Isi keterangan a\"}," +
                "{\"title\":\"Isi Title A\",\"desc\":\"Isi keterangan a\"}," +
                "{\"title\":\"Isi Title A\",\"desc\":\"Isi keterangan a\"}," +
                "{\"title\":\"Isi Title A\",\"desc\":\"Isi keterangan a\"}," +
                "{\"title\":\"Isi Title A\",\"desc\":\"Isi keterangan a\"}," +
                "{\"title\":\"Isi Title A\",\"desc\":\"Isi keterangan a\"}," +
                "{\"title\":\"Isi Title A\",\"desc\":\"Isi keterangan a\"}," +
                "{\"title\":\"Isi Title A\",\"desc\":\"Isi keterangan a\"}," +
                "{\"title\":\"Isi Title A\",\"desc\":\"Isi keterangan a\"}," +
                "{\"title\":\"Isi Title A\",\"desc\":\"Isi keterangan a\"}" +
                "]"

        // Menerjemahkan data JSON menjadi objek Java
        val dataArray = JSONArray(jsonData)
        for (i in 0 until dataArray.length()) {
            val data = dataArray.getJSONObject(i)

            val title = data.getString("title")
            val desc = data.getString("desc")

            dataList.add(Notes(title, desc))
        }
    }

    private fun getDataFromAPI() {
        dataList.clear()

        val url = "http://10.0.2.2/notes"
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handling failure request
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string()

                runOnUiThread {
                    // Menerjemahkan data JSON menjadi objek Java
                    val dataArray = JSONArray(jsonData)
                    for (i in 0 until dataArray.length()) {
                        val data = dataArray.getJSONObject(i)
                        val title = data.getString("title")
                        val desc = data.getString("desc")
                        dataList.add(Notes(title,desc))
                    }

                    viewAdapter.notifyDataSetChanged()

                }
            }
        })
    }
}