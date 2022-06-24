package com.dogukan.stockmarketapp.data.repository

import com.dogukan.stockmarketapp.data.local.CompanyListingEntity
import com.dogukan.stockmarketapp.data.local.StockDatabase
import com.dogukan.stockmarketapp.data.mapper.toCompanyListing
import com.dogukan.stockmarketapp.data.remote.dto.StockApi
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
    val api : StockApi,
    val db : StockDatabase
) : StockRepository {
    private val dao = db.dao
    override suspend fun getCompanyListing(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListingEntity>>> {
        return flow {
            emit(Resource.Loading(true))
            val localListing = dao.searcCompanyListing(query)
            emit(Resource.Success(
                data = localListing.map { it.toCompanyListing().toCompanyListing() }
            ))
            val isDbEmpty = localListing.isEmpty() && query.isBlank()
            val justLoadFromCache = !isDbEmpty && !fetchFromRemote
            if(justLoadFromCache){
                emit(Resource.Loading(false))
                return@flow
            }
            val remoteListing = try {
                val response = api.getListing()
            }
            catch (e : IOException){
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
            }catch (e :HttpException){
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))


            }

        }
    }
}