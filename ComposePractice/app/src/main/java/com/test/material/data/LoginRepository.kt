package com.test.material.data

import com.test.material.data.model.LoggedInUser

class LoginRepository(val dataSource: LoginDataSource) {

    private var user: LoggedInUser? = null

    init {
        user = null
    }

    fun login(): Result<LoggedInUser> {
        val result = dataSource.login()

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}
