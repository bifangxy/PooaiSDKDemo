package com.pooai.blesdk.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：created by xieying on 2020-01-12 22:52
 * 功能：
 */
public class ToiletRegisterData {

    private static List<RegisterValue> mRegisterValueList;


    public ToiletRegisterData() {
        mRegisterValueList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            mRegisterValueList.add(new RegisterValue(i, 0));
        }
    }

    private static class SingleHelper {
        public static ToiletRegisterData INSTANCE = new ToiletRegisterData();
    }

    /**
     * 设置寄存器的值
     *
     * @param address 地址
     * @param value   值
     */
    public static void setValue(int address, int value) {
        for (RegisterValue registerValue : mRegisterValueList) {
            if (address == registerValue.getAddress()) {
                registerValue.setValue(value);
            }
        }
    }

    /**
     * 获取寄存器的值
     *
     * @param address 地址
     * @return
     */
    public static int getRegisterValue(int address) {
        for (RegisterValue registerValue : mRegisterValueList) {
            if (registerValue.getAddress() == address) {
                return registerValue.getValue();
            }
        }
        return -1;
    }

}
