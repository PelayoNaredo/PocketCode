package com.pocketcode.app.di

import com.pocketcode.core.ui.snackbar.GlobalToastDispatcher
import com.pocketcode.core.ui.snackbar.GlobalToastEvent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Module
@InstallIn(SingletonComponent::class)
object GlobalUiDispatchersModule {

    @Provides
    @Singleton
    fun provideGlobalToastEventBus(): GlobalToastEventBus = GlobalToastEventBus()

    @Provides
    @Singleton
    fun provideGlobalToastDispatcher(bus: GlobalToastEventBus): GlobalToastDispatcher = bus.dispatcher

    @Provides
    @Singleton
    fun provideGlobalToastEvents(bus: GlobalToastEventBus): SharedFlow<GlobalToastEvent> = bus.events
}

class GlobalToastEventBus {
    private val _events = MutableSharedFlow<GlobalToastEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<GlobalToastEvent> = _events.asSharedFlow()

    val dispatcher: GlobalToastDispatcher = GlobalToastDispatcher.from { event ->
        _events.tryEmit(event)
    }
}
