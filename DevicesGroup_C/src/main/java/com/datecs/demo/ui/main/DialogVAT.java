

package com.datecs.demo.ui.main;

import android.app.Activity;
import android.app.Dialog;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.datecs.demo.DialogVATS_binding;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdService;
import com.datecs.testApp.R;

import java.util.ArrayList;
import java.util.List;

public class DialogVAT extends Dialog implements
        View.OnClickListener {


    public DialogVAT(Activity a) {
        super(a, R.style.Dialog);
    }

    private DialogVATS_binding binder;
    private cmdService.VAT myVATS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binder = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_vats, null, false);
        binder.btnSaveVATS.setOnClickListener(this);
        binder.btnCancelVATS.setOnClickListener(this);
        try {
            myVATS = new cmdService.VAT();
            int dec = Integer.valueOf(myVATS.getDecimals());
            binder.spDecimalPlace.setSelection(dec / 2);// value 0-2 convert to index 0-1

            //Get List of boolean with enabled-true or disabled-false values of fiscal device VATs
            List<Boolean> vatsOn = myVATS.getVatEnabled();
            //Set Selection on spinners
            binder.spVATA.setSelection(vatsOn.get(0) ? 0 : 1);
            binder.spVATB.setSelection(vatsOn.get(1) ? 0 : 1);
            binder.spVATC.setSelection(vatsOn.get(2) ? 0 : 1);
            binder.spVATD.setSelection(vatsOn.get(3) ? 0 : 1);
            binder.spVATE.setSelection(vatsOn.get(4) ? 0 : 1);
            binder.spVATF.setSelection(vatsOn.get(5) ? 0 : 1);
            binder.spVATG.setSelection(vatsOn.get(6) ? 0 : 1);
            binder.spVATH.setSelection(vatsOn.get(7) ? 0 : 1);

            ///Read VATs from fiscal device and update view
            final List<String> vatsRate = myVATS.getVatRates();
            //Enable or Disable vats
            binder.spVATA.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            binder.edVATA.setEnabled(binder.spVATA.getSelectedItemId() == 0);
                            binder.edVATA.setText(vatsRate.get(0));//R.string._0_00);
                            binder.edVATA.requestFocus();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }

                    }
            );

            binder.spVATB.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            binder.edVATB.setEnabled(binder.spVATB.getSelectedItemId() == 0);
                            binder.edVATB.setText(vatsRate.get(1));//R.string._0_00);
                            binder.edVATB.requestFocus();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    }
            );

            binder.spVATC.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            binder.edVATC.setEnabled(binder.spVATC.getSelectedItemId() == 0);
                            binder.edVATC.setText(vatsRate.get(2));//R.string._0_00);
                            binder.edVATC.requestFocus();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }


                    }
            );

            binder.spVATD.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            binder.edVATD.setEnabled(binder.spVATD.getSelectedItemId() == 0);
                            binder.edVATD.setText(vatsRate.get(3)); //R.string._0_00);
                            binder.edVATD.requestFocus();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    }
            );

            binder.spVATE.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            binder.edVATE.setEnabled(binder.spVATE.getSelectedItemId() == 0);
                            binder.edVATE.setText(vatsRate.get(4));//R.string._0_00);
                            binder.edVATE.requestFocus();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }

                    }
            );

            binder.spVATF.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            binder.edVATF.setEnabled(binder.spVATF.getSelectedItemId() == 0);
                            binder.edVATF.setText(vatsRate.get(5));//R.string._0_00);
                            binder.edVATF.requestFocus();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    }
            );

            binder.spVATG.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            binder.edVATG.setEnabled(binder.spVATG.getSelectedItemId() == 0);
                            binder.edVATG.setText(vatsRate.get(6));//R.string._0_00);
                            binder.edVATG.requestFocus();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }

                    }
            );

            binder.spVATH.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            binder.edVATH.setEnabled(binder.spVATH.getSelectedItemId() == 0);
                            binder.edVATH.setText(vatsRate.get(7));//R.string._0_00);
                            binder.edVATH.requestFocus();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }

                    }
            );
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        setContentView(binder.getRoot());
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////DIALOG ON CLICK
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_saveVATS:
                try {
                    List<String> vats = new ArrayList<>(8);

                    vats.add(binder.edVATA.getText().toString());
                    vats.add(binder.edVATB.getText().toString());
                    vats.add(binder.edVATC.getText().toString());
                    vats.add(binder.edVATD.getText().toString());
                    vats.add(binder.edVATE.getText().toString());
                    vats.add(binder.edVATF.getText().toString());
                    vats.add(binder.edVATG.getText().toString());
                    vats.add(binder.edVATH.getText().toString());

                    //!!! 100.00 is disable device VATS.
                    if ((binder.spVATA.getSelectedItemId() == 1)) vats.set(0, "100.00");
                    if ((binder.spVATB.getSelectedItemId() == 1)) vats.set(1, "100.00");
                    if ((binder.spVATC.getSelectedItemId() == 1)) vats.set(2, "100.00");
                    if ((binder.spVATD.getSelectedItemId() == 1)) vats.set(3, "100.00");
                    if ((binder.spVATE.getSelectedItemId() == 1)) vats.set(4, "100.00");
                    if ((binder.spVATF.getSelectedItemId() == 1)) vats.set(5, "100.00");
                    if ((binder.spVATG.getSelectedItemId() == 1)) vats.set(6, "100.00");
                    if ((binder.spVATH.getSelectedItemId() == 1)) vats.set(7, "100.00");

                    myVATS.setDecimalPoint(String.valueOf((int) 2 * binder.spDecimalPlace.getSelectedItemId()));
                    String remChanges = getContext().getString(R.string.remaining_changes) +
                            String.valueOf(myVATS.setVatRates(vats));


                    Toast.makeText(getContext(), remChanges, Toast.LENGTH_LONG).show();
                    dismiss();
                } catch (Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
            case R.id.btn_cancelVATS:
                dismiss();
                break;
            default:
                break;
        }

    }


}


