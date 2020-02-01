package com.pooai.pooaisdkdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.pooai.blesdk.PooaiBleManager;
import com.pooai.blesdk.PooaiDetectionManager;
import com.pooai.blesdk.data.PooaiBleDevice;
import com.pooai.blesdk.data.PooaiOvulationData;
import com.pooai.blesdk.data.PooaiPregnancyData;
import com.pooai.blesdk.service.PooaiToiletService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.rv_ble_device)
    RecyclerView mRecyclerView;

    private PooaiToiletService mPooaiToiletService;

    private BleApdater mBleApdater;

    private List<PooaiBleDevice> mPooaiBleDeviceList;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PooaiToiletService.PooaiServiceBinder pooaiServiceBinder = (PooaiToiletService.PooaiServiceBinder) service;
            mPooaiToiletService = pooaiServiceBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPooaiBleDeviceList = new ArrayList<>();
        PooaiBleDevice pooaiBleDevice = new PooaiBleDevice();
        pooaiBleDevice.setName("pooai08");
        pooaiBleDevice.setMacAddress("adsdasdasdas");
        mPooaiBleDeviceList.add(pooaiBleDevice);
        mBleApdater = new BleApdater(mPooaiBleDeviceList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mBleApdater);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("BRG", "没有权限");
            // 没有权限，申请权限。
            // 申请授权。
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        initListener();
    }

    private void initListener() {

        mBleApdater.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

            }
        });
    }

    @OnClick(R.id.bt_start_service)
    public void startService() {
        PooaiBleManager pooaiBleManager = new PooaiBleManager();
        pooaiBleManager.scanDevice(new PooaiBleManager.OnBleScanListener() {
            @Override
            public void scanResult(PooaiBleDevice pooaiBleDevice) {
                Log.d(TAG, "deviceName = " + pooaiBleDevice.getName());
                if (!mPooaiBleDeviceList.contains(pooaiBleDevice)) {
                    mPooaiBleDeviceList.add(pooaiBleDevice);
                    mBleApdater.notifyDataSetChanged();
                }
            }
        });

    }
}
