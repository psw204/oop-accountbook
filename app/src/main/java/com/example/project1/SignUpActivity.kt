package com.example.project1

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*

class SignUpActivity : AppCompatActivity()
{
    lateinit var databaseReference: DatabaseReference
    lateinit var userName:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.hide()

        userName = findViewById(R.id.signup_text_name)
        val userPass:EditText = findViewById(R.id.signup_text_password)
        val userCPass:EditText = findViewById(R.id.signup_text_confirm_password)
        val createAccount:MaterialButton = findViewById(R.id.create_account)
        val alreadyAccount:TextView = findViewById(R.id.already_account)

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        createAccount.setOnClickListener {
            val name = userName.text.toString()
            val pass = userPass.text.toString()
            val cPass = userCPass.text.toString()

            if (name.isNotBlank() && name.isNotEmpty())
            {
                if (pass == cPass)
                {
                    userNameAvailable(name,pass)
                }
                else if (pass.isEmpty())
                {
                    userPass.error = "비밀번호 공백"
                }
                else
                {
                    userPass.error = "비밀번호 입력과 다시 입력이 다름"
                }
            }
            else
            {
                userName.error = "아이디 공백"
            }
        }

        alreadyAccount.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

    }

    fun pushData(uName:String,uPass:String)
    {
        val userModel = UserModel(uName,uPass)
        databaseReference.child(uName).setValue(userModel)
            .addOnSuccessListener {
                Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()

                // Save login state and username
                val editor = getSharedPreferences("LoginPrefs", MODE_PRIVATE).edit()
                editor.putBoolean("isLoggedIn", true)
                editor.putString("username", uName)
                editor.apply()

                val intent = Intent(this, MainHomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
               startActivity(intent)
                finish()
            }
            .addOnFailureListener {
            Toast.makeText(applicationContext,"계정 생성 불가",Toast.LENGTH_SHORT).show()
        }
    }

    private fun userNameAvailable(uName: String, uPass: String)
    {
        val data:DatabaseReference = FirebaseDatabase.getInstance().getReference()
        val userNameReference:DatabaseReference = data.child("Users").child(uName)
        val valueEventListener = object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists())
                {
                    userName.error = null
                    pushData(uName,uPass)
                }
                else
                {
                    userName.error = "아이디 중복"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        userNameReference.addListenerForSingleValueEvent(valueEventListener)
    }
    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


}