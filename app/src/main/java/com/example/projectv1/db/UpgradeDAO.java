package com.example.projectv1.db;

import java.util.List;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UpgradeDAO {

    //for forming recycler views of all upgrades that are owned and all that are not owned
    @Query ("SELECT * FROM upgrades WHERE owned = :owned ORDER BY price ASC")
    LiveData<List<Upgrade>> getByOwned(boolean owned);

    //get all upgrades currently in use
    @Query("SELECT * FROM upgrades WHERE enabled AND owned ORDER BY priority ASC")
    List<Upgrade> getEnabled();

    //get all upgrades
    @Query("SELECT * FROM upgrades")
    List<Upgrade> getAll();

    //get upgrade by id, unused
    @Query ("Select * FROM upgrades WHERE upgradeId = :id")
    Upgrade getById(int id);

    @Update
    void update(Upgrade ... upgrade);

    @Insert
    void insert(Upgrade ... upgrade);

    @Delete
    void delete(Upgrade ... upgrade);


}
