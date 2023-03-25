package com.kizune.tapcast.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.kizune.tapcast.R
import com.kizune.tapcast.adapter.SettingsAdapter
import com.kizune.tapcast.data.SettingDataSource
import com.kizune.tapcast.databinding.FragmentSettingsBinding
import com.kizune.tapcast.model.SettingID
import com.kizune.tapcast.viewmodel.ProfileViewModel

class SettingsFragment : Fragment(),
    CustomDialogFragment.CustomDialogListener {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var onNavigateUpCallback: OnBackPressedCallback

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        storage = Firebase.storage

        oneTapClient = Identity.getSignInClient(requireActivity())
        signInRequest = BeginSignInRequest.builder()
            .setAutoSelectEnabled(true)
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.google_token))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        onNavigateUpCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            onNavigateUpCallback.handleOnBackPressed()
        }

        val adapter = SettingsAdapter(
            SettingDataSource.getData(requireContext()),
            onSettingItemClickListener
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(VerticalSpaceItemDecoration(16))
    }

    private val onSettingItemClickListener: (SettingID) -> Unit = { settingID ->
        when (settingID) {
            SettingID.VIEW_PROFILE -> {
                profileViewModel.setEditPhotoUri(Uri.parse(""))
                val action = SettingsFragmentDirections.actionSettingsFragmentToProfileFragment()
                findNavController().navigate(action)
            }
            SettingID.LOGOUT -> {
                showLogoutDialog()
            }
            SettingID.DELETE_PROFILE -> {
                showDeleteDialog()
            }
            SettingID.AUTOPLAY -> {
                Toast.makeText(requireContext(), "Under Development", Toast.LENGTH_SHORT).show()
            }
            SettingID.EXPLICIT -> {
                Toast.makeText(requireContext(), "Under Development", Toast.LENGTH_SHORT).show()
            }
            SettingID.INTERRUPTION -> {
                Toast.makeText(requireContext(), "Under Development", Toast.LENGTH_SHORT).show()
            }
            SettingID.VERSION -> {

            }
            SettingID.TERMS -> {
                Toast.makeText(requireContext(), "Under Development", Toast.LENGTH_SHORT).show()
            }
            SettingID.PRIVACY -> {
                Toast.makeText(requireContext(), "Under Development", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteDialog() {
        val dialogFragment = CustomDialogFragment()
        val args = Bundle()
        args.putInt("type", CustomDialogType.DELETE_ACCOUNT.ordinal)
        args.putInt("title", R.string.delete_profile_title)
        args.putInt("message", R.string.delete_profile_message)
        args.putInt("positiveButtonText", R.string.confirm)
        args.putInt("negativeButtonText", R.string.cancel)
        dialogFragment.arguments = args
        dialogFragment.show(childFragmentManager, "delete")
    }

    private fun showLogoutDialog() {
        val dialogFragment = CustomDialogFragment()
        val args = Bundle()
        args.putInt("type", CustomDialogType.LOGOUT.ordinal)
        args.putInt("title", R.string.account_logout_title)
        args.putInt("message", R.string.account_logout_message)
        args.putInt("positiveButtonText", R.string.account_logout_exit)
        args.putInt("negativeButtonText", R.string.cancel)
        dialogFragment.arguments = args
        dialogFragment.show(childFragmentManager, "logout")
    }

    private var resultLauncherOneTap =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val signInCredential = oneTapClient.getSignInCredentialFromIntent(intent)
                val idToken = signInCredential.googleIdToken
                val mail = signInCredential.id

                if (idToken != null && mail == auth.currentUser?.email) {
                    reauthenticate(idToken)
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.wrong_account_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("MyTag", "Selected account wrong")
                }
            }
        }

    private fun reauthenticate(idToken: String) {
        val authCredential = GoogleAuthProvider.getCredential(idToken, null)
        val user = auth.currentUser
        user?.reauthenticate(authCredential)?.addOnSuccessListener {
            deleteProfileData(user)
        }?.addOnFailureListener { exception ->
            Toast.makeText(
                requireContext(),
                getString(R.string.connection_error),
                Toast.LENGTH_SHORT
            ).show()
            Log.e("MyTag", "Reauthenticate exception:", exception)
        }
    }

    private fun deleteProfileData(user: FirebaseUser?) {
        val uid = user!!.uid
        val file = storage.reference
            .child("Users")
            .child(uid)
            .child("$uid.png")
        file.delete().addOnCompleteListener {
            deleteUser(user)
        }
    }

    private fun deleteUser(user: FirebaseUser?) {
        user?.delete()?.addOnSuccessListener {
            Log.d("MyTag", "User account deleted")
            oneTapClient.signOut()
            auth.signOut()
            val action = SettingsFragmentDirections.actionSettingsFragmentToLoginFragment()
            findNavController().navigate(action)
        }?.addOnFailureListener { exception ->
            Toast.makeText(
                requireContext(),
                getString(R.string.connection_error),
                Toast.LENGTH_SHORT
            ).show()
            Log.e("MyTag", "Deleting user exception:", exception)
        }
    }

    override fun onPositiveButtonClicked(dialog: DialogFragment, type: CustomDialogType) {
        when(type) {
            CustomDialogType.LOGOUT -> {
                oneTapClient.signOut()
                auth.signOut()
                val action = SettingsFragmentDirections.actionSettingsFragmentToLoginFragment()
                findNavController().navigate(action)
                dialog.dismiss()
            }
            CustomDialogType.DELETE_ACCOUNT -> {
                dialog.dismiss()
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
    }

    override fun onNegativeButtonClicked(dialog: DialogFragment, type: CustomDialogType) {
        dialog.dismiss()
    }
}