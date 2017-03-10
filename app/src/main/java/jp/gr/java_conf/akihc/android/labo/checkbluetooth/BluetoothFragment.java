package jp.gr.java_conf.akihc.android.labo.checkbluetooth;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by ts214009 on 2017/03/01.
 */

public class BluetoothFragment extends Fragment {
    public final String TAG = getClass().getSimpleName();
    public static final String ARG_TAP_NUMBER = "TAP_NUMBER";
    private View rootView;
    private ListView listViewBluetoothDevice;
    private MyBluetoothAdapter mAdapter;
    private ArrayList<MyBluetoothDevice> bluetoothDeviceList;
    /**
     * 最後に接続したデバイスのMACアドレス
     */
    public String mLastAddress;

    /**
     * 繋いでいるReaderの情報を入れる
     */
    private MyBluetoothDevice usingDevice;

    /**
     * プリファレンス（最後に接続したデバイス情報を格納）
     */
    public SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        //Bluetoothデバイス発見の通知を受信
        IntentFilter action_found = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, action_found);

        IntentFilter action_name_changed = new IntentFilter(BluetoothDevice.ACTION_NAME_CHANGED);
        getActivity().registerReceiver(mReceiver, action_name_changed);

        //デバイスの切断を受信
        IntentFilter action_disconnected = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        getActivity().registerReceiver(mReceiver, action_disconnected);

        //デバイス探知終了
        IntentFilter action_discover_end = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, action_discover_end);

        mPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        mLastAddress = mPreferences.getString(Constants.KEY_LAST_ADDRESS, null);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //リーダー一覧のビューを取得
        rootView = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        //一覧のListView設定
        listViewBluetoothDevice = (ListView) rootView.findViewById(R.id.listViewBluetoothDevice);
        listViewBluetoothDevice.setAdapter(mAdapter);

        listViewBluetoothDevice.setOnItemClickListener(mDeviceClickListener);
//        listViewBluetoothDevice.setOnItemLongClickListener(mDeviceLongClickListener);
        updateList();
        startDiscovery();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentListener) {
            if (context instanceof MainActivity) {
                MainActivity activity = (MainActivity) context;
                setListener(activity);
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().unregisterReceiver(mReceiver);
    }


    /**
     * インスタンス生成
     *
     * @return インスタンス
     */
    public static BluetoothFragment newInstance() {
        return new BluetoothFragment();
    }


    /**
     * リスト表示更新
     */
    public Runnable updateListTable = new Runnable() {
        public void run() {
            //UIスレッドでリーダー一覧を更新する
            BluetoothFragment.this.mAdapter.notifyDataSetChanged();
        }
    };


    FragmentListener mListener;

    /**
     * Activityリスナーセット
     *
     * @param listener
     */
    public void setListener(FragmentListener listener) {
        mListener = listener;

    }

    private BluetoothAdapter mBluetoothAdapter;

    public void updateList() {
        if (mBluetoothAdapter == null) {
            return;
        }

        Set<BluetoothDevice> mBondedDevices = mBluetoothAdapter.getBondedDevices();
        bluetoothDeviceList = new ArrayList<>();
        for (BluetoothDevice btd : mBondedDevices) {
            addDevice(btd);
        }

        mAdapter = new MyBluetoothAdapter(getActivity(), bluetoothDeviceList);
        listViewBluetoothDevice.setAdapter(mAdapter);
    }

    /**
     * Bluetoothアダプタを設定する
     *
     * @param bluetoothAdapter
     */
    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        mBluetoothAdapter = bluetoothAdapter;
    }

    private ProgressDialog discover_dialog = null;  //リーダー検索処理中ダイアログ

    public void startDiscovery() {
        //TODO: 優先接続を表示
        //TODO: 前回接続を表示
        if (!mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.startDiscovery();
            ContextThemeWrapper themedContext;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                themedContext = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog);
            } else {
                themedContext = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Light);
            }
//            discover_dialog = new ProgressDialog(getActivity());
            discover_dialog = new ProgressDialog(themedContext);
            discover_dialog.setTitle("スキャン中");
            discover_dialog.setMessage("リーダーを探しています...");
            discover_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            discover_dialog.setIcon(R.drawable.ic_bluetooth_connected_black_36dp);
            discover_dialog.setButton(Dialog.BUTTON_NEGATIVE, getResources().getString(R.string.readers_list_button_stop), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
//                        updateFab();
                    }
                }
            });

            discover_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
//                        updateFab();
                    }
                }
            });
            discover_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
//                    updateFab();
                }
            });
            discover_dialog.show();
        }
    }


    /**
     * Bluetooth接続の解除
     *
     * @param address MACアドレス
     */
    public void setDisconnectedReader(String address) {
        for (int index = 0; index < bluetoothDeviceList.size(); index++) {
            MyBluetoothDevice rfidReader = bluetoothDeviceList.get(index);
            rfidReader.setDisconnected();
        }
        getActivity().runOnUiThread(updateListTable);
    }


    /**
     * デバイス情報を追加
     *
     * @param bluetoothDevice BluetoothDevice
     */
    private void addDevice(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice.getName() == null) {
            return;
        }

        String name = bluetoothDevice.getName().toUpperCase();
        int index = 0;

        for (index = 0; index < bluetoothDeviceList.size(); index++) {
            MyBluetoothDevice reader = bluetoothDeviceList.get(index);
            if (reader.getName().equals(bluetoothDevice.getName())) {
                break;
            }
        }

        if (index != bluetoothDeviceList.size()) {
            return;
        }

        //特定のデバイスだけ表示したいとき
//        if (name.startsWith("HOGE")) {
            int iconId = R.drawable.ic_bluetooth_disabled_black_36dp;
            boolean connected = false;

            if (usingDevice != null && usingDevice.getAddress().equals(bluetoothDevice.getAddress())) {
                iconId = R.drawable.ic_bluetooth_connected_black_36dp;
                connected = usingDevice.isConnected();
            }

            bluetoothDeviceList.add(new MyBluetoothDevice(iconId, connected, bluetoothDevice));
//        }
    }


    /**
     * ブロードキャスト受信
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //BluetoothDevice.ACTION_FOUND : ブロードキャストアクション：リモートデバイスが検出されました
                addDevice(device);
                //UIスレッドで一覧を更新する
                getActivity().runOnUiThread(updateListTable);
            } else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                //BluetoothDevice.ACTION_FOUND : ブロードキャストアクション：リモートデバイスのフレンドリ名が最初に取得されたか、最後の取得以降に変更されたことを示します。
                addDevice(device);
                //UIスレッドで一覧を更新する
                getActivity().runOnUiThread(updateListTable);

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //(BluetoothDevice.ACTION_ACL_DISCONNECTED : ブロードキャストアクション：リモートデバイスからの低レベル（ACL）の切断を示します。
                //ACL: Asynchronous Connection-Less
                setDisconnectedReader(device.getAddress());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //BluetoothAdapter.ACTION_DISCOVERY_FINISHED : ブロードキャストアクション：ローカルBluetoothアダプタがデバイス検出プロセスを終了しました。
                Toast.makeText(getActivity(), getResources().getString(R.string.readers_list_msg_discover), Toast.LENGTH_SHORT).show();
                //リーダー探索ダイアログが表示中だったらキャンセルする
                if (discover_dialog != null) {
                    discover_dialog.cancel();
                    discover_dialog = null;
                }
            }
        }
    };


    /**
     * 接続/切断デバイスの選択
     */
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //TODO: 接続切断
        }
    };
}
