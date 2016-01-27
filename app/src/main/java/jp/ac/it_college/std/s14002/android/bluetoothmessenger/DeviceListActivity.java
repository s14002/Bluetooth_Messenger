package jp.ac.it_college.std.s14002.android.bluetoothmessenger;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {
    //    public static final String OUI_OTHER_DEVICES = "00:16:53";
    static final String PAIRING = "pairing";
    //    private static final String TAG = "DeviceListActivity";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter newDevicesAdapter;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String dName = null;
            BluetoothDevice foundDevice;
            ListView nonpairedList = (ListView) findViewById(R.id.nonPiredDeviceList);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Toast.makeText(context, R.string.start_scan, Toast.LENGTH_LONG).show();
                //デバイスが検出された
                foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if ((dName = foundDevice.getName()) != null) {
                    if (foundDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                        //接続したことのないデバイスのみアダプタに詰める
                        newDevicesAdapter.add(dName + "\n" + foundDevice.getAddress());
                        Log.d("ACTION_FOUND", dName);
                    }
                }
                nonpairedList.setAdapter(newDevicesAdapter);
            }
            if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                //名前が検出された
                Log.d("ACTION_NAME_CHENGED", dName);
                foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (foundDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // 接続したことのないデバイスのみに詰める
                    newDevicesAdapter.add(dName + "\n" + foundDevice.getAddress());
                }
                nonpairedList.setAdapter(newDevicesAdapter);
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                Toast.makeText(context, R.string.finish_scan, Toast.LENGTH_LONG).show();


            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //名前はまだ登録しない
                if ((dName = device.getName()) != null) {
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        newDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                }
            }

            //　名前が検出された
            if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                //インテントからデバイスを取得
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    newDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
            // 発見終了
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (newDevicesAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.no_paired_devices).toString();
                    newDevicesAdapter.add(noDevices);
                }
            }

        }
    };
    private AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            bluetoothAdapter.cancelDiscovery();
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            Intent intent = new Intent();
            Bundle data = new Bundle();
            data.putString(EXTRA_DEVICE_ADDRESS, address);
            data.putBoolean(PAIRING, av.getId() == R.id.other_devices);
            intent.putExtras(data);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //オプションメニューが選択された時の処理
        TextView nonPairedListTitle = (TextView) findViewById(R.id.title_none_devices);
        nonPairedListTitle.setText("接続履歴なしデバイスリスト");

        //自デバイスの検出を有効にする
        Intent discoverableOn = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableOn.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableOn);

        if (item.getItemId() == Menu.FIRST) {
            //インテントフィルターとBroadReceiverの登録
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, filter);

            newDevicesAdapter = new ArrayAdapter(this, R.layout.activity_device_list);
            //接続可能なデバイスを検出
            if (bluetoothAdapter.isDiscovering()) {
                //検出中の場合は検出をキャンセルする
                bluetoothAdapter.cancelDiscovery();
            }
            //デバイスを検索する
            bluetoothAdapter.startDiscovery();
        }

        return false;
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

        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        newDevicesAdapter = new ArrayAdapter<>(this, R.layout.device_name);

        ListView pairedList = (ListView) findViewById(R.id.paired_devices);
        pairedList.setAdapter(pairedDevicesAdapter);
        pairedList.setOnItemClickListener(deviceClickListener);

        ListView newDevicesListView = (ListView) findViewById(R.id.other_devices);
        newDevicesListView.setAdapter(newDevicesAdapter);
        newDevicesListView.setOnItemClickListener(deviceClickListener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //BluetoothAdapterから接続履歴のあるデバイスの情報を取得
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//        boolean otherDeviceFound = false;
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            //接続履歴のあるデバイスが存在する
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }

        /*if (otherDeviceFound) {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesAdapter.add(noDevices);
        }*/

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

