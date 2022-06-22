package com.dogukan.stockmarketapp.data.local

import androidx.room.*

@Dao
interface StockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun instertCompanyListing(
        companyListingEntities : List<CompanyListingEntity>
    )
    @Query("DELETE FROM companylistingentity")
    suspend fun clearCompanyListing()

    @Query(
        """
            SELECT *
            FROM companylistingentity
            WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' OR
                UPPER(:query) == symbol
        """
    )
    suspend fun searcCompanyListing(query : String) : List<CompanyListingEntity>

}