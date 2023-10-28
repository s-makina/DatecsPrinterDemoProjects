package com.datecs.demo.ui.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.datecs.fiscalprinter.SDK.FiscalException;
import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdClients;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdConfig;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdPinpad;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt;
import com.datecs.testApp.R;

import java.io.IOException;

import static com.datecs.fiscalprinter.SDK.FiscalErrorCodesV2.ERR_PINPAD_FP_TRANS;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt.PaymentType.debit_card;


public class PinpadFragment extends Fragment {

    private ProgressDialog progress;
    private cmdPinpad myPinpad = new cmdPinpad();
    private PinpadFragmentbinding binder;
    private cmdReceipt.FiscalReceipt fiscalReceipt = new cmdReceipt.FiscalReceipt();
    private cmdReceipt.FiscalSale testSale = new cmdReceipt.FiscalSale();
    private cmdReceipt.NonFiscalReceipt noFiscalReceipt = new cmdReceipt.NonFiscalReceipt();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.pinpad_fragment, container, false);
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            //Init
            binder.spPaymentType.setSelection(2);//Debit card
            binder.spPinpadReceiptInfo.setSelection(myPinpad.getPinpadReceiptInfo());
            binder.spPinpadNumberOfCopies.setSelection(myPinpad.getPinpadReceiptCopies());
            binder.chbxShortReceiptPP.setChecked(myPinpad.getPinpadShortRec());
            binder.tvSTL.setText(String.format("%.2f", new cmdReceipt.FiscalTransaction().getNotPaid()));

            ArrayAdapter<String> adapter;
            switch (myPinpad.getPinpadType()) {
                case BORICA:
                    adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"7  - Return with money", "13 - Return with points"});
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binder.spPaymentToVoid.setAdapter(adapter);
                    binder.edVoidAC.setEnabled(true);
                    binder.edVoidRRN.setEnabled(true);
                    break;
                case UBB:
                    adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"16 - Return with AC number", "17 - Return with receipt number"});
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binder.spPaymentToVoid.setAdapter(adapter);
                    binder.edVoidAC.setEnabled(false);
                    binder.edVoidRRN.setEnabled(false);
                    break;

                case DSK:
                    adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"16 - Return with money"});
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binder.spPaymentToVoid.setAdapter(adapter);
                    binder.edVoidAC.setEnabled(false);
                    binder.edVoidRRN.setEnabled(false);
                    break;
            }


        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }

        binder.btnSale001.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = ProgressDialog.show(getContext(), "", getString(R.string.msg_please_wait), true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String salePoint = "1"; //Number of point of sale from 1...99999;
                        String NSale; //Unique sale number (21 chars "LLDDDDDD-CCCC-DDDDDDD"
                        try {
                            fiscalReceipt = new cmdReceipt.FiscalReceipt();
                            if (!fiscalReceipt.isOpen()) { //Open new fiscal receipt
                                //Note: WP-500X, WP-50X, WP-25X, DP-25X, DP-150X, DP-05C: the default password for each operator is
                                //equal to the corresponding number (for example, for Operator1 the password is "1") .
                                //FMP-350X, FMP-55X, FP-700X: the default password for each operator is “0000”
                                int operatorCode = Integer.parseInt(DatecsFiscalDevice.getConnectedModelV2().getCurrentOpCode());
                                if (operatorCode < 1) operatorCode = 1;
                                //String operatorPassword =  myFiscalDevice.getConnectedModelV2().getDefaultOpPass();
                                //Password of operator. Text up to 8 symbols. ( Require Service jumper )
                                String operatorPassword = new cmdInfo().GetOperPasw(operatorCode - 1);
                                //Open Fiscal bon in current receipt
                                //Creating the unique sale number "LLDDDDDD-CCCC-DDDDDDD"
                                NSale = new com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdInfo().GetDeviceSerialNumber() + "-" +
                                        String.format("%04d", operatorCode) + "-" + // Pad left with trailing zero
                                        String.format("%07d", Integer.parseInt(fiscalReceipt.getAllreceipt()));  //Next Document number pad left with trailing zero

                                //Open Fiscal bon in current receipt and return number of receipt
                                fiscalReceipt.openFiscalReceipt(
                                        String.valueOf(operatorCode),
                                        operatorPassword,
                                        NSale,
                                        salePoint,
                                        false);
                            }
                            //Registration of item for sale with the minimum required set of parameters
                            testSale.add("Прясно мляко", "2", "1", "0.01", "1", cmdReceipt.FiscalSale.DiscountType.noDiscount, "");
                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                        } finally {
                            progress.dismiss();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    binder.tvSTL.setText(String.valueOf(testSale.readSubtotal(false, cmdReceipt.FiscalSale.DiscountType.noDiscount, "0.00")));
                                    binder.edAmount.setText(binder.tvSTL.getText());
                                } catch (Exception e) {
                                    postToast(e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        binder.btnPinpadTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = ProgressDialog.show(getContext(), getString(R.string.msg_connect_to_device), getString(R.string.msg_please_wait), true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            //'9'  - Check connection with Pinpad;
                            if (myPinpad.checkPinpadConnection())
                                postToast("Pinpad is connected ...");

                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                        } finally {
                            progress.dismiss();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                    }
                }).start();
            }
        });

        binder.btnPinpadTestTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = ProgressDialog.show(getContext(), getString(R.string.msg_transaction), getString(R.string.msg_please_wait), true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            //'10' - Check connection with server;
                            if (myPinpad.CheckPinpadServerConnection())
                                postToast("Bank server is OK !");

                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                        } finally {
                            progress.dismiss();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                    }
                }).start();
            }
        });

        binder.btnPinpadVoid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = ProgressDialog.show(getContext(), getString(R.string.msg_void_transaction), getString(R.string.msg_please_wait), true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int PayTypeIndex;
                        // do the thing that takes a long time
                        try {
                            switch (myPinpad.getPinpadType()) {
                                case BORICA:
                                    PayTypeIndex = (7 + 6 * (int) binder.spPaymentToVoid.getSelectedItemId());
                                    myPinpad.voidTransactionBorica(
                                            String.valueOf(PayTypeIndex),
                                            Double.valueOf(binder.edVoidAmount.getText().toString()),
                                            binder.edVoidRRN.getText().toString(),
                                            binder.edVoidAC.getText().toString());
                                    break;
                                case UBB:
                                    PayTypeIndex = 16 + (int) binder.spPaymentToVoid.getSelectedItemId();
                                    myPinpad.voidTransactionUBB(String.valueOf(PayTypeIndex), Double.valueOf(binder.edVoidAmount.getText().toString()), "");
                                    break;
                                case DSK:
                                    myPinpad.voidTransactionDSK(Double.valueOf(binder.edVoidAmount.getText().toString()));
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                        } finally {
                            progress.dismiss();
                        }
                    /*    getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                postToast("OK !");
                            }
                        });*/
                    }
                }).start();
            }
        });

        binder.btnCloseAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    tryToCloseFiscalReceipt();
                    binder.tvSTL.setText(String.format("%.2f", new cmdReceipt.FiscalTransaction().getNotPaid()));
                    binder.edAmount.setText(binder.tvSTL.getText());
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });


        binder.btnPinpadConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    DialogPinpadConfig d = new DialogPinpadConfig(getActivity());
                    d.show();
                    d.setTitle("Pinpad config...");
                    d.show();
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int width = (int) ((int) displaymetrics.widthPixels * 0.85);
                    int height = (int) ((int) displaymetrics.heightPixels * 0.75);
                    d.getWindow().setLayout(width, height);

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        binder.btnPinpadSetDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sQuestion = new StringBuilder("The pinpad time will be changed?\n\r");
                final String[] sDeviceDT = new String[1];


                progress = ProgressDialog.show(getContext(), getString(R.string.msg_void_transaction), getString(R.string.msg_please_wait), true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            sDeviceDT[0] = new cmdConfig.DateTime().getDateTime();
                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                        } finally {
                            progress.dismiss();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                sQuestion.append("Fiscal device time: " + sDeviceDT[0]);
                                sQuestion.append("\nAre you sure you want to synchronize pinpad clock ?");
                                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                                alertDialog.setTitle("Synchronize the pinpad date and time...");
                                alertDialog.setMessage(sQuestion);
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    myPinpad.setPinpadDateTime(new cmdConfig.DateTime().getDateTime());
                                                    postToast("Pinpad data  time was changed...");
                                                    dialog.dismiss();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    postToast(e.getMessage());
                                                }
                                            }
                                        });
                                alertDialog.show();
                            }
                        });
                    }
                }).start();
            }
        });

        binder.btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = ProgressDialog.show(getContext(), getString(R.string.msg_transaction), getString(R.string.msg_please_wait), true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            if (binder.chkbxMakeSaleFromPinpad.isChecked()) {
                                makeSaleFromPinpad();
                                return;
                            }
                            testSale.saleTotal(
                                    cmdReceipt.PaymentType.values()[(int) binder.spPaymentType.getSelectedItemId()],
                                    binder.edAmount.getText().toString());

                            if (cmdReceipt.PaymentType.values()[(int) binder.spPaymentType.getSelectedItemId()] == debit_card) {
                                printAndClose();
                            } else fiscalReceipt.closeFiscalReceipt();

                        } catch (Exception e) {
                            if (e.getMessage().contains(String.valueOf(ERR_PINPAD_FP_TRANS))) //ERR_PINPAD_FP_TRANS = -111560; // Correct 111558
                                returnTransaction(e.getMessage());
                            e.printStackTrace();
                            postToast(e.getMessage());
                        } finally {
                            progress.dismiss();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    binder.tvSTL.setText(String.format("%.2f", new cmdReceipt.FiscalTransaction().getNotPaid()));
                                    binder.edAmount.setText(binder.tvSTL.getText());
                                    progress.dismiss();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    postToast(e.getMessage());
                                }
                            }
                        });

                    }
                }).start();
            }
        });

        binder.btnPrintCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = ProgressDialog.show(getContext(), getString(R.string.msg_transaction), getString(R.string.msg_please_wait), true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            myPinpad.printCopyTransactionDocument(
                                    cmdPinpad.DocTypeToCopy.values()[(int) binder.spCopyPpDoc.getSelectedItemId()],
                                    binder.edDocToCopyNumber.getText().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                        } finally {
                            progress.dismiss();
                        }
                    }
                }).start();
            }
        });

        binder.btnPinpadEndOfDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = ProgressDialog.show(getContext(), getString(R.string.msg_transaction), getString(R.string.msg_please_wait), true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            myPinpad.endOfDayReport();
                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                        } finally {
                            progress.dismiss();
                        }
                    }
                }).start();
            }
        });
        binder.spCopyPpDoc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                binder.edDocToCopyNumber.setEnabled(position > 0);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binder.spPinpadNumberOfCopies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                try {
                    myPinpad.setPinpadReceiptCopies(position);
                } catch (Exception exception) {
                    postToast(exception.getMessage());
                    exception.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binder.chbxShortReceiptPP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    myPinpad.setPinpadShortRec(binder.chbxShortReceiptPP.isChecked());
                } catch (Exception exception) {
                    postToast(exception.getMessage());
                    exception.printStackTrace();
                }
            }
        });
        binder.chkbxMakeSaleFromPinpad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binder.btnSale001.setEnabled(!binder.chkbxMakeSaleFromPinpad.isChecked());
                binder.spPaymentType.setEnabled(!binder.chkbxMakeSaleFromPinpad.isChecked());
                binder.btnCloseAll.setEnabled(!binder.chkbxMakeSaleFromPinpad.isChecked());
            }
        });

        binder.spPinpadReceiptInfo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                try {
                    myPinpad.setPinpadReceiptInfo(position);
                } catch (Exception exception) {
                    postToast(exception.getMessage());
                    exception.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    /**
     * @throws IOException
     * @throws FiscalException
     */
    private void makeSaleFromPinpad() throws IOException, FiscalException {

        final StringBuilder[] message = new StringBuilder[1];

        new Thread(new Runnable() {
            @Override
            public void run() {
                // do the thing that takes a long time
                cmdPinpad.PurchaseResult res = null;
                try {
                    Double amount = Double.valueOf(binder.edAmount.getText().toString());
                    res = myPinpad.makePurchase(amount);
                    message[0] = new StringBuilder();
                    message[0].append("Authorization code for transaction :" + res.getAC() + "\n\r");
                    message[0].append("Type of card payment :" + res.getTransType() + "\n\r");
                    message[0].append("Card number :" + res.getCardNumber() + "\n\r");
                    message[0].append("Merchant ID :" + res.getMIDNumber() + "\n\r");
                    message[0].append("RRN number for transaction :" + res.getRRN() + "\n\r");
                    message[0].append("Terminal ID :" + res.getTIDNumber() + "\n\r");
                    message[0].append("Transaction date time :" + res.getTransDate() + " " + res.getTransTime() + "\n\r");
                    message[0].append("Transaction number :" + res.getTransNumber() + "\n\r");
                    message[0].append("Transaction status :" + res.getTransStatus() + "\n\r");
                    message[0].append("Transaction type :" + res.getTransType() + "\n\r");
                    message[0].append("Complete response code :" + res.getFulLResponseCode() + "\n\r");
                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                } finally {
                    progress.dismiss();
                }
                cmdPinpad.PurchaseResult finalRes = res;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                        alertDialog.setTitle("Alert");
                        alertDialog.setMessage(message[0]);
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        binder.edDocToCopyNumber.setText(finalRes.getTransNumber());
                                        binder.spCopyPpDoc.setSelection(3);

                                        //Set to void this document
                                        binder.edVoidAC.setText(finalRes.getAC());
                                        binder.edVoidAmount.setText(finalRes.getTransAmount());
                                        binder.edVoidRRN.setText(finalRes.getRRN());
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                });
            }
        }).start();
    }

    /**
     * If the payment was successful,
     * the relevant fiscal and the transaction receipt are printed.
     * The fiscal document for the purchase is closed.
     *
     * @throws Exception
     */
    private void printAndClose() throws Exception {
        if (binder.chbxPrintOriginalReceiptPP.isChecked())
            myPinpad.printTransactionReceipt();
        fiscalReceipt.closeFiscalReceipt();
        if (binder.chbxPrintCopiesReceiptPP.isChecked())
            myPinpad.printTransactionReceipt(); //Print Copies
    }

    /**
     * This method attempts to refuse a fiscal or non-fiscal receipt.
     * And if there a startup payment on the amount and it is not fully paid,
     * it issues a payment message.
     */
    private void tryToCloseFiscalReceipt() throws Exception {

        if (noFiscalReceipt.isOpen()) {
            noFiscalReceipt.closeNonFiscalReceipt();
            return;
        }

        if (fiscalReceipt.isOpen()) {
            final Double owedSum = new cmdReceipt.FiscalTransaction().getNotPaid();//owedSum=Amount-Tender
            Double payedSum = new cmdReceipt.FiscalTransaction().getPaid();
            //If a TOTAL in the opened receipt has not been set, it will be canceled
            if (payedSum == 0.0) {
                fiscalReceipt.cancel();
                return;
            }

            //If a TOTAL  set with a partial payment, there is a Amount and Tender is positive.
            //Offer payment of the amount and completion of the sale.
            if (owedSum > 0.0) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("Cancel opened receipt.");
                String sQuestion = String.format("Cannot cancel receipt, payment has already started.\n\r" +
                        "Do you want to pay the owed sum: %2.2f -and close it?", owedSum);
                dialog.setMessage(sQuestion);
                dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            //TOTAL
                            new cmdReceipt.FiscalSale().saleTotal(
                                    cmdReceipt.FiscalSale.PaymentType.cash,
                                    "0.0"    //Whole sum
                            );
                            fiscalReceipt.closeFiscalReceipt();
                        } catch (Exception e) {
                            postToast(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
                //The operator decides on his own what to do with the unpaid receipts !
                // So if the answer is NO, the receipt is not closed
                dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            fiscalReceipt.cancel();
                        } catch (Exception e) {
                            postToast(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
                dialog.show();
            } else {
                //If a TOTAL is set with a full payment, there is a Amount-Tender=0.
                //All is OK, completion of the sale!
                fiscalReceipt.closeFiscalReceipt();
            }
        }

    }

    /**
     * Used when command 53( paying with pinpad ) and command 55 ( option 14 )
     * returns error along with sum and last digits of card number
     *
     * @param message
     */
    private void returnTransaction(String message) {

        String sQuestion = String.format(
                message + "\n\r"
                        + "Pinpad error: Unknown result of the transaction between fiscal device and PinPad\n\r" +
                        "YES - Print receipt \n\r" +
                        "No  - VOID Transaction\n\r" +
                        "Cancel - Exit operation\n\r");
        try {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Transaction error...");
            dialog.setMessage(sQuestion);
            dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        myPinpad.returnTransaction(true);
                    } catch (Exception e) {
                        postToast(e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

            dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        myPinpad.returnTransaction(false);
                    } catch (Exception e) {
                        postToast(e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            dialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
            returnTransaction(message); // case of unsuccessful void of transaction
        }

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