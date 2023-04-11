package com.kizune.tapcast.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kizune.tapcast.R
import com.kizune.tapcast.animation.fadeIn
import com.kizune.tapcast.animation.fadeOut
import com.kizune.tapcast.databinding.FragmentLoginBinding
import com.kizune.tapcast.viewmodel.FirebaseViewModel
import com.kizune.tapcast.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var auth: FirebaseAuth

    private val firebaseViewModel: FirebaseViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        oneTapClient = Identity.getSignInClient(requireActivity())
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.google_token))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().moveTaskToBack(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            firebaseViewModel.registerUser.collect { register ->
                val idToken = firebaseViewModel.getIDToken()
                if(idToken.isNotBlank() && register) {
                    profileViewModel.setPhotoUri(Uri.parse(""))
                    val action =
                        LoginFragmentDirections.actionLoginFragmentToRegistrationFragment(idToken)
                    findNavController().navigate(action)
                    firebaseViewModel.setRegisterUser(false)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            firebaseViewModel.loginUser.collect { login ->
                if(login) {
                    Log.d("MyTag", (login).toString())
                    val action =
                        LoginFragmentDirections.actionLoginFragmentToDashboardFragment()
                    findNavController().navigate(action)
                    firebaseViewModel.setLoginUser(false)
                }
            }
        }

        binding.appLogo.load(R.drawable.logo)
        binding.loginButton.setOnClickListener {
            oneTapClient.beginSignIn(signInRequest).addOnSuccessListener { result ->
                val intentSenderRequest =
                    IntentSenderRequest.Builder(result.pendingIntent.intentSender)
                        .build()
                resultLauncherOneTap.launch(intentSenderRequest)
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.connection_error),
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("MyTag", "BeginSignInRequest exception:", exception)
            }
        }
    }

    private var resultLauncherOneTap =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val credential = oneTapClient.getSignInCredentialFromIntent(intent)
                val idToken = credential.googleIdToken
                val mail = credential.id

                if (idToken != null) {
                    binding.loadingBar.fadeIn {
                        firebaseViewModel.setIDToken(idToken)
                        binding.loginButton.isEnabled = false
                        checkIfUserExists(idToken, mail)
                    }
                }
            }
        }

    private fun checkIfUserExists(idToken: String, mail: String) {
        auth.fetchSignInMethodsForEmail(mail).addOnSuccessListener { result ->
            val methods = result.signInMethods
            if (methods.isNullOrEmpty()) {
                firebaseViewModel.setIDToken(idToken)
                firebaseViewModel.setRegisterUser(true)
            } else {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                loginToFirebase(firebaseCredential)
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(
                requireContext(),
                getString(R.string.connection_error),
                Toast.LENGTH_SHORT
            ).show()
            binding.loadingBar.fadeOut {
                binding.loginButton.isEnabled = true
            }
            Log.e("MyTag", "Check If User Exists exception:", exception)
        }
    }

    private fun loginToFirebase(firebaseCredential: AuthCredential) {
        auth.signInWithCredential(firebaseCredential).addOnSuccessListener {
            firebaseViewModel.setLoginUser(true)
        }.addOnFailureListener { exception ->
            Toast.makeText(
                requireContext(),
                getString(R.string.connection_error),
                Toast.LENGTH_SHORT
            ).show()
            binding.loadingBar.fadeOut {
                binding.loginButton.isEnabled = true
            }
            Log.e("MyTag", "Login To Firebase exception:", exception)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}