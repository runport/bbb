package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.FabricRollEntity
import com.example.data.entity.FabricTypeEntity
import com.example.data.entity.InventoryTransactionEntity
import com.example.data.entity.MaterialEntity
import com.example.data.entity.PriceHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    // Fabric Types
    @Query("SELECT * FROM fabric_types ORDER BY name ASC")
    fun getAllFabricTypes(): Flow<List<FabricTypeEntity>>

    @Query("SELECT * FROM fabric_types ORDER BY name ASC")
    suspend fun getAllFabricTypesSync(): List<FabricTypeEntity>

    @Query("SELECT * FROM fabric_types WHERE id = :id")
    suspend fun getFabricTypeById(id: Long): FabricTypeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFabricType(fabricType: FabricTypeEntity): Long

    @Update
    suspend fun updateFabricType(fabricType: FabricTypeEntity)

    @Delete
    suspend fun deleteFabricType(fabricType: FabricTypeEntity)

    // Fabric Rolls
    @Query("SELECT * FROM fabric_rolls ORDER BY id DESC")
    fun getAllFabricRolls(): Flow<List<FabricRollEntity>>

    @Query("SELECT * FROM fabric_rolls WHERE status != 'تمام‌شده' ORDER BY id DESC")
    fun getAvailableFabricRolls(): Flow<List<FabricRollEntity>>

    @Query("SELECT * FROM fabric_rolls WHERE id = :id")
    suspend fun getFabricRollById(id: Long): FabricRollEntity?

    @Query("SELECT * FROM fabric_rolls WHERE id = :id")
    fun getFabricRollByIdFlow(id: Long): Flow<FabricRollEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFabricRoll(roll: FabricRollEntity): Long

    @Update
    suspend fun updateFabricRoll(roll: FabricRollEntity)

    @Delete
    suspend fun deleteFabricRoll(roll: FabricRollEntity)

    // Materials
    @Query("SELECT * FROM materials ORDER BY category ASC, name ASC")
    fun getAllMaterials(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE category = :category ORDER BY name ASC")
    fun getMaterialsByCategory(category: String): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE id = :id")
    suspend fun getMaterialById(id: Long): MaterialEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialEntity): Long

    @Update
    suspend fun updateMaterial(material: MaterialEntity)

    @Delete
    suspend fun deleteMaterial(material: MaterialEntity)

    // Price History
    @Query("SELECT * FROM price_history ORDER BY date DESC")
    fun getAllPriceHistory(): Flow<List<PriceHistoryEntity>>

    @Query("SELECT * FROM price_history WHERE itemType = :itemType AND itemId = :itemId ORDER BY date DESC")
    fun getPriceHistoryForItem(itemType: String, itemId: Long): Flow<List<PriceHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(record: PriceHistoryEntity): Long

    // Inventory Transactions
    @Query("SELECT * FROM inventory_transactions ORDER BY date DESC")
    fun getAllInventoryTransactions(): Flow<List<InventoryTransactionEntity>>

    @Query("SELECT * FROM inventory_transactions WHERE itemType = :itemType AND itemId = :itemId ORDER BY date DESC")
    fun getTransactionsForItem(itemType: String, itemId: Long): Flow<List<InventoryTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryTransaction(tx: InventoryTransactionEntity): Long
}
