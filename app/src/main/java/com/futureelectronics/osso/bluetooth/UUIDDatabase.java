package com.futureelectronics.osso.bluetooth;

/**
 * Created by Kyle.Harman on 1/15/2015.
 */
import java.util.UUID;

/**
 * This class will store the UUID of the GATT services and characteristics
 */
public class UUIDDatabase {
	/**
	 * Generic UUIDs
	 */

	/**
	 * Descriptors
	 */
	public final static UUID UUID_CLIENT_CHARACTERISTIC_CONFIG_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /**
     * Future Electronics OSSO UUIDs
     */
    public final static UUID OSSO_CUSTOM_SERVICE = UUID.fromString("42821a40-e477-11e2-82d0-0002a5d5c51b");
    public final static UUID OSSO_TEMP_CHAR = UUID.fromString("a32e5520-e477-11e2-a9e3-0002a5d5c51b"); // Characteristic for OSSO temperature
    public final static UUID OSSO_HUMIDITY_CHAR = UUID.fromString("01c50b60-e48c-11e2-a073-0002a5d5c51b"); // Characteristic for OSSO humidity
    public final static UUID OSSO_IR_TEMP_CHAR = UUID.fromString("02c50b60-e48c-11e2-a073-0002a5d5c51b"); // Characteristic for OSSO IR temperature
    public final static UUID OSSO_UV_INDEX_CHAR = UUID.fromString("03c50b60-e48c-11e2-a073-0002a5d5c51b"); // Characteristic for OSSO UV index
    public final static UUID OSSO_GPS_CHAR = UUID.fromString("04c50b60-e48c-11e2-a073-0002a5d5c51b"); // Characteristic for OSSO GPS
    public final static UUID OSSO_BARKING_CHAR = UUID.fromString("05c50b60-e48c-11e2-a073-0002a5d5c51b"); // Characteristic for OSSO barking
    public final static UUID OSSO_BATTERY_LOW_CHAR = UUID.fromString("cd20c480-e48b-11e2-840b-0002a5d5c51b"); // Characteristic for OSSO battery low
    public final static UUID OSSO_ACC_SERVICE = UUID.fromString("02366e80-cf3a-11e1-9ab4-0002a5d5c51b"); // Service for OSSO accelerometer step counter
    public final static UUID OSSO_ACC_CHAR = UUID.fromString("340a1b80-cf4b-11e1-ac36-0002a5d5c51b"); // Characteristic for OSSO accelerometer step counter
}

