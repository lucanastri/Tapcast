package com.kizune.tapcast.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class FirebaseViewModel(
    private val state: SavedStateHandle
): ViewModel() {
    private companion object {
        const val LOGIN_KEY = "login"
        const val REGISTER_KEY = "register"
        const val TOKEN_KEY = "token"
    }

    val loginUser = state.getStateFlow(LOGIN_KEY, false)
    val registerUser = state.getStateFlow(REGISTER_KEY, false)
    private val idToken = state.getStateFlow(TOKEN_KEY, "")

    fun setLoginUser(value: Boolean) {
        state[LOGIN_KEY] = value
    }

    fun setRegisterUser(value: Boolean) {
        state[REGISTER_KEY] = value
    }

    fun getIDToken(): String = idToken.value

    fun setIDToken(value: String) {
        state[TOKEN_KEY] = value
    }

}