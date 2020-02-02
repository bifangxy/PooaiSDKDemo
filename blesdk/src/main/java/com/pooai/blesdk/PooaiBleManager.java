package com.pooai.blesdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.util.Log;

import com.pooai.blesdk.observer.ToiletCommandObservable;
import com.pooai.blesdk.util.TimerTaskUtil;

import java.util.Iterator;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * 作者：created by xieying on 2020-02-01 23:24
 * 功能：
 */
public class PooaiBleManager {
    private static final String TAG = PooaiBleManager.class.getSimpleName();
    //UUID
    private static final String UUID_SERVER = "0000ffe0-0000-1000-8000-00805f9b34fb";

    private static final String UUID_NOTIFY = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private BluetoothAdapter bleAdapter;

    private BluetoothGatt bleGatt;

    private BluetoothGattCharacteristic bleGattCharacteristic;

    private OnBleScanListener mOnBleScanListener;

    private boolean mScanning;

    private boolean mConnected;

    private static class SingletonHolder {
        private static final PooaiBleManager INSTANCE = new PooaiBleManager();
    }

    public static PooaiBleManager getInstance() {
        return PooaiBleManager.SingletonHolder.INSTANCE;
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
        return bleAdapter != null;
    }

    public boolean isBleOpen() {
        return bleAdapter.isEnabled();
    }

    public void startScan(OnBleScanListener onBleScanListener) {
        if (mScanning) {
            return;
        }
        if (bleAdapter == null) {
            return;
        }
        mOnBleScanListener = onBleScanListener;
        TimerTaskUtil.timerRx(10000, new TimerTaskUtil.OnRxListener() {
            @Override
            public void onNext(Long aLong) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {
                stopScan();
            }

            @Override
            public void onSubscribe(Disposable disposable) {

            }
        });
        mScanning = true;
        bleAdapter.startLeScan(startLeScan);
        if (mOnBleScanListener != null) {
            mOnBleScanListener.startScan();
        }
    }

    public void stopScan() {
        if (bleAdapter == null) {
            return;
        }
        bleAdapter.stopLeScan(startLeScan);
        mScanning = false;
        if (mOnBleScanListener != null) {
            mOnBleScanListener.stopScan();
        }
    }

    public void connectDevice(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice == null) {
            return;
        }
        if (bleGatt != null) {
            bleGatt.disconnect();
            bleGatt.close();
            bleGatt = null;
        }
        bleGatt = bluetoothDevice.connectGatt(AppEvent.getContext(), false, bleGattCallback);
    }

    public void disconnectedDevice() {
        if (bleGatt != null) {
            bleGatt.disconnect();
            bleGatt.close();
            bleGatt = null;
        }
    }

    BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "status = " + status + "  newState = " + newState);
            if (newState == 2) {
                Log.d(TAG, "马桶已连接");
                mConnected = true;
                gatt.discoverServices();
            } else if (newState == 0) {
                PooaiToiletCommandManager.getInstance().stopHeartbeat();
                mConnected = false;
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
        PooaiToiletCommandManager.getInstance().startHeartbeat();
    }

    public boolean write(byte[] byteArray) {
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

        void stopScan();
    }

    public boolean isDeviceConnected() {
        return mConnected;
    }

}
