package com.datecs.demo.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdPinpad;
import com.datecs.testApp.R;
import com.google.android.material.snackbar.Snackbar;

public class DialogPinpadConfig extends Dialog implements android.view.View.OnClickListener {
    private PinpadConfigBinding binder;
    private cmdPinpad myPinpad = new cmdPinpad();
    private Activity a;
    private View mDialogView;

    public DialogPinpadConfig(Activity a) {
        super(a, R.style.Dialog);
        this.a = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = this.getLayoutInflater();
        binder = DataBindingUtil.inflate(inflater, R.layout.pinpad_config_fragment, null, false);
        mDialogView = binder.getRoot();
        setContentView(mDialogView);

        try {
            binder.spComPortPinpad.setSelection(myPinpad.getPinpadComPort() - 1);
            binder.spBaudratePinpad.setSelection(myPinpad.getPinpadComBaudRate());
            binder.spTypePinpad.setSelection(myPinpad.getPinpadType().ordinal() - 1);
            binder.spServerConnPinpad.setSelection(myPinpad.getPinpadConnectionType());
            binder.spFunctionPY2Pinpad.setSelection(myPinpad.getPinpadPaymentMenu());
            binder.spFunctionPY4Pinpad.setSelection(myPinpad.getPinpadLoyaltyPayment());
        } catch (Exception e) {
            e.printStackTrace();
            postToast(e.getMessage());
        }


        binder.btnPinpadSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    myPinpad.setPinpadComPort((int) (binder.spComPortPinpad.getSelectedItemId() + 1));
                    myPinpad.setPinpadComBaudRate((int) binder.spBaudratePinpad.getSelectedItemId());
                    myPinpad.setPinpadType(cmdPinpad.PinpadType.values()[(int) (binder.spTypePinpad.getSelectedItemId() + 1)]);
                    myPinpad.setPinpadConnectionType((int) binder.spServerConnPinpad.getSelectedItemId());
                    myPinpad.setPinpadPaymentMenu((int) binder.spFunctionPY2Pinpad.getSelectedItemId());
                    myPinpad.setPinpadLoyaltyPayment((int) binder.spFunctionPY4Pinpad.getSelectedItemId());
                    Snackbar.make(mDialogView, R.string.settings_saved, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } catch (Exception exception) {
                    postToast(exception.getMessage());
                    exception.printStackTrace();
                }

            }

        });


    }


    private void postToast(final String text) {
        a.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(a, text, Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onClick(View v) {

    }
}
