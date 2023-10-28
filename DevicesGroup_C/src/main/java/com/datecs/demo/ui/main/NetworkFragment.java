package com.datecs.demo.ui.main;

import android.app.ProgressDialog;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdConfig;
import com.datecs.testApp.R;

import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdConfig.MainInterfaceType.BLUETOOTH;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdConfig.MainInterfaceType.LAN;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdConfig.MainInterfaceType.RS232;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdConfig.MainInterfaceType.USB;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdConfig.MainInterfaceType.auto_select;


public class NetworkFragment extends Fragment {

    private ProgressDialog progress;
    private cmdConfig myConfig = new cmdConfig();
    private NetworkFragment_binding binder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.network_fragment, container, false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // do the thing that takes a long time
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            binder.edIPLAN.setText(myConfig.GetLANIP());
                            binder.edMask.setText(myConfig.GetLANNetMask());
                            binder.edGatewayLAN.setText(myConfig.GetLANGateway());
                            binder.edPortLAN.setText(myConfig.GetLANport());
                            binder.edDNS1LAN.setText(myConfig.GetLANPriDNS());
                            binder.edDNS2LAN.setText(myConfig.GetLANSecDNS());
                            binder.edMACLAN.setText(myConfig.GetLanMAC());
                            binder.chbxDHCPLAN.setChecked(myConfig.GetDHCPenable());

                            cmdConfig.MainInterfaceType interfaceToConnectWithPC = myConfig.GetMainInterfaceType();
                            binder.rbtnAuto.setChecked(interfaceToConnectWithPC == auto_select);
                            binder.rbtnLAN.setChecked(interfaceToConnectWithPC == LAN);
                            binder.rbtnUSB.setChecked(interfaceToConnectWithPC == USB);
                            binder.rbtnBLUETOOTH.setChecked(interfaceToConnectWithPC == BLUETOOTH);
                            binder.rbtnRS232.setChecked(interfaceToConnectWithPC == RS232);

                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                        }
                    }


                });
            }
        }).start();

        return binder.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binder.btnSaveLanSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String sIPLAN = binder.edIPLAN.getText().toString();
                final String sMask = binder.edMask.getText().toString();
                final String sGatewayLAN = binder.edGatewayLAN.getText().toString();
                final String sDNS1LAN = binder.edDNS1LAN.getText().toString();
                final String sDNS2LAN = binder.edDNS2LAN.getText().toString();
                final String sMACLAN = binder.edMACLAN.getText().toString();
                final Boolean bDHCPLAN = binder.chbxDHCPLAN.isChecked();

                progress = ProgressDialog.show(getContext(), getString(R.string.title_write_items),
                        getString(R.string.msg_please_wait), true);
                final int[] errFocus = new int[1];
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            errFocus[0]++;
                            myConfig.SetLANIP(sIPLAN);
                            errFocus[0]++;
                            myConfig.SetLANNetMask(sMask);
                            errFocus[0]++;
                            myConfig.SetLANGateway(sGatewayLAN);
                            errFocus[0]++;
                            myConfig.SetLANport(binder.edPortLAN.getText().toString());
                            errFocus[0]++;
                            myConfig.SetLANPriDNS(sDNS1LAN);
                            errFocus[0]++;
                            myConfig.SetLANSecDNS(sDNS2LAN);
                            errFocus[0]++;
                            myConfig.SetLanMAC(sMACLAN);
                            errFocus[0]++;
                            myConfig.SetDHCPenable(bDHCPLAN);
                            errFocus[0]++;
                            errFocus[0] = 1000; //No error 1000

                            if (binder.rbtnAuto.isChecked())
                                myConfig.SetMainInterfaceType(auto_select);
                            else if (binder.rbtnBLUETOOTH.isChecked())
                                myConfig.SetMainInterfaceType(BLUETOOTH);
                            else if (binder.rbtnUSB.isChecked()) myConfig.SetMainInterfaceType(USB);
                            else if (binder.rbtnRS232.isChecked())
                                myConfig.SetMainInterfaceType(RS232);
                            else if (binder.rbtnLAN.isChecked()) myConfig.SetMainInterfaceType(LAN);


                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                        } finally {
                            progress.dismiss();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switch (errFocus[0]) {
                                    case 1:
                                        binder.edIPLAN.requestFocus();
                                        break;
                                    case 2:
                                        binder.edMask.requestFocus();
                                        break;
                                    case 3:
                                        binder.edGatewayLAN.requestFocus();
                                        break;
                                    case 4:
                                        binder.edPortLAN.requestFocus();
                                        break;
                                    case 5:
                                        binder.edDNS1LAN.requestFocus();
                                        break;
                                    case 6:
                                        binder.edDNS2LAN.requestFocus();
                                        break;
                                    case 7:
                                        binder.chbxDHCPLAN.requestFocus();
                                        break;
                                    case 1000: //No Error
                                        // MainActivity.sb(getView(),R.string.settings_saved);
                                }
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