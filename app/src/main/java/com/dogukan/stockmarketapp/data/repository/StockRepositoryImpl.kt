package com.dogukan.stockmarketapp.data.repository

import com.dogukan.stockmarketapp.data.csv.CSVParser
import com.dogukan.stockmarketapp.data.csv.IntradayInfoParser
import com.dogukan.stockmarketapp.data.local.StockDatabase
import com.dogukan.stockmarketapp.data.mapper.toCompanyInfo
import com.dogukan.stockmarketapp.data.mapper.toCompanyListing
import com.dogukan.stockmarketapp.data.remote.StockApi
import com.dogukan.stockmarketapp.domain.model.CompanyInfo
import com.dogukan.stockmarketapp.domain.model.CompanyListing
import com.dogukan.stockmarketapp.domain.model.IntradayInfo
import com.dogukan.stockmarketapp.domain.repository.StockRepository
import com.dogukan.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api : StockApi,
    private val db : StockDatabase,
    private val companyListingParser : CSVParser<CompanyListing>,
    private val intradayInfoParser : CSVParser<IntradayInfo>

) : StockRepository {
    private val dao = db.dao
    override suspend fun getCompanyListing(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Loading(true))
            val localListing = dao.searcCompanyListing(query)
            emit(Resource.Success(
                data = localListing.map { it.toCompanyListing() }
            ))
            val isDbEmpty = localListing.isEmpty() && query.isBlank()
            val justLoadFromCache = !isDbEmpty && !fetchFromRemote
            if(justLoadFromCache){
                emit(Resource.Loading(false))
                return@flow
            }
            val remoteListing = try {
                val response = api.getListing()
                companyListingParser.parse(response.byteStream())
            }
            catch (e : IOException){
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            }catch (e :HttpException){
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null

            }

            remoteListing?.let { listing ->
                dao.clearCompanyListing()
                dao.instertCompanyListing(
                    listing.map { it.toCompanyListing() }
                )
                emit(Resource.Success(
                    data = dao
                        .searcCompanyListing("")
                        .map { it.toCompanyListing() }
                ))
                emit(Resource.Loading(false))
            }

        }
    }

    override suspend fun getIntradayInfo(symbol: String): Resource<List<IntradayInfo>> {
        return try {
            val response = api.getIntradayInfo(symbol)
            val results = intradayInfoParser.parse(response.byteStream())
            Resource.Success(results)
        }catch (e : IOException){
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load intraday info"
            )
        }catch (e : HttpException){
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load intraday info"
            )
        }
    }

    override suspend fun getCompanyInfo(symbol: String): Resource<CompanyInfo> {
        return try {
            val result = api.getCompanyInfo(symbol)
            Resource.Success(result.toCompanyInfo())
        }catch (e : IOException){
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load company info"
            )
        }catch (e : HttpException){
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load company info"
            )
        }    }
}