package com.kizune.tapcast.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kizune.tapcast.adapter.DashboardParentAdapter
import com.kizune.tapcast.databinding.FragmentDashboardBinding
import com.kizune.tapcast.model.Podcast
import com.kizune.tapcast.utils.displayGreeting
import com.kizune.tapcast.viewmodel.PodcastViewModel
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var onNavigateUpCallback: OnBackPressedCallback
    private lateinit var auth: FirebaseAuth

    private val viewModel: PodcastViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        onNavigateUpCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().moveTaskToBack(true)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onItemClickListener: (Podcast) -> Unit = { item ->
            viewModel.setSelectedPodcast(item)
            val action = DashboardFragmentDirections.actionDashboardFragmentToPodcastFragment()
            findNavController().navigate(action)
        }

        val parentAdapter = DashboardParentAdapter(onItemClickListener)

        binding.titleMessage.text =
            displayGreeting(requireContext(), auth.currentUser?.displayName ?: "")
        binding.parentRecyclerView.addItemDecoration(VerticalSpaceItemDecoration())
        binding.parentRecyclerView.adapter = parentAdapter
        binding.settingsButton.setOnClickListener {
            val action = DashboardFragmentDirections.actionDashboardFragmentToSettingsFragment()
            findNavController().navigate(action)
        }

        binding.searchInputText.setOnClickListener {
            val extras = FragmentNavigatorExtras(binding.searchInputLayout to "search_to")
            val action = DashboardFragmentDirections.actionDashboardFragmentToSearchFragment()
            findNavController().navigate(
                action,
                extras
            )
        }

        binding.searchInputLayout.setEndIconOnClickListener {
            binding.searchInputText.setText("")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.podcasts.collect { result ->
                parentAdapter.setBaseList(result)
                parentAdapter.submitList(result)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.titleMessage.text =
            displayGreeting(requireContext(), auth.currentUser?.displayName ?: "")
    }

}