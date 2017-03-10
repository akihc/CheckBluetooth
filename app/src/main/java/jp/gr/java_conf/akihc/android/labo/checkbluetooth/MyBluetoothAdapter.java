package jp.gr.java_conf.akihc.android.labo.checkbluetooth;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Bluetoothデバイス一覧用Adapter
 */
public class MyBluetoothAdapter extends ArrayAdapter<MyBluetoothDevice> {
    private final Context mContext;
    private final ArrayList<MyBluetoothDevice> bluetoothDeviceList;

    public MyBluetoothAdapter(Context context, ArrayList<MyBluetoothDevice> deviceList) {
        super(context, R.layout.list_item_bluetooth, deviceList);
        this.mContext = context;
        this.bluetoothDeviceList = deviceList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vHolder = null;
        if (convertView == null) {
            vHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_bluetooth, parent, false);
            vHolder.relativeLayoutDevice = (RelativeLayout) convertView.findViewById(R.id.relativeLayoutDevice);
            vHolder.imageViewStatus = (ImageView) convertView.findViewById(R.id.imageViewStatus);
            vHolder.textViewName = (TextView) convertView.findViewById(R.id.textViewName);
            vHolder.textViewAddress = (TextView) convertView.findViewById(R.id.textViewAddress);
            vHolder.imageViewDevice = (ImageView) convertView.findViewById(R.id.imageViewDevice);
            convertView.setTag(vHolder);
        } else {
            vHolder = (ViewHolder) convertView.getTag();
        }


        vHolder.imageViewStatus.setImageResource(bluetoothDeviceList.get(position).getIconId());
        vHolder.textViewName.setText(bluetoothDeviceList.get(position).getName());
        vHolder.textViewAddress.setText(bluetoothDeviceList.get(position).getAddress());
        if (bluetoothDeviceList.get(position).getIconId() == R.drawable.ic_bluetooth_connected_black_36dp) {
            vHolder.relativeLayoutDevice.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPlacidBlue));
        } else {
            vHolder.relativeLayoutDevice.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorTransparent));
        }

        return convertView;
    }

    static class ViewHolder {
        RelativeLayout relativeLayoutDevice;
        ImageView imageViewStatus;
        TextView textViewName;
        TextView textViewAddress;
        ImageView imageViewDevice;
    }
}
