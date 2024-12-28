package org.thayyil.storecard

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.thayyil.storecard.dao.AppDatabase
import org.thayyil.storecard.data.CreditCard
import org.thayyil.storecard.repo.CreditCardRepository
import org.thayyil.storecard.ui.theme.StoreCardTheme

class MainActivity : FragmentActivity() {

    private lateinit var viewModel: CreditCardViewModel
    private var isBiometricEnabled by mutableStateOf(false)
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize EncryptedSharedPreferences
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPreferences = EncryptedSharedPreferences.create(
            "UserPreferences", // File name
            masterKeyAlias, // Key for encryption
            applicationContext, // Context
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // Retrieve the preference for biometric check
        isBiometricEnabled = isBiometricCheckEnabled()

        if (isBiometricAvailable() && isBiometricEnabled) {
            authenticateUser { isAuthenticated ->
                if (isAuthenticated) {
                    setupAppContent()
                } else {
                    finish() // Close the app if authentication fails
                }
            }
        } else {
            setupAppContent() // Fall back to the app without biometrics
        }
    }

    // Function to save the preference in SharedPreferences
    private fun saveBiometricCheckEnabled(isEnabled: Boolean): Boolean {
        val editor = sharedPreferences.edit()
        editor.putBoolean("biometricCheckEnabled", isEnabled)
        editor.apply() // Don't forget to commit or apply the changes
        return isEnabled
    }

    // Function to get the biometric check preference
    private fun isBiometricCheckEnabled(): Boolean {
        return sharedPreferences.getBoolean("biometricCheckEnabled", true) // Default is true
    }

    private fun setupAppContent() {
        // Create the database, DAO, and repository manually
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "credit_card_database"
        ).build()

        val dao = database.creditCardDao()
        val repository = CreditCardRepository(dao)

        // Create the ViewModel manually
        viewModel = CreditCardViewModel(repository)

        setContent {
            StoreCardTheme {
                CreditCardApp(viewModel)
            }
        }
    }

    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)

        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS ||
                canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }

    private fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun authenticateUser(onResult: (Boolean) -> Unit) {
        if (canAuthenticate()) {
            val executor = ContextCompat.getMainExecutor(this)
            val biometricPrompt =
                BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onResult(true)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.authentication_error, errString), Toast.LENGTH_SHORT
                        ).show()
                        onResult(false)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.authentication_failed), Toast.LENGTH_SHORT
                        ).show()
                        onResult(false)
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.authenticate_to_access))
                .setSubtitle(getString(R.string.use_your_fingerprint))
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            onResult(true)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CreditCardApp(viewModel: CreditCardViewModel) {
        val cards by viewModel.allCards.observeAsState(emptyList())
        var isDialogOpen by remember { mutableStateOf(false) }
        var isHomeOpen by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val navController = rememberNavController()

        Scaffold(
            floatingActionButton = {
                if (isHomeOpen) {
                    FloatingActionButton(
                        onClick = { isDialogOpen = true },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Credit Card",
                            tint = Color.White // Ensure the icon is visible (use white or another contrasting color)
                        )
                    }
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.app_name))
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            authenticateUser { isAuthenticated ->
                                if (canAuthenticate() && isAuthenticated) {
                                    isBiometricEnabled =
                                        saveBiometricCheckEnabled(!isBiometricEnabled)
                                } else if (!canAuthenticate()) {
                                    Toast.makeText(
                                        context,
                                        getString(R.string.device_credentials), Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }) {
                            Icon(
                                if (isBiometricEnabled) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                contentDescription = "Enable Biometric Check"
                            )
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text(stringResource(R.string.home)) },
                        selected = false, // Implement selection logic here
                        onClick = {
                            navController.navigate("home")
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        label = { Text(stringResource(R.string.settings)) },
                        selected = false,
                        onClick = {
                            navController.navigate("settings")
                        }
                    )
                }
            },
        ) { padding ->
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    isHomeOpen = true
                    Box(Modifier.padding(padding)) {
                        CreditCardList(cards)
                    }

                    if (isDialogOpen) {
                        AddCreditCardDialog(
                            onDismiss = { isDialogOpen = false },
                            onAdd = { newCard ->
                                viewModel.addCard(newCard)
                            },
                        )
                    }
                }
                composable("settings") {
                    isHomeOpen = false
                }
            }
        }
    }

    @Composable
    fun CreditCardList(cards: List<CreditCard>) {
        LazyColumn {
            items(cards) { card ->
                CreditCardItem(card)
            }
        }
    }

    @Composable
    fun CreditCardItem(card: CreditCard) {
        var cardNumber by remember { mutableStateOf("**** **** **** ${card.cardNumber.takeLast(4)}") }
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .height(200.dp)
                .clickable {
                    authenticateUser { isAuthenticated ->
                        if (isAuthenticated) {
                            cardNumber = card.cardNumber
                        }
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary) // Use a gradient or image for a more stunning look
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Text(
                        card.bankName + " " + card.cardName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        card.cardholderName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        cardNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Expires ${card.expiryDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
