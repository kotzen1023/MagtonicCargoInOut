package com.magtonic.magtoniccargoinout.persistence

import androidx.room.*
@Dao
interface HistoryDao {
    @Query("SELECT * FROM " + History.TABLE_NAME)

    fun getAll(): List<History>
    //@Insert
    //void insertAll(List<PlayList> playLists);

    @Query("SELECT * FROM " + History.TABLE_NAME + " WHERE barcode LIKE :barcode")
    fun getHistoryByBarcode(barcode: String): History

    @Query("SELECT * FROM " + History.TABLE_NAME + " WHERE date LIKE :date")
    fun getHistoryByDate(date: String): List<History>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(history: History)

    //@Delete
    //fun delete(history: History)

    @Update
    fun update(history: History)
}