package com.example.data.repository

import com.example.data.dao.ArticleDao
import com.example.data.dao.HistoryDao
import com.example.data.dao.ReminderDao
import com.example.data.dao.UserDao
import com.example.data.model.ArticleEntity
import com.example.data.model.HistoryEntity
import com.example.data.model.ReminderEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class NutriRepository(
    private val userDao: UserDao,
    private val historyDao: HistoryDao,
    private val reminderDao: ReminderDao,
    private val articleDao: ArticleDao
) {
    // User Operations
    fun getUserByEmail(email: String): Flow<UserEntity?> = userDao.getUserByEmail(email)
    
    suspend fun getUserByEmailDirect(email: String): UserEntity? = userDao.getUserByEmailDirect(email)
    
    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    // History Operations
    fun getHistoryByUser(userEmail: String): Flow<List<HistoryEntity>> = historyDao.getHistoryByUser(userEmail)
    
    suspend fun insertHistory(history: HistoryEntity) = historyDao.insertHistory(history)
    
    suspend fun deleteHistory(history: HistoryEntity) = historyDao.deleteHistory(history)
    
    suspend fun clearHistoryByUser(userEmail: String) = historyDao.clearHistoryByUser(userEmail)

    // Reminder Operations
    fun getRemindersByUser(userEmail: String): Flow<List<ReminderEntity>> = reminderDao.getRemindersByUser(userEmail)
    
    suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.updateReminder(reminder)

    suspend fun createDefaultRemindersIfNone(userEmail: String) {
        val existing = reminderDao.getRemindersByUser(userEmail).first()
        if (existing.isEmpty()) {
            val defaults = listOf(
                ReminderEntity(userEmail = userEmail, title = "Sarapan Sehat", timeStr = "07:00", type = "Breakfast"),
                ReminderEntity(userEmail = userEmail, title = "Minum Air Putih (Pagi)", timeStr = "09:00", type = "Water"),
                ReminderEntity(userEmail = userEmail, title = "Makan Siang Bernutrisi", timeStr = "12:30", type = "Lunch"),
                ReminderEntity(userEmail = userEmail, title = "Minum Air Putih (Sore)", timeStr = "15:00", type = "Water"),
                ReminderEntity(userEmail = userEmail, title = "Makan Malam Ringan", timeStr = "19:00", type = "Dinner"),
                ReminderEntity(userEmail = userEmail, title = "Waktu Olahraga Harian", timeStr = "17:00", type = "Workout", isEnabled = false)
            )
            reminderDao.insertReminders(defaults)
        }
    }

    // Article Operations
    fun getAllArticles(): Flow<List<ArticleEntity>> = articleDao.getAllArticles()
    

    suspend fun prepopulateArticles() {
        val count = articleDao.getArticleCount()
        if (count == 0) {
            val defaultArticles = listOf(
                ArticleEntity(
                    title = "Panduan Diet Defisit Kalori untuk Pemula",
                    category = "Diet",
                    snippet = "Pelajari dasar-dasar melakukan diet defisit kalori dengan aman dan efektif untuk menurunkan berat badan.",
                    content = "Diet defisit kalori adalah prinsip dasar dari penurunan berat badan. Ini berarti Anda mengonsumsi kalori lebih sedikit daripada yang dibakar tubuh Anda untuk energi harian (TDEE). Untuk pemula, defisit aman berkisar antara 300 hingga 500 kalori di bawah TDEE Anda.\n\nLangkah-langkah Memulai:\n1. Hitung TDEE Anda menggunakan Nutri Sigma.\n2. Tetapkan target kalori harian baru (TDEE dikurangi 300-500 kkal).\n3. Fokus pada makanan padat nutrisi yang mengenyangkan, seperti sayuran, buah-buahan, biji-bijian, dan protein tanpa lemak.\n4. Pantau perkembangan berat badan Anda secara mingguan.\n\nIngat, konsistensi jauh lebih penting daripada diet ekstrem yang tidak dapat dipertahankan dalam jangka panjang.",
                    readTimeMinutes = 5
                ),
                ArticleEntity(
                    title = "Pentingnya Latihan Kekuatan untuk Metabolisme",
                    category = "Olahraga",
                    snippet = "Latihan beban bukan hanya untuk membangun otot, tetapi juga kunci meningkatkan metabolisme basal (BMR) Anda.",
                    content = "Banyak orang mengira kardio adalah satu-satunya jalan untuk membakar lemak. Padahal, latihan kekuatan (strength training) memiliki peran yang sangat penting dalam menjaga berat badan ideal jangka panjang.\n\nKetika Anda melatih otot, Anda menciptakan robekan mikro di jaringan otot yang membutuhkan energi besar untuk diperbaiki oleh tubuh. Selain itu, massa otot aktif secara metabolik, artinya otot membakar lebih banyak kalori bahkan saat Anda sedang istirahat. Hal ini secara langsung meningkatkan BMR (Basal Metabolic Rate) Anda.\n\nSaran Latihan:\n- Lakukan latihan beban 3-4 kali seminggu.\n- Fokus pada gerakan majemuk (compound movements) seperti squat, push-up, deadlift, dan pull-up.\n- Berikan waktu istirahat yang cukup untuk pemulihan otot.",
                    readTimeMinutes = 6
                ),
                ArticleEntity(
                    title = "Hubungan Tidur Cukup dengan Pembakaran Lemak",
                    category = "Tidur",
                    snippet = "Kurang tidur bisa merusak program diet Anda secara hormonal. Simak mengapa istirahat yang cukup sangat vital.",
                    content = "Tidur sering kali diabaikan dalam program kebugaran, padahal tidur adalah fondasi utama kesehatan dan regulasi berat badan. Kurang tidur mengacaukan dua hormon pengatur nafsu makan yang sangat krusial: Ghrelin dan Leptin.\n\nGhrelin bertugas memberi sinyal lapar, sementara Leptin memberi sinyal kenyang. Ketika Anda kurang tidur (kurang dari 7 jam per malam), kadar Ghrelin meningkat tajam, sedangkan Leptin menurun. Ini membuat Anda cenderung mendambakan makanan manis dan berlemak tinggi.\n\nSelain itu, kurang tidur meningkatkan hormon stres kortisol, yang memicu penumpukan lemak terutama di area perut. Pastikan Anda tidur berkualitas selama 7-8 jam setiap malam untuk mendukung metabolisme tubuh yang optimal.",
                    readTimeMinutes = 4
                ),
                ArticleEntity(
                    title = "Kebutuhan Protein Harian: Berapa Banyak yang Anda Butuhkan?",
                    category = "Nutrisi",
                    snippet = "Protein adalah makronutrien paling mengenyangkan dan penting untuk perbaikan sel otot. Berapa target harian Anda?",
                    content = "Protein adalah makronutrien pembangun tubuh. Bagi pelaku diet maupun fitness enthusiast, protein adalah kunci untuk mempertahankan massa otot saat kehilangan lemak, serta memberikan efek kenyang (satiety) yang lebih lama dibanding karbohidrat dan lemak.\n\nKebutuhan protein harian bervariasi:\n- Orang dengan aktivitas rendah: 1.0 - 1.2 gram per kg berat badan harian.\n- Orang dengan aktivitas sedang/aktif: 1.2 - 1.6 gram per kg berat badan.\n- Fitness enthusiast atau atlet pembangun otot: 1.6 - 2.2 gram per kg berat badan harian.\n\nSumber protein terbaik meliputi dada ayam, ikan, telur, tempe, tahu, daging sapi tanpa lemak, dan yogurt.",
                    readTimeMinutes = 5
                ),
                ArticleEntity(
                    title = "Manfaat Minum Air Putih Secara Rutin",
                    category = "Tips",
                    snippet = "Air putih adalah pelarut nutrisi alami dan motor utama sistem metabolisme Anda. Jangan biarkan tubuh dehidrasi.",
                    content = "Air menyusun sekitar 60% dari berat tubuh kita. Setiap sel, jaringan, dan organ memerlukan air agar dapat berfungsi dengan baik. Dehidrasi ringan pun dapat memperlambat metabolisme, mengurangi konsentrasi, dan menyebabkan kelelahan yang tidak perlu.\n\nBagi kesehatan dan diet, minum air putih memiliki manfaat luar biasa:\n1. Membantu mengendalikan asupan kalori (sering kali rasa haus disalahartikan sebagai rasa lapar).\n2. Membantu pembakaran lemak secara optimal.\n3. Membantu mengeluarkan racun dari tubuh melalui urine dan keringat.\n4. Menjaga elastisitas kulit dan kesehatan pencernaan.\n\nRumus dasar hidrasi harian adalah 35 ml dikali berat badan Anda. Gunakan fitur pengingat minum air putih di Nutri Sigma agar hidrasi Anda selalu terjaga.",
                    readTimeMinutes = 4
                )
            )
            articleDao.insertArticles(defaultArticles)
        }
    }
}
