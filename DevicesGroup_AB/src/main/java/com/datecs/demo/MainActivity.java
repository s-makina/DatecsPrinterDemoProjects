/*
 * @author Datecs Ltd. Software Department
 */

package com.datecs.demo;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.datecs.demo.connectivity.AbstractConnector;
import com.datecs.demo.connectivity.ConnectorAdapter;
import com.datecs.demo.connectivity.DeviceListFragment;
import com.datecs.demo.connectivity.NetworkConnector;
import com.datecs.demo.ui.main.MenuFragment;

import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.testApp.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    public static final String PREF_HOST_LIST = "hosts";

    public static SharedPreferences app_preferences;
    public static ConnectorAdapter mConnectorAdapter;
    //The BroadcastReceiver that listens for bluetooth broadcasts
    public static ArrayList<AbstractConnector> mConnectorList;

    public static final int DATECS_USB_VID = 65520;
    public static final int FTDI_USB_VID = 1027;
    private static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private static boolean isBTReconnectRequested = false;
    private BluetoothLostReceiver bluetoothLostReceiver;
    private TextView mTitle;
    public static DatecsFiscalDevice myFiscalDevice = null;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    //When you disconnect the USB device, connector activity is started
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if ((device.getVendorId() == DATECS_USB_VID) ||
                            (device.getVendorId() == FTDI_USB_VID)) showDeviceListFragment();
                }
            }
        }
    };
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        ctx = this;
        IntentFilter filter = new IntentFilter(ACTION_USB_DETACHED);
        registerReceiver(mUsbReceiver, filter);
        setContentView(R.layout.main_activity);
        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mConnectorList = new ArrayList<>();
        mConnectorAdapter = new ConnectorAdapter(mConnectorList, new ConnectorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, AbstractConnector item) {

                final Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            try {
                                item.connect();
                            } catch (Exception e) {
                                 postToast(e.getMessage());
                                 e.printStackTrace();
                                return;
                            }

                            try {
                                PrinterManager.instance.init(item);
                            } catch (Exception e) {
                                try {
                                    item.close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                                postToast(e.getMessage());
                                e.printStackTrace();
                                return;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    myFiscalDevice = PrinterManager.instance.getFiscalDevice();
                                    String s1 = PrinterManager.instance.getModelVendorName();
                                    getSupportActionBar().setTitle(getTitle() + "    " + s1);
                                    isBTReconnectRequested = false;
                                    if (myFiscalDevice != null) ShowMenu();
                                    else finish();
                                }
                            });
                        } finally {

                        }
                    }
                });
                thread.start();

            }


        }, new ConnectorAdapter.OnLongClickListener() {
            @Override
            public void onItemLongClick(View view, AbstractConnector item, int position) {
                // Removed network device from list
                if (item instanceof NetworkConnector) {
                    NetworkConnector networkConnector = (NetworkConnector) item;
                    Set<String> hostList = app_preferences.getStringSet(PREF_HOST_LIST, new HashSet<String>());
                    String url = networkConnector.getAddress() + ":" + networkConnector.getPort();
                    if (hostList.remove(url)) {
                        app_preferences.edit().clear().commit();
                        app_preferences.edit().putStringSet(PREF_HOST_LIST, hostList).commit();
                    }
                }
                mConnectorAdapter.remove(position);
            }
        });

        if (bluetoothLostReceiver == null) {
            bluetoothLostReceiver = new BluetoothLostReceiver();
            bluetoothLostReceiver.setMainActivity(this);
            IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED");
            filter2.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            registerReceiver(bluetoothLostReceiver, filter2);
        }

        if (savedInstanceState == null) requestBTPermissions();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUsbReceiver != null) unregisterReceiver(mUsbReceiver);
        if (bluetoothLostReceiver != null) unregisterReceiver(bluetoothLostReceiver);
    }

    private void requestBTPermissions() {

        String[] permissions; // = info.requestedPermissions;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions = new String[]{
                    BLUETOOTH_CONNECT,
                    BLUETOOTH_SCAN,
            };
        } else {
            permissions = new String[]{BLUETOOTH_CONNECT, BLUETOOTH_SCAN, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};
        }

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }
        }
        showDeviceListFragment();
    }

    private void showDeviceListFragment() {
        DeviceListFragment fragment = new DeviceListFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment, null)
                .commit();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int index = 0;
        Map<String, Integer> PermissionsMap = new HashMap<String, Integer>();
        for (String permission : permissions) {
            PermissionsMap.put(permission, grantResults[index]);
            index++;
        }

        switch (permissions[0]) {
            case BLUETOOTH_CONNECT:
            case ACCESS_FINE_LOCATION:
            case BLUETOOTH_SCAN:
            case ACCESS_COARSE_LOCATION:
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
                    if (PermissionsMap.get(BLUETOOTH_SCAN) != 0) {
                        Toast.makeText(this, "Permissions must for bluetooth connection !", Toast.LENGTH_LONG).show();
                        finish();
                    } else showDeviceListFragment();
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S)
                    if (PermissionsMap.get(ACCESS_FINE_LOCATION) != 0) {
                        Toast.makeText(this, "Permissions must for bluetooth connection !", Toast.LENGTH_LONG).show();
                        finish();
                    } else showDeviceListFragment();

                break;

        }

    }


    private void ShowMenu() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, new MenuFragment(), "menu_frg");
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
//            finish();
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                showDeviceListFragment();
            } else if (getSupportFragmentManager().getBackStackEntryCount() == 0) showExitDialog();
            else super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExitDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.msg_q_exit);
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.setNegativeButton(R.string.no, null);
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!BackStackFragment.handleBackPressed(getSupportFragmentManager())) {
            showExitDialog();
        }
    }


    public abstract static class BackStackFragment extends Fragment {
        public static boolean handleBackPressed(FragmentManager fm) {
            if (fm.getFragments() != null) {
                for (Fragment frag : fm.getFragments()) {
                    if (frag != null && frag.isVisible() && frag instanceof BackStackFragment) {
                        if (((BackStackFragment) frag).onBackPressed()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        protected boolean onBackPressed() {
            FragmentManager fm = getChildFragmentManager();
            if (handleBackPressed(fm)) {
                return true;
            } else if (getUserVisibleHint() && fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
                return true;
            }
            return false;
        }
    }

    public static void closeMyFiscalDevice() {
        if (myFiscalDevice != null) {
            myFiscalDevice.close(); //If reconnect with different device model, close all.
            myFiscalDevice = null;
        }
    }

    //If bluetooth connection is lost, connection activity is started.
    public class BluetoothLostReceiver extends BroadcastReceiver {
        MainActivity main = null;

        public void setMainActivity(MainActivity main) {
            this.main = main;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction()) && !isBTReconnectRequested) {
                main.showDeviceListFragment();
                isBTReconnectRequested = true;
            }
        }
    }
    private void postToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ctx, text, Toast.LENGTH_LONG).show();
            }
        });
    }
}
