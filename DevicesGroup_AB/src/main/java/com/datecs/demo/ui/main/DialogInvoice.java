package com.datecs.demo.ui.main;

import android.app.Activity;
import android.app.Dialog;
import androidx.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.datecs.demo.DialogInvoiceBinding;
import com.datecs.demo.MainActivity;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdConfig;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdReceipt;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdClients;
import com.datecs.testApp.R;
import com.google.android.material.snackbar.Snackbar;

import static com.datecs.demo.PrinterManager.getFiscalDevice;

public class DialogInvoice extends Dialog implements View.OnClickListener {

    private DialogInvoiceBinding binder;
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
        binder.edMOL.setEnabled(getFiscalDevice().isConnectedPrinter());
        setContentView(binder.getRoot());

        binder.btnFindByEIK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    cmdClients myClients=new cmdClients();
                    binder.edSellerName.setText(myClients.ReadSellerName());
                    cmdClients.ClientInfoModel foundedItem = myClients.findClientByEIK(binder.edInvoiceEIK.getText().toString().trim());
                    binder.edInvoiceEIK.setText(foundedItem.getEIK());
                    binder.edInvoiceTaxNo.setText(foundedItem.getVATN());
                    binder.spTypeOfTAXN.setSelection( foundedItem.getTypeTAXN().ordinal() );
                    binder.edBuyerName.setText(foundedItem.getName());
                    binder.edReceiverName.setText(foundedItem.getRecName());
                    binder.edClientAddress1.setText(foundedItem.getAddr1());
                    binder.edClientAddress2.setText(foundedItem.getAddr2());
                    Snackbar.make(binder.getRoot(), R.string.msg_item_was_found, Snackbar.LENGTH_LONG).setAction("Action", null).show();

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
                            binder.edInvoiceEIK.getText().toString(),
                            cmdReceipt.InvoiceClientInfo.BulstatType.fromOrdinal(binder.spTypeOfTAXN.getSelectedItemPosition()),
                            binder.edSellerName.getText().toString(),
                            binder.edReceiverName.getText().toString(),
                            binder.edBuyerName.getText().toString(),
                            binder.edInvoiceTaxNo.getText().toString(),
                            binder.edClientAddress1.getText().toString(),
                            binder.edClientAddress2.getText().toString(),
                            binder.edMOL.getText().toString());

                    boolean res[] = clientInfo.isValid();
/**
 *         res[0] Validation of EIK
 *         res[1] Validation of  Seller
 *         res[2] Validation of Receiver
 *         res[3] Validation of Client
 *         res[4] Validation of TaxNo
 *         res[5] Validation of Address
 */
                    int color;
                    if (!res[0]) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edInvoiceEIK.setTextColor(color);

                    if (!res[1]) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edSellerName.setTextColor(color);

                    if (!res[2]) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edReceiverName.setTextColor(color);

                    if (!res[3]) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edBuyerName.setTextColor(color);

                    if (!res[4]) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edInvoiceTaxNo.setTextColor(color);

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
        try {
            if (!fiscalReceipt.isOpen()) {
                //OPENING OF FISCAL BON
                String operatorCode = "1";//Operator number
                String operatorPassword = null; //Operator password
                String salePoint = "1"; //Number of work place / integer from 1 to 99999 /

                if (MainActivity.myFiscalDevice.isConnectedPrinter())
                    operatorPassword = getFiscalDevice().getConnectedPrinterV1().getDefaultOpPass();

                if (MainActivity.myFiscalDevice.isConnectedECR())
                    operatorPassword = getFiscalDevice().getConnectedECRV1().getDefaultOpPass();


                /**
                 *
                 *    UNP Unique sales number format:
                 * - serial number of the fiscal device
                 * - operator code (four digits or Latin characters)
                 * - sequential sales number (seven digits with leading zeros)
                 *   example: DT000600-0001-0001000
                 *
                 *  Note: DATECS FP-800 / FP-2000 / FP-650 / SK1-21F / SK1-31F/ FMP-10 / FP-550 Only!
                 *
                 *  Before the first sale, the UNP must be set at least once
                 *  if then omitted the parameter device will increment with the number
                 *  of the sale automatically.
                 *
                 */

                String unp = new cmdInfo().GetDeviceSerialNumber() + "-" +
                        String.format("%04d", Integer.parseInt(operatorCode)) + "-" + // Pad left with trailing zero
                        String.format("%07d", 1 + Integer.valueOf(fiscalReceipt.getLastDocNumber()));  //Next Document number pad left with trailing zero

                fiscalReceipt.openInvoice(operatorCode, operatorPassword, salePoint, unp); //For Internal generated use  unp="".
                //РЕГИСТРИРАНЕ (ПРОДАЖБА) НА СТОКА
                cmdReceipt.FiscalReceipt.FiscalSale testSale = new cmdReceipt.FiscalReceipt.FiscalSale();

                fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                fiscalReceipt.printFreeText("Тест продажба без параметри!", true, true, true, cmdReceipt.FiscalReceipt.FreeFiscalTextType.type32dpiA);
                testSale.add(
                        "",
                        "",
                        "Б", //А, Б, В...
                        "0.01",
                        "",
                        "",
                        cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.noCorecction,
                        "");
                testSale.saleTotal(
                        "Тотал:",
                        "",
                        cmdReceipt.PaidMode.CashPayment.getId(),//cmdReceipt.FiscalReceipt.FiscalSale.PaidMode.fromOrdinal(0).getId(),
                        ""); //We pays a full amount

                //If Client Data Info is valid
                clientInfo.printClientInfo();
                fiscalReceipt.closeFiscalReceipt();
            } else fiscalReceipt.cancel();
            binder.btnPrintInvoice.setEnabled(false);
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
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
