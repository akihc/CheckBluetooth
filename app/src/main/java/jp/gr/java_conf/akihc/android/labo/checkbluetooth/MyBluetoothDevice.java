package jp.gr.java_conf.akihc.android.labo.checkbluetooth;

import android.bluetooth.BluetoothDevice;

import lombok.Data;

/**
 * Bluetooth Device 情報
 */
@Data
public class MyBluetoothDevice {
    /**
     * デバイス名称
     */
    private String name;
    /**
     * MACアドレス
     */
    private String address;
    /**
     * 状態
     * true:接続済
     */
    private boolean connected;
    /**
     * 表示アイコンID
     */
    private int iconId;
    /**
     * Bluetoothデバイス情報
     */
    private BluetoothDevice device;
    /**
     * コンストラクタ
     * @param iconId 表示アイコンID
     * @param connected 状態
     * @param device Bluetoothデバイス
     */
    public MyBluetoothDevice(int iconId, boolean connected, BluetoothDevice device) {
        this.iconId = iconId;
        this.connected = connected;
        this.device = device;
        this.name = device.getName();
        this.address = device.getAddress();
    }


    /**
     * 接続状態のセット
     */
    public void setConnected() {
        this.iconId = R.drawable.ic_bluetooth_connected_black_36dp;
        this.connected=true;
    }


    /**
     * 接続解除状態のセット
     */
    public void setDisconnected() {
        this.iconId = R.drawable.ic_bluetooth_disabled_black_36dp;
        this.connected=false;
    }


    /**
     * リーダー情報のクリア
     */
    public void clear(){
        this.iconId = R.drawable.ic_bluetooth_disabled_black_36dp;
        this.connected = false;
        this.device = null;
        this.name = "";
        this.address = "";
    }

}
