package org.thayyil.storecard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardholderName: String,
    val cardNumber: String,
    val expiryDate: String,
    val cardType: String,
    val bankName: String,
    val cardName: String
)