package com.datecs.demo.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt;
import com.datecs.testApp.R;

public class DialogCashInOut extends Dialog implements
        android.view.View.OnClickListener {

    private ImageButton yes, no;
    private EditText edSetCioSum;
    private RadioButton btnradSetCashIn;
    private RadioButton btnradSetCashOut;
    private CheckBox chkSetCioForeign;
    private Activity a;
    public DialogCashInOut(Activity a) {
        super(a, R.style.Dialog);
        this.a=a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_cash_in_out);
        btnradSetCashIn = findViewById(R.id.btnrad_set_cash_In);
        btnradSetCashOut = findViewById(R.id.btnrad_set_cash_out);
        edSetCioSum = findViewById(R.id.ed_set_cio_sum);
        chkSetCioForeign = findViewById(R.id.chk_set_cio_Foreign);
        btnradSetCashOut = findViewById(R.id.btnrad_set_cash_out);

        yes = findViewById(R.id.btn_set_cio_OK);
        no = findViewById(R.id.btn_set_cio_cancel);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_set_cio_OK:
                dismiss();
                try {
                    Double newAmount = Double.valueOf(edSetCioSum.getText().toString());

                    if (btnradSetCashOut.isChecked())
                        newAmount = -1.0 * Double.valueOf(edSetCioSum.getText().toString());
                    CashINOUT(String.format("%.2f", newAmount), chkSetCioForeign.isChecked());
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
                break;
            case R.id.btn_set_cio_cancel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    private void CashINOUT(String valueOfCurrency, boolean foreign) {
        try {
            // cashInSafe Holds result of operation:
            //0-cashSum
            //1-cashIn
            //2-cashOut
            Double[] cashInSafe = new Double[3];


            cashInSafe = new cmdReceipt().cashInCashOut(Double.valueOf(valueOfCurrency), foreign);
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
            builder1.setMessage("Cash in safe:" + cashInSafe[0] + "\n\r" +
                    " Sum of Cash IN:" + cashInSafe[1] + "\n\r" +
                    " Sum of Cash OUT:" + cashInSafe[2]);
            builder1.setCancelable(false);
            builder1.setPositiveButton(R.string.okButtonText, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alert11 = builder1.create();
            alert11.show();

        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }

    }

    private void postToast(final String message) {
        a.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(a, message, Toast.LENGTH_LONG).show();
            }
        });
    }

}

