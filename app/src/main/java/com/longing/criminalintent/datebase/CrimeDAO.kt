package com.longing.criminalintent.datebase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.longing.criminalintent.Crime
import java.util.*

@Dao
interface CrimeDAO {

    @Query("SELECT * FROM crime")
    fun getCrimes(): LiveData<List<Crime>>

    @Query("SELECT * FROM crime WHERE id=(:id)")
    fun getCrime(id: UUID): LiveData<Crime?>

    @Update
    fun update(crime: Crime)

    @Insert
    fun insert(crime: Crime)
}