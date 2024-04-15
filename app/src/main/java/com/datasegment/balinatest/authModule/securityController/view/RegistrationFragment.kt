package com.datasegment.balinatest.authModule.securityController.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import com.datasegment.balinatest.R
import com.datasegment.balinatest.authModule.securityController.viewModel.AuthNetworkManager
import com.datasegment.balinatest.mainModule.MainActivity
import org.json.JSONObject
import java.util.regex.Pattern

class RegistrationFragment : Fragment() {
    private lateinit var regButton: Button
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextPasswordDuplicate: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registration, container, false)
        regButton = view.findViewById(R.id.buttonRegistation)
        regButton.isEnabled = false
        val authNetworkManager = AuthNetworkManager()
        regButton.setOnClickListener {
            val login = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()
            authNetworkManager.sendRegistrationRequest(login, password) { responseData ->
                if (responseData != null) {
                    val jsonResponse = JSONObject(responseData)
                    val status = jsonResponse.getInt("status")
                    if(status == 200){
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        showErrorDialog("An error occurred while performing the operation.")
                    }
                    println(responseData)
                } else {
                    println("Error occurred while sending registration request.")
                }
            }
        }
        editTextUsername = view.findViewById(R.id.editTextUsername)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        editTextPasswordDuplicate = view.findViewById(R.id.editTextPasswordDuplicate)
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

        editTextPasswordDuplicate.addTextChangedListener(object : TextWatcher {
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
        val passwordDuplicate = editTextPasswordDuplicate.text.toString()

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

        if (password != passwordDuplicate) {
            editTextPasswordDuplicate.error = "Passwords do not match."
        } else {
            editTextPasswordDuplicate.error = null
        }
        regButton.isEnabled = usernameMatcher.matches() && passwordMatcher.matches() && password == passwordDuplicate
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
}