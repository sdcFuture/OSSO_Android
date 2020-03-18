package com.futureelectronics.osso.data;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Created by Kyle Harman on 12/4/2018.
 */
@Dao
public interface OssoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Osso osso);

    @Query("DELETE FROM Osso")
    void deleteAll();

    @Query("DELETE FROM Osso WHERE id = :id")
    void deleteOsso(int id);

    @Query("SELECT * from Osso ORDER BY petName ASC")
    LiveData<List<Osso>> getAllOssos();

    @Query("SELECT * FROM Osso WHERE id = :id")
    LiveData<Osso> getOsso(int id);

    @Query("SELECT * FROM Osso WHERE address = :address")
    LiveData<Osso> getOsso(String address);

}
