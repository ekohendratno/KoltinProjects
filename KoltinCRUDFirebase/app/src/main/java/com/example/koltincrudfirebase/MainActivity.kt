package com.example.koltincrudfirebase

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {
    private lateinit var mAdapter: FirestoreRecyclerAdapter<Notes, NotesViewHolder>
    private val mFirestore = FirebaseFirestore.getInstance()
    private val mUsersCollection = mFirestore.collection("notes")
    private val mQuery = mUsersCollection.orderBy("tanggal", Query.Direction.DESCENDING)
    private lateinit var recyclerView : RecyclerView


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
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
        }



        setupAdapter()
    }



    private fun setupAdapter() {

        val fab = findViewById<FloatingActionButton>(R.id.fabAction)

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheet_add, null)
        val btnClose = view.findViewById<Button>(R.id.idBtnDismiss)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setContentView(view)


        fab.setOnClickListener {
            dialog.show()
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

        //set adapter yang akan menampilkan data pada recyclerview
        val options = FirestoreRecyclerOptions.Builder<Notes>()
            .setQuery(mUsersCollection, Notes::class.java)
            .build()

        mAdapter = object : FirestoreRecyclerAdapter<Notes, NotesViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
                return NotesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.items, parent, false))
            }

            override fun onBindViewHolder(viewHolder: NotesViewHolder, position: Int, model: Notes) {
                viewHolder.bindItem(model)
                viewHolder.itemView.setOnClickListener {
                    dialog.show()
                }
            }
        }
        mAdapter.notifyDataSetChanged()
        recyclerView.adapter = mAdapter
    }

    class NotesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bindItem(note: Notes) {
            view.apply {
                val title = "Title   : ${note.strTitle}"
                val desc = "Desc : ${note.strDesc}"

                val et_title = view.findViewById<EditText>(R.id.et_title)
                val et_desc = view.findViewById<EditText>(R.id.et_desc)

                et_title.setText(title)
                et_desc.setText(desc)

            }
        }
    }

}