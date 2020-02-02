package com.pooai.pooaisdkdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.pooai.blesdk.PooaiBleManager;
import com.pooai.blesdk.PooaiDetectionManager;
import com.pooai.blesdk.PooaiToiletCommandManager;
import com.pooai.blesdk.data.PooaiOvulationData;
import com.pooai.blesdk.data.PooaiPregnancyData;
import com.pooai.blesdk.data.PooaiUrineData;
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

    private BleApdater mBleApdater;

    private List<BluetoothDevice> mPooaiBleDeviceList;

    private PooaiBleManager mPooaiBleManager;

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

        mPooaiBleDeviceList = new ArrayList<>();
        mBleApdater = new BleApdater(mPooaiBleDeviceList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mBleApdater);


        mPooaiBleManager = PooaiBleManager.getInstance();
        mPooaiBleManager.initBLE();
        initListener();
    }

    private void initListener() {

        mBleApdater.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                BluetoothDevice pooaiBleDevice = mBleApdater.getData().get(position);
                mPooaiBleManager.connectDevice(pooaiBleDevice);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick(R.id.bt_start_service)
    public void startService() {
        mPooaiBleManager.startScan(new PooaiBleManager.OnBleScanListener() {
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
        });
    }

    @OnClick(R.id.bt_send_command)
    public void sendCommand() {
        PooaiToiletCommandManager.getInstance().startHeartbeat();
    }

    @OnClick(R.id.bt_start_urine)
    public void startUrine() {
        PooaiDetectionManager pooaiDetectionManager = new PooaiDetectionManager();
        pooaiDetectionManager.switchDetectionMode();

        pooaiDetectionManager.openUrineTank();

        pooaiDetectionManager.startUrineTest(new PooaiDetectionManager.OnDetectionListener<PooaiUrineData>() {
            @Override
            public void start() {
                Log.d(TAG, "---开始尿检---");
            }

            @Override
            public void complete(PooaiUrineData data) {
                Log.d(TAG, "---检测完成---");
                Log.d(TAG, "---data---"+data.sourceData);
            }

            @Override
            public void cancel() {

            }

            @Override
            public void error(Throwable throwable) {

            }
        });
    }

    @OnClick(R.id.bt_start_pregnancy)
    public void startPregnancy(){
        PooaiDetectionManager pooaiDetectionManager = new PooaiDetectionManager();
        pooaiDetectionManager.switchDetectionMode();


        pooaiDetectionManager.openPregnancyAndOvulationTank();

        pooaiDetectionManager.startPregnancyTest(new PooaiDetectionManager.OnDetectionListener<PooaiPregnancyData>() {
            @Override
            public void start() {
                Log.d(TAG, "---开始孕检---");
            }

            @Override
            public void complete(PooaiPregnancyData data) {
                Log.d(TAG, "---检测完成---");
                Log.d(TAG, "---data---"+data.pregnancyResult);
            }

            @Override
            public void cancel() {

            }

            @Override
            public void error(Throwable throwable) {

            }
        });
    }

    @OnClick(R.id.bt_start_ovulation)
    public void startOvulation(){
        PooaiDetectionManager pooaiDetectionManager = new PooaiDetectionManager();
        pooaiDetectionManager.switchDetectionMode();


        pooaiDetectionManager.openPregnancyAndOvulationTank();

        pooaiDetectionManager.startOvulationTest(new PooaiDetectionManager.OnDetectionListener<PooaiOvulationData>() {
            @Override
            public void start() {
                Log.d(TAG, "---开始排卵---");
            }

            @Override
            public void complete(PooaiOvulationData data) {
                Log.d(TAG, "---检测完成---");
                Log.d(TAG, "---data---"+data.ovulationResult);
            }

            @Override
            public void cancel() {

            }

            @Override
            public void error(Throwable throwable) {

            }
        });
    }
}
