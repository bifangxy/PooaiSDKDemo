package com.pooai.blesdk;

import com.pooai.blesdk.data.ToiletState;
import com.pooai.blesdk.util.PooaiToiletDataUtil;
import com.pooai.blesdk.util.TimerTaskUtil;

import io.reactivex.disposables.Disposable;

/**
 * 作者：created by xieying on 2020-01-12 18:00
 * 功能 马桶心跳管理
 */
public class PooaiToiletHeartbeatManager {

    private ToiletState mToiletState;

    private static final int SCAN_LENGTH = 30;

    private int startAddress = 0x01;

    private PooaiBleManager mPooaiBleManager;

    private TimerTaskUtil.Task mTask;


    public PooaiToiletHeartbeatManager(ToiletState toiletState) {
        mToiletState = toiletState;

        mPooaiBleManager = new PooaiBleManager();

        switch (mToiletState) {
            case CONTROL:
                startAddress = 0x01;
                break;
            case DETECTION:
                startAddress = 0x50;
                break;
            case HEART:
                break;
        }
    }

    public void startHeartbeat() {
        mTask = TimerTaskUtil.intervalRx(0, 300, new TimerTaskUtil.OnRxListener() {
            @Override
            public void onNext(Long aLong) {
                sendHeartbeatCommand();
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onSubscribe(Disposable disposable) {

            }
        });
    }

    private void sendHeartbeatCommand() {
        byte[] command = PooaiToiletDataUtil.getF03String(startAddress, SCAN_LENGTH);
        mPooaiBleManager.write(command);
    }


    public void stopHeartbeat() {
        if (mTask != null && !mTask.isDisposed()) {
            mTask.cancel();
        }
    }

    public void changeToiletState(ToiletState toiletState) {
        if (mToiletState != toiletState) {
            mToiletState = toiletState;
            switch (mToiletState) {
                case CONTROL:
                    startAddress = 0x01;
                    break;
                case DETECTION:
                    startAddress = 0x50;
                    break;
                case HEART:
                    break;
            }
        }
    }


}
