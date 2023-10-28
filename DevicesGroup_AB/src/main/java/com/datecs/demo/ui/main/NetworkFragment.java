package com.datecs.demo.ui.main;

import android.app.ProgressDialog;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.datecs.demo.NetworkSettings_frg_binding;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdConfig;
import com.datecs.testApp.R;


public class NetworkFragment extends Fragment {

    private ProgressDialog progress;
    private cmdConfig myConfig = new cmdConfig();
    private NetworkSettings_frg_binding binder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.network_fragment, container, false);

        return binder.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // do the thing that takes a long time
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        cmdConfig.LANSettings LAN = new cmdConfig().GetLANSettings();
                                        binder.edIPLAN.setText(LAN.getIPAddr());
                                        binder.edMask.setText(LAN.getSubnetMask());
                                        binder.edGatewayLAN.setText(LAN.getDefGateway());
                                        binder.edPortLAN.setText(LAN.getTCPPort());
                                        binder.chbxDHCPLAN.setChecked(LAN.getDHCP());
                                        binder.edMACLAN.setText(LAN.getMACAddr());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        postToast(e.getMessage());
                                    }
                                }


                });
            }
        }).start();



        binder.btnSaveLanSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String sIPLAN = binder.edIPLAN.getText().toString();
                final String sMask = binder.edMask.getText().toString();
                final String sPort = binder.edPortLAN.getText().toString();
                final String sGatewayLAN = binder.edGatewayLAN.getText().toString();
                final boolean DHCP = binder.chbxDHCPLAN.isChecked();
                final String sMACLAN = binder.edMACLAN.getText().toString();

                progress = ProgressDialog.show(getContext(), getString(R.string.title_write_items),
                        getString(R.string.msg_please_wait), true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            myConfig.SetLANSettings(new cmdConfig.LANSettings(sIPLAN, sMask, sPort, sGatewayLAN, DHCP, sMACLAN));
                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                        } finally {
                            progress.dismiss();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(getView(), R.string.msg_lan_saved, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            }
                        });
                    }
                }).start();
            }
        });
    }


    private void postToast(final String text) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
            }
        });
    }
}