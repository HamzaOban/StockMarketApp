package com.dogukan.stockmarketapp.presentation.company_info

import com.dogukan.stockmarketapp.domain.model.CompanyInfo
import com.dogukan.stockmarketapp.domain.model.IntradayInfo

data class CompanyInfoState(
    val stockInfos : List<IntradayInfo> = emptyList(),
    val company : CompanyInfo?= null,
    val isLoading : Boolean = false,
    val error : String ?= null
)
