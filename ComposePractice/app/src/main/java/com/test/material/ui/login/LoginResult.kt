package com.test.material.ui.login

data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)
