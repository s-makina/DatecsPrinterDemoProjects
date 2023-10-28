/**
 * Shows the current device status in the graphical interface.
 * <p>
 * After each command is completed, the status of the connected fiscal drive is completed (changed or not).
 * Marking of stats in this fragment determines whether a status is active or not.
 * Some of the device statuses are informative, others are related to the current commands execution.
 * Whether a given status will be defined as an "error exeption" that blocking execution of the user application
 * determined by flags at initial initialization.
 *
 * @author Datecs Ltd. Software Department
 * @see PrinterManger.java - initCriticalStatus();
 */
package com.datecs.demo.ui.main;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import com.datecs.demo.ListDeviceStatusFrgBinding;
import com.datecs.demo.MainActivity;
import com.datecs.fiscalprinter.SDK.FiscalDeviceV2;
import com.datecs.testApp.R;
import java.io.Serializable;
import java.util.ArrayList;


public class DeviceStatusFragment extends Fragment {
    private ListDeviceStatusFrgBinding binder;
    private DeviceStatusListAdapter adapter;
    private ArrayList<DeviceStatusListModel> items;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        items = new ArrayList<>();
        binder = DataBindingUtil.inflate(inflater, R.layout.device_status_fragment, container, false);

        String[] deviceStatusDescr_Byte = new String[8];
        boolean[] deviceStatusFlags_Byte = new boolean[8];
        boolean[] isCritical=new boolean[8];
        //       byte[] statusOfDevice;

        try {
            //statusOfDevice=MainActivity.myFiscalDevice.GetStatus(); //Get the device status with command 74.
            for (int by = 0; by < 8; by++) {
                for (int bit = 0; bit < 8; bit++) {
                    if (MainActivity.myFiscalDevice.isConnectedDeviceV2()) {
                        deviceStatusDescr_Byte[bit] = MainActivity.myFiscalDevice.getConnectedModelV2().getStatusBitDescriptionEn(by, bit);
                        deviceStatusFlags_Byte[bit] = MainActivity.myFiscalDevice.getConnectedModelV2().getStatusBitBol(by, bit);
                        isCritical[bit] = FiscalDeviceV2.getIsStatusCritical(by, bit);
                    }

                }
                //deviceStatusFlags_Byte = byteToBoolArr(statusOfDevice[by]); //CMD 74
                DeviceStatusListModel temp = new DeviceStatusListModel(deviceStatusFlags_Byte, deviceStatusDescr_Byte,isCritical);
                items.add(temp);
            }
            adapter = new DeviceStatusListAdapter(getActivity(), items);
            binder.lvDeviceStatus.setAdapter(adapter);
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }
        return binder.getRoot();
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible && isResumed()) {

        }

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isResumed() && isVisibleToUser) {
        }
    }


//    public static boolean[] byteToBoolArr(byte b) {
//        boolean boolArr[] = new boolean[8];
//        for (int i = 0; i < 8; i++) boolArr[7 - i] = (b & (byte) (128 / Math.pow(2, i))) != 0;
//        return boolArr;
//    }

    private void postToast(final String text) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
            }
        });
    }


    public class DeviceStatusListAdapter extends ArrayAdapter<DeviceStatusListModel> {

        private Context context;
        private ArrayList<DeviceStatusListModel> item;

        public DeviceStatusListAdapter(@NonNull Context context, @NonNull ArrayList<DeviceStatusListModel> objects) {
            super(context, R.layout.custom_device_status_list_lv, objects);
            this.context = context;
            item = objects;
        }


        class ViewHolder {
            CheckBox getChkBit0;
            CheckBox getChkBit1;
            CheckBox getChkBit2;
            CheckBox getChkBit3;
            CheckBox getChkBit4;
            CheckBox getChkBit5;
            CheckBox getChkBit6;
            CheckBox getChkBit7;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            ViewHolder holder;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.custom_device_status_list_lv, parent, false);
                holder.getChkBit0 = convertView.findViewById(R.id.chkBit0);
                holder.getChkBit1 = convertView.findViewById(R.id.chkBit1);
                holder.getChkBit2 = convertView.findViewById(R.id.chkBit2);
                holder.getChkBit3 = convertView.findViewById(R.id.chkBit3);
                holder.getChkBit4 = convertView.findViewById(R.id.chkBit4);
                holder.getChkBit5 = convertView.findViewById(R.id.chkBit5);
                holder.getChkBit6 = convertView.findViewById(R.id.chkBit6);
                holder.getChkBit7 = convertView.findViewById(R.id.chkBit7);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.getChkBit0.setText(item.get(position).getStatusDesc0());
            holder.getChkBit1.setText(item.get(position).getStatusDesc1());
            holder.getChkBit2.setText(item.get(position).getStatusDesc2());
            holder.getChkBit3.setText(item.get(position).getStatusDesc3());
            holder.getChkBit4.setText(item.get(position).getStatusDesc4());
            holder.getChkBit5.setText(item.get(position).getStatusDesc5());
            holder.getChkBit6.setText(item.get(position).getStatusDesc6());
            holder.getChkBit7.setText(item.get(position).getStatusDesc7());

            holder.getChkBit0.setChecked(item.get(position).isStatusBit0());
            holder.getChkBit1.setChecked(item.get(position).isStatusBit1());
            holder.getChkBit2.setChecked(item.get(position).isStatusBit2());
            holder.getChkBit3.setChecked(item.get(position).isStatusBit3());
            holder.getChkBit4.setChecked(item.get(position).isStatusBit4());
            holder.getChkBit5.setChecked(item.get(position).isStatusBit5());
            holder.getChkBit6.setChecked(item.get(position).isStatusBit6());
            holder.getChkBit7.setChecked(item.get(position).isStatusBit7());

            holder.getChkBit0.setTextColor(item.get(position).isCriticalBit0() ? Color.RED : Color.BLACK);
            holder.getChkBit1.setTextColor(item.get(position).isCriticalBit1() ? Color.RED : Color.BLACK);
            holder.getChkBit2.setTextColor(item.get(position).isCriticalBit2() ? Color.RED : Color.BLACK);
            holder.getChkBit3.setTextColor(item.get(position).isCriticalBit3() ? Color.RED : Color.BLACK);
            holder.getChkBit4.setTextColor(item.get(position).isCriticalBit4() ? Color.RED : Color.BLACK);
            holder.getChkBit5.setTextColor(item.get(position).isCriticalBit5() ? Color.RED : Color.BLACK);
            holder.getChkBit6.setTextColor(item.get(position).isCriticalBit6() ? Color.RED : Color.BLACK);
            holder.getChkBit7.setTextColor(item.get(position).isCriticalBit7() ? Color.RED : Color.BLACK);


            return convertView;
        }
    }

    public class DeviceStatusListModel implements Serializable {
        private String statusDesc0;
        private String statusDesc1;
        private String statusDesc2;
        private String statusDesc3;
        private String statusDesc4;
        private String statusDesc5;
        private String statusDesc6;
        private String statusDesc7;

        private boolean statusBit0;
        private boolean statusBit1;
        private boolean statusBit2;
        private boolean statusBit3;
        private boolean statusBit4;
        private boolean statusBit5;
        private boolean statusBit6;
        private boolean statusBit7;

        private boolean isCriticalBit0;
        private boolean isCriticalBit1;
        private boolean isCriticalBit2;
        private boolean isCriticalBit3;
        private boolean isCriticalBit4;
        private boolean isCriticalBit5;
        private boolean isCriticalBit6;
        private boolean isCriticalBit7;


//    String statusDesc0, String statusDesc1, String statusDesc2, String statusDesc3, String statusDesc4, String statusDesc5, String statusDesc6, String statusDesc7, boolean statusBit0, boolean statusBit1, boolean statusBit2, boolean statusBit3, boolean statusBit4, boolean statusBit5, boolean statusBit6, boolean statusBit7

        public DeviceStatusListModel(boolean[] statusBit, String statusDesc[], boolean[] isCritical) {
            this.statusDesc0 = statusDesc[0];
            this.statusDesc1 = statusDesc[1];
            this.statusDesc2 = statusDesc[2];
            this.statusDesc3 = statusDesc[3];
            this.statusDesc4 = statusDesc[4];
            this.statusDesc5 = statusDesc[5];
            this.statusDesc6 = statusDesc[6];
            this.statusDesc7 = statusDesc[7];
            this.statusBit0 = statusBit[0];
            this.statusBit1 = statusBit[1];
            this.statusBit2 = statusBit[2];
            this.statusBit3 = statusBit[3];
            this.statusBit4 = statusBit[4];
            this.statusBit5 = statusBit[5];
            this.statusBit6 = statusBit[6];
            this.statusBit7 = statusBit[7];

            this.isCriticalBit0 = isCritical[0];
            this.isCriticalBit1 = isCritical[1];
            this.isCriticalBit2 = isCritical[2];
            this.isCriticalBit3 = isCritical[3];
            this.isCriticalBit4 = isCritical[4];
            this.isCriticalBit5 = isCritical[5];
            this.isCriticalBit6 = isCritical[6];
            this.isCriticalBit7 = isCritical[7];

        }

        public void setStausByte(boolean[] statusBit, String statusDesc[], boolean[] isCritical) {
            this.statusDesc0 = statusDesc[0];
            this.statusDesc1 = statusDesc[1];
            this.statusDesc2 = statusDesc[2];
            this.statusDesc3 = statusDesc[3];
            this.statusDesc4 = statusDesc[4];
            this.statusDesc5 = statusDesc[5];
            this.statusDesc6 = statusDesc[6];
            this.statusDesc7 = statusDesc[7];
            this.statusBit0 = statusBit[0];
            this.statusBit1 = statusBit[1];
            this.statusBit2 = statusBit[2];
            this.statusBit3 = statusBit[3];
            this.statusBit4 = statusBit[4];
            this.statusBit5 = statusBit[5];
            this.statusBit6 = statusBit[6];
            this.statusBit7 = statusBit[7];
            this.isCriticalBit0 = isCritical[0];
            this.isCriticalBit1 = isCritical[1];
            this.isCriticalBit2 = isCritical[2];
            this.isCriticalBit3 = isCritical[3];
            this.isCriticalBit4 = isCritical[4];
            this.isCriticalBit5 = isCritical[5];
            this.isCriticalBit6 = isCritical[6];
            this.isCriticalBit7 = isCritical[7];
        }

        public void setStatusDesc0(String statusDesc0) {
            this.statusDesc0 = statusDesc0;
        }

        public void setStatusDesc1(String statusDesc1) {
            this.statusDesc1 = statusDesc1;
        }

        public void setStatusDesc2(String statusDesc2) {
            this.statusDesc2 = statusDesc2;
        }

        public void setStatusDesc3(String statusDesc3) {
            this.statusDesc3 = statusDesc3;
        }

        public void setStatusDesc4(String statusDesc4) {
            this.statusDesc4 = statusDesc4;
        }

        public void setStatusDesc5(String statusDesc5) {
            this.statusDesc5 = statusDesc5;
        }

        public void setStatusDesc6(String statusDesc6) {
            this.statusDesc6 = statusDesc6;
        }

        public void setStatusDesc7(String statusDesc7) {
            this.statusDesc7 = statusDesc7;
        }

        public void setStatusBit0(boolean statusBit0) {
            this.statusBit0 = statusBit0;
        }

        public void setStatusBit1(boolean statusBit1) {
            this.statusBit1 = statusBit1;
        }

        public void setStatusBit2(boolean statusBit2) {
            this.statusBit2 = statusBit2;
        }

        public void setStatusBit3(boolean statusBit3) {
            this.statusBit3 = statusBit3;
        }

        public void setStatusBit4(boolean statusBit4) {
            this.statusBit4 = statusBit4;
        }

        public void setStatusBit5(boolean statusBit5) {
            this.statusBit5 = statusBit5;
        }

        public void setStatusBit6(boolean statusBit6) {
            this.statusBit6 = statusBit6;
        }

        public void setStatusBit7(boolean statusBit7) {
            this.statusBit7 = statusBit7;
        }


        public String getStatusDesc0() {
            return statusDesc0;
        }

        public String getStatusDesc1() {
            return statusDesc1;
        }

        public String getStatusDesc2() {
            return statusDesc2;
        }

        public String getStatusDesc3() {
            return statusDesc3;
        }

        public String getStatusDesc4() {
            return statusDesc4;
        }

        public String getStatusDesc5() {
            return statusDesc5;
        }

        public String getStatusDesc6() {
            return statusDesc6;
        }

        public String getStatusDesc7() {
            return statusDesc7;
        }

        public boolean isStatusBit0() {
            return statusBit0;
        }

        public boolean isStatusBit1() {
            return statusBit1;
        }

        public boolean isStatusBit2() {
            return statusBit2;
        }

        public boolean isStatusBit3() {
            return statusBit3;
        }

        public boolean isStatusBit4() {
            return statusBit4;
        }

        public boolean isStatusBit5() {
            return statusBit5;
        }

        public boolean isStatusBit6() {
            return statusBit6;
        }

        public boolean isStatusBit7() {
            return statusBit7;
        }


        public boolean isCriticalBit0() {
            return isCriticalBit0;
        }

        public boolean isCriticalBit1() {
            return isCriticalBit1;
        }

        public boolean isCriticalBit2() {
            return isCriticalBit2;
        }

        public boolean isCriticalBit3() {
            return isCriticalBit3;
        }

        public boolean isCriticalBit4() {
            return isCriticalBit4;
        }

        public boolean isCriticalBit5() {
            return isCriticalBit5;
        }

        public boolean isCriticalBit6() {
            return isCriticalBit6;
        }

        public boolean isCriticalBit7() {
            return isCriticalBit7;
        }
    }

}
