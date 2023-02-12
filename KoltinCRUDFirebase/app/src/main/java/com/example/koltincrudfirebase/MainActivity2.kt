package com.example.koltincrudfirebase

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity2 : AppCompatActivity() {

    val rootRef = FirebaseFirestore.getInstance()
    val query = rootRef!!.collection("notes").orderBy("title", Query.Direction.ASCENDING)
    private lateinit var recyclerView : RecyclerView
    private var adapter: NotesFirestoreRecyclerAdapter? = null


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.getItemId()

        if (id == R.id.refresh) {
            Toast.makeText(this, "Item One Clicked", Toast.LENGTH_LONG).show()
            return true
        }else if (id == R.id.logout) {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Konfirmasi")
            builder.setMessage("Yakin ingin keluar?")

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->



                val sharedPref =  getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE)
                sharedPref.edit().clear().commit()

                val intent = Intent (this, MainLogin::class.java)
                startActivity(intent)
                finish()


            }

            builder.setNegativeButton(android.R.string.no) { dialog, which ->
                dialog.dismiss()
            }

            builder.show()

            return true
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        val fab = findViewById<FloatingActionButton>(R.id.fabAction)

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheet_add, null)
        val btnClose = view.findViewById<ImageView>(R.id.btnDismiss)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setContentView(view)


        fab.setOnClickListener {
            dialog.show()
        }


        val options = FirestoreRecyclerOptions.Builder<Notes>().setQuery(query, Notes::class.java).build()
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

    private inner class NotesViewHolder constructor(private val view: View) : RecyclerView.ViewHolder(view) {
        fun setNotes(notesModel: Notes) {
            val title: TextView = view.findViewById(R.id.title)
            val desc: TextView = view.findViewById(R.id.desc)
            val edit: TextView = view.findViewById(R.id.btnEdit)
            val delete: TextView = view.findViewById(R.id.btnDelete)

            title.text = notesModel.title
            desc.text = notesModel.desc

            edit.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    //onClick.edit(position)
                }
            })


            delete.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    //onClick.delete(position)
                }
            })
        }
    }

    private inner class NotesFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<Notes>) : FirestoreRecyclerAdapter<Notes, NotesViewHolder>(options) {

        override fun onBindViewHolder(NotesViewHolder: NotesViewHolder, position: Int, notesModel: Notes) {
            NotesViewHolder.setNotes(notesModel)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.items, parent, false)
            return NotesViewHolder(view)
        }
    }


}