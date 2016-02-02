package jp.ac.it_college.std.s14002.android.bluetoothmessenger;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class DeviceListActivity extends Activity {
    public static final String OUI_OTHER_DEVICES = "00:16:53";
    static final String PAIRING = "pairing";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> pairedDevicesAdapter;
    private ArrayAdapter<String> nonPairedDeviceAdapter;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //名前不明はまだ登録しない
                if (device.getName() != null) {
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        nonPairedDeviceAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                }


            }

            //　名前が検出された
            if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                //インテントからデバイスを取得
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    nonPairedDeviceAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
            // 発見終了
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (nonPairedDeviceAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    nonPairedDeviceAdapter.add(noDevices);
                }
            }

        }
    };
    private String myNumber;
    private AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            bluetoothAdapter.cancelDiscovery();
            String info = ((TextView) v).getText().toString();
            Log.e("DeviceListActivity", info);
            String macAddress = info.substring(info.length() - 17);
            Log.e("DeviceListActivity" , macAddress);
            Intent intent = new Intent();
            Bundle data = new Bundle();
            data.putString(EXTRA_DEVICE_ADDRESS, macAddress);
            data.putBoolean(PAIRING, av.getId() == R.id.other_devices);
            intent.putExtras(data);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //サーバースレッド起動、クライアントのからの要求待ちを開始
        BluetoothServerThread BtServerThread = new BluetoothServerThread(this, myNumber, bluetoothAdapter);
        BtServerThread.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //アクションバーにProgressアイコンを表示させる
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);

        setResult(Activity.RESULT_CANCELED);


        //デバイスをスキャンボタンが押された時の処理
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // デバイススキャン処理
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });


        // 接続履歴のあるデバイスを取得
        pairedDevicesAdapter = new ArrayAdapter<>(this, R.layout.device_name);

        nonPairedDeviceAdapter = new ArrayAdapter<>(this, R.layout.device_name);

        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesAdapter);
        pairedListView.setOnItemClickListener(deviceClickListener);

        ListView newDevicesListView = (ListView) findViewById(R.id.other_devices);
        newDevicesListView.setAdapter(nonPairedDeviceAdapter);
        newDevicesListView.setOnItemClickListener(deviceClickListener);

        //インテントフィルターとBroadReceiverの登録
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        //BluetoothAdapterから接続履歴のあるデバイスの情報を取得
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        boolean devicesFound = false;
        findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
        if (pairedDevices.size() > 0) {
            //接続履歴のあるデバイスが存在する
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().startsWith(OUI_OTHER_DEVICES)) {
                    devicesFound = true;
                    pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }

            }

        }

        if (!devicesFound) {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesAdapter.add(noDevices);

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void doDiscovery() {
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        findViewById(R.id.title_other_devices).setVisibility(View.VISIBLE);

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }
}

