package com.datecs.demo.ui.main;

import android.app.Activity;
import android.app.Dialog;

import androidx.databinding.DataBindingUtil;

import android.graphics.Color;
import android.os.Bundle;

import com.datecs.demo.DialogInvoice_binding;
import com.datecs.demo.PrinterManager;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdClients;
import com.google.android.material.snackbar.Snackbar;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdConfig;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt;
import com.datecs.testApp.R;

import java.util.ArrayList;

import static com.datecs.demo.PrinterManager.getFiscalDevice;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt.PaymentType.cash;

public class DialogInvoice extends Dialog implements View.OnClickListener {

    private DialogInvoice_binding binder;
    private Activity a;
    private cmdReceipt.FiscalReceipt.InvoiceClientInfo clientInfo;
    private cmdReceipt.FiscalReceipt fiscalReceipt = new cmdReceipt.FiscalReceipt();

    public DialogInvoice(Activity a) {
        super(a, R.style.Dialog);
        this.a = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = this.getLayoutInflater();
        binder = DataBindingUtil.inflate(inflater, R.layout.dialog_invoice, null, false);

        try {
            ArrayList<String> intervalInfo = new cmdConfig().GetInvoiceInterval();
            binder.edInvoiceStart.setText(intervalInfo.get(0));
            binder.edInvoiceEnd.setText(intervalInfo.get(1));
            binder.btnPrintInvoice.setText("Print Invoice:" + intervalInfo.get(2));
            binder.btnPrintInvoice.setEnabled(false);
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }


        final View mDialogView = binder.getRoot();
        setContentView(mDialogView);
        binder.btnSetInvoiceRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmdConfig myConfig = new cmdConfig();
                try {
                    ArrayList<String> invoiceInterval = myConfig.GetInvoiceInterval();
                    if (Integer.valueOf(invoiceInterval.get(1)) < Integer.valueOf(invoiceInterval.get(2))) {
                        //If the current invoice counter have reached the end of the interval.
                        myConfig.SetInvoiceInterval(binder.edInvoiceStart.getText().toString(), binder.edInvoiceEnd.getText().toString());
                    } else // Extend of interval
                        myConfig.SetExInvoiceInterval(binder.edInvoiceEnd.getText().toString());
                    Snackbar.make(mDialogView, R.string.settings_saved, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    postToast(ex.getMessage());
                }
            }
        });

        binder.btnEIKSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    if (PrinterManager.isECR()) {
                        cmdClients myClients = new cmdClients();
                        cmdClients.ClientInfoModel res = myClients.findClientByEIK(binder.edInvoiceTaxN.getText().toString());

                        binder.edBuyerName.setText(res.getName());
                        binder.edReceiverName.setText(res.getRecName());
                        binder.edVATN.setText(res.getVATN());
                        binder.edInvoiceTaxN.setText(res.getEIK());
                        binder.edClientAddress1.setText(res.getAddr1());
                        binder.edClientAddress2.setText(res.getAddr2());
                    }

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        binder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dismiss();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        binder.btnValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    clientInfo = new cmdReceipt.FiscalReceipt.InvoiceClientInfo(
                            cmdReceipt.FiscalReceipt.InvoiceClientInfo.TypeTAXN.fromOrdinal(binder.spTypeOfTAXN.getSelectedItemPosition()),
                            binder.edInvoiceTaxN.getText().toString(),
                            binder.edVATN.getText().toString(),
                            binder.edSellerName.getText().toString(),
                            binder.edReceiverName.getText().toString(),
                            binder.edBuyerName.getText().toString(),
                            binder.edClientAddress1.getText().toString(),
                            binder.edClientAddress2.getText().toString());

                    boolean res[] = clientInfo.isValid();

                    int color;
                    if (!res[0]) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edInvoiceTaxN.setTextColor(color);

                    if (!res[1]) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edVATN.setTextColor(color);

                    if (!res[2]) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edSellerName.setTextColor(color);

                    if (!res[3]) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edReceiverName.setTextColor(color);

                    if (!res[4]) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edBuyerName.setTextColor(color);

                    if (!res[5]) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edClientAddress1.setTextColor(color);

                    if (!res[6]) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edClientAddress2.setTextColor(color);


                    boolean isValid = true;
                    for (boolean tRes : res) {
                        isValid &= tRes;
                    }
                    binder.btnPrintInvoice.setText("Print Invoice:" + new cmdConfig().GetInvoiceInterval().get(2));
                    binder.btnPrintInvoice.setEnabled(isValid);

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        binder.btnPrintInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printInvoice();
            }
        });

    }


    private void printInvoice() {
        String salePoint = "1"; //Number of point of sale from 1...99999;
        String NSale; //Unique sale number (21 chars "LLDDDDDD-CCCC-DDDDDDD"

        try {
            //Note: WP-500X, WP-50X, WP-25X, DP-25X, DP-150X, DP-05C: the default password for each operator is
            //equal to the corresponding number (for example, for Operator1 the password is "1") . FMP-350X, FMP-55X,
            //FP-700X: the default password for each operator is “0000”
            //Set Operator and password
            Integer operatorCode = Integer.valueOf(getFiscalDevice().getConnectedModelV2().getCurrentOpCode());
            //String operatorPassword = getFiscalDevice().getConnectedModelV2().getDefaultOpPass();
            String operatorPassword = new cmdInfo().GetOperPasw(operatorCode - 1);//Password of operator. Text up to 8 symbols. ( Require Service jumper )
            if (!fiscalReceipt.isOpen()) {
                //Open Fiscal bon in current receipt and return number of receipt
                fiscalReceipt.openInvoice(
                        String.valueOf(operatorCode),
                        operatorPassword,
                        salePoint);
                //Registration of item for sale with the minimum required set of parameters
                cmdReceipt.FiscalReceipt.FiscalSale testSale =
                        new cmdReceipt.FiscalReceipt.FiscalSale(
                                "Бонбон",
                                "2",
                                "0.01").add();
                //TOTAL
                testSale.saleTotal(cash, "0.01");

                //TOTAL Foreign Currency
                //testSale.saleTotalForeignCurrency("1", cmdReceipt.FiscalSale.TypeOfChange.currentCurrency);
                //TOTAL Debit Card
                //testSale.saleTotalDebitCard("", cmdReceipt.FiscalSale.TypeOfCardPayment.paymentWithPoints);

                //Combined Payment test, please increase the amount of the item
                //testSale.saleTotal(cash,"0.10");
                //testSale.saleTotal(debit_card,"");
                //Save info after Total of fiscal receipt

                //If Client Data Info is valid
                clientInfo.saveClientInfo();
                fiscalReceipt.closeFiscalReceipt();
            } else fiscalReceipt.cancel();
            binder.btnPrintInvoice.setEnabled(false);

        } catch (Exception e1) {
            postToast(e1.getMessage());
            e1.printStackTrace();
        }

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


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
