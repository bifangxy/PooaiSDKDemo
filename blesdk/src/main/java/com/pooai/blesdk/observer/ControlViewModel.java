package com.pooai.blesdk.observer;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pooai.blesdk.data.ToiletControl;

import java.util.Observable;
import java.util.Observer;

/**
 * 作者：created by xieying on 2020-01-12 18:33
 * 功能：
 */
public class ControlViewModel extends ViewModel implements Observer {

    private MutableLiveData<ToiletControl> mToiletControlMutableLiveData = new MutableLiveData<>();

    public ControlViewModel() {
        ControlParamsObservable.getInstance().addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Byte[]) {
            //分析该数据
        }
    }

    public MutableLiveData<ToiletControl> getToiletControlMutableLiveData() {
        return mToiletControlMutableLiveData;
    }
}
