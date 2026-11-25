
package com.kamaujames.pesatrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kamaujames.pesatrack.data.local.PesaTrackDatabase
import com.kamaujames.pesatrack.data.repository.TransactionRepository
import com.kamaujames.pesatrack.ui.dashboard.PesaTrackApp
import com.kamaujames.pesatrack.ui.theme.PesaTrackTheme
import com.kamaujames.pesatrack.viewmodel.FinanceViewModel
import com.kamaujames.pesatrack.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = PesaTrackDatabase.getDatabase(applicationContext)
        val repository = TransactionRepository(
            transactionDao = database.transactionDao(),
            budgetDao = database.budgetDao()
        )
        val factory = FinanceViewModelFactory(repository)

        setContent {
            PesaTrackTheme {
                val financeViewModel: FinanceViewModel = viewModel(
                    factory = factory
                )

                Surface(modifier = Modifier.fillMaxSize()) {
                    PesaTrackApp(viewModel = financeViewModel)
                }
            }
        }
    }
}
