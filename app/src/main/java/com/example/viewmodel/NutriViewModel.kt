package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ArticleEntity
import com.example.data.model.HistoryEntity
import com.example.data.model.ReminderEntity
import com.example.data.model.UserEntity
import com.example.data.repository.NutriRepository
import com.example.util.NutritionCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class Success(val user: UserEntity) : AuthState
    data class Error(val message: String) : AuthState
}

@OptIn(ExperimentalCoroutinesApi::class)
class NutriViewModel(
    application: Application,
    private val repository: NutriRepository,
) : AndroidViewModel(application) {

    // Authentication States
    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    // Observe logged in user
    val currentUser: StateFlow<UserEntity?> = _currentUserEmail
        .flatMapLatest { email ->
            if (email != null) repository.getUserByEmail(email) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Observe calculations history
    val historyList: StateFlow<List<HistoryEntity>> = _currentUserEmail
        .flatMapLatest { email ->
            if (email != null) repository.getHistoryByUser(email) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observe reminders list
    val remindersList: StateFlow<List<ReminderEntity>> = _currentUserEmail
        .flatMapLatest { email ->
            if (email != null) repository.getRemindersByUser(email) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observe articles
    val articlesList: StateFlow<List<ArticleEntity>> = repository.getAllArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Article for Detail Screen
    private val _selectedArticle = MutableStateFlow<ArticleEntity?>(null)
    val selectedArticle: StateFlow<ArticleEntity?> = _selectedArticle.asStateFlow()

    // Daily trackers (stored persistently in UserEntity in Room database)
    val waterIntakeMl: StateFlow<Int> = currentUser
        .map { it?.waterIntakeMl ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val loggedCalories: StateFlow<Int> = currentUser
        .map { it?.loggedCalories ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Prepopulate articles
        viewModelScope.launch {
            repository.prepopulateArticles()
        }
    }

    // Auto load default configurations for a user
    fun onUserLoginSuccess(email: String) {
        _currentUserEmail.value = email
        _loginState.value = AuthState.Idle
        viewModelScope.launch {
            repository.createDefaultRemindersIfNone(email)
        }
    }

    fun login(email: String, word: String) {
        if (email.isBlank() || word.isBlank()) {
            _loginState.value = AuthState.Error("Email dan password tidak boleh kosong.")
            return
        }
        _loginState.value = AuthState.Loading
        viewModelScope.launch {
            val user = repository.getUserByEmailDirect(email.trim().lowercase())
            if (user == null) {
                _loginState.value = AuthState.Error("Email tidak terdaftar.")
            } else {
                val hashed = hashPassword(word)
                if (user.passwordHash == hashed) {
                    onUserLoginSuccess(user.email)
                    _loginState.value = AuthState.Success(user)
                } else {
                    _loginState.value = AuthState.Error("Password yang Anda masukkan salah.")
                }
            }
        }
    }

    fun register(name: String, email: String, word: String, confirmWord: String) {
        if (name.isBlank() || email.isBlank() || word.isBlank()) {
            _registerState.value = AuthState.Error("Semua field harus diisi.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerState.value = AuthState.Error("Format email tidak valid.")
            return
        }
        if (word.length < 6) {
            _registerState.value = AuthState.Error("Password minimal 6 karakter.")
            return
        }
        if (word != confirmWord) {
            _registerState.value = AuthState.Error("Konfirmasi password tidak cocok.")
            return
        }

        _registerState.value = AuthState.Loading
        viewModelScope.launch {
            val normalizedEmail = email.trim().lowercase()
            val existing = repository.getUserByEmailDirect(normalizedEmail)
            if (existing != null) {
                _registerState.value = AuthState.Error("Email sudah terdaftar.")
            } else {
                val hashed = hashPassword(word)
                val newUser = UserEntity(
                    email = normalizedEmail,
                    name = name.trim(),
                    passwordHash = hashed
                )
                repository.insertUser(newUser)
                onUserLoginSuccess(newUser.email)
                _registerState.value = AuthState.Success(newUser)
            }
        }
    }

    fun logout() {
        _currentUserEmail.value = null
        _loginState.value = AuthState.Idle
        _registerState.value = AuthState.Idle
    }

    // Reset flow states
    fun resetAuthStates() {
        _loginState.value = AuthState.Idle
        _registerState.value = AuthState.Idle
    }

    // Add Water Track
    fun addWater(amountMl: Int) {
        val email = _currentUserEmail.value ?: return
        viewModelScope.launch {
            repository.getUserByEmailDirect(email)?.let { user ->
                val updatedWater = (user.waterIntakeMl + amountMl).coerceAtLeast(0)
                repository.updateUser(user.copy(waterIntakeMl = updatedWater))
            }
        }
    }

    fun addCalories(amountKcal: Int) {
        val email = _currentUserEmail.value ?: return
        viewModelScope.launch {
            val user = repository.getUserByEmailDirect(email)
            if (user != null) {
                val updatedCalories = (user.loggedCalories + amountKcal).coerceAtLeast(0)
                repository.updateUser(user.copy(loggedCalories = updatedCalories))
            }
        }
    }

    fun resetDailyTrackers() {
        val email = _currentUserEmail.value ?: return
        viewModelScope.launch {
            val user = repository.getUserByEmailDirect(email)
            if (user != null) {
                repository.updateUser(user.copy(waterIntakeMl = 0, loggedCalories = 0))
            }
        }
    }

    // Perform Nutrition calculation and save history
    fun calculateAndSaveNutrition(
        weight: Double,
        height: Double,
        age: Int,
        gender: String,
        activityLevel: Double,
        goalType: String
    ) {
        val email = _currentUserEmail.value ?: return
        viewModelScope.launch {
            // Calculate
            val res = NutritionCalculator.calculate(
                weight = weight,
                height = height,
                age = age,
                gender = gender,
                activityMultiplier = activityLevel,
                goalType = goalType
            )

            // 1. Update user metrics in UserEntity
            val user = repository.getUserByEmailDirect(email)
            if (user != null) {
                val updatedUser = user.copy(
                    weight = weight,
                    height = height,
                    age = age,
                    gender = gender,
                    activityLevel = activityLevel,
                    goalType = goalType,
                    targetWeight = user.targetWeight // keep original target or default
                )
                repository.updateUser(updatedUser)
            }

            // 2. Save calculation record in History table
            val history = HistoryEntity(
                userEmail = email,
                timestamp = System.currentTimeMillis(),
                weight = weight,
                height = height,
                age = age,
                gender = gender,
                bmi = res.bmi,
                bmr = res.bmr,
                tdee = res.tdee,
                targetCalorie = res.targetCalorie,
                targetProtein = res.targetProtein,
                targetCarb = res.targetCarb,
                targetFat = res.targetFat,
                targetWater = res.targetWater,
                goalType = goalType,
                category = res.category
            )
            repository.insertHistory(history)
        }
    }

    // Delete History
    fun deleteHistoryRecord(history: HistoryEntity) {
        viewModelScope.launch {
            repository.deleteHistory(history)
        }
    }


    fun clearAllHistory() {
        val email = _currentUserEmail.value ?: return
        viewModelScope.launch {
            repository.clearHistoryByUser(email)
        }
    }

    // Toggle Reminder state
    fun toggleReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isEnabled = !reminder.isEnabled))
        }
    }


    fun selectArticle(article: ArticleEntity?) {
        _selectedArticle.value = article
    }

    // Update Profile general settings
    fun updateProfileSettings(
        name: String,
        targetWeight: Double,
        isDarkMode: Boolean
    ) {
        val email = _currentUserEmail.value ?: return
        viewModelScope.launch {
            val user = repository.getUserByEmailDirect(email)
            if (user != null) {
                val updated = user.copy(
                    name = name,
                    targetWeight = targetWeight,
                    isDarkMode = isDarkMode
                )
                repository.updateUser(updated)
            }
        }
    }

    fun updateAvatar(avatarUri: String?) {
        val email = _currentUserEmail.value ?: return
        viewModelScope.launch {
            val user = repository.getUserByEmailDirect(email)
            if (user != null) {
                val updated = user.copy(avatarUri = avatarUri)
                repository.updateUser(updated)
            }
        }
    }

    // Cryptographic hashing helper
    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(password.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            password // Fallback if hashing error occurs
        }
    }
}

class ViewModelFactory(
    private val application: Application,
    private val repository: NutriRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NutriViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NutriViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
