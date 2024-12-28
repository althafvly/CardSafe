package org.thayyil.storecard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import org.thayyil.storecard.data.CreditCard

@Composable
fun AddCreditCardDialog(onAdd: (CreditCard) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    // List of supported card types
    val supportedCardTypes = listOf("Visa", "RuPay", "MasterCard", "American Express")

    // Function to validate expiry date in "MMYY" format
    fun isValidExpiry(newValues: String): Boolean {
        return newValues.isNotEmpty() && newValues.length == 4
    }

    fun isValidCardNumber(newValues: String): Boolean {
        return newValues.isNotEmpty() && newValues.length == 16
    }

    // Function to validate the card type
    fun isValidCardType(): Boolean {
        return cardType.isNotEmpty() && supportedCardTypes.contains(cardType.trim())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_credit_card)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.cardholder_name)) },
                    isError = isError && name.isEmpty(), // Error if the name is empty
                    maxLines = 1,
                    singleLine = true
                )
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text(stringResource(R.string.bank_name)) },
                    isError = isError && bankName.isEmpty(), // Error if the name is empty
                    maxLines = 1,
                    singleLine = true
                )
                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text(stringResource(R.string.card_name)) },
                    isError = isError && cardName.isEmpty(), // Error if the name is empty
                    maxLines = 1,
                    singleLine = true
                )
                OutlinedTextField(
                    value = number,
                    onValueChange = {
                        if (it.length <= 16) {
                            number = it
                        }
                    },
                    label = { Text(stringResource(R.string.card_number)) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    isError = isError && !isValidCardNumber(number), // Error if the number exceeds 16 digits
                    maxLines = 1,
                    singleLine = true
                )
                OutlinedTextField(
                    value = expiry,
                    onValueChange = {
                        if (it.length <= 4) {
                            expiry = it
                        }
                    },
                    label = { Text(stringResource(R.string.expiry_date_mmyy)) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    isError = isError && !isValidExpiry(expiry), // Error if expiry is invalid
                    maxLines = 1,
                    singleLine = true
                )

                Box {
                    // Trigger for the dropdown
                    OutlinedTextField(
                        value = cardType,
                        onValueChange = {},
                        readOnly = true, // Make it read-only to act as a dropdown
                        label = { Text(stringResource(R.string.card_type)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { expanded = !expanded }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = isError && !isValidCardType(), // Error if the name is empty
                        maxLines = 1,
                        singleLine = true
                    )

                    // DropdownMenu anchored to the Box
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        supportedCardTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    cardType = type // Update the selection
                                    expanded = false // Close the menu
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isError =
                        cardType.isEmpty() || name.isEmpty() || bankName.isEmpty() || cardName.isEmpty()
                                || !isValidCardNumber(number) || !isValidExpiry(expiry) || !isValidCardType()
                    if (!isError) {
                        onAdd(
                            CreditCard(
                                cardholderName = name,
                                cardNumber = number,
                                expiryDate = expiry,
                                cardType = cardType,
                                bankName = bankName,
                                cardName = cardName
                            )
                        )
                        onDismiss()
                    }
                }
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
