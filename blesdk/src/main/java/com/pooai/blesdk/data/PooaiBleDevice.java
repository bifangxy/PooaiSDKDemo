package com.pooai.blesdk.data;

import android.bluetooth.le.ScanResult;

/**
 * 作者：created by xieying on 2020-01-18 18:05
 * 功能：
 */
public class PooaiBleDevice {

    private String name;

    private String macAddress;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
