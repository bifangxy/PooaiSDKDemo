package com.pooai.blesdk;

import com.pooai.blesdk.data.ToiletCommand;
import com.pooai.blesdk.data.ToiletConfig;
import com.pooai.blesdk.data.ToiletRegisterData;
import com.pooai.blesdk.data.ToiletState;

/**
 * 作者：created by xieying on 2020-01-12 17:43
 * 功能：
 */
public class PooaiControlManager {

    private static class SingletonHolder {
        private static final PooaiControlManager INSTANCE = new PooaiControlManager();
    }

    private ToiletRegisterData mToiletRegisterData;

    private PooaiToiletCommandManager mPooaiToiletCommandManager;

    public static PooaiControlManager getInstance() {
        return PooaiControlManager.SingletonHolder.INSTANCE;
    }

    public PooaiControlManager() {
        mToiletRegisterData = ToiletRegisterData.getInstance();
        mPooaiToiletCommandManager = PooaiToiletCommandManager.getInstance();
    }

    /**
     * 开始臀洗
     */
    public static void startHipWash() {

    }

    /**
     * 停止臀洗
     */
    public static void stopHipWash() {

    }


    /**
     * 开始妇洗
     */
    public static void startWomanWash() {

    }


    /**
     * 停止妇洗
     */
    public static void stopWomanWash() {

    }

    /**
     * 开始通便
     */
    public static void startLaxative() {

    }

    /**
     * 停止通便
     */
    public static void stopLaxative() {

    }

    /**
     * 开始烘干
     */
    public static void startDrying() {

    }

    /**
     * 停止烘干
     */
    public static void stopDrying() {

    }

    /**
     * 开始按摩
     */
    public static void startMassage() {

    }

    /**
     * 停止按摩
     */
    public static void stopMassage() {

    }

    /**
     * 开始雾化
     */
    public static void startAtomize() {

    }

    /**
     * 停止雾化
     */
    public static void stopAtomize() {

    }

    /**
     * 调节坐垫温度
     *
     * @param gear 档位
     */
    public static void cushionTemp(int gear) {

    }

    /**
     * 调节水温
     *
     * @param gear 档位
     */
    public static void waterTemp(int gear) {

    }

    /**
     * 调节喷嘴位置
     *
     * @param gear 档位
     */
    public static void nozzlePosition(int gear) {

    }

    /**
     * 调节风温
     *
     * @param gear 档位
     */
    public static void windTemp(int gear) {

    }

    /**
     * 调节水压
     *
     * @param gear 档位
     */
    public static void waterPressure(int gear) {

    }

    /**
     * 开双盖
     */
    public static void openLid() {

    }

    /**
     * 开单盖
     */
    public static void openHalfLid() {

    }

    /**
     * 关盖
     */
    public static void closeLid() {

    }

    public void switchControlMode() {
        PooaiToiletCommandManager.getInstance().changeToiletState(ToiletState.CONTROL);
    }

    public void openLight() {
        if (mToiletRegisterData.getRegisterBitValue(ToiletConfig.REGISTER_TOILET_CONFIG5, 13) != 1) {
            ToiletCommand toiletCommand = mToiletRegisterData.getRegisterBitCommand(ToiletConfig.REGISTER_TOILET_CONFIG5, 13, 1);
            mPooaiToiletCommandManager.addToiletCommand(toiletCommand);
        }
    }

    public void closeLight() {
        if (mToiletRegisterData.getRegisterBitValue(ToiletConfig.REGISTER_TOILET_CONFIG5, 13) == 1) {
            ToiletCommand toiletCommand = mToiletRegisterData.getRegisterBitCommand(ToiletConfig.REGISTER_TOILET_CONFIG5, 13, 0);
            mPooaiToiletCommandManager.addToiletCommand(toiletCommand);
        }
    }

    public void openAutoFlip() {
        if (mToiletRegisterData.getRegisterBitValue(ToiletConfig.REGISTER_TOILET_CONFIG5, 14) != 1) {
            ToiletCommand toiletCommand = mToiletRegisterData.getRegisterBitCommand(ToiletConfig.REGISTER_TOILET_CONFIG5, 14, 1);
            mPooaiToiletCommandManager.addToiletCommand(toiletCommand);
        }
    }

    public void closeAutoFlip() {
        if (mToiletRegisterData.getRegisterBitValue(ToiletConfig.REGISTER_TOILET_CONFIG5, 14) == 1) {
            ToiletCommand toiletCommand = mToiletRegisterData.getRegisterBitCommand(ToiletConfig.REGISTER_TOILET_CONFIG5, 14, 0);
            mPooaiToiletCommandManager.addToiletCommand(toiletCommand);
        }
    }


}
