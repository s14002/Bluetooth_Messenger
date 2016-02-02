package jp.ac.it_college.std.s14002.android.bluetoothmessenger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by s14002 on 16/01/29.
 */
public class BluetoothServerThread extends Thread {
    //Server側の処理
    private final BluetoothServerSocket serverSocket;
    static BluetoothAdapter myServerAdapter;
    private Context mContext;
    //UUID作成
    public static final UUID IT_BLUETOOTH_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public String myNumber;

    //コントラスタの定義
    public BluetoothServerThread(Context context, String myNum, BluetoothAdapter bluetoothAdapter) {
        mContext = context;
        BluetoothServerSocket tmpServerSocket = null;
        myServerAdapter = bluetoothAdapter;
        myNumber = myNum;
        try {
            //自デバイスのBluetoothサーバーソケットを取得
            tmpServerSocket = myServerAdapter.listenUsingInsecureRfcommWithServiceRecord("BlueToothSample03", IT_BLUETOOTH_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSocket = tmpServerSocket;
    }

    public void run() {
        BluetoothSocket receivedSocket;
        while (true) {
            try {
                //クライアントからの接続要求待ち。ソケットが返される
                receivedSocket = serverSocket.accept();
            } catch (IOException e) {
                break;
            }

            if (receivedSocket != null) {
                //ソケットを受け取れていた（接続完了時）の処理
                //manageSocketを移す
                ReadWriteModel readWriteModel = new ReadWriteModel(mContext, receivedSocket, myNumber);
                readWriteModel.start();

                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void cancel() {
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
    }
}