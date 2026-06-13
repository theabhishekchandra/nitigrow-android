package com.websbaba.nitigrow.domain.usecase.leads

import com.websbaba.nitigrow.domain.model.Lead
import com.websbaba.nitigrow.domain.repository.LeadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLeadsUseCase @Inject constructor(
    private val repo: LeadRepository
) {
    operator fun invoke(): Flow<List<Lead>> = repo.observeLeads()
}
