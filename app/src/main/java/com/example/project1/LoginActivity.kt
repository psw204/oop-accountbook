package com.example.project1

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import android.widget.Toast

class LoginActivity : AppCompatActivity()
{
    private lateinit var userName:EditText
    private lateinit var userPass:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()

        userName = findViewById(R.id.login_text_name)
        userPass = findViewById(R.id.login_text_password)
        val loginButton:MaterialButton = findViewById(R.id.login_proceed)
        val createNew:TextView = findViewById(R.id.create_new_account)

        createNew.setOnClickListener {
            val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val name = userName.text.toString()
            val pass = userPass.text.toString()

            if (name.isNotBlank())
            {
                if (pass.isNotBlank())
                {
                    checkCredentials(name,pass)
                }
                else
                {
                    userPass.error = "비밀번호 공백"
                }
            }
            else
            {
                userName.error = "아이디 공백"
            }
        }
    }
    fun checkCredentials(name:String,pass:String)
    {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.child(name).get().addOnSuccessListener {
            if (it.exists()) {
                val user = it.getValue(UserModel::class.java)
                if (user != null && user.pass == pass) {
                    // Login successful
                    Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()

                    val editor = getSharedPreferences("LoginPrefs", MODE_PRIVATE).edit()
                    editor.putBoolean("isLoggedIn", true)
                    editor.putString("username", name)
                    editor.apply()

                    val intent = Intent(this@LoginActivity, MainHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Invalid password
                    userPass.error = "비밀번호가 일치하지 않습니다"
                }
            } else {
                // User does not exist
                userName.error = "존재하지 않는 아이디입니다"
                }
        }.addOnFailureListener {
            Toast.makeText(this, "로그인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}