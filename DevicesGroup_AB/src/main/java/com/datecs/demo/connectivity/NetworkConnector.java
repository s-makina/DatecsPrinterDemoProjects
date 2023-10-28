/*
 * @author Datecs Ltd. Software Department
 */

package com.datecs.demo.connectivity;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class NetworkConnector extends AbstractConnector implements Parcelable {

    private String mHost;
    private int mPort;
    private Socket mSocket;

    public NetworkConnector(Context context, String host, int port) {
        mHost = host;
        mPort = port;
        mSocket = new Socket();
    }


    protected NetworkConnector(Parcel in) {
        mHost = in.readString();
        mPort = in.readInt();
    }

    public static final Creator<NetworkConnector> CREATOR = new Creator<NetworkConnector>() {
        @Override
        public NetworkConnector createFromParcel(Parcel in) {
            return new NetworkConnector(in);
        }

        @Override
        public NetworkConnector[] newArray(int size) {
            return new NetworkConnector[size];
        }
    };

    public int getPort() {
        return mPort;
    }

    @Override
    public void connect() throws IOException {
        SocketAddress address = new InetSocketAddress(mHost, mPort);
        mSocket.connect(address);
        mSocket.setSoTimeout(0);
        mSocket.setTcpNoDelay(true);

    }

    @Override
    public void close() throws IOException {
        mSocket.shutdownInput();
        mSocket.shutdownOutput();
        mSocket.close();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getAddress() {
        return mHost;
    }

    @Override
    public NetworkConnector getDevice() {
        return this;
    }

    @Override
    public int getRSSI() {
        return 0;
    }

    @Override
    public boolean isBonded() {
        return false;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return mSocket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return mSocket.getOutputStream();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof NetworkConnector) {
            NetworkConnector connector = (NetworkConnector)o;
            return mHost.equals(connector.mHost) && mPort == connector.mPort;
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mHost);
        dest.writeInt(mPort);
    }
}
