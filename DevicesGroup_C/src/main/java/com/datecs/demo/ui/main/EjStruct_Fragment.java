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
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.datecs.demo.EjStructBinding;
import com.datecs.demo.ui.main.tools.DocTypeToRead;
import com.google.android.material.snackbar.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.datecs.demo.ui.main.tools.SetTime;
import com.datecs.demo.ui.main.tools.TextViewDatePicker;
import com.datecs.demo.ui.main.tools.cmdEJStructInfo;
import com.datecs.fileselector.FileOperation;
import com.datecs.fileselector.FileSelector;
import com.datecs.fileselector.OnHandleFileListener;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdEJournal;
import com.datecs.testApp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EjStruct_Fragment extends Fragment {
    private EjStructBinding binder;
    private ProgressDialog progress;
    private cmdEJStructInfo myEjStructInfo = new cmdEJStructInfo();
    private final String[] mFileFilter = {".txt"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.ej_struct_fragment, container, false);
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


                if (binder.chbKLrangeBynum.isChecked())
                    progress.setTitle(R.string.title_reading_documents_by_num);
                else progress.setTitle(R.string.title_reading_documents_by_dat);
                progress.show();
                final String[] toastText = {"0"};
                final String[] finalReadedDocData ={""};
                final int[] readedDocNum = {0};
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String readedDocData = null;
                        // do the thing that takes a long time
                        try {

                            //Search EJ by Number of Documents
                            if (binder.chbKLrangeBynum.isChecked()) {
                                int fromNum = Integer.parseInt(binder.edKLfromNo.getText().toString());
                                int toNum = Integer.parseInt(binder.edKLtoNo.getText().toString());
                                if (fromNum > toNum) toNum = fromNum;
                                for (int docNum = fromNum; docNum <= toNum; docNum++) {
                                    readedDocData = myEjStructInfo.ReadDocumentByNumber(docNum, DocTypeToRead.all_types); //All type of receipt
                                    if (readedDocData != null) {
                                        finalReadedDocData[0] += readedDocData;
                                        readedDocNum[0]++;
                                    }
                                }
                            } else //Search EJ by Date Time
                            {
                                String fromDT = binder.edKLstartDate.getText() + " " + binder.edKLstartTime.getText();
                                String toDT = binder.edKLtoDate.getText() + " " + binder.edKLtoTime.getText();
                                int[] docRange = myEjStructInfo.SearchDocumentsInPeriod(fromDT, toDT);
                                for (int docNum : docRange) {
                                    readedDocData = myEjStructInfo.ReadDocumentByNumber(docNum,DocTypeToRead.all_types); //All type of receipt
                                    if (readedDocData != null) {
                                        finalReadedDocData[0] += readedDocData;
                                        readedDocNum[0]++;
                                    }
                                }
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
                                binder.edEjTextMonitor.setMaxLines(finalReadedDocData[0].length());
                                binder.edEjTextMonitor.setText(finalReadedDocData[0]);
                                toastText[0] = getString(R.string.msg_doc_received) + String.valueOf(readedDocNum[0]);
                                Snackbar.make(getView(), toastText[0], Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            }
                        });
                    }
                }).start();

            }
        });

        binder.btnSaveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    FileSelector fileSelector = new FileSelector(getActivity(), FileOperation.SAVE, mSaveZReportFileListener, mFileFilter, Environment.getExternalStorageDirectory());
                    //"log_" + DateTime.Today.ToString("dd_MMM_yy") + "_" + DateTime.Now.ToString("HH.mm.ss") + ".txt";
                    DateFormat dateFormat = new SimpleDateFormat("dd_MMM_yy_HHmmss");
                    Date date = new Date();
                    fileSelector.setDefaultFileName("log_" + dateFormat.format(date));

                    fileSelector.show();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });

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

    private OnHandleFileListener mSaveZReportFileListener = new OnHandleFileListener() {
        @Override
        public void handleFile(final String filePath) {
            if (SaveToFile(filePath))
                Snackbar.make(getView(), R.string.msg_file_saved, Snackbar.LENGTH_LONG).setAction("Action", null).show();

        }
    };

    private boolean SaveToFile(String fn) {
        String etName = binder.edEjTextMonitor.getText().toString();
        if (!etName.trim().equals("")) {
            try {
                File file = new File(fn);
                //if file doesnt exists, then create it
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream f = new FileOutputStream(file);
                PrintWriter pw = new PrintWriter(f);
                pw.println(etName);
                pw.flush();
                pw.close();
                f.close();
                return true;
            } catch (FileNotFoundException e) {
                postToast(e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                postToast(e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
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

