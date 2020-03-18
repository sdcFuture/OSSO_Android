package com.futureelectronics.osso.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Created by Kyle Harman on 12/3/2018.
 */
@Entity
public class Osso {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int id;

    public String address="";
    public String petName="";
    public double lastLongitude;
    public double lastLatitude;
    public int steps;
    public int barks;
    public int battery=0;
    public String fw="";

    @Ignore
    public int rssi;

    @Ignore
    public double temperature;
    @Ignore
    public double humidity;
    @Ignore
    public double ir_temperature;
    @Ignore
    public int uv_index;
    @Ignore
    public int exposure_time;
}
