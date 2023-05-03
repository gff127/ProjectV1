package com.example.projectv1.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//entity class for an upgrade
//the actual functionality of the upgrades will be captured elsewhere
@Entity(tableName = "upgrades")
public class Upgrade {
    public Upgrade(int upgradeId, String name, String description, int price, int priority,
                   boolean owned, boolean enabled, boolean canDisable ){
        this.upgradeId = upgradeId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.priority = priority; //the order in which upgrades are applied
        this.owned = owned; //is upgrade owned
        this.enabled = enabled; //is the upgrade currently enabled
        this.canDisable = canDisable; //can the upgrade be disabled
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "upgradeId")
    public int upgradeId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "price")
    public int price;

    @ColumnInfo(name = "priority")
    public int priority;
    @ColumnInfo(name = "owned")
    public boolean owned;

    @ColumnInfo(name = "enabled")
    public boolean enabled;

    @ColumnInfo(name = "canDisable")
    public boolean canDisable;




}
