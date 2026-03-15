package com.onlystack.starterapp.core.sync

sealed class ResolutionStrategy {
    object ServerWins : ResolutionStrategy()
    object ClientWins : ResolutionStrategy()
    object MostRecent : ResolutionStrategy()
    object Merge : ResolutionStrategy()
    object Manual : ResolutionStrategy()
}

object ConflictResolver {
    fun resolve(conflict: SyncConflict, strategy: ResolutionStrategy): ResolutionStrategy {
        return when (strategy) {
            is ResolutionStrategy.MostRecent -> ResolutionStrategy.MostRecent
            else -> strategy
        }
    }
}
