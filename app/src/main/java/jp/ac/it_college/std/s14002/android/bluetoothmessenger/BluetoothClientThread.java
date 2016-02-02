package jp.ac.it_college.std.s14002.android.bluetoothmessenger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by s14002 on 16/01/29.
 */
public class BluetoothClientThread extends Thread {
    //Client側の処理
    private final BluetoothSocket clientSocket;
    private Context mContext;
    // UUIDの生成
    public static final UUID IT_BLUETOOTH_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static BluetoothAdapter myClientAdapter;
    public String myNumber;

    //コンストラクタ定義
    public BluetoothClientThread(Context context, String myNum,BluetoothDevice device, BluetoothAdapter bluetoothAdapter) {
        //各種初期化
        mContext = context;
        BluetoothSocket tmpSocket = null;
        myClientAdapter = bluetoothAdapter;
        myNumber = myNum;

        try {
            //自デバイスのBluetoothクライアントソケットの取得
            tmpSocket = device.createRfcommSocketToServiceRecord(IT_BLUETOOTH_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientSocket = tmpSocket;
    }

    public void run() {
        //接続要求を出す前に、検索処理を中断させる
        if (myClientAdapter.isDiscovering()) {
            myClientAdapter.cancelDiscovery();
        }
        try {
            // サーバー側に接続要求
            clientSocket.connect();
        } catch (IOException e) {
            try {
                clientSocket.close();
            } catch (IOException closeException) {
                e.printStackTrace();
            }
            return;
        }

        //接続完了時の処理
        ReadWriteModel readWriteModel = new ReadWriteModel(mContext, clientSocket, myNumber);
        readWriteModel.start();
    }
    public void cancel() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}