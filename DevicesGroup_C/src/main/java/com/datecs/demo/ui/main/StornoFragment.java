/**
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

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.datecs.demo.FragmentStorno_binding;
import com.datecs.demo.MainActivity;
import com.datecs.demo.ui.main.tools.DocTypeToRead;
import com.datecs.demo.ui.main.tools.SetTime;
import com.datecs.demo.ui.main.tools.TextViewDatePicker;
import com.datecs.demo.ui.main.tools.cmdEJStructInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt.FiscalReceipt.Storno.StornoType;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdService;
import com.datecs.testApp.R;
import java.util.ArrayList;
import java.util.List;

import static com.datecs.demo.PrinterManager.getFiscalDevice;

public class StornoFragment extends Fragment {
    private FragmentStorno_binding binder;
    private cmdInfo myInfo = new cmdInfo();
    private cmdEJStructInfo myEjStructInfo = new cmdEJStructInfo();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binder = DataBindingUtil.inflate(inflater, R.layout.fragment_storno, container, false);

        try {
            //Init Operators Spinner
            int maxOperators = MainActivity.myFiscalDevice.getConnectedModelV2().getMaxOperators();
            String[] items = new String[maxOperators];
            for (int i = 0; i < maxOperators; i++) items[i] = myInfo.GetOperName(i);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binder.spOpCode.setAdapter(adapter);

            // unp - Unique sale number (21 chars "LLDDDDDD-CCCC-DDDDDDD", L[A-Z], C[0-9A-Za-z],//D[0-9] )
            // The parameter is not required only if the original document is printed by the cashier and not by the PC program.
            //We will use the UNP of the last issued fiscal receipt

            cmdInfo.QRinfo lastReceiptInfo = myInfo.GetQRInfo();
            int lastSalesReceiptNumber = lastReceiptInfo.getNumber();
            String unp = myInfo.GetDeviceSerialNumber() + "-" +
                    String.format("%04d", binder.spOpCode.getSelectedItemId() + 1) + "-" +
                    String.format("%04d", lastSalesReceiptNumber);


            binder.edDocDate.setText(lastReceiptInfo.getDatetime().split(" ")[0]);
            binder.edDocTime.setText(lastReceiptInfo.getDatetime().split(" ")[1]);//Time

            // binder.edDocUNP.setText(unp);
            binder.edDocNum.setText(String.valueOf(lastSalesReceiptNumber));
            //Set Date time pickers
            new TextViewDatePicker(getContext(), binder.edDocDate);
            new SetTime(getContext(), binder.edDocTime);

            //Set Operator and password
            //Note: WP-500X, WP-50X, WP-25X, DP-25X, DP-150X, DP-05C: the default password for each operator is
            //equal to the corresponding number (for example, for Operator1 the password is "1") . FMP-350X, FMP-55X,
            //FP-700X: the default password for each operator is “0000”

            Integer operatorCode = Integer.valueOf(getFiscalDevice().getConnectedModelV2().getCurrentOpCode());
            //String operatorPassword = getFiscalDevice().getConnectedModelV2().getDefaultOpPass();
            String operatorPassword = myInfo.GetOperPasw(operatorCode - 1);//Password of operator. Text up to 8 symbols. ( Require Service jumper )
            binder.spOpCode.setSelection(operatorCode - 1);
            binder.edOpPwd.setText(operatorPassword);
            binder.edFMNum.setText(new cmdService().GetFiscalMemoryNumber());

        } catch (Exception e) {
            e.printStackTrace();
            postToast(e.getMessage());
        }

        return binder.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Open new Storno of Document
        binder.btnPrintStorno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String stornoDocDT = binder.edDocDate.getText().toString() + " " + binder.edDocTime.getText().toString();
                    if (binder.chbxStornoInvoice.isChecked()) {
                        new cmdReceipt.FiscalReceipt.Storno(
                                String.valueOf((int) binder.spOpCode.getSelectedItemId() + 1),
                                binder.edOpPwd.getText().toString(),
                                binder.edTillNmb.getText().toString(),
                                StornoType.values()[(int) binder.spStornoType.getSelectedItemId()],
                                binder.edDocNum.getText().toString(),
                                stornoDocDT,
                                binder.edFMNum.getText().toString(),
                                true,
                                binder.edDocNum.getText().toString(),
                                binder.edStornoReason.getText().toString(),
                                binder.edDocUNP.getText().toString()).open();
                    } else {
                        new cmdReceipt.FiscalReceipt.Storno(
                                String.valueOf((int) binder.spOpCode.getSelectedItemId() + 1),
                                binder.edOpPwd.getText().toString(),
                                binder.edTillNmb.getText().toString(),
                                StornoType.values()[(int) binder.spStornoType.getSelectedItemId()],
                                binder.edDocNum.getText().toString(),
                                stornoDocDT,
                                binder.edFMNum.getText().toString()).open();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }

        });

        //Validate fields used to Open new Storno
        binder.btnValidStorno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Boolean> isValidateOK = new ArrayList<>();
                isValidateOK = new cmdReceipt.FiscalReceipt.Storno().validateOpenStorno(
                        String.valueOf(binder.spOpCode.getSelectedItemId() + 1),//0
                        binder.edOpPwd.getText().toString().trim(),                   //1
                        binder.edTillNmb.getText().toString().trim(),                 //2
                        StornoType.values()[(int) binder.spStornoType.getSelectedItemId()],//3
                        binder.edDocNum.getText().toString().trim(),                              //4
                        binder.edDocDate.getText().toString().trim() + " " + binder.edDocTime.getText().toString().trim(),//5
                        binder.edFMNum.getText().toString().trim(),                   //6
                        binder.edDocUNP.getText().toString().trim()                   //7
                );
                markAllInvalidFieldsInRED(isValidateOK);
            }

            /**
             *  opCode, //0
             *  opPwd,  //1
             *  tillNmb, //2
             *  stornoType,//3
             *  docNumber,//4
             *  docDateTime,//5
             *  fiscalMemoryID,//6
             *  nSale//7
             *
             */

            private void markAllInvalidFieldsInRED(List<Boolean> isValidateOK) {
                int color;

                if (binder.edTillNmb.getText().toString().trim().length() == 0)
                    binder.edTillNmb.setText("???");

                if (binder.edOpPwd.getText().toString().trim().length() == 0)
                    binder.edOpPwd.setText("????");

                if (binder.edDocNum.getText().toString().trim().length() == 0)
                    binder.edDocNum.setText("???");


                if (isValidateOK.size() == 8) {
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
                    ((TextView) binder.spStornoType.getSelectedView()).setTextColor(color);


                    if (!isValidateOK.get(4)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edDocNum.setTextColor(color);

                    if (!isValidateOK.get(5)) color = Color.RED;
                    else
                        color = Color.BLACK;
                    binder.edDocDate.setTextColor(color);
                    binder.edDocTime.setTextColor(color);

                    if (!isValidateOK.get(6)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edFMNum.setTextColor(color);

                    if (!isValidateOK.get(7)) color = Color.RED;
                    else color = Color.BLACK;
                    binder.edDocUNP.setTextColor(color);

                }

            }
        });
        //Add item to storno receipt
        binder.btnAddToStorno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating Storno receipt as a fiscal receipt.
                cmdReceipt.FiscalReceipt stornoReceipt = new cmdReceipt.FiscalReceipt();

                if (stornoReceipt.isOpen()) {
                    //Creating fiscal sale (storno) in opened receipt.
                    cmdReceipt.FiscalReceipt.FiscalSale itemStorno = new cmdReceipt.FiscalReceipt.FiscalSale();
                    try {
                        itemStorno.add(
                                "Демонстрация",
                                "2", //1-A,2-B...
                                "",
                                "0.01",
                                "",
                                cmdReceipt.FiscalReceipt.FiscalSale.DiscountType.noDiscount,
                                "");

                        //TOTAL
                        stornoReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                        itemStorno.saleTotal(
                                cmdReceipt.PaymentType.cash,
                                "0.01");
                        if (binder.chbxStornoInvoice.isSelected()) {
                            new cmdReceipt.FiscalReceipt.InvoiceClientInfo(
                                    cmdReceipt.FiscalReceipt.InvoiceClientInfo.TypeTAXN.BULSTAT,
                                    "000713391",
                                    "00000000000001",
                                    "",
                                    "Datecs",
                                    "",
                                    "",
                                    "").saveClientInfo();
                        }

                        if (binder.chbxStornoInvoice.isChecked()) {
                            new cmdReceipt.FiscalReceipt.InvoiceClientInfo(
                                    cmdReceipt.FiscalReceipt.InvoiceClientInfo.TypeTAXN.BULSTAT,
                                    "000713391",
                                    "00000000000001",
                                    "",
                                    "Datecs",
                                    "",
                                    "",
                                    "").saveClientInfo();

                        }

                        //CLOSE
                        stornoReceipt.closeFiscalReceipt();

                    } catch (Exception e) {
                        e.printStackTrace();
                        postToast(e.getMessage());
                    }

                }

            }

        });

        //Search doc number in Electronic Journal
        binder.btnSearchInEJ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    cmdEJStructInfo.FiscalDocInfo recInfo = myEjStructInfo.ReadReceiptInfoFromEJ(
                            Integer.valueOf(binder.edDocNum.getText().toString()),
                            DocTypeToRead.all_types);
                    if (recInfo.isfDocData()) {
                        recInfoDialog(recInfo);
                    } else postToast("Sale Receipt not found in EJ...");
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }


    private void postToast(final String message) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    private void recInfoDialog(cmdEJStructInfo.FiscalDocInfo recInfo) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        String msg;
        if (recInfo.isOther()) msg = "The Receipt found is not a sale or invoice! \n";
        else msg = "Sale Receipt found in EJ.\n";
        if (recInfo.isAllVoid()) msg = "\nThe found document has been canceled.\n";

        if (recInfo.isInvoice()) {
            msg += "\ninvoice number:" + String.format("%010d", recInfo.getInvoiceNumber());
            msg += "\nClient EIK:" + recInfo.getClientEIK();
        }
        msg += "\nClosed on:" + recInfo.getDateTime();
        msg += "\nOperator ID:" + recInfo.getOperator();
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        binder.chbxStornoInvoice.setChecked(recInfo.isInvoice());
                        binder.spOpCode.setSelection(recInfo.getOperator() - 1);
                        binder.edDocDate.setText(recInfo.getDateTime().split(" ")[0]);
                        binder.edDocTime.setText(recInfo.getDateTime().split(" ")[1]);
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

}
