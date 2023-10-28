/**
 * This fragment provides a custom graphical interface for one of the most important
 * settings for printing and the final look of the Fiscal receipt.
 * Here you can select a bitmap image, jpg-image or png.
 * to load as a logo printed on any fiscal or non-fiscal receipt.
 * <p>
 * ************************************************************************************************
 * Attention !!!
 * In some models of mobile fiscal devices, printing on a solid  black logo can cause a
 * drop in voltage and Power down Error.
 * <p>
 * Тhe size of the uploaded images is not checked, images over the allowable size for your device
 * will not be printed. Please use the sample pictures only for testing included in  ..\\TestLogo folder.
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
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.datecs.demo.MainActivity;
import com.datecs.demo.PrintConfig_binding;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdConfig;
import com.datecs.testApp.R;
import com.google.android.gms.common.util.Hex;
import com.google.android.gms.common.util.IOUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class PrintConfigFragment extends Fragment {
    private int MAX_LOGO_H;
    private int MAX_LOGO_W;
    private static int LOAD_LOGO_RESULTS = 12312;
    private ProgressDialog progress;
    private final cmdConfig myPrintConfig = new cmdConfig();
    private PrintConfig_binding binder;


    public PrintConfigFragment() throws Exception {
    }


    // private ProgressDialog progress;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.print_config_fragment, container, false);

        //Init Barcode Height Spinner - 3 mm to 30 mm
        String[] items = new String[28];
        for (int i = 0; i < 28; i++) items[i] = String.valueOf(i + 3);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binder.spBarcodeHeight.setAdapter(adapter);

        //Init Logo Height Spinner
        if(myPrintConfig.isConnectedECR())
        { MAX_LOGO_W=myPrintConfig.getConnectedECRV1().getMaxLogoWidthPixel();
          MAX_LOGO_H=myPrintConfig.getConnectedECRV1().getMaxLogoHeightPixel();}
        if(myPrintConfig.isConnectedPrinter())
        { MAX_LOGO_W=myPrintConfig.getConnectedPrinterV1().getMaxLogoWidthPixel();
          MAX_LOGO_H=myPrintConfig.getConnectedPrinterV1().getMaxLogoHeightPixel();}

        items = new String[MAX_LOGO_H];
        for (int i = 0; i < MAX_LOGO_H; i++) items[i] = String.valueOf(i + 1);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        binder.spLogoHeight.setAdapter(adapter);

        binder.chbFormatReceipt.setEnabled(!MainActivity.myFiscalDevice.isConnectedECR());//Bug Fix Hus
        binder.chbFormatReceipt.setEnabled(!MainActivity.myFiscalDevice.isConnectedECR());//Bug Fix Hus
        binder.chkDrawerOpen.setEnabled(!MainActivity.myFiscalDevice.isConnectedECR());//Bug Fix Hus


        try {
            binder.spPrintContrast.setSelection(myPrintConfig.GetPrnQuality());

            if (MainActivity.myFiscalDevice.isFontSupported()) {
                binder.spPrinterFont.setEnabled(true);
                binder.spPrinterFont.setSelection((myPrintConfig.GetFontType()));
            } else binder.spPrinterFont.setEnabled(false);


            if (MainActivity.myFiscalDevice.isConnectedECR()) //   not implemented
            {
                binder.spBarcodeHeight.setSelection(0);
                binder.spLogoHeight.setSelection(0);
            } else {
                binder.spBarcodeHeight.setSelection(myPrintConfig.GetBarcodeHeight() - 3);
                binder.spLogoHeight.setSelection(myPrintConfig.GetLogoHeight() - 1); //1-96 to index 0-95
            }
            binder.chbFormatReceipt.setChecked(myPrintConfig.GetReceiptFormat());
            if (MainActivity.myFiscalDevice.isPaperCuttingDevice()) {
                binder.chbPaperCutting.setEnabled(true);
                binder.chbPaperCutting.setChecked(myPrintConfig.GetAutoPaperCutting());
            } else binder.chbPaperCutting.setEnabled(false);

            binder.chbPrintLogo.setChecked(myPrintConfig.GetLogoPrint());
            binder.spLogoHeight.setSelection(Integer.valueOf(myPrintConfig.GetLogoHeight() - 1));

            binder.edExRate.setText(myPrintConfig.GetExchangeRate());
            binder.chbAltCurrency.setChecked(myPrintConfig.GetForeignPrint());
            binder.chkDrawerOpen.setChecked(myPrintConfig.GetDrawerOpening());
            binder.chkDoveritelPrint.setChecked(myPrintConfig.GetDepartmentPrint());
            binder.chkVatPrintEnable.setChecked(myPrintConfig.GetPrintVAT());

            //Set Invoice Range
            ArrayList<String> intervalInfo = new cmdConfig().GetInvoiceInterval();
            binder.edInvoiceStart.setText(intervalInfo.get(0));
            binder.edInvoiceEnd.setText(intervalInfo.get(1));
            binder.lbNextInvoice.setText("Next Invoice:" + intervalInfo.get(2));


        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }
        return binder.getRoot();
    }

    /**
     * Upload in background
     *
     * @param bmpLogo - Converted image
     */
    private void uploadLogoThread(final Bitmap bmpLogo) {

        progress = ProgressDialog.show(getContext(), getString(R.string.title_loading_logo), getString(R.string.msg_please_wait), true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // do the thing that takes a long time
                try {
                    writeLogoToDevice(bmpLogo);
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                } finally {
                    progress.dismiss();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(getView(), R.string.uploading_ok, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                });
            }
        }).start();

    }


    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binder.btnSavePrintConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    myPrintConfig.SetPrnQuality((int) binder.spPrintContrast.getSelectedItemId());

                    if (MainActivity.myFiscalDevice.isFontSupported())
                        myPrintConfig.SetFontType((int) binder.spPrinterFont.getSelectedItemId());

                    myPrintConfig.SetBarcodeHeight((Integer.valueOf((String) binder.spBarcodeHeight.getSelectedItem())));

                    if (!MainActivity.myFiscalDevice.isConnectedECR()) //Bug Fix Hus
                    {
                        myPrintConfig.SetReceiptFormat(binder.chbFormatReceipt.isSelected());
                        myPrintConfig.SetLogoPrint(binder.chbPrintLogo.isChecked(), String.valueOf(binder.spLogoHeight.getSelectedItemId() + 1));
                        myPrintConfig.SetDrawerOpening(binder.chkDrawerOpen.isChecked());
                    }


                    if (MainActivity.myFiscalDevice.isPaperCuttingDevice())
                        myPrintConfig.GetAutoPaperCutting(binder.chbPaperCutting.isChecked());

                    myPrintConfig.SetForeignPrint(binder.chbAltCurrency.isChecked(), binder.edExRate.getText().toString());
                    myPrintConfig.SetDepartmentPrint(binder.chkDoveritelPrint.isChecked());
                    myPrintConfig.SetPrintVAT(binder.chkVatPrintEnable.isChecked());
                    Snackbar.make(view, R.string.configuration_saved_, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        binder.btnWriteLogo.setOnClickListener(new View.OnClickListener() {
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

        binder.btnSetInvoiceRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmdConfig myConfig = new cmdConfig();
                try {
                    myConfig.SetInvoiceInterval(binder.edInvoiceStart.getText().toString(), binder.edInvoiceEnd.getText().toString());
                    Snackbar.make(binder.getRoot(), R.string.settings_saved, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    postToast(ex.getMessage());
                }
            }
        });


        binder.btnReadLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downLogoThread();
            }
        });
    }


    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        //To Convert in background use this:
        //final BitmapConvertor convertor = new BitmapConvertor(getContext());

        if ((requestCode == LOAD_LOGO_RESULTS && resultCode == MainActivity.RESULT_OK))
            try {
                InputStream stream = getContext().getContentResolver().openInputStream(data.getData());
                final byte[] bytes = IOUtils.toByteArray(stream);
                //Show BMP in alert dialog
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                stream.close();
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_bmp_dialog, null);
                dialog.setView(dialogView);
                final ImageView ivLogo = dialogView.findViewById(R.id.iv_alert_dialog);

                //To Convert in background use this: convertor.convertBitmap(bitmap);
                final Bitmap bwLogo = bmpDithering(bitmap);
                ivLogo.setImageBitmap(bwLogo);

                dialog.setMessage(("Size XY:" + String.valueOf(String.valueOf(bitmap.getWidth()) +
                        "," + String.valueOf(bitmap.getHeight()))));

                dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (requestCode == LOAD_LOGO_RESULTS) uploadLogoThread(bwLogo);
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
                if (workBuffer[y * imgWIDTH + x] > 128)
                    bmpGrayscale.setPixel(x, y, Color.WHITE);
                else bmpGrayscale.setPixel(x, y, Color.BLACK);
            }
        return bmpGrayscale;
    }

    /**
     * Create 1bpp bitmap and write pixels(HEX represented) to device
     *
     * @param bmpOriginal - BW Bitmap
     * @return
     */
    private void writeLogoToDevice(Bitmap bmpOriginal) throws Exception {
        for (int y = 0; y < bmpOriginal.getHeight(); y++) {
            String ROW_DATA = "";

            for (int j = 0; j < bmpOriginal.getWidth() / 8; j++) {
                int tmpB = 0;
                int index = j * 8;
                if (bmpOriginal.getPixel(index, y) <= Color.GRAY) tmpB = (tmpB | 128);
                if (bmpOriginal.getPixel(index + 1, y) <= Color.GRAY) tmpB = (tmpB | 64);
                if (bmpOriginal.getPixel(index + 2, y) <= Color.GRAY) tmpB = (tmpB | 32);
                if (bmpOriginal.getPixel(index + 3, y) <= Color.GRAY) tmpB = (tmpB | 16);
                if (bmpOriginal.getPixel(index + 4, y) <= Color.GRAY) tmpB = (tmpB | 8);
                if (bmpOriginal.getPixel(index + 5, y) <= Color.GRAY) tmpB = (tmpB | 4);
                if (bmpOriginal.getPixel(index + 6, y) <= Color.GRAY) tmpB = (tmpB | 2);
                if (bmpOriginal.getPixel(index + 7, y) <= Color.GRAY) tmpB = (tmpB | 1);
                ROW_DATA += String.format("%02X", tmpB);

            }
            myPrintConfig.Logo_WriteRow(y, ROW_DATA);
        }
        myPrintConfig.Logo_SaveToDevice();
    }

    public static boolean[] byteToBoolArr(byte b) {
        boolean boolArr[] = new boolean[8];
        for (int i = 0; i < 8; i++) boolArr[i] = (b & (byte) (128 / Math.pow(2, i))) != 0;
        return boolArr;
    }

    private Bitmap logoToBitmap(String logo) {

        Bitmap bitmap = Bitmap.createBitmap(MAX_LOGO_W, MAX_LOGO_H, Bitmap.Config.ARGB_8888);
        try {
            byte[] decoded = Hex.stringToBytes(logo);
            for (int y = 0; y < MAX_LOGO_H; y++) {
                for (int x = 0; x < MAX_LOGO_W / 8; x++) {
                    int index = x * 8;
                    boolean[] bwPixel = byteToBoolArr(decoded[y * MAX_LOGO_W / 8 + x]);
                    bitmap.setPixel(index, y, bwPixel[0] ? Color.BLACK : Color.WHITE);
                    bitmap.setPixel(index + 1, y, bwPixel[1] ? Color.BLACK : Color.WHITE);
                    bitmap.setPixel(index + 2, y, bwPixel[2] ? Color.BLACK : Color.WHITE);
                    bitmap.setPixel(index + 3, y, bwPixel[3] ? Color.BLACK : Color.WHITE);
                    bitmap.setPixel(index + 4, y, bwPixel[4] ? Color.BLACK : Color.WHITE);
                    bitmap.setPixel(index + 5, y, bwPixel[5] ? Color.BLACK : Color.WHITE);
                    bitmap.setPixel(index + 6, y, bwPixel[6] ? Color.BLACK : Color.WHITE);
                    bitmap.setPixel(index + 7, y, bwPixel[7] ? Color.BLACK : Color.WHITE);
                }
            }
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();

        }
        return bitmap;
    }


    private void downLogoThread() {

        progress = ProgressDialog.show(getContext(), getString(R.string.title_downloading_logo), getString(R.string.msg_please_wait), true);
        new Thread(new Runnable() {
            String logo = "";

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                // do the thing that takes a long time
                try {

                    for (int i = 0; i < MAX_LOGO_H; i++) {
                        logo += myPrintConfig.Logo_ReadRow(i);//Read all 348 pixels. 96/2 = 48*8; 48*8=348 pixel
                    }
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                } finally {
                    progress.dismiss();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binder.ivLoadedLogo.setImageBitmap(logoToBitmap(logo));

                    }
                });
            }
        }).start();

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