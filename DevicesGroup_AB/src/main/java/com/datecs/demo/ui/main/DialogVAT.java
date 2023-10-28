/**
 * In this dialog you can enter the tax rates VATs on the fiscal device.
 * <p>
 * ***********************
 * *     Attention:      *
 * ***********************
 * Beware of changing tax rates may make it impossible to issue Storno Receipt.
 *
 * @author Datecs Ltd. Software Department
 *
 */
package com.datecs.demo.ui.main;

import android.app.Activity;
import android.app.Dialog;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.datecs.demo.DialogVatBinding;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdService;
import com.datecs.testApp.R;

public class DialogVAT extends Dialog implements
        View.OnClickListener {


    public DialogVAT(Activity a) {
        super(a, R.style.Dialog);
    }

    private DialogVatBinding binder;
    private cmdService.VAT myVATS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binder = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_vat, null, false);
        binder.btnSaveVATS.setOnClickListener(this);
        binder.btnCancelVATS.setOnClickListener(this);
        try {
            myVATS = new cmdService.VAT();
            myVATS.readVatRates(); // Call First !!!

            int dec = Integer.valueOf(myVATS.getDecimals());
            binder.spDecimalPlace.setSelection(dec / 2);// value 0-2 convert to index 0-1
            binder.edCurrency.setText(myVATS.getCurrencyName());
            binder.spVATA.setSelection(myVATS.isEnableTaxA() ? 0 : 1);
            binder.spVATB.setSelection(myVATS.isEnableTaxB() ? 0 : 1);
            binder.spVATC.setSelection(myVATS.isEnableTaxC() ? 0 : 1);
            binder.spVATD.setSelection(myVATS.isEnableTaxD() ? 0 : 1);
            binder.spVATE.setSelection(myVATS.isEnableTaxE() ? 0 : 1);
            binder.spVATF.setSelection(myVATS.isEnableTaxF() ? 0 : 1);
            binder.spVATG.setSelection(myVATS.isEnableTaxG() ? 0 : 1);
            binder.spVATH.setSelection(myVATS.isEnableTaxH() ? 0 : 1);
            binder.spVATA.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            binder.edVATA.setEnabled(binder.spVATA.getSelectedItemId() == 0);//Enabled
                            binder.edVATA.setText(myVATS.getTaxA());
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
                            binder.edVATB.setText(myVATS.getTaxB());//R.string._0_00);
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
                            binder.edVATC.setText(myVATS.getTaxC());//R.string._0_00);
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
                            binder.edVATD.setText(myVATS.getTaxD()); //R.string._0_00);
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
                            binder.edVATE.setText(myVATS.getTaxE());//R.string._0_00);
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
                            binder.edVATF.setText(myVATS.getTaxF());//R.string._0_00);
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
                            binder.edVATG.setText(myVATS.getTaxG());//R.string._0_00);
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
                            binder.edVATH.setText(myVATS.getTaxH());//R.string._0_00);
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

                    String maskVATsEnabled = "";
                    maskVATsEnabled += String.valueOf(1 - binder.spVATA.getSelectedItemId());
                    maskVATsEnabled += String.valueOf(1 - binder.spVATB.getSelectedItemId());
                    maskVATsEnabled += String.valueOf(1 - binder.spVATC.getSelectedItemId());
                    maskVATsEnabled += String.valueOf(1 - binder.spVATD.getSelectedItemId());
                    maskVATsEnabled += String.valueOf(1 - binder.spVATE.getSelectedItemId());
                    maskVATsEnabled += String.valueOf(1 - binder.spVATF.getSelectedItemId());
                    maskVATsEnabled += String.valueOf(1 - binder.spVATG.getSelectedItemId());
                    maskVATsEnabled += String.valueOf(1 - binder.spVATH.getSelectedItemId());

                    //!!!
                    myVATS.setEnabledMask(maskVATsEnabled);
                    myVATS.setCurrencyName(binder.edCurrency.getText().toString());
                    int dec = (int) binder.spDecimalPlace.getSelectedItemId();
                    myVATS.setDecimals(String.valueOf(dec * 2)); // Convert index 0-1 to value 0-2
                    myVATS.saveVatRates(
                            binder.edVATA.getText().toString(),
                            binder.edVATB.getText().toString(),
                            binder.edVATC.getText().toString(),
                            binder.edVATD.getText().toString(),
                            binder.edVATE.getText().toString(),
                            binder.edVATF.getText().toString(),
                            binder.edVATG.getText().toString(),
                            binder.edVATH.getText().toString());
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


