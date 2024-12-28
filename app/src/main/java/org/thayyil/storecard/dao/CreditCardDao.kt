package org.thayyil.storecard.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.thayyil.storecard.data.CreditCard

@Dao
interface CreditCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CreditCard)

    @Query("SELECT * FROM credit_cards")
    fun getAllCards(): Flow<List<CreditCard>>

    @Delete
    suspend fun delete(card: CreditCard)
}