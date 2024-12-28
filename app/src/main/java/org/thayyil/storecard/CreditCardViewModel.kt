package org.thayyil.storecard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.thayyil.storecard.data.CreditCard
import org.thayyil.storecard.repo.CreditCardRepository

class CreditCardViewModel(private val repository: CreditCardRepository) : ViewModel() {

    val allCards: LiveData<List<CreditCard>> = repository.allCards.asLiveData()

    fun addCard(card: CreditCard) {
        viewModelScope.launch {
            repository.addCard(card)
        }
    }
}