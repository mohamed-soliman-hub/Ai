package com.aiphone.agent.data.local.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aiphone.agent.domain.model.ProviderType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "ai_phone_agent_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // ── Onboarding ──────────────────────────────────────────────────────
    fun isOnboardingComplete(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    fun setOnboardingComplete(complete: Boolean) =
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply()

    // ── API Keys ──────────────────────────────────────────────────────
    fun getApiKey(provider: ProviderType): String =
        prefs.getString("${KEY_API_KEY_PREFIX}${provider.name}", "") ?: ""

    fun setApiKey(provider: ProviderType, apiKey: String) =
        prefs.edit().putString("${KEY_API_KEY_PREFIX}${provider.name}", apiKey).apply()

    fun hasApiKey(provider: ProviderType): Boolean =
        getApiKey(provider).isNotBlank()

    // ── Provider Selection ──────────────────────────────────────────────
    fun getSelectedProvider(): ProviderType {
        val name = prefs.getString(KEY_SELECTED_PROVIDER, ProviderType.OPENAI.name)
        return runCatching { ProviderType.valueOf(name!!) }.getOrDefault(ProviderType.OPENAI)
    }

    fun setSelectedProvider(provider: ProviderType) =
        prefs.edit().putString(KEY_SELECTED_PROVIDER, provider.name).apply()

    // ── Model Selection ──────────────────────────────────────────────
    fun getSelectedModel(provider: ProviderType): String =
        prefs.getString("${KEY_MODEL_PREFIX}${provider.name}", provider.defaultModel)
            ?: provider.defaultModel

    fun setSelectedModel(provider: ProviderType, model: String) =
        prefs.edit().putString("${KEY_MODEL_PREFIX}${provider.name}", model).apply()

    // ── Sandbox ──────────────────────────────────────────────────────
    fun isSandboxEnabled(): Boolean =
        prefs.getBoolean(KEY_SANDBOX_ENABLED, false)

    fun setSandboxEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_SANDBOX_ENABLED, enabled).apply()

    fun getSandboxFolder(): String =
        prefs.getString(KEY_SANDBOX_FOLDER, "") ?: ""

    fun setSandboxFolder(folder: String) =
        prefs.edit().putString(KEY_SANDBOX_FOLDER, folder).apply()

    // ── Token tracking ──────────────────────────────────────────────
    fun getTotalTokensUsed(): Long =
        prefs.getLong(KEY_TOTAL_TOKENS, 0L)

    fun addTokensUsed(tokens: Int) {
        val current = getTotalTokensUsed()
        prefs.edit().putLong(KEY_TOTAL_TOKENS, current + tokens).apply()
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        private const val KEY_API_KEY_PREFIX = "api_key_"
        private const val KEY_SELECTED_PROVIDER = "selected_provider"
        private const val KEY_MODEL_PREFIX = "model_"
        private const val KEY_SANDBOX_ENABLED = "sandbox_enabled"
        private const val KEY_SANDBOX_FOLDER = "sandbox_folder"
        private const val KEY_TOTAL_TOKENS = "total_tokens"
    }
}
