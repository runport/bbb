package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.CuttingBatchEntity
import com.example.data.entity.CuttingItemEntity
import com.example.data.entity.FinishedGoodEntity
import com.example.data.entity.ProductionBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductionDao {
    // Cutting Batches
    @Query("SELECT * FROM cutting_batches ORDER BY date DESC")
    fun getAllCuttingBatches(): Flow<List<CuttingBatchEntity>>

    @Query("SELECT * FROM cutting_batches WHERE id = :id")
    suspend fun getCuttingBatchById(id: Long): CuttingBatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuttingBatch(batch: CuttingBatchEntity): Long

    @Update
    suspend fun updateCuttingBatch(batch: CuttingBatchEntity)

    // Cutting Items
    @Query("SELECT * FROM cutting_items ORDER BY date DESC")
    fun getAllCuttingItems(): Flow<List<CuttingItemEntity>>

    @Query("SELECT * FROM cutting_items WHERE batchId = :batchId")
    fun getItemsForBatch(batchId: Long): Flow<List<CuttingItemEntity>>

    @Query("SELECT * FROM cutting_items WHERE batchId = :batchId")
    suspend fun getItemsForBatchSync(batchId: Long): List<CuttingItemEntity>

    @Query("SELECT * FROM cutting_items WHERE id = :id")
    suspend fun getCuttingItemById(id: Long): CuttingItemEntity?

    @Query("SELECT * FROM cutting_items WHERE fabricRollId = :rollId")
    fun getCuttingItemsForRoll(rollId: Long): Flow<List<CuttingItemEntity>>

    @Query("SELECT * FROM cutting_items WHERE fabricRollId = :rollId")
    suspend fun getCuttingItemsForRollSync(rollId: Long): List<CuttingItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuttingItem(item: CuttingItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuttingItems(items: List<CuttingItemEntity>)

    @Update
    suspend fun updateCuttingItem(item: CuttingItemEntity)

    // Production Batches
    @Query("SELECT * FROM production_batches ORDER BY startDate DESC")
    fun getAllProductionBatches(): Flow<List<ProductionBatchEntity>>

    @Query("SELECT * FROM production_batches WHERE status = :status ORDER BY startDate DESC")
    fun getProductionBatchesByStatus(status: String): Flow<List<ProductionBatchEntity>>

    @Query("SELECT * FROM production_batches WHERE id = :id")
    suspend fun getProductionBatchById(id: Long): ProductionBatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductionBatch(batch: ProductionBatchEntity): Long

    @Update
    suspend fun updateProductionBatch(batch: ProductionBatchEntity)

    // Finished Goods
    @Query("SELECT * FROM finished_goods ORDER BY lastUpdated DESC")
    fun getAllFinishedGoods(): Flow<List<FinishedGoodEntity>>

    @Query("SELECT * FROM finished_goods WHERE availableQuantity > 0 ORDER BY productName ASC")
    fun getAvailableFinishedGoods(): Flow<List<FinishedGoodEntity>>

    @Query("SELECT * FROM finished_goods WHERE id = :id")
    suspend fun getFinishedGoodById(id: Long): FinishedGoodEntity?

    @Query("""
        SELECT * FROM finished_goods 
        WHERE productId = :productId 
          AND productModelId = :modelId 
          AND colorName = :colorName 
          AND sizeName = :sizeName 
        LIMIT 1
    """)
    suspend fun findFinishedGood(
        productId: Long,
        modelId: Long,
        colorName: String,
        sizeName: String
    ): FinishedGoodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinishedGood(item: FinishedGoodEntity): Long

    @Update
    suspend fun updateFinishedGood(item: FinishedGoodEntity)

    @Delete
    suspend fun deleteFinishedGood(item: FinishedGoodEntity)
}
