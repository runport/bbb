package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ColorEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.ProductModelEntity
import com.example.data.entity.SizeEntity
import com.example.data.entity.SizeGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    // Products
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    // Models
    @Query("SELECT * FROM product_models ORDER BY id DESC")
    fun getAllModels(): Flow<List<ProductModelEntity>>

    @Query("SELECT * FROM product_models WHERE productId = :productId")
    fun getModelsForProduct(productId: Long): Flow<List<ProductModelEntity>>

    @Query("SELECT * FROM product_models WHERE productId = :productId")
    suspend fun getModelsForProductSync(productId: Long): List<ProductModelEntity>

    @Query("SELECT * FROM product_models WHERE id = :id")
    suspend fun getModelById(id: Long): ProductModelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: ProductModelEntity): Long

    @Update
    suspend fun updateModel(model: ProductModelEntity)

    @Delete
    suspend fun deleteModel(model: ProductModelEntity)

    // Size Groups
    @Query("SELECT * FROM size_groups ORDER BY id ASC")
    fun getAllSizeGroups(): Flow<List<SizeGroupEntity>>

    @Query("SELECT * FROM size_groups ORDER BY id ASC")
    suspend fun getAllSizeGroupsSync(): List<SizeGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSizeGroup(sizeGroup: SizeGroupEntity): Long

    @Update
    suspend fun updateSizeGroup(sizeGroup: SizeGroupEntity)

    @Delete
    suspend fun deleteSizeGroup(sizeGroup: SizeGroupEntity)

    // Sizes
    @Query("SELECT * FROM sizes ORDER BY sortOrder ASC, id ASC")
    fun getAllSizes(): Flow<List<SizeEntity>>

    @Query("SELECT * FROM sizes WHERE sizeGroupId = :groupId ORDER BY sortOrder ASC, id ASC")
    fun getSizesForGroup(groupId: Long): Flow<List<SizeEntity>>

    @Query("SELECT * FROM sizes WHERE sizeGroupId = :groupId ORDER BY sortOrder ASC, id ASC")
    suspend fun getSizesForGroupSync(groupId: Long): List<SizeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSize(size: SizeEntity): Long

    @Update
    suspend fun updateSize(size: SizeEntity)

    @Delete
    suspend fun deleteSize(size: SizeEntity)

    // Colors
    @Query("SELECT * FROM colors ORDER BY name ASC")
    fun getAllColors(): Flow<List<ColorEntity>>

    @Query("SELECT * FROM colors ORDER BY name ASC")
    suspend fun getAllColorsSync(): List<ColorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColor(color: ColorEntity): Long

    @Update
    suspend fun updateColor(color: ColorEntity)

    @Delete
    suspend fun deleteColor(color: ColorEntity)
}
