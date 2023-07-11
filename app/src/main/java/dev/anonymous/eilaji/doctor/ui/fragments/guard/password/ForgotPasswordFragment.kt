package dev.anonymous.eilaji.doctor.ui.fragments.guard.password

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dev.anonymous.eilaji.doctor.R
import dev.anonymous.eilaji.doctor.databinding.FragmentForgotPasswordBinding
import dev.anonymous.eilaji.doctor.firebase.FirebaseController

class ForgotPasswordFragment : Fragment() {
    private lateinit var _binding: FragmentForgotPasswordBinding
    private val binding get() = _binding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        val navController = findNavController()
        with(binding) {

            buSend.setOnClickListener {
                val email = edEmail.text.toString()
                // Firebase
                if (email.isNotEmpty()) {
                    FirebaseController.getInstance().forgotPassword(email,
                        object : FirebaseController.ForgotPasswordListener {
                            override fun onTaskSuccessful() {
                                // Handle successful task
                                Snackbar.make(root,
                                    "Check Your Email Address For The Restore Message!✔✨👌",
                                    Snackbar.LENGTH_LONG).show()
                                navController.navigate(R.id.action_navigation_forgotPassword_to_navigation_login)
                            }
                            override fun onTaskFailed(exception: Exception) {
                                // Handle failed task
                                Log.e("ForgotPasswordViewModel", "setupListeners: Ex:"+ exception.localizedMessage)
                                Snackbar.make(root, exception.localizedMessage!!, Snackbar.LENGTH_LONG).show()
                            }
                        })
                } else {
                    Snackbar.make(root, "Enter Your Email", Snackbar.LENGTH_LONG).show()
                }
            }

        }
    }
}