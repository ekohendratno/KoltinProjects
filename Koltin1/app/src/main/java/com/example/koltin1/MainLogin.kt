package com.example.koltin1

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.EditText

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainLogin  : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)



        var et_username = findViewById(R.id.et_email) as EditText
        var et_password = findViewById(R.id.et_password) as EditText

        findViewById<Button>(R.id.btnReset).setOnClickListener {
            // clearing user_name and password edit text views on reset button click
            et_username.setText("")
            et_password.setText("")
        }


        // set on-click listener
        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val email = et_username.text.toString()
            val password = et_password.text.toString()
            if (email.isEmpty()|| password.isEmpty()) {
                Toast.makeText(this, "Please Insert Email and Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(email.equals("admin@mail.com") && password.equals("admin")){



                val sharedPref =  getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putBoolean("_login", true)
                    apply()
                }

                redirectLogin()

            }

        }

        redirectLogin()


    }


    fun redirectLogin(){


        val sharedPref =  getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE)
        val _login = sharedPref.getBoolean("_login",false)

        if(_login == true){


            val progressDialog = ProgressDialog(this, R.style.Theme_Koltin1)
            progressDialog.isIndeterminate = true
            progressDialog.setMessage("Loading...")
            progressDialog.show()

            Handler().postDelayed({

                val intent = Intent (this, MainActivity::class.java)
                startActivity(intent)
                finish()


                if(progressDialog.isShowing){
                    progressDialog.dismiss()
                }

            },1500)

        }

    }
}