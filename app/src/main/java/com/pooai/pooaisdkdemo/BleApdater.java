package com.pooai.pooaisdkdemo;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.pooai.blesdk.data.PooaiBleDevice;

import java.util.List;

/**
 * 作者：created by xieying on 2020-02-01 14:01
 * 功能：
 */
public class BleApdater extends BaseQuickAdapter<PooaiBleDevice, BaseViewHolder> {
    public BleApdater(@Nullable List<PooaiBleDevice> data) {
        super(R.layout.item_ble_device, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, PooaiBleDevice item) {
        helper.setText(R.id.tv_ble_device_name,item.getName()+":"+item.getMacAddress());
    }
}
