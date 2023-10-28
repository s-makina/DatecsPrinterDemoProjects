/**
 * This fragment provides a custom graphical interface for one of the most important
 * settings for printing and the final look of the Fiscal receipt.
 * Here you can select a bitmap image (black and white 1bit per pixel)
 * to load as a logo printed on any fiscal or non-fiscal receipt.
 * <p>
 * ************************************************************************************************
 * Attention !!!
 * In some models of mobile fiscal devices, printing on a solid  black logo can cause a
 * drop in voltage and Power down Error.
 * <p>
 * Тhe size of the uploaded images is not checked, images over the allowable size for your device
 * will not be printed.
 * Please use the sample pictures only for testing included in  ..\\TestLogo folder.
 *
 * @author Datecs Ltd. Software Department
 */
package com.datecs.demo.ui.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.databinding.DataBindingUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.datecs.testApp.PrintConfigFrgBinding;
import com.google.android.material.snackbar.Snackbar;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.datecs.demo.MainActivity;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdConfig;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdInfo;
import com.datecs.testApp.R;
import com.google.android.gms.common.util.IOUtils;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;

public class PrintConfigFragment extends Fragment {

    private int printerColSettings;
    private PrintConfigFrgBinding binder;
    private static int LOAD_LOGO_RESULTS = 123;
    private static int LOAD_STAMP_RESULTS = 124;
    private ProgressDialog progress;
    private String stampName8X3 = null;
    private Bitmap bmpOriginal;
    private final cmdConfig myPrintConfig = new cmdConfig();


    public PrintConfigFragment() throws Exception {
    }

    // private ProgressDialog progress;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.print_config_fragment, container, false);
        return binder.getRoot();
    }


    private void loadLogoThread(final String base64String) {

        progress = ProgressDialog.show(getContext(), getString(R.string.title_loading_logo), getString(R.string.msg_please_wait), true);
        new Thread(new Runnable() {
            final int COUNT_BYTES = 36;

            @Override
            public void run() {
                // do the thing that takes a long time

                boolean res = false;
                try {
                    myPrintConfig.Logo_StartLoading();

                    String base64TempString = base64String.replaceAll("(\\n)", "");
                    while (base64TempString.length() > 0) {

                        if (base64TempString.length() >= COUNT_BYTES) {
                            myPrintConfig.Logo_DataLoading(base64TempString.substring(0, COUNT_BYTES));
                            base64TempString = base64TempString.substring(COUNT_BYTES);
                        } else {
                            myPrintConfig.Logo_DataLoading(base64TempString);
                            break;
                        }
                    }
                    myPrintConfig.Logo_StopLoading();
                    res = true;

                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());

                } finally {
                    progress.dismiss();
                }
                final boolean finalRes = res;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalRes) ShowDlg_RestartDevice();

                    }
                });
            }
        }).start();

    }

    private void loadStampThread(final String base64String) {

        progress = ProgressDialog.show(getContext(),
                getString(R.string.loading_stamp),
                getString(R.string.msg_please_wait), true);
        new Thread(new Runnable() {
            final int COUNT_BYTES = 36;

            @Override
            public void run() {
                // do the thing that takes a long time
                try {
                    myPrintConfig.Stamp_StartLoading();
                    String base64TempString = base64String.replaceAll("(\\n)", "");
                    while (base64TempString.length() > 0) {

                        if (base64TempString.length() >= COUNT_BYTES) {
                            myPrintConfig.Stamp_DataLoading(base64TempString.substring(0, COUNT_BYTES));
                            base64TempString = base64TempString.substring(COUNT_BYTES);
                        } else {
                            myPrintConfig.Stamp_DataLoading(base64TempString);
                            break;
                        }
                    }
                    myPrintConfig.Stamp_StopLoading();
                    myPrintConfig.Stamp_Operation("1", stampName8X3);
                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                } finally {
                    progress.dismiss();
                }
            }
        }).start();
    }

    private void ShowDlg_RestartDevice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.title_restarting);
        builder.setMessage(R.string.msg_q_restart_device);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    myPrintConfig.Logo_Updtae();
                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                } finally {
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }




    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        try {
            //Init Print Columns Spinner
            String[] items = myPrintConfig.GetPrintColumnsSupported();
            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binder.spPrinterColumns.setAdapter(adapter1);

            //Init Barcode Height Spinner
            items = new String[10];//
            for (int i = 0; i < 10; i++) items[i] = String.valueOf((i + 1) * 7) + " mm";
            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binder.spBarcodeHeight.setAdapter(adapter2);

            //Init AutoPowerOff Spinner
            items = new String[16];//
            items[0] = "disable";
            for (int i = 1; i < 16; i++) items[i] = i + " minutes";
            ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
            adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binder.spAutoPowerOff.setAdapter(adapter3);

            //Init BkLightAutoOff Spinner
            items = new String[6];//
            items[0] = "disable";
            for (int i = 1; i < 6; i++) items[i] = i + " minutes";
            ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
            adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binder.spBkLightAutoOff.setAdapter(adapter4);
            binder.spPrintContrast.setSelection(myPrintConfig.GetPrnQuality());
            printerColSettings = myPrintConfig.GetPrintColumns(); // If not changed, D'not save this item
            binder.spPrinterColumns.setSelection((
                    //Set Spinner selection to position via adapter
                    (ArrayAdapter<String>) binder.spPrinterColumns.getAdapter()).getPosition(String.valueOf(printerColSettings))); //42-48-64
            binder.spAutoPowerOff.setSelection(myPrintConfig.GetAutoPowerOff()); //0-15
            binder.spBkLightAutoOff.setSelection(myPrintConfig.GetBkLightAutoOff()); //0-5
            binder.chbPrintBarcode.setChecked(myPrintConfig.GetBarcodePrint());
            binder.chbPrintLogo.setChecked(myPrintConfig.GetLogoPrint());
            binder.chbAltCurrency.setChecked(myPrintConfig.GetForeignPrint() > 0);
            binder.chbExchangeRate.setChecked(myPrintConfig.GetForeignPrint() == 2);
            binder.spTypeReceipt.setSelection(myPrintConfig.GetEcrExtendedReceipt() ? 1 : 0);
            binder.spBarcodeHeight.setSelection(myPrintConfig.GetBarcodeHeight() - 1); //'1' (7mm) to '10' (70mm);
            binder.chbCondensedPrint.setChecked(myPrintConfig.GetCondensedPrint());
            binder.chbDuplicateRec.setChecked(myPrintConfig.GetDuplicateRec());
            binder.chkPrintEmptyLine.setChecked(myPrintConfig.GetEmptyLineAfterTotal());
            binder.chkNearPaperEnd.setChecked(myPrintConfig.GetNearPaperEndEnabled());
            binder.chkDoveritelPrint.setChecked(myPrintConfig.GetDoveritelPrint());
            binder.chkPrintVAT.setChecked(myPrintConfig.GetVatPrint());
            binder.chkEcrPluDailyClearing.setChecked(myPrintConfig.GetEcrPluDailyClearing());
            binder.chkEcrSafeOpening.setChecked(myPrintConfig.GetEcrSafeOpening());
            binder.chkEcrConnectedGroupsReport.setChecked(myPrintConfig.GetEcrConnectedGroupsReport());
            binder.chkEcrConnectedCashReport.setChecked(myPrintConfig.GetEcrConnectedCashReport());
            binder.edCurrNameLocal.setText(new cmdInfo().GetCurrNameLocal());
            binder.edCurrNameForeign.setText(new cmdInfo().GetCurrNameForeign());
            binder.edExchangeRate.setText(new cmdInfo().GetExchangeRate());
            //Init interval of Invoices
            ArrayList<String> intervalInfo = new cmdConfig().GetInvoiceInterval();
            binder.edInvoiceStart.setText(intervalInfo.get(0));
            binder.edInvoiceEnd.setText(intervalInfo.get(1));
            binder.lbNextInvoice.setText(intervalInfo.get(2));


        } catch (Exception e) {
            e.printStackTrace();
            postToast(e.getMessage());
        }


        binder.btnSavePrintConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    myPrintConfig.SetPrnQuality(binder.spPrintContrast.getSelectedItem().toString());

                    int newPrinterColSettings = Integer.valueOf((String) binder.spPrinterColumns.getSelectedItem());
                    if (newPrinterColSettings != printerColSettings)
                        //Save Number of printer columns will restart device !!!
                        myPrintConfig.SetPrintColumns(newPrinterColSettings);
                    myPrintConfig.SetBarcodePrint(binder.chbPrintBarcode.isChecked());
                    myPrintConfig.SetLogoPrint(binder.chbPrintLogo.isChecked());

                    myPrintConfig.SetAutoPowerOff((int) binder.spAutoPowerOff.getSelectedItemId() + 1);
                    myPrintConfig.SetBkLightAutoOff(((int) binder.spBkLightAutoOff.getSelectedItemId() + 1));

                    int jorkaLogic = -1;
                    if (binder.chbAltCurrency.isChecked() && binder.chbExchangeRate.isChecked()) {
                        jorkaLogic = 2;
                    } else if (binder.chbAltCurrency.isChecked() && (!binder.chbExchangeRate.isChecked())) {
                        jorkaLogic = 1;
                    } else if (!binder.chbAltCurrency.isChecked()) {
                        jorkaLogic = 0;
                    }

                    myPrintConfig.SetForeignPrint(jorkaLogic);
                    myPrintConfig.SetEcrExtendedReceipt(binder.spTypeReceipt.getSelectedItemId() == 1);
                    myPrintConfig.SetCondensedPrint(binder.chbCondensedPrint.isChecked());
                    myPrintConfig.SetEmptyLineAfterTotal(binder.chkPrintEmptyLine.isChecked());
                    myPrintConfig.SetDuplicateRec(binder.chbDuplicateRec.isChecked());
                    myPrintConfig.SetNearPaperEndEnabled(binder.chkNearPaperEnd.isChecked());
                    myPrintConfig.SetDoveritelPrint(binder.chkDoveritelPrint.isChecked());
                    myPrintConfig.SetVatPrintEnable(binder.chkPrintEmptyLine.isChecked());
                    myPrintConfig.SetBarcodeHeight((int) binder.spBarcodeHeight.getSelectedItemId() + 1);
                    myPrintConfig.SetEcrSafeOpening(binder.chkEcrSafeOpening.isChecked());
                    myPrintConfig.SetEcrConnectedGroupsReport(binder.chkEcrConnectedGroupsReport.isChecked());
                    myPrintConfig.SetEcrConnectedCashReport(binder.chkEcrConnectedCashReport.isChecked());
                    myPrintConfig.SetEcrPluDailyClearing(binder.chkEcrPluDailyClearing.isChecked());
                    myPrintConfig.SetCurrNameLocal(binder.edCurrNameLocal.getText().toString());
                    myPrintConfig.SetCurrNameForeign(binder.edCurrNameForeign.getText().toString());
                    myPrintConfig.SetExchangeRate(binder.edExchangeRate.getText().toString());

                    Snackbar.make(view, R.string.configuration_saved_, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }
        });

        binder.btnLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    //TODO: Zaredi poslednoto URI !!!
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, LOAD_LOGO_RESULTS);

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        binder.btnLoadStamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stampName8X3 = binder.edStampName.getText().toString();
                if (!stampName8X3.matches("^.{1,8}[.].{3}$")) {
                    postToast(getString(R.string.msg_wrong_stamp));
                    return;
                }
                try {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    //TODO: Zaredi poslednoto URI !!!
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, LOAD_STAMP_RESULTS);
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        binder.btnPrintStamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    myPrintConfig.Stamp_Operation("0", binder.edStampName.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }
        });

        binder.btnSetInvoiceRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cmdConfig myConfig = new cmdConfig();
                    try {
                        ArrayList<String> invoiceInterval = myConfig.GetInvoiceInterval();
                        if (Integer.valueOf(invoiceInterval.get(1)) < Integer.valueOf(invoiceInterval.get(2))) {
                            //If the current invoice counter have reached the end of the interval.
                            myConfig.SetInvoiceInterval(binder.edInvoiceStart.getText().toString(), binder.edInvoiceEnd.getText().toString());
                        } else // Extend of interval
                            myConfig.SetExInvoiceInterval(binder.edInvoiceEnd.getText().toString());
                        Snackbar.make(view, R.string.okButtonText, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        postToast(ex.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }
        });
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {

        if ((requestCode == LOAD_LOGO_RESULTS && resultCode == MainActivity.RESULT_OK) ||
                (requestCode == LOAD_STAMP_RESULTS && resultCode == MainActivity.RESULT_OK))
            try {

                InputStream stream = getContext().getContentResolver().openInputStream(data.getData());
                final byte[] bytes = IOUtils.toByteArray(stream);
                bmpOriginal = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                stream.close();
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());

                LayoutInflater inflater = this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_bmp_dialog, null);
                dialog.setView(dialogView);

                final Bitmap bwLogo = bmpDithering(bmpOriginal);
                final ImageView ivLogo = dialogView.findViewById(R.id.iv_alert_dialog);
                ivLogo.setImageBitmap(bwLogo);


                dialog.setMessage(("Size XY:" + String.valueOf(String.valueOf(bmpOriginal.getWidth()) +
                        "," + String.valueOf(bmpOriginal.getHeight()))));
                dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String base64TempString = Base64.encodeToString(bytes, Base64.DEFAULT);
                        if (requestCode == LOAD_LOGO_RESULTS) loadLogoThread(base64TempString);
                        if (requestCode == LOAD_STAMP_RESULTS) loadStampThread(base64TempString);
                    }

                });
                dialog.setNegativeButton(R.string.no, null);
                dialog.show();


            } catch (Exception e) {
                postToast(e.getMessage());
                e.printStackTrace();
            }
        super.onActivityResult(requestCode, resultCode, data);
    }


    //10X Jorkata
    private Bitmap bmpDithering(Bitmap inBmp) {

        final int imgWIDTH = inBmp.getWidth();
        final int imgHEGHT = inBmp.getHeight();
        Bitmap bmpGrayscale = Bitmap.createBitmap(imgWIDTH, imgHEGHT, Bitmap.Config.ARGB_8888);

        int[] workBuffer = new int[imgWIDTH * imgHEGHT];
        Arrays.fill(workBuffer, 0);
        for (int y = 0; y < imgHEGHT - 1; y++)
            for (int x = 0; x < imgWIDTH - 1; x++) {
                int tmpColor = inBmp.getPixel(x, y);
                int b_b = (Color.blue(tmpColor));  // >> 16);
                int g_b = (Color.green(tmpColor)); // >>  8);
                int r_b = (Color.red(tmpColor));
                workBuffer[y * imgWIDTH + x] = (int) Math.round(0.114 * r_b + 0.587 * g_b + 0.299 * b_b);
            }
        // floyd-steinberg
        for (int y = 0; y < imgHEGHT - 2; y++)
            for (int x = 0; x < imgWIDTH - 2; x++) {
                int d = workBuffer[(y * imgWIDTH) + x];
                if (d > 128) {
                    d = 255 - d;
                    if (d < 0) d = 0;
                    workBuffer[(y * imgWIDTH) + x + 1] = workBuffer[(y * imgWIDTH) + x + 1] - (d >> 1);
                    workBuffer[((y + 1) * imgWIDTH) + x] = workBuffer[((y + 1) * imgWIDTH) + x] - (d >> 2);
                    workBuffer[((y + 1) * imgWIDTH) + x + 1] = workBuffer[((y + 1) * imgWIDTH) + x + 1] - (d >> 3);
                    workBuffer[((y + 1) * imgWIDTH) + x - 1] = workBuffer[((y + 1) * imgWIDTH) + x - 1] - (d >> 3);
                    workBuffer[(y * imgWIDTH) + x] = 255;
                } else {
                    if (d < 0) d = 0;
                    workBuffer[(y * imgWIDTH) + x + 1] = workBuffer[(y * imgWIDTH) + x + 1] + (d >> 1);
                    workBuffer[((y + 1) * imgWIDTH) + x] = workBuffer[((y + 1) * imgWIDTH) + x] + (d >> 2);
                    workBuffer[((y + 1) * imgWIDTH) + x + 1] = workBuffer[((y + 1) * imgWIDTH) + x + 1] + (d >> 3);
                    workBuffer[((y + 1) * imgWIDTH) + x - 1] = workBuffer[((y + 1) * imgWIDTH) + x - 1] + (d >> 3);
                    workBuffer[(y * imgWIDTH) + x] = 0;
                }
            }
        for (int y = 0; y < imgHEGHT - 1; y++)
            for (int x = 0; x < imgWIDTH - 1; x++) {
                if (workBuffer[y * imgWIDTH + x] > 128) {
                    bmpGrayscale.setPixel(x, y, Color.WHITE);
                } else {
                    bmpGrayscale.setPixel(x, y, Color.BLACK);
                }
            }
        return bmpGrayscale;
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