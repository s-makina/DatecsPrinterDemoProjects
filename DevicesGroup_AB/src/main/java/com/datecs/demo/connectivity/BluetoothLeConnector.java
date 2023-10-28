package com.datecs.demo.connectivity;

import static com.datecs.testApp.BuildConfig.DEBUG;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;


@SuppressLint("MissingPermission")
public class BluetoothLeConnector extends AbstractConnector {

    private static final String TAG = "BluetoothConnector";
    private final static String UUID_SERVICE = "d839fc3c-84dd-4c36-9126-";

    private final static String UUID_POWER_CHARACTERISTIC = "22ffc547-1bef-48e2-aa87-b87e23ac0bbd";

    private final static String UUID_WAKE_CHARACTERISTIC = "f953144b-e33a-4079-b202-e3d7c1f3dbb0";

    private final static String UUID_READ_CHARACTERISTIC = "1f6b14c9-97fa-4f1e-aaa6-7e152fdd04f4";

    private final static String UUID_WRITE_CHARACTERISTIC = "b378db85-4ec3-4daa-828e-1b99607bd6a0";

    private static final int CONNECTION_STATE_DISCONNECTED = 0;

    private static final int CONNECTION_STATE_CONNECTED = 1;

    private static final int CONNECTION_STATE_READ_CHARACTERISTIC_ENABLED = 2;

    private static final int CONNECTION_STATE_POWER_CHARACTERISTIC_ENABLED = 3;

    private static final int CONNECTION_STATE_POWER_CHARACTERISTIC_RETRIEVED = 4;

    private static final int CONNECTION_STATE_WAKE_UP_REQUEST = 5;

    private static final int CONNECTION_STATE_READY = 6;

    private final Context mContext;

    private final Handler mHandler;

    private final BluetoothDevice mDevice;

    private BluetoothGatt mGatt;

    private BluetoothGattCharacteristic mPowerCharacteristic;

    private BluetoothGattCharacteristic mWakeCharacteristic;

    private BluetoothGattCharacteristic mReadCharacteristic;

    private BluetoothGattCharacteristic mWriteCharacteristic;

    private InputStreamImpl mInputStream;

    private OutputStreamImpl mOutputStream;

    private ConnectorReceiver mReceiver = new ConnectorReceiver();

    private int mConnectionState = CONNECTION_STATE_DISCONNECTED;

    private boolean mWakeUpRequired;

    private final BluetoothGattCallbackImpl mCallback = new BluetoothGattCallbackImpl();

    private final Runnable mPostReady = () -> {
        createStreams();
        notifyDeviceReady();
    };

    private final Runnable mServiceDiscoveryFailed = () -> {
        close();
        connect();
    };
    private final int mRSSI;


    public BluetoothLeConnector(Context context, BluetoothDevice device, int rssi) {
        mContext = context;
        mHandler = new Handler(context.getMainLooper());
        mDevice = device;
        mRSSI = rssi;

    }

    public BluetoothLeConnector(Context ctx, BluetoothDevice device) {
        mContext = ctx;
        mHandler = new Handler(ctx.getMainLooper());
        mDevice = device;
        mRSSI = 0;
    }


    private void enableCharacteristicNotification(final BluetoothGattCharacteristic characteristic) {
        if (DEBUG)
            Log.d(TAG, "enableCharacteristicNotification() - characteristic=" + characteristic.getUuid());

        boolean status = mGatt.setCharacteristicNotification(characteristic, true);
        if (!status) {
            if (DEBUG)
                Log.e(TAG, "enableCharacteristicNotification() - mGatt.setCharacteristicNotification() return false");
            return;
        }

        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
        if (!descriptors.isEmpty()) {
            BluetoothGattDescriptor descriptor = descriptors.get(0);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            status = mGatt.writeDescriptor(descriptor);
            if (!status) {
                if (DEBUG)
                    Log.e(TAG, "enableCharacteristicNotification() - mGatt.writeDescriptor() return false");
            }
        }
    }

    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (DEBUG) Log.d(TAG, "readCharacteristic() - characteristic=" + characteristic.getUuid());

        boolean status = mGatt.readCharacteristic(characteristic);
        if (!status) {
            if (DEBUG) Log.e(TAG, "readCharacteristic() - mGatt.readCharacteristic() return false");
        }
    }

    private void wakeUp(String name) {
        if (DEBUG) Log.d(TAG, "wakeUp() - name=" + name);

        boolean status = mWakeCharacteristic.setValue(name);
        if (!status) {
            if (DEBUG) Log.e(TAG, "wakeUp() - mWakeCharacteristic.setValue() return false");
            return;
        }

        status = mGatt.writeCharacteristic(mWakeCharacteristic);
        if (!status) {
            if (DEBUG) Log.e(TAG, "wakeUp() - mGatt.writeCharacteristic() return false");
        }
    }

    private void discoverServices() {
        Log.d(TAG, "discoverServices()");
        boolean status = mGatt.discoverServices();
        if (!status) {
            if (DEBUG) Log.e(TAG, "discoverServices() - mGatt.discoverServices() return false");
        }
    }

    private void notifyConnected() {
        if (DEBUG) Log.d(TAG, "notifyConnected()");
        mHandler.post(() -> mReceiver.onConnect());
        //  LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(BLE_MSG).putExtra(BLE_STATE, (int) 1));
    }

    private void notifyDisconnected() {
        if (DEBUG) Log.d(TAG, "notifyDisconnected()");
        mHandler.post(() -> mReceiver.onDisconnect());
        // LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(BLE_MSG).putExtra(BLE_STATE, (int) 3));
    }

    private void notifyDeviceReady() {
        if (DEBUG) Log.d(TAG, "notifyDeviceReady()");
        mHandler.post(() -> mReceiver.onDeviceReady(mInputStream, mOutputStream));
        //LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(BLE_MSG).putExtra(BLE_STATE, (int) 2));
    }

    private void createStreams() {
        if (DEBUG) Log.d(TAG, "createStreams()");
        mInputStream = new InputStreamImpl();
        mOutputStream = new OutputStreamImpl(mGatt, mWriteCharacteristic);
    }

    private void closeStreamsAndRelease() {
        if (DEBUG) Log.d(TAG, "closeStreams()");

        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
        } catch (Exception ignored) {
            //e.printStackTrace();
        }

        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }
        } catch (Exception ignored) {
            //e.printStackTrace();
        }

        mPowerCharacteristic = null;

        mWakeCharacteristic = null;

        mReadCharacteristic = null;

        mWriteCharacteristic = null;

        mConnectionState = CONNECTION_STATE_DISCONNECTED;

        mWakeUpRequired = false;

        mHandler.removeCallbacks(mPostReady);

        mHandler.removeCallbacks(mServiceDiscoveryFailed);
    }


    public void connect() {
        if (DEBUG) Log.d(TAG, "connect()");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mGatt = mDevice.connectGatt(mContext, true, mCallback, BluetoothDevice.TRANSPORT_LE,
                    BluetoothDevice.PHY_LE_1M_MASK, mHandler);
        } else {
            mGatt = mDevice.connectGatt(mContext, true, mCallback);
        }
    }


    public void close() {
        if (DEBUG) Log.d(TAG, "close()");
        closeStreamsAndRelease();
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }
    }

    public InputStreamImpl getInputStream() {
        return mInputStream;
    }

    public OutputStreamImpl getOutputStream() {
        return mOutputStream;
    }

    public void setReceiver(ConnectorReceiver receiver) {
        mReceiver = receiver;
    }

    public String getName() {
        return mDevice.getName();
    }

    public String getAddress() {
        return mDevice.getAddress();
    }

    @Override
    public Parcelable getDevice() {
        return mDevice;
    }

    @Override
    public int getRSSI() {
        return 100+mRSSI;
    }

    @Override
    public boolean isBonded() {
        return mDevice.getBondState() == BluetoothDevice.BOND_BONDED;
    }

    @SuppressLint("MissingPermission")
    private class BluetoothGattCallbackImpl extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (DEBUG)
                Log.d(TAG, "onConnectionStateChange() - status=" + status + ", newState=" + newState);

            if (newState == 2) {
                mConnectionState = CONNECTION_STATE_CONNECTED;
                notifyConnected();
                discoverServices();
                mHandler.postDelayed(mServiceDiscoveryFailed, 2000);
            } else {
                if (mConnectionState > CONNECTION_STATE_DISCONNECTED) {
                    notifyDisconnected();
                }
                closeStreamsAndRelease();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (DEBUG) Log.d(TAG, "onServicesDiscovered() - status=" + status);

            mHandler.removeCallbacks(mServiceDiscoveryFailed);

            for (BluetoothGattService service : gatt.getServices()) {
                String serviceUuid = service.getUuid().toString();

                if (DEBUG) Log.d(TAG, "onServicesDiscovered() - " + serviceUuid);

                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (DEBUG) Log.d(TAG, "onServicesDiscovered() -   " + characteristicUuid);

                    if (serviceUuid.startsWith(UUID_SERVICE)) {
                        if (characteristicUuid.equals(UUID_POWER_CHARACTERISTIC)) {
                            mPowerCharacteristic = characteristic;
                        }
                        if (characteristicUuid.equals(UUID_WAKE_CHARACTERISTIC)) {
                            mWakeCharacteristic = characteristic;
                        }
                        if (characteristicUuid.equals(UUID_READ_CHARACTERISTIC)) {
                            mReadCharacteristic = characteristic;
                        }
                        if (characteristicUuid.equals(UUID_WRITE_CHARACTERISTIC)) {
                            mWriteCharacteristic = characteristic;
                        }
                    }
                }
            }

            if (mReadCharacteristic != null) {
                if (DEBUG)
                    Log.d(TAG, "onServicesDiscovered() - enable READ characteristic notification");
                enableCharacteristicNotification(mReadCharacteristic);
            } else {
                if (DEBUG) Log.e(TAG, "onServicesDiscovered() - missing READ characteristic");
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (DEBUG)
                Log.d(TAG, "onDescriptorWrite() - descriptor=" + descriptor.getUuid().toString() + ", status=" + status);

            if (status != 0) {
                if (DEBUG) Log.e(TAG, "onDescriptorWrite() - error status " + status);
                return;
            }

            if (mConnectionState == CONNECTION_STATE_CONNECTED) {
                if (DEBUG)
                    Log.d(TAG, "onDescriptorWrite() - READ characteristic notification is enabled");
                mConnectionState = CONNECTION_STATE_READ_CHARACTERISTIC_ENABLED;

                if (mPowerCharacteristic != null) {
                    if (DEBUG)
                        Log.d(TAG, "onDescriptorWrite() - enable POWER characteristic notification");
                    enableCharacteristicNotification(mPowerCharacteristic);
                } else {
                    if (DEBUG) Log.e(TAG, "onDescriptorWrite() - missing POWER characteristic");
                }
            } else if (mConnectionState == CONNECTION_STATE_READ_CHARACTERISTIC_ENABLED) {
                if (DEBUG)
                    Log.d(TAG, "onDescriptorWrite() - POWER characteristic notification is enabled");
                mConnectionState = CONNECTION_STATE_POWER_CHARACTERISTIC_ENABLED;

                if (DEBUG) Log.d(TAG, "onDescriptorWrite() - read POWER characteristic");
                readCharacteristic(mPowerCharacteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (DEBUG)
                Log.d(TAG, "onCharacteristicChanged() - characteristic=" + characteristic.getUuid().toString());

            if (characteristic.equals(mReadCharacteristic)) {
                byte[] value = characteristic.getValue();

                if (DEBUG)
                    Log.d(TAG, "onCharacteristicChanged() - received " + (value != null ? value.length : 0) + " bytes");
                if (mInputStream != null) {
                    mInputStream.appendData(value);
                } else {
                    if (DEBUG) Log.d(TAG, "onCharacteristicChanged() - unexpected input stream");
                }
            } else if (characteristic.equals(mPowerCharacteristic)) {
                int powerStatus = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                if (powerStatus == 0x31) {
                    if (DEBUG) Log.d(TAG, "onCharacteristicChanged() - pinpad is ON");

                    if (mConnectionState == CONNECTION_STATE_POWER_CHARACTERISTIC_RETRIEVED) {
                        if (mWakeUpRequired) {
                            String name = mGatt.getDevice().getName();
                            if (DEBUG)
                                Log.d(TAG, "onCharacteristicChanged() - wake up device " + name);
                            wakeUp(name);
                            mConnectionState = CONNECTION_STATE_WAKE_UP_REQUEST;
                            mWakeUpRequired = false;
                        } else {
                            mConnectionState = CONNECTION_STATE_READY;
                            mHandler.postDelayed(mPostReady, 200);
                        }
                    } else {
                        if (DEBUG)
                            Log.d(TAG, "onCharacteristicChanged() - unexpected connection state " + mConnectionState);
                    }
                } else if (powerStatus == 0x30) {
                    if (DEBUG) Log.d(TAG, "onCharacteristicChanged() - pinpad is OFF");
                    if (DEBUG) Log.d(TAG, "onCharacteristicChanged() - wake up is required");
                    mWakeUpRequired = true;
                } else {
                    if (DEBUG)
                        Log.e(TAG, "onCharacteristicChanged() - unknown pinpad status " + powerStatus);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (DEBUG)
                Log.d(TAG, "onCharacteristicRead() - characteristic=" + characteristic.getUuid() + ", status=" + status);

            if (status != 0) {
                if (DEBUG) Log.e(TAG, "onCharacteristicRead() - error status " + status);
                return;
            }

            if (characteristic.equals(mPowerCharacteristic)) {
                if (DEBUG) Log.d(TAG, "onCharacteristicRead() - POWER characteristic is retrieved");
                mConnectionState = CONNECTION_STATE_POWER_CHARACTERISTIC_RETRIEVED;

                int powerStatus = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                if (powerStatus == 0x31) {
                    if (DEBUG) Log.d(TAG, "onCharacteristicRead() - pinpad is ON");
                    mConnectionState = CONNECTION_STATE_READY;
                    mHandler.postDelayed(mPostReady, 200);
                } else if (powerStatus == 0x30) {
                    if (DEBUG) Log.d(TAG, "onCharacteristicRead() - pinpad is OFF");
                    if (DEBUG) Log.d(TAG, "onCharacteristicRead() - wake up is required");
                    mWakeUpRequired = true;
                } else {
                    if (DEBUG)
                        Log.e(TAG, "onCharacteristicRead() - unknown pinpad status " + powerStatus);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (DEBUG)
                Log.d(TAG, "onCharacteristicWrite() - characteristic=" + characteristic.getUuid().toString() + ", status=" + status);

            if (status != 0) {
                if (DEBUG) Log.e(TAG, "onCharacteristicWrite() - error status " + status);
                return;
            }

            if (characteristic.equals(mWakeCharacteristic)) {
                if (mConnectionState == CONNECTION_STATE_WAKE_UP_REQUEST) {
                    if (DEBUG) Log.d(TAG, "onCharacteristicWrite() - device is waked up");
                    mConnectionState = CONNECTION_STATE_READY;
                    mHandler.postDelayed(mPostReady, 2200);
                } else {
                    if (DEBUG)
                        Log.d(TAG, "onCharacteristicWrite() - unexpected connection state " + mConnectionState);
                }
            } else if (characteristic.equals(mWriteCharacteristic)) {
                if (mConnectionState == CONNECTION_STATE_READY) {
                    if (mOutputStream != null) {
                        mOutputStream.confirmTransmit();
                    }
                } else {
                    if (DEBUG)
                        Log.d(TAG, "onCharacteristicWrite() - unexpected connection state " + mConnectionState);
                }
            }
        }
    }


    private static class InputStreamImpl extends InputStream {

        private final LinkedList<Byte> mInputBuffer = new LinkedList<>();

        private volatile IOException mException = null;

        @Override
        public int read() throws IOException {

            synchronized (mInputBuffer) {
                while (true) {
                    if (mException != null) {
                        throw mException;
                    }

                    if (!mInputBuffer.isEmpty()) {
                        return mInputBuffer.remove() & 0xff;
                    } else {
                        try {
                            mInputBuffer.wait(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {

            synchronized (mInputBuffer) {
                while (true) {
                    if (mException != null) {
                        throw mException;
                    }

                    if (!mInputBuffer.isEmpty()) {
                        int n;
                        for (n = 0; n < mInputBuffer.size() && n < len; n++) {
                            b[n + off] = mInputBuffer.remove();
                        }
                        return n;
                    } else {
                        try {
                            mInputBuffer.wait(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        @Override
        public void close() throws IOException {
            mException = new IOException("The stream is closed");
        }

        @Override
        public int available() throws IOException {
            synchronized (mInputBuffer) {
                return mInputBuffer.size();
            }
        }

        public void appendData(byte[] data) {
            if (data == null || data.length == 0) {
                return;
            }

            synchronized (mInputBuffer) {
                for (byte b : data) {
                    mInputBuffer.add(b);
                }
            }
        }
    }

    private static class OutputStreamImpl extends OutputStream {

        private static final int MTU = 19;

        private final BluetoothGatt mGatt;

        private final BluetoothGattCharacteristic mGattCharacteristic;

        private final LinkedList<Byte> mOutputBuffer = new LinkedList<>();

        private final boolean[] mSyncRoot = new boolean[1];

        private volatile IOException mException = null;

        private int mWriteCharacteristicCounter = 0;

        OutputStreamImpl(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            mGatt = gatt;
            mGattCharacteristic = characteristic;
        }

        @Override
        public void write(int b) throws IOException {

            if (mException != null) {
                throw mException;
            }
            synchronized (mOutputBuffer) {
                mOutputBuffer.add((byte) b);
            }
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {

            if (mException != null) {
                throw mException;
            }

            synchronized (mOutputBuffer) {
                for (int i = 0; i < len; i++) {
                    mOutputBuffer.add(b[off + i]);
                }
            }
        }

        @Override
        public void flush() throws IOException {

            if (mException != null) {
                throw mException;
            }

            synchronized (mOutputBuffer) {
                int size = mOutputBuffer.size();
                if (size > 0) {
                    byte[] buffer = new byte[size];
                    for (int i = 0; i < size; i++) {
                        buffer[i] = mOutputBuffer.remove();
                    }
                    transmitAll(buffer);
                }
            }
        }

        @Override
        public void close() throws IOException {
            mException = new IOException("The stream is closed");
            synchronized (mSyncRoot) {
                mSyncRoot.notifyAll();
            }
        }

        private void transmitAll(byte[] data) throws IOException {
            int n = 0;
            while (n < data.length) {
                if (mException != null) {
                    throw mException;
                }
                byte[] value = new byte[Math.min(MTU, data.length - n)];
                System.arraycopy(data, n, value, 0, value.length);
                transmitAndWait(value);
                n += value.length;
            }
        }

        private void transmitAndWait(byte[] data) throws IOException {
            if (DEBUG) Log.d(TAG, "transmitAndWait() - data.length=" + data.length);

            boolean status;

            if (mGattCharacteristic == null) {
                throw new IOException("Characteristic is null");
            }

            status = mGattCharacteristic.setValue(data);
            if (!status) {
                Log.e(TAG, "transmitAndWait() - setValue() return false");
                throw new IOException("setValue() return false");
            }

            if ((mWriteCharacteristicCounter % 20) == 0) {
                mGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            } else {
                mGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            }

            synchronized (mSyncRoot) {
                mSyncRoot[0] = false;

                status = mGatt.writeCharacteristic(mGattCharacteristic);
                if (!status) {
                    Log.e(TAG, "transmitAndWait() - writeCharacteristic() return false");
                    throw new IOException("writeCharacteristic() return false");
                }

                mWriteCharacteristicCounter++;

                try {
                    mSyncRoot.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mException != null) {
                    throw mException;
                }

                if (!mSyncRoot[0]) {
                    throw new IOException("write error");
                }
            }
        }

        public void confirmTransmit() {
            Log.d(TAG, "confirmTransmit()");

            synchronized (mSyncRoot) {
                mSyncRoot[0] = true;
                mSyncRoot.notify();
            }
        }
    }

    public static class ConnectorReceiver {

        public void onConnect() {
        }

        public void onDisconnect() {
        }

        public void onDeviceReady(InputStream in, OutputStream out) {
        }
    }

}
