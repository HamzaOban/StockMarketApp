package com.dogukan.stockmarketapp.presentation.company_listing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dogukan.stockmarketapp.domain.repository.StockRepository
import com.dogukan.stockmarketapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyListingViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel(){
    var state by mutableStateOf(CompanyListingState())
    private var searchJob : Job ?= null
    fun onEvent(event: CompanyListingEvent){
        when(event){
            is CompanyListingEvent.Refresh ->{
                getCompanyListings(fetchFromRemote = true)
            }
            is CompanyListingEvent.OnSearchQueryChange ->{
                state = state.copy(searchQuery = event.query)
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    getCompanyListings()
                }
            }
        }
    }
    fun getCompanyListings(
        query : String = state.searchQuery.lowercase(),
        fetchFromRemote : Boolean = false
    ){
        viewModelScope.launch {
            repository
                .getCompanyListing(fetchFromRemote,query)
                .collect{result->
                    when(result){
                        is Resource.Success->{
                            result.data?.let { listing->
                                state = state.copy(
                                    companies = listing
                                )
                            }
                        }
                        is Resource.Error-> Unit
                        is Resource.Loading ->{
                            state = state.copy(isLoading = result.isLoading)
                        }
                    }
                }
        }
    }
}