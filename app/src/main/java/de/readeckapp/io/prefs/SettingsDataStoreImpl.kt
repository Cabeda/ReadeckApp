package de.readeckapp.io.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import de.readeckapp.domain.model.AutoSyncTimeframe
import de.readeckapp.domain.model.DefaultFilter
import de.readeckapp.domain.model.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStoreImpl @Inject constructor(@ApplicationContext private val context: Context) :
    SettingsDataStore {

    private val encryptedSharedPreferences = EncryptionHelper.getEncryptedSharedPreferences(context)

    private val KEY_USERNAME = stringPreferencesKey("username")
    private val KEY_TOKEN = stringPreferencesKey("token")
    private val KEY_URL = stringPreferencesKey("url")
    private val KEY_AUTH_STATE = stringPreferencesKey("auth_state")
    private val KEY_LAST_BOOKMARK_TIMESTAMP = stringPreferencesKey("lastBookmarkTimestamp")
    private val KEY_LAST_SYNC_TIMESTAMP = stringPreferencesKey("lastSyncTimestamp")
    private val KEY_INITIAL_SYNC_PERFORMED = "initial_sync_performed"
    private val KEY_AUTOSYNC_ENABLED = booleanPreferencesKey("autosync_enabled")
    private val KEY_AUTOSYNC_TIMEFRAME = stringPreferencesKey("autosync_timeframe")
    private val KEY_THEME = stringPreferencesKey("theme")
    private val KEY_ZOOM_FACTOR = intPreferencesKey("zoom_factor")
    private val KEY_SYNC_READ_PROGRESS = booleanPreferencesKey("sync_read_progress")
    private val KEY_SCROLL_TO_PROGRESS = booleanPreferencesKey("scroll_to_progress")
    private val KEY_DEFAULT_FILTER = stringPreferencesKey("default_filter")

    override fun saveUsername(username: String) {
        Timber.d("saveUsername")
        encryptedSharedPreferences.edit {
            putString(KEY_USERNAME.name, username)
        }
    }

    override fun saveAuthState(authState: String) {
        Timber.d("saveAuthState")
        encryptedSharedPreferences.edit {
            putString(KEY_AUTH_STATE.name, authState)
        }
    }

    override fun saveToken(token: String) {
        Timber.d("saveToken")
        encryptedSharedPreferences.edit {
            putString(KEY_TOKEN.name, token)
        }
    }

    override fun saveUrl(url: String) {
        Timber.d("saveUrl")
        encryptedSharedPreferences.edit {
            putString(KEY_URL.name, url)
        }
    }

    override suspend fun saveLastBookmarkTimestamp(timestamp: Instant) {
        encryptedSharedPreferences.edit(commit = true) {
            putString(KEY_LAST_BOOKMARK_TIMESTAMP.name, timestamp.toString())
        }
    }

    override suspend fun getLastBookmarkTimestamp(): Instant? {
        return encryptedSharedPreferences.getString(KEY_LAST_BOOKMARK_TIMESTAMP.name, null)?.let {
            Instant.parse(it)
        }
    }

    override suspend fun saveLastSyncTimestamp(timestamp: Instant) {
        encryptedSharedPreferences.edit(commit = true) {
            putString(KEY_LAST_SYNC_TIMESTAMP.name, timestamp.toString())
        }
    }

    override suspend fun getLastSyncTimestamp(): Instant? {
        return encryptedSharedPreferences.getString(KEY_LAST_SYNC_TIMESTAMP.name, null)?.let {
            Instant.parse(it)
        }
    }

    override suspend fun setInitialSyncPerformed(performed: Boolean) {
        encryptedSharedPreferences.edit(commit = true) {
            putBoolean(KEY_INITIAL_SYNC_PERFORMED, performed)
        }
    }

    override suspend fun isInitialSyncPerformed(): Boolean {
        return encryptedSharedPreferences.getBoolean(KEY_INITIAL_SYNC_PERFORMED, false)
    }

    override suspend fun isAutoSyncEnabled(): Boolean {
        return encryptedSharedPreferences.getBoolean(KEY_AUTOSYNC_ENABLED.name, false)
    }

    override suspend fun setAutoSyncEnabled(isEnabled: Boolean) {
        encryptedSharedPreferences.edit(commit = true) {
            putBoolean(KEY_AUTOSYNC_ENABLED.name, isEnabled)
        }
    }

    override suspend fun getAutoSyncTimeframe(): AutoSyncTimeframe {
        return encryptedSharedPreferences.getString(KEY_AUTOSYNC_TIMEFRAME.name, AutoSyncTimeframe.MANUAL.name)?.let {
            AutoSyncTimeframe.valueOf(it)
        } ?: AutoSyncTimeframe.MANUAL
    }

    override suspend fun saveAutoSyncTimeframe(autoSyncTimeframe: AutoSyncTimeframe) {
        Timber.d("saveAutoSyncTimeframe")
        encryptedSharedPreferences.edit(commit = true) {
            putString(KEY_AUTOSYNC_TIMEFRAME.name, autoSyncTimeframe.name)
        }
    }

    override suspend fun getTheme(): Theme {
        return encryptedSharedPreferences.getString(KEY_THEME.name, Theme.SYSTEM.name)?.let {
            Theme.valueOf(it)
        } ?: Theme.SYSTEM
    }

    override suspend fun saveTheme(theme: Theme) {
        encryptedSharedPreferences.edit(commit = true) {
            putString(KEY_THEME.name, theme.name)
        }
    }

    override suspend fun getZoomFactor(): Int {
        return encryptedSharedPreferences.getInt(KEY_ZOOM_FACTOR.name, 100)
    }

    override suspend fun saveZoomFactor(zoomFactor: Int) {
        encryptedSharedPreferences.edit(commit = true) {
            putInt(KEY_ZOOM_FACTOR.name, zoomFactor.coerceIn(25, 400))
        }
    }

    override suspend fun isSyncReadProgressEnabled(): Boolean {
        return encryptedSharedPreferences.getBoolean(KEY_SYNC_READ_PROGRESS.name, true)
    }

    override suspend fun setSyncReadProgressEnabled(enabled: Boolean) {
        encryptedSharedPreferences.edit(commit = true) {
            putBoolean(KEY_SYNC_READ_PROGRESS.name, enabled)
        }
    }

    override suspend fun isScrollToProgressEnabled(): Boolean {
        return encryptedSharedPreferences.getBoolean(KEY_SCROLL_TO_PROGRESS.name, true)
    }

    override suspend fun setScrollToProgressEnabled(enabled: Boolean) {
        encryptedSharedPreferences.edit(commit = true) {
            putBoolean(KEY_SCROLL_TO_PROGRESS.name, enabled)
        }
    }

    override suspend fun getDefaultFilter(): DefaultFilter {
        return encryptedSharedPreferences.getString(KEY_DEFAULT_FILTER.name, DefaultFilter.ALL.name)?.let {
            DefaultFilter.valueOf(it)
        } ?: DefaultFilter.ALL
    }

    override suspend fun saveDefaultFilter(defaultFilter: DefaultFilter) {
        encryptedSharedPreferences.edit(commit = true) {
            putString(KEY_DEFAULT_FILTER.name, defaultFilter.name)
        }
    }

    override val tokenFlow = getStringFlow(KEY_TOKEN.name, null)
    override val usernameFlow = getStringFlow(KEY_USERNAME.name, null)
    override val urlFlow = getStringFlow(KEY_URL.name, null)
    override val authStateFlow = getStringFlow(KEY_AUTH_STATE.name, null)
    override val themeFlow = getStringFlow(KEY_THEME.name, Theme.SYSTEM.name)
    override val zoomFactorFlow = getIntFlow(KEY_ZOOM_FACTOR.name, 100)
    
    init {
        // Migration: remove legacy password if it exists
        if (encryptedSharedPreferences.contains("password")) {
            Timber.d("Migration: Removing legacy password")
            encryptedSharedPreferences.edit {
                remove("password")
            }
        }

        // Migration: remove /api suffix from url
        encryptedSharedPreferences.getString(KEY_URL.name, null)?.takeIf { it.endsWith("/api") }?.let {
            saveUrl(it.removeSuffix("/api"))
        }
    }

    override suspend fun clearCredentials() {
        Timber.d("clearCredentials")
        encryptedSharedPreferences.edit(commit = true) {
            remove(KEY_USERNAME.name)
            remove(KEY_AUTH_STATE.name)
            remove(KEY_TOKEN.name)
            remove(KEY_URL.name)
        }
    }

    override suspend fun saveCredentials(
        url: String,
        username: String,
        token: String,
        authState: String
    ) {
        Timber.d("saveCredentials")
        encryptedSharedPreferences.edit(commit = true) {
            putString(KEY_URL.name, url)
            putString(KEY_USERNAME.name, username)
            putString(KEY_AUTH_STATE.name, authState)
            putString(KEY_TOKEN.name, token)
        }
    }

    private fun getStringFlow(key: String, defaultValue: String? = null): StateFlow<String?> =
        preferenceFlow(key, defaultValue) { encryptedSharedPreferences.getString(key, defaultValue) }

    private fun getIntFlow(key: String, defaultValue: Int = 100): StateFlow<Int> =
        preferenceFlow(key, defaultValue) { encryptedSharedPreferences.getInt(key, defaultValue) }

    private fun <T> preferenceFlow(key: String, defaultValue: T, getValue: () -> T): StateFlow<T> {
        val state = MutableStateFlow(defaultValue)

        // Read the actual value asynchronously to avoid blocking construction
        // and to ensure EncryptedSharedPreferences is fully initialized
        CoroutineScope(Dispatchers.IO).launch {
            state.value = getValue()
        }

        // Note: EncryptedSharedPreferences encrypts keys, so OnSharedPreferenceChangeListener
        // receives encrypted key names. We cannot reliably match them to original keys.
        // Real-time updates are handled by ViewModels manually calling getX() after saveX().
        return state.asStateFlow()
    }
}
