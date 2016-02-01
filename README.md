# Bluetooth_Messenger
・レイアウト  
・Bluetoothの対応端末チェックとBluetooth有効化ダイアログ　MainActivityクラスのonStart()メソッド  
・「接続履歴のあるデバイス」リスト（端末名とMACアドレス）　　DeviceListActivityクラスのonCreateメソッド  
・「その他のデバイス」検出　　DeviceListActivityクラスのBroadcastReceiverメソッド  
・自分の端末を検出可能にする(300秒間)リクエストダイアログ MainActivityクラスのenDiscoverable()メソッド  
・プログレスダイアログ　MainActivityクラスのstartReadWriteModelメソッド  
・終了ダイアログ　DeviceListActivityクラスのdisplayAlertDialog()メソッド  
