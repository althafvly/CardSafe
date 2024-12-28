package org.thayyil.storecard.repo

import kotlinx.coroutines.flow.Flow
import org.thayyil.storecard.dao.CreditCardDao
import org.thayyil.storecard.data.CreditCard

class CreditCardRepository(private val dao: CreditCardDao) {
    val allCards: Flow<List<CreditCard>> = dao.getAllCards()

    suspend fun addCard(card: CreditCard) = dao.insert(card)
    suspend fun removeCard(card: CreditCard) = dao.delete(card)
}