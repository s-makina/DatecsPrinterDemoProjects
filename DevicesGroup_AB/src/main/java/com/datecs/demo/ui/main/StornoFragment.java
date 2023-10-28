/**
 * This fragment provides a custom graphical user interface to edit and add items (articles) in the
 * device built-in database of articles items.
 *
 * @author Datecs Ltd. Software Department
 */
package com.datecs.demo.ui.main;


import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.datecs.demo.MainActivity;
import com.datecs.demo.StornoFrgBinding;
import com.datecs.demo.ui.main.tools.SetTime;
import com.datecs.demo.ui.main.tools.StructuredInfoRegister_DeviceGroup_A;
import com.datecs.demo.ui.main.tools.TextViewDatePicker;
import com.datecs.demo.ui.main.tools.cmdEJStructInfoA;
import com.datecs.demo.ui.main.tools.cmdEJStructInfoB;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdReceipt;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdService;
import com.datecs.testApp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.datecs.demo.PrinterManager.getFiscalDevice;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdReceipt.InvoiceClientInfo.BulstatType.EIK;


public class StornoFragment extends Fragment {
    private StornoFrgBinding binder;
    private final SimpleDateFormat stornoDateFormat = new SimpleDateFormat("ddMMyy");
    private final SimpleDateFormat stornoTimeFormat = new SimpleDateFormat("HHmmss");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binder = DataBindingUtil.inflate(inflater, R.layout.fragment_storno, container, false);

        boolean issueStornoByNum = binder.chbIssueStornoDocument.isChecked();

        initControls(issueStornoByNum);

        new TextViewDatePicker(getContext(), binder.edDocDate);
        TextViewDatePicker.setDateServerPattern("ddMMyy");     //Set data time picker
        new SetTime(getContext(), binder.edDocTime, ""); //Set Military Time Format HHMMSS

        String operatorPassword = null;
        if (MainActivity.myFiscalDevice.isConnectedPrinter())
            operatorPassword = getFiscalDevice().getConnectedPrinterV1().getDefaultOpPass();


        if (MainActivity.myFiscalDevice.isConnectedECR()) {
            //Not supported on  DP-05, DP-25, DP-35, WP-50, DP-150
            operatorPassword = getFiscalDevice().getConnectedECRV1().getDefaultOpPass();
            binder.edStornoUNP.setEnabled(false);
            binder.edStornoUNP.setVisibility(View.INVISIBLE);
            binder.chbIssueStornoDocument.setEnabled(false);
            binder.edStornoReason.setEnabled(false);
        }


        try {
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
                    String.format("%04d", binder.spOpCode.getSelectedItemId() + 1) + "-" + // Pad left with trailing zero
                    String.format("%07d", 1 + Integer.valueOf(new cmdReceipt.FiscalReceipt().getLastDocNumber()));  //Next Document number pad left with trailing zero
            binder.edStornoUNP.setText(unp);
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }

        binder.edOpPwd.setText(operatorPassword);
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binder.btnOpenPrintStorno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (binder.chbxStornoInvoice.isChecked()) {
                        invoiceStorno();
                    } else receiptStorno();

                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }

        });

        binder.btnValidStorno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Boolean> isValidateOK = new ArrayList<>();
                //Validate only fields used to Issue of Storno by Document Number
                if (binder.chbIssueStornoDocument.isChecked()) {

                    isValidateOK = new cmdReceipt.FiscalReceipt.Storno().capIssueStornoReceipt(
                            String.valueOf(
                                    binder.spOpCode.getSelectedItemId() + 1),       //0
                            binder.edOpPwd.getText().toString(),             //1
                            binder.edTillNmb.getText().toString(),           //2
                            binder.edStornoUNP.getText().toString(),         //3
                            //Get ID (E,R ot T) of StornoType by Index:
                            cmdReceipt.FiscalReceipt.Storno.StornoType.fromOrdinal((int) binder.spStornoType.getSelectedItemId()),  //4
                            binder.edDocNum.getText().toString(),             //5
                            binder.edStornoReason.getText().toString());      //6
                } else {
                    //Validate  fields used to Open new Storno
                    isValidateOK = new cmdReceipt.FiscalReceipt.Storno().capIssueStornoReceipt(

                            String.valueOf(binder.spOpCode.getSelectedItemId() + 1),      //0
                            binder.edOpPwd.getText().toString(),                   //1
                            binder.edTillNmb.getText().toString(),                 //2
                            binder.edStornoUNP.getText().toString(),               //3
                            //Get ID (E,R ot T) of StornoType by Index:
                            cmdReceipt.FiscalReceipt.Storno.StornoType.fromOrdinal((int) binder.spStornoType.getSelectedItemId()),   //4
                            binder.edDocNum.getText().toString(),                  //5
                            binder.edDocUNP.getText().toString(),                  //6
                            binder.edDocDate.getText().toString() + binder.edDocTime.getText().toString(),      //7
                            binder.edFMNum.getText().toString(),                   //8
                            binder.edStornoReason.getText().toString()             //9
                    );
                }
                markAllInvalidFieldsInRED(isValidateOK);
            }

            /**
             * @param isValidateOK
             */
            private void markAllInvalidFieldsInRED(List<Boolean> isValidateOK) {
                int color;

                if (binder.edTillNmb.getText().toString().trim().length() == 0)
                    binder.edTillNmb.setText("???");

                if (binder.edOpPwd.getText().toString().trim().length() == 0)
                    binder.edOpPwd.setText("????");

                if (binder.edDocNum.getText().toString().trim().length() == 0)
                    binder.edDocNum.setText("???");


                if (isValidateOK.size() == 7) //Case of print (issue) storno
                {
                    if (!isValidateOK.get(0)) color = Color.RED;
                    else color = Color.BLACK;
                    ((TextView) binder.spOpCode.getSelectedView()).setTextColor(color);

                    if (!isValidateOK.get(1)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edOpPwd.setTextColor(color);

                    if (!isValidateOK.get(2)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edTillNmb.setTextColor(color);

                    if (!isValidateOK.get(3)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edStornoUNP.setTextColor(color);

                    if (!isValidateOK.get(4)) color = Color.RED;
                    else color = Color.BLACK;
                    ((TextView) binder.spStornoType.getSelectedView()).setTextColor(color);
                    if (!isValidateOK.get(5)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edDocNum.setTextColor(color);

                    if (!isValidateOK.get(6)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edStornoReason.setTextColor(color);

                } else //Case of open storno
                {
                    if (binder.edDocUNP.getText().toString().trim().length() == 0)
                        binder.edDocUNP.setText("????????-???-??????");

                    if (binder.edFMNum.getText().toString().trim().length() == 0)
                        binder.edFMNum.setText("???????");

                    if (!isValidateOK.get(0)) color = Color.RED;
                    else color = Color.BLACK;
                    ((TextView) binder.spOpCode.getSelectedView()).setTextColor(color);
                    if (!isValidateOK.get(1)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edOpPwd.setTextColor(color);

                    if (!isValidateOK.get(2)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edTillNmb.setTextColor(color);

                    if (!isValidateOK.get(3)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edStornoUNP.setTextColor(color);

                    if (!isValidateOK.get(4)) color = Color.RED;
                    else color = Color.BLACK;

                    ((TextView) binder.spStornoType.getSelectedView()).setTextColor(color);

                    if (!isValidateOK.get(5)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edDocNum.setTextColor(color);

                    if (!isValidateOK.get(6)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edDocUNP.setTextColor(color);
                    if (!isValidateOK.get(7)) color = Color.RED;
                    else color = Color.BLACK;
                    {
                        binder.edDocDate.setTextColor(color);
                        binder.edDocTime.setTextColor(color);
                    }

                    if (!isValidateOK.get(8)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edFMNum.setTextColor(color);

                    if (!isValidateOK.get(9)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edStornoReason.setTextColor(color);

                }

            }
        });

        binder.btnSearchEJ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating Storno receipt as a fiscal receipt.
                cmdReceipt.FiscalReceipt stornoReceipt = new cmdReceipt.FiscalReceipt.Storno();
                try {
                    if (getFiscalDevice().isConnectedPrinter()) {
                        cmdEJStructInfoA cmd = new cmdEJStructInfoA();
                        cmdEJStructInfoA.DocInfo docInfoFound = null;
                        docInfoFound = cmd.getDocInfo(Integer.valueOf(binder.edDocNum.getText().toString()));
                        if (docInfoFound.getRecType() == null)
                            postToast("Not found in EJ Receipt :" + binder.edDocNum.getText().toString());
                        else recInfoDialog(docInfoFound);

                    }
                    if (getFiscalDevice().isConnectedECR()) {
                        cmdEJStructInfoB cmd = new cmdEJStructInfoB();
                        cmdEJStructInfoB.DocInfo docInfoFound = cmd.ReadDocInfo(Integer.parseInt(binder.edDocNum.getText().toString()));
                        if (docInfoFound == null)
                            postToast("Not found in EJ Receipt :" + binder.edDocNum.getText().toString());
                        else recInfoDialogB(docInfoFound);

                    }
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });


        binder.btnAddToStorno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating Storno receipt as a fiscal receipt.
                cmdReceipt.FiscalReceipt stornoReceipt = new cmdReceipt.FiscalReceipt.Storno();
                try {
                    if (stornoReceipt.isOpen()) {
                        //Creating fiscal sale (storno) in opened receipt.
                        cmdReceipt.FiscalReceipt.FiscalSale itemStorno = new cmdReceipt.FiscalReceipt.FiscalSale();
                        itemStorno.add(
                                "Демонстрация",
                                "Сторно на стока",
                                "Б",  //А, Б, В...
                                "0.01",
                                "",
                                "", //Note! Units is not supported on DP-05, DP-25, DP-35 , WP-50, DP-150
                                cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.noCorecction,
                                "");

                        //TOTAL
                        stornoReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                        cmdReceipt.TotalResult totalResult = itemStorno.saleTotal(
                                "Тотал:",
                                "",
                                cmdReceipt.FiscalReceipt.FiscalSale.PaidMode.fromOrdinal(0).getId(), //Cash
                                "0.01");


                        /**
                         * In case of add Items in Invoice, entering clients Info
                         Except for the first all other parameters are not required. If you have to ask
                         any parameter, all before it must be set. If a blank or unsupported parameter is left
                         empty space to fill the hand.
                         */
                        if (binder.chbxStornoInvoice.isChecked())
                            new cmdReceipt.InvoiceClientInfo("000713391", EIK, "Datecs", "", "", "", "", "").saveClientInfo();

                        //CLOSE
                        stornoReceipt.closeFiscalReceipt();

                    }
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }

        });

        binder.chbIssueStornoDocument.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                initControls(binder.chbIssueStornoDocument.isChecked());
            }
        });

        binder.chbxStornoInvoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (binder.chbxStornoInvoice.isChecked()) {
                    binder.edStornoReason.setEnabled(true);//If ECR device connect this field is disabled
                    binder.edStornoReason.setText("Credit note example");
                    binder.txtinpInvoiceNum.setVisibility(View.VISIBLE);
                } else
                    binder.txtinpInvoiceNum.setVisibility(View.GONE);
            }
        });

    }

    private void recInfoDialogB(cmdEJStructInfoB.DocInfo recInfo) {
        String title = "Document found...";

        String msg = "";
        if (recInfo.ismCanceled()) msg += "Canceled ";

        switch (recInfo.getFoundDocumentType()) {
            case notUsed:
                msg += "Error...";
                break;
            case fiscal:
                msg += "Fiscal receipt.";
                break;
            case invoice:
                msg += "Fiscal  invoice.";
                binder.edInvoiceNum.setText(String.valueOf(recInfo.getInvoiceNumber()));
                binder.chbxStornoInvoice.setChecked(true);
                break;
            case storno:
                msg += "Storno receipt.";
                break;
            case creditNote:
                msg += "Credit note.\n";
                msg += "\nOperator: " + recInfo.getOperator();
                msg += "\nInvoice number : " + recInfo.getCreditNoteNum();
                break;
            case cashIn:
                msg += "CashIn receipt";
                break;
            case cashOut:
                msg += "CashOut receipt";
                break;
            case xReport:
                msg += "X report";
                break;
            case zReport:
                msg += "Z report ";
                break;
            case nonFiscal:
                msg += "Non fiscal ";
                break;


        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        binder.spOpCode.setSelection(recInfo.getOperator() - 1);
                        binder.edDocDate.setText(recInfo.getDate().replace("-",""));
                        binder.edDocTime.setText(recInfo.getTime().replace(":",""));

                            if (!binder.chbxStornoInvoice.isChecked())
                                binder.chbxStornoInvoice.setChecked(true);
                        }


                });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();


    }

    /**
     * Analyzes the information read from the document (found in the EJ).
     * Fills in the relevant controls with data for print the correct reversal receipt.
     *
     * @param recInfo
     */
    private void recInfoDialog(cmdEJStructInfoA.DocInfo recInfo) {
        String title = "Document found...";
        if (recInfo == null) return;
        String msg = "";
        switch (recInfo.getRecType()) {
            case fiscal:
                msg = "Fiscal receipt.";
                break;
            case invoice:
                msg = "Fiscal  invoice.";
                break;
            case reversal:
                msg = "Storno receipt.";
                break;
            case credit_note:
                msg = "Storno receipt.Credit note.";
                break;
            case cancelled:
                msg = "Canceled receipt";
                break;
            case cancelled_storno:
                msg = "Canceled storno receipt.";
                break;
            case cancelled_invoice:
                msg = "Canceled invoice";
                break;
            case cancelled_credit_note:
                msg = "Cancelled credit note";
                break;
            default:
                msg = "Error";

        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Date dateEJ = null;

                        try {
                            //Format EJ date string to Storno command date string
                            dateEJ = new SimpleDateFormat("dd-MM-yyyy").parse(recInfo.getDocFinishedDate());
                        } catch (ParseException e) {
                            postToast(e.getMessage());
                            e.printStackTrace();
                        }
                        binder.edDocDate.setText(stornoDateFormat.format(dateEJ));
                        binder.edDocTime.setText(recInfo.getDocFinishedTime().replace(":", ""));
                        if (recInfo.getRecType() == StructuredInfoRegister_DeviceGroup_A.RecType.invoice) {
                            binder.edInvoiceNum.setText(String.valueOf(recInfo.getInvoiceNumber()));
                            if (!binder.chbxStornoInvoice.isChecked())
                                binder.chbxStornoInvoice.setChecked(true);
                        }

                    }
                });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    /**
     * @throws Exception
     */
    private void invoiceStorno() throws Exception {

        if (binder.chbIssueStornoDocument.isChecked()) { //Print Storno
            new cmdReceipt.FiscalReceipt.Storno().printStornoInvoice(
                    String.valueOf((int) binder.spOpCode.getSelectedItemId() + 1),
                    binder.edOpPwd.getText().toString(),
                    binder.edTillNmb.getText().toString(),
                    binder.edStornoUNP.getText().toString(),
                    cmdReceipt.FiscalReceipt.Storno.StornoType.fromOrdinal((int) binder.spStornoType.getSelectedItemId()), //E,R,T
                    binder.edDocNum.getText().toString(),
                    "", //Not mandatory
                    binder.edStornoReason.getText().toString());

        } else { //Open new Storno Document
            String stornoDocDT = binder.edDocDate.getText().toString() + binder.edDocTime.getText().toString();

            new cmdReceipt.FiscalReceipt.Storno().openStornoInvoice(
                    String.valueOf((int) binder.spOpCode.getSelectedItemId() + 1),
                    binder.edOpPwd.getText().toString(),
                    binder.edTillNmb.getText().toString(),
                    binder.edStornoUNP.getText().toString(),
                    cmdReceipt.FiscalReceipt.Storno.StornoType.fromOrdinal((int) binder.spStornoType.getSelectedItemId()), //E,R,T,
                    binder.edDocNum.getText().toString(),
                    binder.edDocUNP.getText().toString(),
                    stornoDocDT,
                    binder.edFMNum.getText().toString(),
                    binder.edInvoiceNum.getText().toString(),
                    binder.edStornoReason.getText().toString());
        }
    }

    /**
     * @throws Exception
     */
    private void receiptStorno() throws Exception {
        if (binder.chbIssueStornoDocument.isChecked()) { //Print Storno
            new cmdReceipt.FiscalReceipt.Storno(
                    String.valueOf((int) binder.spOpCode.getSelectedItemId() + 1),
                    binder.edOpPwd.getText().toString(),
                    binder.edTillNmb.getText().toString(),
                    binder.edStornoUNP.getText().toString(),
                    cmdReceipt.FiscalReceipt.Storno.StornoType.fromOrdinal((int) binder.spStornoType.getSelectedItemId()), //E,R,T
                    binder.edDocNum.getText().toString(),
                    binder.edStornoReason.getText().toString()
            ).printStornoReceipt();

        } else { //Open new Storno Document
            String stornoDocDT = binder.edDocDate.getText().toString() + binder.edDocTime.getText().toString();

            new cmdReceipt.FiscalReceipt.Storno().openStornoReceipt(
                    String.valueOf((int) binder.spOpCode.getSelectedItemId() + 1),
                    binder.edOpPwd.getText().toString(),
                    binder.edTillNmb.getText().toString(),
                    binder.edStornoUNP.getText().toString(),//Not used on  DP-05, DP-25, DP-35, WP-50, DP-150
                    cmdReceipt.FiscalReceipt.Storno.StornoType.fromOrdinal((int) binder.spStornoType.getSelectedItemId()), //E,R,T,
                    binder.edDocNum.getText().toString(),
                    binder.edDocUNP.getText().toString(),
                    stornoDocDT,
                    binder.edFMNum.getText().toString(),
                    binder.edStornoReason.getText().toString());
        }

    }

    /**
     * @param isIssueStorno
     */
    private void initControls(boolean isIssueStorno) {

        binder.edDocTime.setEnabled(!isIssueStorno); //No need for issue
        binder.edDocDate.setEnabled(!isIssueStorno); //No need for issue
        binder.edDocUNP.setEnabled(!isIssueStorno);  //No need for issue
        binder.edFMNum.setEnabled(!isIssueStorno);  //No need for issue
        binder.btnAddToStorno.setEnabled(!isIssueStorno); //No need for issue

        try {
            if (isIssueStorno) {
                binder.btnOpenPrintStorno.setText("Print...");
                binder.edDocDate.setText("");
                binder.edDocTime.setText("");
                binder.edDocUNP.setText("");
                binder.edFMNum.setText("");
            } else {
                //Unp Prefix of Searched  document
                String unpPrefix = new cmdInfo().GetDeviceSerialNumber() + "-" + String.format("%04d", binder.spOpCode.getSelectedItemId() + 1) + "-0000000";

                binder.btnOpenPrintStorno.setText("Open...");
                binder.edFMNum.setText(new cmdService().GetFiscalMemoryNumber());
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
                String currentDate = sdf.format(new Date());
                binder.edDocDate.setText(currentDate);
                sdf = new SimpleDateFormat("HHmmss");
                String currentTime = sdf.format(new Date());
                binder.edDocTime.setText(currentTime);
                binder.edDocUNP.setText(unpPrefix);
            }
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }

    }


    private void postToast(final String message) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
