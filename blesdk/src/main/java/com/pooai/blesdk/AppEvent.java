package com.pooai.blesdk;

import android.content.Context;

import com.polidea.rxandroidble2.LogConstants;
import com.polidea.rxandroidble2.LogOptions;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;

/**
 * 作者：created by xieying on 2020-01-12 15:31
 * 功能：
 */
public class AppEvent {

    private static RxBleClient mRxBleClient;

    private static RxBleDevice mRxBleDevice;

    public static void init(Context context){
        mRxBleClient = RxBleClient.create(context);
        RxBleClient.updateLogOptions(new LogOptions.Builder()
                .setLogLevel(LogConstants.INFO)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        );
    }

    public static RxBleClient getRxBleClient(){
        if(mRxBleClient ==null){
            throw new RuntimeException("RxBleClient should be init");
        }
        return mRxBleClient;
    }

    public static RxBleDevice getRxBleDevice() {
        return mRxBleDevice;
    }

    public static void setRxBleDevice(RxBleDevice mRxBleDevice) {
        AppEvent.mRxBleDevice = mRxBleDevice;
    }
}
