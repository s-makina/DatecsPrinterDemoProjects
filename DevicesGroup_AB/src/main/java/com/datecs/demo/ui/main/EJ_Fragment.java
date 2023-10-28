/**
 * This fragment provides a custom graphical user interface for document search in the device's
 * electronic journal (EJ).
 * Search can be done by document number by date-time
 * and the results can be filtered by document type.
 * <p>
 * The following methods have been demonstrated as part of the Datecs Java SDK:
 *
 * @author Datecs Ltd. Software Department
 */

package com.datecs.demo.ui.main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.datecs.demo.EJFrgBinding;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.datecs.demo.MainActivity;
import com.datecs.demo.ui.main.tools.SetTime;
import com.datecs.demo.ui.main.tools.TextViewDatePicker;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdEJournal;
import com.datecs.testApp.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EJ_Fragment extends Fragment {
    private EJFrgBinding binder;
    private ProgressDialog progress;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.ej_fragment, container, false);
        try {
            //Graphical interface configurations depending on the device and its functional capabilities
            String[] ejDocTypeArray = null;
            if (MainActivity.myFiscalDevice.isConnectedECR()) {

                binder.chbKLrangeByZ.setText(R.string.by_z_report_number);
                //Populate the spinners with the correct type of documents
                ejDocTypeArray = getResources().getStringArray(R.array.array_ej_reports_ecr);
                binder.chbCondensedFont.setVisibility(View.INVISIBLE); // Not supported option
            }
            if (MainActivity.myFiscalDevice.isConnectedPrinter()) {
                binder.tinpToZNum.setVisibility(View.INVISIBLE);
                binder.chbKLrangeByZ.setText(R.string.in_Z_number);
                //Populate the spinners with the correct type of documents
                ejDocTypeArray = getResources().getStringArray(R.array.array_ej_reports_fp);
            }

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, ejDocTypeArray);

            binder.spKLdocType.setAdapter(spinnerArrayAdapter);
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
            String currentDate = sdf.format(new Date());
            binder.edKLstartDate.setText(currentDate);
            binder.edKLtoDate.setText(currentDate);
            sdf = new SimpleDateFormat("HHmmss");
            String currentTime = sdf.format(new Date());
            binder.edKLstartTime.setText(currentTime);
            binder.edKLtoTime.setText(currentTime);

        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final cmdEJournal myEJournal = new cmdEJournal();

        TextViewDatePicker datePickerFrom = new TextViewDatePicker(getContext(), binder.edKLstartDate);
        TextViewDatePicker datePickerTo = new TextViewDatePicker(getContext(), binder.edKLtoDate);
        datePickerFrom.setDateServerPattern("ddMMyy");
        datePickerTo.setDateServerPattern("ddMMyy");
        new SetTime(getContext(), binder.edKLstartTime, "");// Set time picker to format HHMMSS
        new SetTime(getContext(), binder.edKLtoTime, ""); //Set time picker to format HHMMSS


        binder.btnReadEJ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (!checkRange()) return;
                myEJournal.setUserBreak(false); //Pri povtoren print
                progress = new ProgressDialog(getContext());
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setIndeterminate(false);
                progress.setCancelable(false);
                progress.setButton(DialogInterface.BUTTON_NEGATIVE,
                        getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myEJournal.setUserBreak(true);
                                dialog.dismiss();
                            }
                        });
                final boolean isSearchByNumber = binder.chbKLrangeBynum.isChecked();
                final boolean isSearchInZreports = binder.chbKLrangeByZ.isChecked();
                if (isSearchByNumber) progress.setTitle(R.string.title_reading_documents_by_num);
                else progress.setTitle(R.string.title_reading_documents_by_dat);
                progress.show();

                final String[] readedDocsList = new String[1];
                readedDocsList[0] = "";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {   // FP-800 / FP-2000 / FP-650 / SK1-21F / SK1-31F/ FMP-10 / FP-550
                            if (MainActivity.myFiscalDevice.isConnectedPrinter())
                                if (isSearchByNumber) //Search by number
                                {

                                    if (isSearchInZreports)
                                        readedDocsList[0] = myEJournal.readEjDocumentsInZReport(
                                                binder.edEJFromNum.getText().toString(),
                                                binder.edEJToNum.getText().toString(),
                                                binder.edEJFromZ.getText().toString(),
                                                cmdEJournal.EjDocTypePrn.fromOrdinal((int) binder.spKLdocType.getSelectedItemId())

                                        );
                                    else {
                                        readedDocsList[0] = myEJournal.readEjDocumentsByNumbersRange(
                                                binder.edEJFromNum.getText().toString(),
                                                binder.edEJToNum.getText().toString(),
                                                cmdEJournal.EjDocTypePrn.fromOrdinal((int) binder.spKLdocType.getSelectedItemId())
                                        );
                                    }

                                } else //..by DateTime
                                {
                                    readedDocsList[0] = myEJournal.readEjDocumentsByDateRange(
                                            binder.edKLstartDate.getText().toString() + binder.edKLstartTime.getText().toString(),
                                            binder.edKLtoDate.getText().toString() + binder.edKLtoTime.getText().toString(),
                                            cmdEJournal.EjDocTypePrn.fromOrdinal((int) binder.spKLdocType.getSelectedItemId()));
                                }

                            //else // DP-05, DP-25, DP-35, WP-50, DP-150
                            if (MainActivity.myFiscalDevice.isConnectedECR()) {
                                if (isSearchByNumber) //Search by number
                                {
                                    if(isSearchInZreports)
                                    {
                                        //Search for documents by day order number 1, 2,3 ... in Z reports range
                                        readedDocsList[0] = myEJournal.readEjDocumentsInZReports(
                                                binder.edEJFromNum.getText().toString(),
                                                binder.edEJToNum.getText().toString(),
                                                binder.edEJFromZ.getText().toString(),
                                                binder.edEJToZ.getText().toString(),
                                                String.valueOf(binder.spKLdocType.getSelectedItemId())); //0,1,2...8,9);

                                    }else //by Number range
                                    readedDocsList[0] = myEJournal.readEjDocumentsByNumbersRange(
                                            String.valueOf(binder.spKLdocType.getSelectedItemId()), //0,1,2...8,9
                                            binder.edEJFromNum.getText().toString(),
                                            binder.edEJToNum.getText().toString());

                                } else //..by DateTime
                                {   //Use overwrite version of readEjDocumentsByDateRange for ECR !
                                    readedDocsList[0] = myEJournal.readEjDocumentsByDateRange(
                                            String.valueOf(binder.spKLdocType.getSelectedItemId()),
                                            binder.edKLstartDate.getText().toString() + binder.edKLstartTime.getText().toString(),
                                            binder.edKLtoDate.getText().toString() + binder.edKLtoTime.getText().toString());
                                }

                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                            return;
                        } finally {
                            progress.dismiss();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binder.edEjTextMonitor.setText("");
                                binder.edEjTextMonitor.setMaxLines(readedDocsList[0].length());
                                binder.edEjTextMonitor.setText(readedDocsList[0]);
                                String toastText = getString(R.string.msg_lines_received) + String.valueOf(binder.edEjTextMonitor.getLineCount());
                                Snackbar.make(getView(), toastText, Snackbar.LENGTH_LONG).setAction("Action", null).show();

                            }
                        });
                    }
                }).start();

            }
        });

        binder.btnEJInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    cmdEJournal.InfoEJ infoEJ = myEJournal.readEjInfo();
                    binder.edEjTextMonitor.setMaxLines(112);
                    String textInfo = "";
                    if (MainActivity.myFiscalDevice.isConnectedECR()) {
                        textInfo = "EJ Id number for the device......:" + infoEJ.getNumber() + "\n\r" +
                                "Date and time of EJ activation:" + infoEJ.getDateTime() + "\n\r";
                    }
                    textInfo += "Size of the EJ......:" + infoEJ.getSize() + " MB\n\r" +
                            "Used......:" + infoEJ.getUsed() + " MB\n\r" +
                            "First Z report number......:" + infoEJ.getFromZ() + "\n\r" +
                            "Last Z report number......:" + infoEJ.getToZ() + "\n\r" +
                            "First Document number......:" + infoEJ.getFromDoc() + "\n\r" +
                            "Last Document number......:" + infoEJ.getToDoc() + "\n\r";

                    binder.edEjTextMonitor.setText(textInfo);

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
                    int msgText ;
                   if (myEJournal.validateEj()) msgText =R.string.ej_is_valid;
                           else   msgText =R.string.ej_is_not_valid;

                    Snackbar.make(getView(), msgText, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });
        binder.btnPrintDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final boolean isPrintByNumber = binder.chbKLrangeBynum.isChecked();
                final boolean isSearchInZreports = binder.chbKLrangeByZ.isChecked();
                final int[] printedCount = {0};

                myEJournal.setUserBreak(false); //Pri povtoren print
                progress = new ProgressDialog(getContext());
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setIndeterminate(false);
                progress.setCancelable(false);
                progress.setButton(DialogInterface.BUTTON_NEGATIVE,
                        getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myEJournal.setUserBreak(true);
                                dialog.dismiss();
                            }
                        });


                if (isPrintByNumber) progress.setTitle(R.string.title_printing_documents_by_num);
                else progress.setTitle(R.string.title_printing_documents_by_dat);
                progress.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            // FP-800 / FP-2000 / FP-650 / SK1-21F / SK1-31F/ FMP-10 / FP-550
                            if (MainActivity.myFiscalDevice.isConnectedPrinter())
                            if (isPrintByNumber) //Search by number
                            {

                                if (!isSearchInZreports)
                                    myEJournal.printEjDocumentsByNumbersRange(
                                            binder.chbCondensedFont.isChecked(),
                                            binder.edEJFromNum.getText().toString(),
                                            binder.edEJToNum.getText().toString(),
                                            cmdEJournal.EjDocTypePrn.fromOrdinal((int) binder.spKLdocType.getSelectedItemId()));
                                else {
                                    myEJournal.printEjDocumentsInZReport(
                                            binder.chbCondensedFont.isChecked(),
                                            binder.edEJFromNum.getText().toString(),
                                            binder.edEJToNum.getText().toString(),
                                            binder.edEJFromZ.getText().toString(),
                                            cmdEJournal.EjDocTypePrn.fromOrdinal((int) binder.spKLdocType.getSelectedItemId()));
                                }

                            } else //Print by Date
                            {
                                myEJournal.printEjDocumentsByDateRange(
                                        binder.chbCondensedFont.isChecked(),
                                        binder.edKLstartDate.getText().toString() + binder.edKLstartTime.getText().toString(),
                                        binder.edKLtoDate.getText().toString() + binder.edKLtoTime.getText().toString(),
                                        cmdEJournal.EjDocTypePrn.fromOrdinal((int) binder.spKLdocType.getSelectedItemId()));
                            }

                            //else // DP-05, DP-25, DP-35, WP-50, DP-150
                            if (MainActivity.myFiscalDevice.isConnectedECR()) {
                                if (isPrintByNumber) //Print by number
                                {
                                    if(isSearchInZreports)
                                    {
                                        //Search for documents by day order number 1, 2,3 ... in Z reports range
                                        printedCount[0] = myEJournal.printEjDocumentsInZReportRange(
                                                binder.edEJFromNum.getText().toString(),
                                                binder.edEJToNum.getText().toString(),
                                                binder.edEJFromZ.getText().toString(),
                                                binder.edEJToZ.getText().toString(),
                                                String.valueOf(binder.spKLdocType.getSelectedItemId())); //0,1,2...8);

                                    }else
                                    printedCount[0] = myEJournal.printEjDocumentsByNumbersRange(
                                            String.valueOf(binder.spKLdocType.getSelectedItemId()), //0,1,2...8
                                            binder.edEJFromNum.getText().toString(),
                                            binder.edEJToNum.getText().toString());

                                } else //..by DateTime
                                {   //Use overwrite version of printEjDocumentsByDateRange for ECR !
                                    printedCount[0] = myEJournal.printEjDocumentsByDateRange(
                                            String.valueOf(binder.spKLdocType.getSelectedItemId()),
                                            binder.edKLstartDate.getText().toString() + binder.edKLstartTime.getText().toString(),
                                            binder.edKLtoDate.getText().toString() + binder.edKLtoTime.getText().toString());
                                }

                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                            return;
                        } finally {
                            progress.dismiss();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(getView(), R.string.msg_doc_printed , Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            }
                        });
                    }
                }).start();

            }
        });

        binder.chbKLrangeByZ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isZRange = binder.chbKLrangeByZ.isChecked();
                binder.edEJFromZ.setEnabled(isZRange);
                binder.edEJToZ.setEnabled(isZRange);

                // Set range of Z reports, Document
                //The search for documents is for the current report, Doc 1, 2,3 ... and so on
                if(MainActivity.myFiscalDevice.isConnectedECR())
                {
                    try {
                     cmdEJournal.InfoEJ nfo= myEJournal.readEjInfo();
                        binder.edEJFromZ.setText(nfo.getFromZ());
                        binder.edEJToZ.setText(nfo.getToZ());
                        binder.edEJFromNum.setText("1");
                        binder.edEJToNum.setText("1");
                    } catch (Exception e) {
                        postToast(e.getMessage());
                        e.printStackTrace();
                    }

                }


             }

        });

        binder.chbKLrangeBynum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isNumberRange = binder.chbKLrangeBynum.isChecked();
                boolean isZRange = binder.chbKLrangeByZ.isChecked();
                //Setup control enable
                binder.chbKLrangeByZ.setEnabled(isNumberRange);
                binder.edKLstartDate.setEnabled(!isNumberRange);
                binder.edKLstartTime.setEnabled(!isNumberRange);
                binder.edKLtoDate.setEnabled(!isNumberRange);
                binder.edKLtoTime.setEnabled(!isNumberRange);

                binder.edEJFromNum.setEnabled(isNumberRange);
                binder.edEJToNum.setEnabled(isNumberRange);
                binder.edEJFromZ.setEnabled(isNumberRange && isZRange);
                binder.edEJToZ.setEnabled(isNumberRange && isZRange);


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

