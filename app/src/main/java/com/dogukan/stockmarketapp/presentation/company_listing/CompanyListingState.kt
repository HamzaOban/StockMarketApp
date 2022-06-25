package com.dogukan.stockmarketapp.presentation.company_listing

import com.dogukan.stockmarketapp.domain.model.CompanyListing

data class CompanyListingState(
    val companies : List<CompanyListing> = emptyList(),
    val isLoading : Boolean = false,
    val isRefreshing : Boolean = false,
    val searchQuery : String = ""

)
