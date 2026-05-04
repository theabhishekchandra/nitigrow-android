package com.ardym.nitigrow.domain.usecase.leads

import com.ardym.nitigrow.domain.model.Lead
import com.ardym.nitigrow.domain.repository.LeadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLeadsUseCase @Inject constructor(
    private val repo: LeadRepository
) {
    operator fun invoke(): Flow<List<Lead>> = repo.observeLeads()
}
