package com.dogukan.stockmarketapp.data.repository

import com.dogukan.stockmarketapp.data.csv.CSVParser
import com.dogukan.stockmarketapp.data.local.CompanyListingEntity
import com.dogukan.stockmarketapp.data.local.StockDatabase
import com.dogukan.stockmarketapp.data.mapper.toCompanyListing
import com.dogukan.stockmarketapp.data.remote.dto.StockApi
import com.dogukan.stockmarketapp.domain.model.CompanyListing
import com.dogukan.stockmarketapp.domain.repository.StockRepository
import com.dogukan.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api : StockApi,
    private val db : StockDatabase,
    private val companyListingParser : CSVParser<CompanyListing>
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
}