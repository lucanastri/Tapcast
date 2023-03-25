package com.kizune.tapcast.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.kizune.tapcast.R
import com.kizune.tapcast.databinding.DialogLayoutBinding

enum class CustomDialogType {
    LOGOUT,
    DELETE_ACCOUNT
}

class CustomDialogFragment : DialogFragment() {
    private lateinit var listener: CustomDialogListener

    interface CustomDialogListener {
        fun onPositiveButtonClicked(dialog: DialogFragment, type: CustomDialogType)
        fun onNegativeButtonClicked(dialog: DialogFragment, type: CustomDialogType)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = parentFragment as CustomDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " has to implement CustomDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val type = CustomDialogType.values()[arguments?.getInt("type") ?: -1]
            val title = arguments?.getInt("title") ?: R.string.cancel
            val message = arguments?.getInt("message") ?: R.string.cancel
            val positiveButtonText = arguments?.getInt("positiveButtonText") ?: R.string.cancel
            val negativeButtonText = arguments?.getInt("negativeButtonText") ?: R.string.cancel
            val binding: DialogLayoutBinding = DialogLayoutBinding.inflate(layoutInflater)
            val builder = AlertDialog.Builder(it)
            binding.title.text = getString(title)
            binding.message.text = getString(message)
            binding.positiveButton.text = getString(positiveButtonText)
            binding.positiveButton.setOnClickListener {
                listener.onPositiveButtonClicked(this, type)
            }
            binding.negativeButton.text = getString(negativeButtonText)
            binding.negativeButton.setOnClickListener {
                listener.onNegativeButtonClicked(this, type)
            }

            builder.setCancelable(true)
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}