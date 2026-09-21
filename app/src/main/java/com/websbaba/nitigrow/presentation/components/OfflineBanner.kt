package com.websbaba.nitigrow.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ConnectionState
import com.websbaba.nitigrow.core.network.ConnectivityObserver
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiType
import com.websbaba.nitigrow.core.work.PendingMessageScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    observer: ConnectivityObserver,
    private val pendingScheduler: PendingMessageScheduler
) : ViewModel() {

    val state: StateFlow<ConnectionState> = observer.observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConnectionState.Available)

    init {
        observer.observe()
            .onEach { s -> if (s == ConnectionState.Available) pendingScheduler.enqueue() }
            .launchIn(viewModelScope)
    }
}

@Composable
fun OfflineBanner(vm: ConnectivityViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    AnimatedVisibility(
        visible = state == ConnectionState.Lost,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        val colors = Niti.colors
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceHigh)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = NitiIcons.WifiOff,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "You're offline. Changes will sync when connected.",
                style = NitiType.label,
                color = colors.onSurface
            )
        }
    }
}
