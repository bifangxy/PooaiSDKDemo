package com.pooai.blesdk;

import android.util.Log;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.pooai.blesdk.data.PooaiBleDevice;
import com.pooai.blesdk.observer.ToiletCommandObservable;
import com.pooai.blesdk.util.TimerTaskUtil;

import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

/**
 * 作者：created by xieying on 2020-01-12 15:16
 * 功能：蓝牙操作管理类
 */
public class PooaiBleManager {
    private static final String TAG = PooaiBleManager.class.getSimpleName();

    private static final String SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";

    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private RxBleClient mRxBleClient;

    private RxBleDevice mRxBleDevice;

    private Disposable mScanDisposable;

    private Disposable mConnectionDisposable;

    private Disposable mConnectionStateDisposable;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();


    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();


    public PooaiBleManager() {
        mRxBleClient = AppEvent.getRxBleClient();
        mRxBleDevice = AppEvent.getRxBleDevice();
    }

    /**
     * 调用此方法前应获取打开蓝牙并获取权限
     *
     * @param onBleScanListener
     */
    public void scanDevice(OnBleScanListener onBleScanListener) {
        if (mScanDisposable != null) {
            return;
        }
        Log.d(TAG, "---开始扫描---");
        mScanDisposable = mRxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::stopScanDevice)
                .subscribe((ScanResult scanResult) -> {
                    if (onBleScanListener != null) {
                        PooaiBleDevice pooaiBleDevice = new PooaiBleDevice();
                        pooaiBleDevice.setMacAddress(scanResult.getBleDevice().getMacAddress());
                        pooaiBleDevice.setName(scanResult.getBleDevice().getName());
                        onBleScanListener.scanResult(pooaiBleDevice);
                    }
                }, throwable -> {

                });
        compositeDisposable.add(mScanDisposable);

        TimerTaskUtil.timerRx(10000, new TimerTaskUtil.OnRxListener() {
            @Override
            public void onNext(Long aLong) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {
                stopScanDevice();
            }

            @Override
            public void onSubscribe(Disposable disposable) {

            }
        });
    }

    public void stopScanDevice() {
        if (mScanDisposable != null) {
            Log.d(TAG, "---停止扫描---");
            mScanDisposable.dispose();
            mScanDisposable = null;
        }
    }


    public void connectDevice(String macAddress) {
        if (macAddress == null) {
            throw new RuntimeException("ScanResult can not be null");
        }
        mRxBleDevice = mRxBleClient.getBleDevice(macAddress);
        if (mRxBleDevice == null) {
            throw new RuntimeException("RxBleDevice can not be null");
        }
        AppEvent.setRxBleDevice(mRxBleDevice);
        mConnectionDisposable = prepareConnectionObservable()
                .flatMapSingle(RxBleConnection::discoverServices)
                .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(CHARACTERISTIC_UUID))
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::stopConnectDevice)
                .subscribe(bluetoothGattCharacteristic -> {
                    //连接成功
                    openNotify();
                    Log.d(TAG,"---连接成功---");
                }, throwable -> {
                    Log.d(TAG,"---连接失败---");
                    //连接失败
                }, () -> {
                    Log.d(TAG,"---连接断开---");
                    //连接断开
                });
        compositeDisposable.add(mConnectionDisposable);

    }

    //停止连接
    public void stopConnectDevice() {
        if (mConnectionDisposable != null) {
            mConnectionDisposable.dispose();
            mConnectionDisposable = null;
        }
    }

    //断开连接
    public void disconnectDevice(RxBleDevice bleDevice) {
        disconnectTriggerSubject.onNext(true);
    }

    public void write(byte[] args) {
        if (isConnected()) {
            Disposable disposable = prepareConnectionObservable()
                    .firstOrError()
                    .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(CHARACTERISTIC_UUID, args))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            bytes -> {
                                Log.d(TAG,"--发送成功--");
                                //write success
                            }, throwable -> {
                                Log.d(TAG,"--失败--");
                                //write fail
                            }
                    );
            compositeDisposable.add(disposable);
        }
    }

    //打开通知
    public void openNotify() {
        if (isConnected()) {
            Disposable disposable = prepareConnectionObservable()
                    .flatMap(rxBleConnection -> rxBleConnection.setupNotification(CHARACTERISTIC_UUID))
                    .doOnNext(notificationObservable -> {

                    })
                    .flatMap(notificationObservable -> notificationObservable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bytes -> {
                        ToiletCommandObservable.getInstance().setValue(bytes);
                    }, throwable -> {

                    });
            compositeDisposable.add(disposable);
        } else {
            Log.e(TAG, "device is not connected");
        }
    }

    //观察连接状态
    public void observerConnectState(OnBleConnectStateListener onBleConnectStateListener) {
        mConnectionStateDisposable = mRxBleDevice.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxBleConnectionState -> {
                    if (onBleConnectStateListener != null) {
                        onBleConnectStateListener.connectState(rxBleConnectionState);
                    }
                });
        compositeDisposable.add(mConnectionStateDisposable);
    }

    public void stopObserverConnectState() {
        if (mConnectionStateDisposable != null) {
            mConnectionStateDisposable.dispose();
            mConnectionStateDisposable = null;
        }
    }

    public void clear() {
        compositeDisposable.clear();
    }

    public interface OnBleScanListener {
        void scanResult(PooaiBleDevice pooaiBleDevice);
    }

    public interface OnBleConnectStateListener {
        void connectState(RxBleConnection.RxBleConnectionState connectionState);
    }

    private boolean isConnected() {
        if (mRxBleDevice != null) {
            return mRxBleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
        }
        return false;
    }

    private Observable<RxBleConnection> prepareConnectionObservable() {
        if (mRxBleDevice != null) {
            return mRxBleDevice.establishConnection(false)
                    .takeUntil(disconnectTriggerSubject);
        } else {
            throw new RuntimeException("RxBleDevice can not be null");
        }
    }
}
