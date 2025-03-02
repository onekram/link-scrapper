package backend.academy.bot.repository.state

import backend.academy.bot.state.State

data class StateRecord(
    val current: State,
    val previous: State
)
