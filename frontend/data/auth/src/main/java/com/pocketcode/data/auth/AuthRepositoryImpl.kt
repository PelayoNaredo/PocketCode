package com.pocketcode.data.auth

import android.content.Context
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pocketcode.domain.auth.model.AuthFailure
import com.pocketcode.domain.auth.model.AuthFailureException
import com.pocketcode.domain.auth.model.AuthUser
import com.pocketcode.domain.auth.model.DeveloperKey
import com.pocketcode.domain.auth.model.SignUpRequest
import com.pocketcode.domain.auth.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

private val Context.developerKeyDataStore by preferencesDataStore(name = "developer_keys")

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    private val dataStore: DataStore<Preferences> by lazy { context.developerKeyDataStore }
    private val secureRandom = SecureRandom()

    override suspend fun signUp(request: SignUpRequest): Result<AuthUser> {
        return runCatching {
            val result = firebaseAuth.createUserWithEmailAndPassword(request.email, request.password).await()
            val firebaseUser = result.user ?: throw IllegalStateException("firebase_user_missing")

            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(request.fullName)
                .build()
            firebaseUser.updateProfile(profileUpdate).await()

            request.byokKey
                ?.takeIf { it.isNotBlank() }
                ?.let { upsertDeveloperKey(firebaseUser.uid, it).getOrThrow() }

            AuthUser(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: request.email,
                displayName = firebaseUser.displayName ?: request.fullName,
                createdAt = Instant.now()
            )
        }.mapError()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return runCatching {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Unit
        }.mapError()
    }

    override suspend fun upsertDeveloperKey(userId: String, developerKey: String): Result<DeveloperKey> {
        return runCatching {
            val key = developerKey.trim()
            require(key.length >= 24) { "developer_key_too_short" }
            val preferenceKey = stringPreferencesKey("developer_key_$userId")
            dataStore.edit { preferences ->
                preferences[preferenceKey] = key
            }
            DeveloperKey(value = key)
        }.mapError()
    }

    override suspend fun generateDeveloperKey(userId: String): Result<DeveloperKey> {
        return runCatching {
            val randomBytes = ByteArray(32)
            secureRandom.nextBytes(randomBytes)
            val key = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
            upsertDeveloperKey(userId, key).getOrThrow()
        }.mapError()
    }

    override fun mapException(throwable: Throwable): AuthFailure {
        return when (throwable) {
            is FirebaseAuthUserCollisionException -> AuthFailure.EmailAlreadyInUse(throwable.message)
            is FirebaseAuthWeakPasswordException -> AuthFailure.WeakPassword(throwable.reason)
            is FirebaseAuthInvalidCredentialsException -> AuthFailure.InvalidCredentials(throwable.message)
            is FirebaseNetworkException -> AuthFailure.Network(throwable.message)
            is FirebaseAuthException -> AuthFailure.Unknown(throwable.message)
            else -> AuthFailure.Unknown(throwable.message)
        }
    }

    private fun <T> Result<T>.mapError(): Result<T> {
        return this.fold(
            onSuccess = { Result.success(it) },
            onFailure = { throwable ->
                val failure = when (throwable) {
                    is AuthFailureException -> throwable.failure
                    else -> mapException(throwable)
                }
                Result.failure(AuthFailureException(failure))
            }
        )
    }
}
