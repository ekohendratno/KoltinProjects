package com.example.koltincrudfirebase

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.checkerframework.common.subtyping.qual.Bottom
import java.util.*


class MainActivity : AppCompatActivity() {

    val rootRef = FirebaseFirestore.getInstance()
    val query = rootRef!!.collection("notes")
    val sorty = query.orderBy("tanggal", Query.Direction.DESCENDING)
    private lateinit var recyclerView : RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var adapter: NotesFirestoreRecyclerAdapter? = null
    private lateinit var dialog: BottomSheetDialog


    private lateinit var empty_template:  TextView
    private lateinit var alertDialog : AlertDialog.Builder
    private lateinit var dialog_title : TextView

    private lateinit var et_key : EditText
    private lateinit var et_title : EditText
    private lateinit var et_desc : EditText

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.getItemId()

        if (id == R.id.refresh) {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        alertDialog = AlertDialog.Builder(this)
        dialog = BottomSheetDialog(this)

        viewManager = LinearLayoutManager(this)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = adapter
        }


        empty_template = findViewById<TextView>(R.id.empty_template)
        val fab = findViewById<FloatingActionButton>(R.id.fabAction)


        val view = layoutInflater.inflate(R.layout.bottomsheet_add, null)

        dialog_title = view.findViewById<TextView>(R.id.dialog_title)

        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val btnDismiss = view.findViewById<ImageView>(R.id.btnDismiss)

        et_key = view.findViewById<EditText>(R.id.et_key)
        et_title = view.findViewById<EditText>(R.id.et_title)
        et_desc = view.findViewById<EditText>(R.id.et_desc)

        btnDismiss.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setContentView(view)

        btnSubmit.setOnClickListener{

            val key = et_key.text.toString()
            val title = et_title.text.toString()
            val desc = et_desc.text.toString()


            val tanggal = Calendar.getInstance().timeInMillis.toString()

            if(title.isEmpty() || desc.isEmpty()){
                Toast.makeText(applicationContext,"Beberapa data masih kosong! harap diisi",Toast.LENGTH_SHORT).show()
            }else{

                if(!key.isEmpty() ){
                    query.document(key).set(Notes(title, desc, tanggal)).addOnCompleteListener { task: Task<Void> ->
                        if (task.isSuccessful) {
                            println("Successfully updated data")
                        } else {
                            println(task.exception)
                        }
                    }
                }else{
                    query.add(Notes(title, desc, tanggal))
                }

                dialog.dismiss()
                Toast.makeText(applicationContext,"Data telah berhasil disimpan",Toast.LENGTH_SHORT).show()
            }

        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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


        val options = FirestoreRecyclerOptions.Builder<Notes>().setQuery(sorty, Notes::class.java).build()
        adapter = NotesFirestoreRecyclerAdapter(options)
        recyclerView.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()

        if (adapter != null) {
            adapter!!.stopListening()
        }
    }

    private inner class NotesFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<Notes>) : FirestoreRecyclerAdapter<Notes, NotesFirestoreRecyclerAdapter.NotesViewHolder>(options) {

        override fun onBindViewHolder(notesViewHolder: NotesViewHolder, position: Int, notesModel: Notes) {
            notesViewHolder.setNotes(notesModel, position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.items, parent, false)
            return NotesViewHolder(view)
        }

        inner class NotesViewHolder constructor(private val view: View) : RecyclerView.ViewHolder(view) {
            fun setNotes(notesModel: Notes, position: Int) {

                val key = getSnapshots().getSnapshot(position).getId().toString();

                val title: TextView = view.findViewById(R.id.title)
                val desc: TextView = view.findViewById(R.id.desc)
                val edit: TextView = view.findViewById(R.id.btnEdit)
                val delete: TextView = view.findViewById(R.id.btnDelete)

                title.text = notesModel.title
                desc.text = notesModel.desc

                edit.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        Log.e("__", key);

                        dialog.show()
                        dialog_title.setText("Ubah Notes")

                        et_key.setText(key)
                        et_title.setText(notesModel.title)
                        et_desc.setText(notesModel.desc)


                    }
                })


                delete.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        Log.e("__", key);

                        alertDialog.setTitle("Konfirmasi")
                        alertDialog.setMessage("Yakin ingin hapus data?")
                        alertDialog.setPositiveButton(android.R.string.yes) { dialog, which ->

                            query.document(key).delete()

                            Toast.makeText(applicationContext,"Data telah berhasil dihapus",Toast.LENGTH_SHORT).show()
                        }
                        alertDialog.setNegativeButton(android.R.string.no) { dialog, which ->
                            dialog.dismiss()
                        }

                        alertDialog.show()

                    }
                })


            }
        }

    }




}