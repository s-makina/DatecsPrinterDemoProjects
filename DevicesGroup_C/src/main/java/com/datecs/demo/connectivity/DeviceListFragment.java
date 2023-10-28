package com.datecs.demo.connectivity;

import static com.datecs.demo.MainActivity.DATECS_USB_VID;
import static com.datecs.demo.MainActivity.FTDI_USB_VID;
import static com.datecs.demo.MainActivity.PREF_HOST_LIST;
import static com.datecs.demo.MainActivity.app_preferences;
import static com.datecs.demo.MainActivity.log;
import static com.datecs.demo.MainActivity.mConnectorAdapter;
import static com.datecs.demo.MainActivity.mConnectorList;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datecs.testApp.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class DeviceListFragment extends Fragment {
    private static final String ARG_SCAN_BLE_MODE = "ARG_SCAN_BLE_MODE";
    private RecyclerView mRecyclerView;



    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private PendingIntent mPermissionIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView = recyclerView;
        mRecyclerView.setAdapter(mConnectorAdapter);
        initUI();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Context context = getContext();
        assert context != null;
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

    }

    @Override
    public void onStop() {
        super.onStop();
        Context context = getContext();
        assert context != null;

    }


    private void initUI() {
        mConnectorAdapter.clear();
        // Enumerate all network devices.
        Set<String> hostList = app_preferences.getStringSet(PREF_HOST_LIST, new HashSet<String>());

        for (String url : hostList) {
            int delimiter = url.indexOf(":");
            String host = url.substring(0, delimiter > 0 ? delimiter : url.length());
            int port = Integer.parseInt(url.substring(delimiter > 0 ? delimiter + 1 : 0));
            AbstractConnector connector = new NetworkConnector(getContext(), host, port);
            mConnectorAdapter.add(connector);
        }

        // Enumerate USB devices
        UsbManager manager = (UsbManager) getContext().getSystemService(Context.USB_SERVICE);
        if (manager != null) {
            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();

                if ((device.getVendorId() == DATECS_USB_VID) || (device.getVendorId() == FTDI_USB_VID)) {
                    manager.requestPermission(device, mPermissionIntent);
                    AbstractConnector connector = new UsbDeviceConnector(getContext(),device);
                    mConnectorList.add(connector);
                }
            }
        }

        // Enumerate Bluetooth devices
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {

            Set<BluetoothDevice> boundedDevices = adapter.getBondedDevices();

            for (BluetoothDevice device : boundedDevices) {
                AbstractConnector connector;
                if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                    connector = new BluetoothLeConnector(getContext(), device);
                } else {
                    connector = new BluetoothSppConnector(device );
                }

                mConnectorAdapter.add(connector);
            }
        }
        mConnectorAdapter.notifyDataSetChanged();

    }


}