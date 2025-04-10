package com.cs_499.capstone

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext

// Sign in or register a user
class SignIn : AppCompatActivity() {
    var user: DBHelper.User = DBHelper.User(0,"", "","")
    val TAG: String = "SignIn"
    var registerEnabled: Boolean = false

    private val coroutineContext: CoroutineContext = newSingleThreadContext("searchDB")
    private val scope = CoroutineScope(coroutineContext)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val registerBtn: Button = findViewById(R.id.register)
        val usernameEText: EditText = findViewById(R.id.username)
        val passwordEText: EditText = findViewById(R.id.password)
        var verify_passwordEText: EditText = findViewById(R.id.verify_password)
        val loginBtn: Button = findViewById(R.id.login)

        // when the text is changed on any of the fields, validations are called
        // to see whether to enable the signin or register buttons
        usernameEText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (validateFields(usernameEText, passwordEText, loginBtn, registerBtn)) {
                    user.username = s.toString()
                }
            }
        })

        passwordEText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (validateFields(usernameEText, passwordEText, loginBtn, registerBtn)) {
                   user.password = s.toString()
                }
            }
        })

        verify_passwordEText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (verifyPassword(passwordEText,verify_passwordEText)) {
                    registerEnabled = true
                } else {
                    registerEnabled = false
                }
            }
        })
    }

    // when returning to the login screen (on logout) clear the username and password fields
    override fun onResume() {
        super.onResume()
        var username: EditText = findViewById(R.id.username)
        var password: EditText = findViewById(R.id.password)
        password.setText("")
        username.setText("")

    }

    // ensure passwords match
    fun verifyPassword(password: EditText, verify_password: EditText): Boolean {
        if (password.text.toString().compareTo(verify_password.text.toString()) == 0 ) {
            return true
        } else {
            return false
        }
    }

    // ensure username and password fields are not empty
    fun validateFields(username: EditText, password: EditText, login: Button, register: Button): Boolean {
        if (username.text.isNotEmpty() && password.text.isNotEmpty()) {
            login.isEnabled = true
            register.isEnabled = true
            return true
        } else {
            login.isEnabled = false
            register.isEnabled = false
            return false
        }
    }

    // register user; display toasts if already registered or passwords do not match
    fun register(view: View) {
        val usernameEText: EditText = findViewById(R.id.username)
        val passwordEText: EditText = findViewById(R.id.password)
        val verify_passwordEText: EditText = findViewById(R.id.verify_password)

        user.username = usernameEText.text.toString()
        user.password = passwordEText.text.toString()

         val db = DBHelper.EventDatabase.getDatabaseInstance(this)

         scope.launch {
             var ids: List<Long> = emptyList()
             try {
                 ids = db.userDao().retrieveId(user.username!!)
             } catch (e: Exception) {
                 Log.d(TAG, e.toString())
             }
             if (ids.isEmpty()) {
                 Log.d(TAG, "Can register user")
                 //Register User
                 if (verifyPassword(passwordEText,verify_passwordEText)) {
                     try {
                         var salt = randomSalt()
                         Log.d(TAG, salt.toString())
                         user.password = hashPass(passwordEText.text.toString(), salt)
                         user.salt = salt
                         db.userDao().insertUser(listOf(user))
                         Log.d(TAG, "user " + usernameEText.text.toString() + " inserted")
                         Thread(Runnable {
                             this@SignIn.runOnUiThread {
                                 val verify_password: EditText = findViewById(R.id.verify_password)
                                 verify_password.visibility = View.GONE
                                 verify_password.setText("")
                                 val toast =
                                     Toast.makeText(this@SignIn, "User registered", Toast.LENGTH_LONG)
                                 toast.show()

                             }
                         }).start()
                     } catch (e: Exception) {
                         Log.d(TAG, e.toString())
                     }
                 } else {
                     Thread(Runnable {
                         this@SignIn.runOnUiThread {
                             val verify_password: EditText = findViewById(R.id.verify_password)
                             verify_password.visibility = View.VISIBLE
                             if (verify_password.text.toString().isNotEmpty()) {
                                 val toast = Toast.makeText(
                                     this@SignIn,
                                     "Passwords do not match",
                                     Toast.LENGTH_LONG
                                 )
                                 toast.show()
                             }

                         }
                 }).start()
                }
             } else if (ids.size == 1) {
                 Log.d(TAG, "user exists")
             Thread(Runnable {
                 this@SignIn.runOnUiThread {
                     val toast = Toast.makeText(this@SignIn, "User exists", Toast.LENGTH_LONG)
                     toast.show()
                 }
             }).start()
             } else {
                 Log.d(TAG, "ERROR: More than one user record exists")
             }
         }
     }

    // sign in
    // displays toast if credentials are not found in database
    fun signIn(view: View) {

        val usernameEText: EditText = findViewById(R.id.username)
        val passwordEText: EditText = findViewById(R.id.password)

        var username = usernameEText.text.toString()
        var password = passwordEText.text.toString()

        user.username = username
        user.password = password

        var db = DBHelper.EventDatabase.getDatabaseInstance(this)
        scope.launch {
            try {
                var salt = db.userDao().getSalt(user.username)



                var ids: List<DBHelper.User> = db.userDao().login(user.username, hashPass(user.password, salt))

                if (ids.isEmpty()) {
                    Thread(Runnable {
                        this@SignIn.runOnUiThread {
                            val toast = Toast.makeText(this@SignIn, "Invalid credentials", Toast.LENGTH_LONG)
                            toast.show()
                        }
                    }).start()
                } else if (ids.size == 1) {
                    val intent = Intent(this@SignIn, ShowEvents::class.java)
                    Log.d(TAG, ids[0].id.toString())
                    intent.putExtra("userId", ids[0].id)
                    startActivity(intent)
                } else {
                    Log.d(TAG, "error, more than one " + username + " registered")
                }
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
            }
        }
    }
}
