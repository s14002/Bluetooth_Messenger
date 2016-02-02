package jp.ac.it_college.std.s14002.android.bluetoothmessenger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by s14002 on 16/01/29.
 */
public class ReadWriteModel extends Thread {
    public static final int DISCONNECT = 99;

    public static final int DISPLAY_TOAST = 1000;
    public static final int STATE_CONNECTED = 1001;
    public static final int STATE_CONNECTERROR = 1002;
    public static final int STATE_RECEIVEERROR = 1004;
    public static final int STATE_SENDERROR = 1005;
    public static final int NO_DELAY = 0;
    public static final String OUI_OTHER_DEVICES = "00:16:53";
    private static final UUID IT_BLUETOOTH_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static InputStream inputStream;
    public static OutputStream outputStream;
    //Uiからmessagesを受け取る
    final Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message myMessage) {
            super.handleMessage(myMessage);
        }
    };
    BluetoothAdapter bluetoothAdapter;
    private String sendNumber;
    private Context mContext;
    private BluetoothSocket bluetoothSocket = null;
    private DataOutputStream dataOutputStream = null;
    private DataInputStream dataInputStream = null;
    private boolean connected = false;
    private Handler uiHandler;
    private String macAddress;
    private MainActivity myActivity;

    //コンストラクタの定義
    public ReadWriteModel(Context context, BluetoothSocket socket, String string) {
        sendNumber = string;
        mContext = context;

        try {
            //接続済みソケットからI/Oストリームをそれぞれ取得
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ReadWriteModel(MainActivity myActivity, Handler uiHandler, BluetoothAdapter bluetoothAdapter) {
        this.myActivity = myActivity;
        this.uiHandler = uiHandler;
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public Handler getHandler() {
        return myHandler;
    }

    public void write(byte[] buf) {
        //OutputStreamへのデータ書き込み
        try {
            outputStream.write(buf);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void run() {
        byte[] buf = new byte[1024];
        String rcvNum = null;
        int tmpBuf = 0;
        createDeviceConnection();

        try {
            write(sendNumber.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                tmpBuf = inputStream.read(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (tmpBuf != 0) {
                try {
                    rcvNum = new String(buf, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra("NUMBER", rcvNum);
            mContext.startActivity(intent);
        }
    }

    private void createDeviceConnection() {
        try {
            BluetoothSocket bluetoothSocketTemporary;
            BluetoothDevice bluetoothDevice;
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);

            if (bluetoothDevice == null) {
                sendToast(myActivity.getResources().getString(R.string.no_paired_devices));
                sendState(STATE_CONNECTERROR);
                return;
            }

            bluetoothSocketTemporary = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(IT_BLUETOOTH_UUID);
            bluetoothSocketTemporary.connect();
            bluetoothSocket = bluetoothSocketTemporary;

            dataInputStream = new DataInputStream(bluetoothSocket.getInputStream());
            dataOutputStream = new DataOutputStream(bluetoothSocket.getOutputStream());

            connected = true;
        } catch (IOException e) {
            Log.d("ReadWiteMode", "error createDeviceConnection()", e);
            if (myActivity.newDevice) {
                sendToast(myActivity.getResources().getString(R.string.pairing_message));
                sendState(STATE_CONNECTERROR);
            } else {
                sendState(STATE_CONNECTERROR);
            }
            return;
        }

        sendState(STATE_CONNECTED);
    }

    private void destroyDeviceConnection() {
        try {
            if (bluetoothSocket != null) {
                connected = false;
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
            dataInputStream = null;
            dataOutputStream = null;
        } catch (IOException e) {
            sendToast(myActivity.getResources().getString(R.string.problem_at_closing));
        }
    }

    private void sendToast(String toastText) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", DISPLAY_TOAST);
        myBundle.putString("toastText", toastText);
        sendBundle(myBundle);
    }

    private void sendState(int message) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", message);
        sendBundle(myBundle);
    }

    private void sendState(int message, int value) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", message);
        myBundle.putInt("value1", value);
        sendBundle(myBundle);
    }

    private void sendState(int message, float value) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", message);
        myBundle.putFloat("value1", value);
        sendBundle(myBundle);


    }

    private void sendBundle(Bundle myBundle) {
        Message myMessage = myHandler.obtainMessage();
        myMessage.setData(myBundle);
        uiHandler.sendMessage(myMessage);
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}