package com.pooai.blesdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.pooai.blesdk.observer.DetectionParamsObservable;
import com.pooai.blesdk.observer.ToiletCommandObservable;

import java.util.Iterator;
import java.util.List;

/**
 * 作者：created by xieying on 2020-02-01 23:24
 * 功能：
 */
public class PooaiBleManager2 {
    private static final String TAG = PooaiBleManager2.class.getSimpleName();

    //UUID
    public final static String UUID_SERVER = "0000ffe0-0000-1000-8000-00805f9b34fb";

    public final static String UUID_NOTIFY = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private BluetoothAdapter bleAdapter;

    private BluetoothLeScanner bleScanner;

    private BluetoothGatt bleGatt;

    private BluetoothGattCharacteristic bleGattCharacteristic;

    private Handler mHandler = new Handler();

    private OnBleScanListener mOnBleScanListener;

    private boolean mScanning;

    private static class SingletonHolder {
        private static final PooaiBleManager2 INSTANCE = new PooaiBleManager2();
    }

    public static PooaiBleManager2 getInstance() {
        return PooaiBleManager2.SingletonHolder.INSTANCE;
    }

    private BluetoothAdapter.LeScanCallback startLeScan = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "deviceName = " + device.getName());
            if (device.getName() == null) {
                return;
            }
            if (mOnBleScanListener != null) {
                mOnBleScanListener.scanResult(device);
            }
        }
    };


    public boolean initBLE() {
        if (!AppEvent.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) AppEvent.getContext().getSystemService(android.content.Context.BLUETOOTH_SERVICE);
        bleAdapter = bluetoothManager.getAdapter();
        if (bleAdapter == null) {
            return false;
        }
        return true;
    }

    public boolean isBleOpen() {
        return bleAdapter.isEnabled();
    }

    public void startScan(OnBleScanListener onBleScanListener) {
        mOnBleScanListener = onBleScanListener;
        mHandler.postDelayed(() -> {
            mScanning = false;
            bleAdapter.stopLeScan(startLeScan);
        }, 10000);

        mScanning = true;
        bleAdapter.startLeScan(startLeScan);
        /*bleScanner = bleAdapter.getBluetoothLeScanner();
        bleScanner.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                if (device == null) {
                    return;
                }
                String deviceName = device.getName();
                if (deviceName == null) {
                    return;
                }
                if (onBleScanListener != null) {
                    onBleScanListener.scanResult(device);
                }

            }
        });
        if (onBleScanListener != null) {
            onBleScanListener.startScan();
        }*/
    }

    public void stopScan() {

    }

    public void connectDevice(BluetoothDevice bluetoothDevice) {
        if (bleGatt != null) {
            bleGatt.disconnect();
            bleGatt.close();
            bleGatt = null;
        }
        bleGatt = bluetoothDevice.connectGatt(AppEvent.getContext(), false, bleGattCallback);
    }

    BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == 2) {
                Log.d(TAG, "马桶已连接");
                gatt.discoverServices();
            } else if (newState == 0) {
                Log.d(TAG, "马桶断开连接");
                gatt.disconnect();
                gatt.close();
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            findService(gatt.getServices());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] arrayOfbyte = characteristic.getValue();
            ToiletCommandObservable.getInstance().setValue(arrayOfbyte);
        }
    };

    private void findService(List<BluetoothGattService> paramList) {
        Iterator iterator1 = paramList.iterator();
        while (iterator1.hasNext()) {
            BluetoothGattService localBluetoothGattService = (BluetoothGattService) iterator1
                    .next();
            if (localBluetoothGattService.getUuid().toString().equalsIgnoreCase(UUID_SERVER)) {
                List localList = localBluetoothGattService.getCharacteristics();
                Iterator iterator2 = localList.iterator();
                while (iterator2.hasNext()) {
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = (BluetoothGattCharacteristic) iterator2.next();
                    if (bluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_NOTIFY)) {
                        bleGattCharacteristic = bluetoothGattCharacteristic;
                        break;
                    }
                }
                break;
            }
        }
        boolean isEnableNotification = bleGatt.setCharacteristicNotification(bleGattCharacteristic, true);
        if (isEnableNotification) {
            List<BluetoothGattDescriptor> descriptorList = bleGattCharacteristic.getDescriptors();
            if (descriptorList != null && descriptorList.size() > 0) {
                for (BluetoothGattDescriptor descriptor : descriptorList) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bleGatt.writeDescriptor(descriptor);
                }
            }
        }
    }

    public boolean write(byte byteArray[]) {
        if (bleGattCharacteristic == null) {
            return false;
        }
        if (bleGatt == null) {
            return false;
        }
        bleGattCharacteristic.setValue(byteArray);
        return bleGatt.writeCharacteristic(bleGattCharacteristic);
    }

    public interface OnBleScanListener {
        void scanResult(BluetoothDevice bluetoothDevice);

        void startScan();
    }

}
