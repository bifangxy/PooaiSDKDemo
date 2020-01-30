package com.pooai.pooaisdkdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.pooai.blesdk.PooaiBleManager;
import com.pooai.blesdk.PooaiDetectionManager;
import com.pooai.blesdk.data.PooaiOvulationData;
import com.pooai.blesdk.data.PooaiPregnancyData;
import com.pooai.blesdk.service.PooaiToiletService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private PooaiToiletService mPooaiToiletService;

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

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("BRG", "没有权限");
            // 没有权限，申请权限。
            // 申请授权。
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @OnClick(R.id.bt_start_service)
    public void startService() {

        PooaiDetectionManager pooaiDetectionManager  = new PooaiDetectionManager();
        /*pooaiDetectionManager.startOvulationTest(new PooaiDetectionManager.OnDetectionListener<PooaiOvulationData>() {
            @Override
            public void start() {
                Log.d(TAG,"检测开始");
            }

            @Override
            public void complete(PooaiOvulationData data) {
                Log.d(TAG,"检测完成 "+data.ovulationResult);
            }

            @Override
            public void cancel() {

            }

            @Override
            public void error(Throwable throwable) {

            }
        });*/

//        pooaiDetectionManager.startHeartTest();
//        PooaiBleManager pooaiBleManager = new PooaiBleManager();
//        pooaiBleManager.scanDevice(new PooaiBleManager.OnBleScanListener() {
//            @Override
//            public void scanResult(String deviceName) {
//                Log.d(TAG, "deviceName = " + deviceName);
//            }
//        });
//        Intent intent = new Intent(this, PooaiToiletService.class);
//        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
}
