package com.kizune.tapcast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.kizune.tapcast.adapter.SearchAdapter
import com.kizune.tapcast.databinding.FragmentSearchBinding
import com.kizune.tapcast.model.Podcast
import com.kizune.tapcast.ui.VerticalSpaceItemDecoration
import com.kizune.tapcast.viewmodel.PodcastViewModel
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var onNavigateUpCallback: OnBackPressedCallback

    private val viewModel: PodcastViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transition =
            TransitionInflater
                .from(requireContext())
                .inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = transition
        sharedElementReturnTransition = null

        onNavigateUpCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onItemClickListener: (Podcast) -> Unit = { item ->
            viewModel.setSelectedPodcast(item)
            val action = SearchFragmentDirections.actionSearchFragmentToPodcastFragment()
            findNavController().navigate(action)
        }
        val searchAdapter = SearchAdapter(onItemClickListener)
        val linearLayoutManager = LinearLayoutManager(requireContext())

        binding.searchInputText.requestFocus()
        binding.searchInputText.addTextChangedListener { text ->
            searchAdapter.filter.filter(text.toString())
            linearLayoutManager.scrollToPositionWithOffset(0, 0)
        }

        val itemDecoration = VerticalSpaceItemDecoration(16)
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.addItemDecoration(itemDecoration)
        binding.recyclerView.adapter = searchAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.podcasts.collect { result ->
                val lista = result.flatMap { it.list }
                searchAdapter.setBaseList(lista)
            }
        }
    }
}