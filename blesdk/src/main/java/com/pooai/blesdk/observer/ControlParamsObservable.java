package com.pooai.blesdk.observer;

import java.util.Observable;

/**
 * 作者：created by xieying on 2020-01-12 18:38
 * 功能：
 */
public class ControlParamsObservable extends Observable {

    private static class SingletonHolder {
        private static final ControlParamsObservable INSTANCE = new ControlParamsObservable();
    }

    public static ControlParamsObservable getInstance() {
        return SingletonHolder.INSTANCE;
    }



    public void setValue(byte[] values) {
        setChanged();
        notifyObservers(values);
    }

}
