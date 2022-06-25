package com.dogukan.stockmarketapp.domain.repository

import com.dogukan.stockmarketapp.data.local.CompanyListingEntity
import com.dogukan.stockmarketapp.domain.model.CompanyListing
import com.dogukan.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    suspend fun getCompanyListing(
        fetchFromRemote : Boolean,
        query : String
    ) : Flow<Resource<List<CompanyListing>>>
}