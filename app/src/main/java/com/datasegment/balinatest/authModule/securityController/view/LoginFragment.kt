package com.datasegment.balinatest.authModule.securityController.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.datasegment.balinatest.R
import com.datasegment.balinatest.authModule.securityController.model.SignInModel
import com.datasegment.balinatest.authModule.securityController.viewModel.AuthNetworkManager
import com.datasegment.balinatest.mainModule.MainActivity
import org.json.JSONObject
import java.util.regex.Pattern

class LoginFragment : Fragment() {

    private lateinit var loginButton: Button
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private val signInModel = SignInModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        loginButton = view.findViewById(R.id.buttonLogin)
        loginButton.isEnabled = false
        val authNetworkManager = AuthNetworkManager()
        val signInModel = SignInModel()
        loginButton.setOnClickListener {
            val login = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()
            authNetworkManager.sendLoginRequest(login, password) { responseData ->
                if (responseData != null) {
                    val jsonResponse = JSONObject(responseData)
                    val status = jsonResponse.getInt("status")
                    val dataSTR = jsonResponse.getString("data")
                    val dataJSON = JSONObject(dataSTR)
                    signInModel.userToken = dataJSON.getString("token")
                    signInModel.userName = dataJSON.getString("login")
                    Log.d("userToken+userName", signInModel.userToken + "+" + signInModel.userName)

                    if(status == 200){
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        intent.putExtra("USER_TOKEN", signInModel.userToken)
                        intent.putExtra("USER_NAME", signInModel.userName)
                        startActivity(intent)

                    } else {
                        showErrorDialog("An error occurred while performing the operation.")
                    }
                } else {
                }
            }
        }
        editTextUsername = view.findViewById(R.id.editTextUsername)
        editTextPassword = view.findViewById(R.id.editTextPassword)

        editTextUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkInputValidity()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkInputValidity()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun checkInputValidity() {
        val username = editTextUsername.text.toString()
        val password = editTextPassword.text.toString()

        val usernamePattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,}\$"
        val passwordPattern = "^.{8,500}$"

        val usernameMatcher = Pattern.compile(usernamePattern).matcher(username)
        val passwordMatcher = Pattern.compile(passwordPattern).matcher(password)

        if (!usernameMatcher.matches()) {
            editTextUsername.error = "email format incorrect"
        } else {
            editTextUsername.error = null
        }

        if (!passwordMatcher.matches()) {
            editTextPassword.error = "The password must contain at least 8 characters."
        } else {
            editTextPassword.error = null
        }
        loginButton.isEnabled = usernameMatcher.matches() && passwordMatcher.matches()
    }

    private fun showErrorDialog(message: String) {
        requireActivity().runOnUiThread {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Error")
            builder.setMessage(message)
            builder.setPositiveButton("OK", null)
            val dialog = builder.create()
            dialog.show()
        }
    }

    fun getUserToken(): String? {
        return signInModel.userToken
    }

    fun getUserName(): String? {
        return signInModel.userName
    }
}