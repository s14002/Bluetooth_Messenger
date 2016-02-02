package jp.ac.it_college.std.s14002.android.bluetoothmessenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int MENU_TOGGLE_CONNECT = Menu.FIRST;
    private static final int REQUEST_CONNECT_DEVICE = 1000;
    private static final int REQUEST_ENABLE_BT = 2000;
    static String TAG = "Menu";
    boolean newDevice;
    private ReadWriteModel myReadWriteModel = null;
    private ProgressDialog connectingProgressDialog;
    private boolean connected = false;
    private boolean bt_error_pending = false;
    private Handler btcHandler;
    private Toast mShortToast;
    //Menu
    private Menu myMenu;
    final Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message myMessage) {
            switch (myMessage.getData().getInt("message")) {
                case ReadWriteModel.STATE_CONNECTED:
                    connected = true;
                    connectingProgressDialog.dismiss();
                    updateButtonsAndMenu();
                    showToastLong(getResources().getString(R.string.connected));

                    break;
                case ReadWriteModel.STATE_CONNECTERROR:
                    connectingProgressDialog.dismiss();
                    break;

                case ReadWriteModel.STATE_RECEIVEERROR:
                case ReadWriteModel.STATE_SENDERROR:
                    destroyReadWriteModel();

                    if (!bt_error_pending) {
                        bt_error_pending = true;
                        DialogFragment newFragment = MyAlertDialogFragment.newInstance(
                                R.string.bt_error_dialog_title, R.string.bt_error_dialog_message);
                        newFragment.show(getFragmentManager(), "dialog");
                    }

                    break;

            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String macAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    newDevice = data.getExtras().getBoolean(DeviceListActivity.PAIRING);
                    if (newDevice) {
                        enDiscoverable();
                    }
                    startReadWriteModel(macAddress);
                }
                break;

            case REQUEST_ENABLE_BT:
                switch (resultCode) {

                    //BluetoothがONにされた場合の処理
                    case Activity.RESULT_OK:
                        selectDevices();
                        Toast.makeText(this, R.string.bt_is_enabled, Toast.LENGTH_SHORT).show();
                        break;

                    //BluetoothがOFFにされた場合の処理
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, R.string.bt_needs_to_be_enabled, Toast.LENGTH_SHORT).show();
                        finish();
                        break;

                    default:
                        Toast.makeText(this, R.string.problem_at_connecting, Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                }
        }
    }

    // connectingProgressのあとの処理をかく　
    private void startReadWriteModel(String macAddress) {
        connectingProgressDialog = ProgressDialog.show(this, "", getResources().getString(
                R.string.connecting_please_wait), true);

        if (myReadWriteModel == null) {
            createReadWriteModel();
        }


        switch ((myReadWriteModel).getState()) {
            case NEW:
                myReadWriteModel.setMacAddress(macAddress);
                myReadWriteModel.start();
                break;
            default:
                connected = false;
                myReadWriteModel = null;
                createReadWriteModel();
                myReadWriteModel.setMacAddress(macAddress);
                myReadWriteModel.start();
                break;
        }

        updateButtonsAndMenu();
    }

    public void doPositiveClick() {
        bt_error_pending = false;
        selectDevices();
    }


    private void destroyReadWriteModel() {
        if (myReadWriteModel != null) {
            sendBtcMessage(ReadWriteModel.NO_DELAY, ReadWriteModel.DISCONNECT);
            myReadWriteModel = null;
        }
        connected = false;
    }

    public void sendBtcMessage(int delay, int message) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", message);
        Message myMessage = myHandler.obtainMessage();
        myMessage.setData(myBundle);

        if (delay == 0)
            btcHandler.sendMessage(myMessage);
        else
            btcHandler.sendMessageDelayed(myMessage, delay);
    }

    public void sendBtcMessage(int delay, int message, int value1) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", message);
        myBundle.putInt("value1", value1);
        Message myMessage = myHandler.obtainMessage();
        myMessage.setData(myBundle);

        if (delay == 0)
            btcHandler.sendMessage(myMessage);
        else
            btcHandler.sendMessageDelayed(myMessage, delay);
    }

    public void sendBtcMessage(int delay, int message, int value1, int value2) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", message);
        myBundle.putInt("value1", value1);
        myBundle.putInt("value2", value2);
        Message myMessage = myHandler.obtainMessage();
        myMessage.setData(myBundle);

        if (delay == 0)
            btcHandler.sendMessage(myMessage);
        else
            btcHandler.sendMessageDelayed(myMessage, delay);
    }

    private void showToastLong(String textToShow) {
        mShortToast.setText(textToShow);
        mShortToast.show();
    }

    private void createReadWriteModel() {
        myReadWriteModel = new ReadWriteModel(this, myHandler, BluetoothAdapter.getDefaultAdapter());
        btcHandler = myReadWriteModel.getHandler();
    }

    private void enDiscoverable() {
        Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        myMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    protected void onStop() {
        super.onStop();
        destroyReadWriteModel();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connection_settings:
                Log.d(TAG, "Select connection.");
                selectDevices();
                break;
            case R.id.name_settings:
                Log.d(TAG, "Select name");
                break;
            case R.id.quit:
                Log.d(TAG, "Select quit");
                displayAlertDialog();
                break;
        }
        return true;
    }

    private void displayAlertDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("確認");
        alert.setMessage("アプリを終了しますか？");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Yesボタンが押された時の処理
                finish();
                Toast.makeText(MainActivity.this, R.string.quit_message, Toast.LENGTH_LONG).show();
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Noボタンが押された時の処理
            }
        });
        alert.show();
    }

    private void updateButtonsAndMenu() {
        if (myMenu == null) return;
        myMenu.removeItem(MENU_TOGGLE_CONNECT);
        if (connected) {
            myMenu.add(0, MENU_TOGGLE_CONNECT, 1, getResources().getString(R.string.disconnect));
        } else {
            myMenu.add(0, MENU_TOGGLE_CONNECT, 1, getResources().getString(R.string.connect));
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        //BluetoothAdapter取得
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null) {
            //Bluetooth対応端末の場合の処理
            Toast.makeText(this, R.string.bt_available, Toast.LENGTH_SHORT).show();
        } else {
            //Bluetooth非対応端末の場合の処理
            Toast.makeText(this, R.string.bt_is_not_available, Toast.LENGTH_SHORT).show();
            finish();
        }
        assert bluetoothAdapter != null;
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, R.string.bt_needs_to_be_enabled, Toast.LENGTH_SHORT).show();
            // OFFだった場合、Bluetooth有効化ダイアログを表示
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            // BluetoothがONだった場合の処理
            selectDevices();

        }
    }


    private void selectDevices() {
        Intent intent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
    }

    @Override
    public void onClick(View v) {

    }
}

