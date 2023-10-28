/*
 * @author Datecs Ltd. Software Department
 */

package com.datecs.demo.connectivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.datecs.testApp.R;

import java.util.List;

public class ConnectorAdapter extends RecyclerView.Adapter<ConnectorAdapter.ViewHolderItem> {

    public interface OnItemClickListener {
        void onItemClick(View view, AbstractConnector item);
    }

    public interface OnLongClickListener {
        void onItemLongClick(View view, AbstractConnector item, int position);
    }

    private final List<AbstractConnector> mItems;
    private final OnItemClickListener mListener;
    private final OnLongClickListener mLongClickListener;

    public ConnectorAdapter(List<AbstractConnector> items, OnItemClickListener s, OnLongClickListener mLongClickListener) {
        this.mItems = items;
        this.mListener = s;
        this.mLongClickListener = mLongClickListener;
    }

    @Override
    public ViewHolderItem onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.connector_list_item, parent, false);
        return new ViewHolderItem(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(ViewHolderItem holder, final int position) {
        final AbstractConnector item = getItem(position);

        if (item instanceof NetworkConnector) {
            NetworkConnector connector = (NetworkConnector) item;
            holder.icon.setImageResource(R.drawable.ic_network);
                holder.name.setText(connector.getAddress()  + ":" + connector.getPort() );

        } else if (item instanceof UsbDeviceConnector) {
            UsbDeviceConnector connector = (UsbDeviceConnector) item;
            holder.icon.setImageResource(R.drawable.ic_usb);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.name.setText(connector.getName());
            } else {
                holder.name.setText("USB DEVICE");
            }
            holder.adr.setText(connector.getAddress());

        } else if (item instanceof BluetoothSppConnector) {
            holder.icon.setImageResource(R.drawable.ic_bluetooth);
            BluetoothSppConnector connector = (BluetoothSppConnector) item;
            String name = connector.getName();

            if (name == null || name.isEmpty()) {
                holder.name.setText("BLUETOOTH DEVICE");
            } else {
                holder.name.setText(name);
            }
            holder.adr.setText(connector.getAddress());

        } else if (item instanceof BluetoothLeConnector) {
            BluetoothLeConnector connector = (BluetoothLeConnector) item;
            holder.icon.setImageResource(R.drawable.ic_bt_le);
            String name = connector.getName();

            if (name == null || name.isEmpty()) {
                holder.name.setText("BLUETOOTH DEVICE");
            } else {
                holder.name.setText(name);
            }

            holder.adr.setText(connector.getAddress());

        } else {
            throw new IllegalArgumentException("Invalid connector");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(v, item);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mLongClickListener.onItemLongClick(v, item, position);
                return false;
            }

        });


    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private AbstractConnector getItem(int position) {
        return mItems.get(position);
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public void add(AbstractConnector connector) {
        add(mItems.size(), connector);
    }

    public void add(int position, AbstractConnector connector) {
        mItems.add(position, connector);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolderItem extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView name;
        private TextView adr;


        private ViewHolderItem(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            adr = itemView.findViewById(R.id.address);
        }
    }
}