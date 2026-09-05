package com.smartledger.ui.record

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.SmartLedgerApp
import com.smartledger.data.db.entity.Category
import com.smartledger.data.db.entity.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionRepo = (application as SmartLedgerApp).transactionRepository
    private val categoryRepo = (application as SmartLedgerApp).categoryRepository

    fun getCategories(type: String): Flow<List<Category>> = categoryRepo.getByType(type)

    suspend fun getTransaction(id: Long): Transaction? = transactionRepo.getById(id)

    fun saveTransaction(
        existing: Transaction? = null,
        amount: Double,
        type: String,
        categoryId: Long?,
        merchant: String?,
        paymentMethod: String?,
        note: String?,
        transactionTime: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (existing == null) {
                transactionRepo.insert(
                    Transaction(
                        amount = amount,
                        type = type,
                        categoryId = categoryId,
                        merchant = merchant,
                        paymentMethod = paymentMethod,
                        note = note,
                        source = "manual",
                        transactionTime = transactionTime
                    )
                )
            } else {
                transactionRepo.update(
                    existing.copy(
                        amount = amount,
                        type = type,
                        categoryId = categoryId,
                        merchant = merchant,
                        paymentMethod = paymentMethod,
                        note = note,
                        transactionTime = transactionTime
                    )
                )
            }
            onSuccess()
        }
    }
}
