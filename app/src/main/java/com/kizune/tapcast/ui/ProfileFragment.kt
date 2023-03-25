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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kizune.tapcast.GlideApp
import com.kizune.tapcast.R
import com.kizune.tapcast.animation.fadeIn
import com.kizune.tapcast.animation.fadeOut
import com.kizune.tapcast.databinding.FragmentProfileBinding
import com.kizune.tapcast.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var onNavigateUpCallback: OnBackPressedCallback
    private lateinit var auth: FirebaseAuth

    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        onNavigateUpCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            onNavigateUpCallback.handleOnBackPressed()
        }
        binding.profileIconCard.setOnClickListener {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        binding.usernameInputText.addTextChangedListener { text ->
            binding.registrationButton.isEnabled = !text.isNullOrBlank() && text.length >= 3
        }

        binding.usernameInputText.setText(auth.currentUser?.displayName)
        binding.registrationButton.setOnClickListener {
            binding.loadingBar.fadeIn {
                binding.registrationButton.isEnabled = false
                updateUser()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.editPhotoURI.collect { uri ->
                val photoRef = when(profileViewModel.isEditPhotoUriEmpty()) {
                    true -> Firebase.storage.reference
                        .child("Users")
                        .child(auth.uid ?: "")
                        .child("${auth.uid}.png")
                    else -> uri
                }

                GlideApp.with(binding.profileIcon)
                    .load(photoRef)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.profileIcon)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUser() {
        val username = binding.usernameInputText.text.toString()
        val user = auth.currentUser
        val update = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()
        user?.updateProfile(update)?.addOnSuccessListener {
            updatePhoto()
        }?.addOnFailureListener { exception ->
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

    private fun updatePhoto() {
        if (!profileViewModel.isEditPhotoUriEmpty()) {
            Firebase.storage.reference
                .child("Users")
                .child(auth.uid ?: "")
                .child("${auth.uid}.png")
                .putFile(profileViewModel.getEditPhotoUri()).addOnSuccessListener {
                    onNavigateUpCallback.handleOnBackPressed()
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
            onNavigateUpCallback.handleOnBackPressed()
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                profileViewModel.setEditPhotoUri(data?.data ?: Uri.parse(""))
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
}