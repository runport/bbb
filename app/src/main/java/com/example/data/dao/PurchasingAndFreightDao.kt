package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.CarrierEntity
import com.example.data.entity.FreightAllocationEntity
import com.example.data.entity.FreightBillEntity
import com.example.data.entity.PurchaseEntity
import com.example.data.entity.PurchaseItemEntity
import com.example.data.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchasingAndFreightDao {
    // Suppliers
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: Long): SupplierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    // Carriers
    @Query("SELECT * FROM carriers ORDER BY name ASC")
    fun getAllCarriers(): Flow<List<CarrierEntity>>

    @Query("SELECT * FROM carriers WHERE id = :id")
    suspend fun getCarrierById(id: Long): CarrierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarrier(carrier: CarrierEntity): Long

    @Update
    suspend fun updateCarrier(carrier: CarrierEntity)

    @Delete
    suspend fun deleteCarrier(carrier: CarrierEntity)

    // Freight Bills
    @Query("SELECT * FROM freight_bills ORDER BY date DESC")
    fun getAllFreightBills(): Flow<List<FreightBillEntity>>

    @Query("SELECT * FROM freight_bills WHERE id = :id")
    suspend fun getFreightBillById(id: Long): FreightBillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFreightBill(bill: FreightBillEntity): Long

    @Update
    suspend fun updateFreightBill(bill: FreightBillEntity)

    @Delete
    suspend fun deleteFreightBill(bill: FreightBillEntity)

    // Freight Allocations
    @Query("SELECT * FROM freight_allocations WHERE freightBillId = :billId")
    fun getAllocationsForBill(billId: Long): Flow<List<FreightAllocationEntity>>

    @Query("SELECT * FROM freight_allocations WHERE freightBillId = :billId")
    suspend fun getAllocationsForBillSync(billId: Long): List<FreightAllocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocations(allocations: List<FreightAllocationEntity>)

    @Query("DELETE FROM freight_allocations WHERE freightBillId = :billId")
    suspend fun deleteAllocationsForBill(billId: Long)

    // Purchases
    @Query("SELECT * FROM purchases ORDER BY date DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getPurchaseById(id: Long): PurchaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    // Purchase Items
    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    fun getItemsForPurchase(purchaseId: Long): Flow<List<PurchaseItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)
}
