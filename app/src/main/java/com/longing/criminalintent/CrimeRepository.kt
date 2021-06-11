package com.longing.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.longing.criminalintent.datebase.CrimeDatabase
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime=database"

class CrimeRepository private constructor(context: Context) {
    private val dateBase: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME,
    ).build()

    private val crimeDao = dateBase.crimeDAO()
    private val executor = Executors.newSingleThreadExecutor()


    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    fun updateCrime(crime: Crime) {
        executor.execute {
            dateBase.crimeDAO().update(crime)
        }

    }

    fun addCrime(crime: Crime) {
        executor.execute {
            dateBase.crimeDAO().insert(crime)
        }
    }

    companion object {
        private var INSTANCE: CrimeRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialize")
        }
    }
}