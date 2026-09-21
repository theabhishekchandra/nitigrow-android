package com.websbaba.nitigrow.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.websbaba.nitigrow.core.ui.theme.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

/**
 * Persists the user's selected [AppTheme] in its own DataStore file, separate
 * from the session prefs in [TokenDataStore] so the choice survives logout.
 * Unknown/missing keys resolve to [AppTheme.Default] via [AppTheme.fromKey].
 */
@Singleton
class ThemeDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyAppTheme = stringPreferencesKey("app_theme")

    val theme: Flow<AppTheme> =
        context.themeDataStore.data.map { AppTheme.fromKey(it[keyAppTheme]) }

    suspend fun setTheme(theme: AppTheme) {
        context.themeDataStore.edit { it[keyAppTheme] = theme.key }
    }
}
