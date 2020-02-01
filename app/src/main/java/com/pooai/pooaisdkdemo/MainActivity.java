package com.pooai.pooaisdkdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.pooai.blesdk.PooaiBleManager;
import com.pooai.blesdk.PooaiBleManager2;
import com.pooai.blesdk.PooaiDetectionManager;
import com.pooai.blesdk.PooaiToiletCommandManager;
import com.pooai.blesdk.data.PooaiBleDevice;
import com.pooai.blesdk.data.PooaiOvulationData;
import com.pooai.blesdk.data.PooaiPregnancyData;
import com.pooai.blesdk.service.PooaiToiletService;
import com.pooai.blesdk.util.TimerTaskUtil;

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

    private PooaiBleManager2 pooaiBleManager2;

    private PooaiBleManager mPooaiBleManager;

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


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("BRG", "没有权限");
            // 没有权限，申请权限。
            // 申请授权。
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        mBleApdater = new BleApdater(mPooaiBleDeviceList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mBleApdater);


        pooaiBleManager2 = PooaiBleManager2.getInstance();
        pooaiBleManager2.initBLE();

        mPooaiBleManager = PooaiBleManager.getInstance();
        initListener();
    }

    private void initListener() {

        mBleApdater.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
               /* BluetoothDevice pooaiBleDevice = mBleApdater.getData().get(position);
                pooaiBleManager2.connectDevice(pooaiBleDevice);*/

               PooaiBleDevice pooaiBleDevice = mBleApdater.getData().get(position);
               mPooaiBleManager.connectDevice(pooaiBleDevice.getMacAddress());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

       /* pooaiBleManager2.startScan(new PooaiBleManager2.OnBleScanListener() {
            @Override
            public void scanResult(BluetoothDevice bluetoothDevice) {
                if (!mPooaiBleDeviceList.contains(bluetoothDevice)) {
                    mPooaiBleDeviceList.add(bluetoothDevice);
                    mBleApdater.notifyDataSetChanged();
                }
            }

            @Override
            public void startScan() {

            }
        });*/

    }

    @OnClick(R.id.bt_send_command)
    public void sendCommand() {
        PooaiToiletCommandManager.getInstance().startHeartbeat();
    }
}
