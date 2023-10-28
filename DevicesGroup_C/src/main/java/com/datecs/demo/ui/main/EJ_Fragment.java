/**
 * This fragment provides a custom graphical user interface for document search in the device's
 * electronic journal (EJ).
 * Search can be done by document number by date-time
 * and the results can be filtered by document type.
 *
 * @author Datecs Ltd. Software Department
 */
package com.datecs.demo.ui.main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.datecs.demo.EjFrgBinding;
import com.google.android.material.snackbar.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import com.datecs.demo.ui.main.tools.SetTime;
import com.datecs.demo.ui.main.tools.TextViewDatePicker;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdEJournal;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdInfo;
import com.datecs.testApp.R;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EJ_Fragment extends Fragment {
    private EjFrgBinding binder;
    private ProgressDialog progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.ej_fragment, container, false);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
            String currentDate = sdf.format(new Date());
            binder.edKLstartDate.setText(currentDate);
            binder.edKLtoDate.setText(currentDate);
            sdf = new SimpleDateFormat("HH:mm:ss");
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
        new TextViewDatePicker(getContext(), binder.edKLstartDate).setDateServerPattern("dd-MM-yy");
        TextViewDatePicker datePickerTo = new TextViewDatePicker(getContext(), binder.edKLtoDate);
        new SetTime(getContext(), binder.edKLstartTime);
        new SetTime(getContext(), binder.edKLtoTime);


        binder.btnReadEJ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checkRange()) return;
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

                final Boolean byNum = binder.chbKLrangeBynum.isChecked();
                if (byNum) progress.setTitle(R.string.title_reading_documents_by_num);
                else progress.setTitle(R.string.title_reading_documents_by_dat);
                progress.show();
                final String[] toastText = {"0"};
                final int[] receivedCnt = new int[1];
                final String[] readedDocsList = new String[1];
                readedDocsList[0] = "";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            if (byNum) {
                                receivedCnt[0] = myEJournal.get_Documents_ByNumbersRange(
                                        cmdEJournal.EjDocumentType.fromOrdinal((int) binder.spKLdocType.getSelectedItemId()),
                                        setVarsByNumbers(),
                                        readedDocsList);
                            } else {
                                cmdEJournal.EJ_ParamRange docRange = myEJournal.set_SearchDocuments_ByDateRange(
                                        cmdEJournal.EjDocumentType.fromOrdinal((int) binder.spKLdocType.getSelectedItemId()),
                                        setVarsByDate());
                                receivedCnt[0] = myEJournal.get_Documents_ByNumbersRange(
                                        cmdEJournal.EjDocumentType.fromOrdinal((int) binder.spKLdocType.getSelectedItemId()),
                                        docRange,
                                        readedDocsList);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                        } finally {
                            progress.dismiss();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binder.edEjTextMonitor.setText("");
                                binder.edEjTextMonitor.setMaxLines(readedDocsList[0].length());
                                binder.edEjTextMonitor.setText(readedDocsList[0]);
                                toastText[0] = getString(R.string.msg_doc_received) + String.valueOf(receivedCnt[0]);
                                Snackbar.make(getView(), toastText[0], Snackbar.LENGTH_LONG).setAction("Action", null).show();

                            }
                        });
                    }
                }).start();

            }
        });


        binder.btnPrintDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                final Boolean byNum = binder.chbKLrangeBynum.isChecked();
                if (byNum) progress.setTitle(R.string.title_printing_documents_by_num);
                else progress.setTitle(R.string.title_printing_documents_by_dat);
                progress.show();
                final String[] toastText = {"0"};
                final int[] receivedCnt = new int[1];

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            if (byNum) {
                                receivedCnt[0] = myEJournal.print_Documents_ByNumbersRange(
                                        cmdEJournal.EjDocumentType.fromOrdinal((int) binder.spKLdocType.getSelectedItemId()),
                                        setVarsByNumbers());
                            } else {
                                cmdEJournal.EJ_ParamRange docRange = myEJournal.set_SearchDocuments_ByDateRange(
                                        cmdEJournal.EjDocumentType.fromOrdinal((int) binder.spKLdocType.getSelectedItemId()),
                                        setVarsByDate());
                                receivedCnt[0] = myEJournal.print_Documents_ByNumbersRange(
                                        cmdEJournal.EjDocumentType.fromOrdinal((int) binder.spKLdocType.getSelectedItemId()),
                                        docRange);
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
                                toastText[0] = getString(R.string.msg_doc_printed) + String.valueOf(receivedCnt[0]);
                                Snackbar.make(getView(), toastText[0], Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            }
                        });
                    }
                }).start();

            }
        });


        binder.spKLdocType.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        try {
                            binder.edKLtoNo.setText(new cmdInfo().GetNumberBonFiscal());

                            if (position == 2)//Z reports
                                binder.edKLtoNo.setText(new cmdInfo().GetNumberOfLastZreport());
                            else binder.edKLtoNo.setText(new cmdInfo().GetNumberBonFiscal());

                        } catch (Exception e) {
                            postToast(e.getMessage());
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                    //add some code here
                }
        );


        binder.chbKLrangeBynum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    boolean isNumberRange = binder.chbKLrangeBynum.isChecked();
                    binder.edKLstartDate.setEnabled(!isNumberRange);
                    binder.edKLstartTime.setEnabled(!isNumberRange);
                    binder.edKLtoDate.setEnabled(!isNumberRange);
                    binder.edKLtoTime.setEnabled(!isNumberRange);
                    binder.tvFromNo.setEnabled(isNumberRange);
                    binder.tvToNo.setEnabled(isNumberRange);
                    binder.edKLfromNo.setEnabled(isNumberRange);
                    binder.edKLtoNo.setEnabled(isNumberRange);

                } catch (
                        Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });

    }

    /**
     * @return  Input parameters for search process
     */
    private cmdEJournal.EJ_ParamRange setVarsByDate() {
        return new cmdEJournal.EJ_ParamRange(
                1, 2,
                1, 2,
                binder.edKLstartDate.getText().toString() + " " + binder.edKLstartTime.getText().toString(),
                binder.edKLtoDate.getText().toString() + " " + binder.edKLtoTime.getText().toString()
        );
    }

    /**
     *
     * @return True is parameters is parameters is OK
     *
     */
    private boolean checkRange() {
        if (!binder.edKLfromNo.getText().toString().trim().matches("^\\d+$")) {
            binder.edKLfromNo.requestFocus();
            return false;
        } else if (!binder.edKLtoNo.getText().toString().trim().matches("^\\d+$")) {
            binder.edKLtoNo.requestFocus();
            return false;
        } else return true;

    }

    /**
     *
     * @return Input parameters for search process
     */
    private cmdEJournal.EJ_ParamRange setVarsByNumbers() {
        cmdEJournal.EJ_ParamRange resParam = new cmdEJournal.EJ_ParamRange();
        resParam.setDocNumStart(Integer.valueOf(binder.edKLfromNo.getText().toString().trim()));
        resParam.setDocNumEnd(Integer.valueOf(binder.edKLtoNo.getText().toString().trim()));
        return resParam;

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

