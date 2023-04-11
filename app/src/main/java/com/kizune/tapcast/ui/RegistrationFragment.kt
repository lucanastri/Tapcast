package com.kizune.tapcast.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.kizune.tapcast.R
import com.kizune.tapcast.animation.fadeIn
import com.kizune.tapcast.animation.fadeOut
import com.kizune.tapcast.databinding.FragmentRegistrationBinding
import com.kizune.tapcast.viewmodel.FirebaseViewModel
import com.kizune.tapcast.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch


class RegistrationFragment : Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var onNavigateUpCallback: OnBackPressedCallback
    private lateinit var oneTapClient: SignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference

    private val args: RegistrationFragmentArgs by navArgs()
    private lateinit var idToken: String

    private val firebaseViewModel: FirebaseViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        storageRef = Firebase.storage.reference

        oneTapClient = Identity.getSignInClient(requireActivity())
        onNavigateUpCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            oneTapClient.signOut()
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        idToken = args.idToken

        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.photoURI.collect { uri ->
                binding.profileIcon.load(uri)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            firebaseViewModel.loginUser.collect { login ->
                if (login) {
                    val action =
                        RegistrationFragmentDirections.actionRegistrationFragmentToDashboardFragment()
                    findNavController().navigate(action)
                    firebaseViewModel.setLoginUser(false)
                }
            }
        }

        binding.backButton.setOnClickListener {
            onNavigateUpCallback.handleOnBackPressed()
        }
        binding.profileIconCard.setOnClickListener {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        binding.usernameInputText.addTextChangedListener { text ->
            binding.registrationButton.isEnabled = !text.isNullOrBlank() && text.length >= 3
        }
        binding.registrationButton.setOnClickListener {
            binding.loadingBar.fadeIn {
                binding.registrationButton.isEnabled = false
                profileViewModel.setUsername(binding.usernameInputText.text.toString())
                registerToFirebase()
            }
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    profileViewModel.setPhotoUri(data.data ?: Uri.parse(""))
                }
            }
        }

    private var requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                resultLauncher.launch(intent)
            } else {
                Toast.makeText(
                    requireContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun registerToFirebase() {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential).addOnSuccessListener {
            storeUserData()
        }.addOnFailureListener { exception ->
            Toast.makeText(
                requireContext(),
                getString(R.string.connection_error),
                Toast.LENGTH_SHORT
            ).show()
            binding.loadingBar.fadeOut {
                binding.registrationButton.isEnabled = true
            }
            Log.e("MyTag", "SignInWithCredential Exception", exception)
        }
    }

    private fun storeUserData() {
        val user = auth.currentUser
        if (user != null) {
            val update = UserProfileChangeRequest.Builder()
                .setDisplayName(profileViewModel.getUsername())
                .build()
            user.updateProfile(update).addOnSuccessListener {
                storeUserProfilePhoto(user)
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.connection_error),
                    Toast.LENGTH_SHORT
                ).show()
                binding.loadingBar.fadeOut {
                    binding.registrationButton.isEnabled = true
                }
                Log.e("MyTag", "Store Fields Exception", exception)
            }
        }
    }

    private fun storeUserProfilePhoto(user: FirebaseUser) {
        val uid = user.uid
        if (!profileViewModel.isPhotoUriEmpty()) {
            storageRef.child("Users").child(uid).child("$uid.png")
                .putFile(profileViewModel.getPhotoUri()).addOnSuccessListener {
                    firebaseViewModel.setLoginUser(true)
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.connection_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.loadingBar.fadeOut {
                        binding.registrationButton.isEnabled = true
                    }
                    Log.e("MyTag", "Store User Profile Photo Exception", exception)
                }
        } else {
            firebaseViewModel.setLoginUser(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}