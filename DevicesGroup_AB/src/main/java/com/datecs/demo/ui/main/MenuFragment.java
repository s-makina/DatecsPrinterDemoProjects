
package com.datecs.demo.ui.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.datecs.demo.MainActivity;
import com.datecs.demo.MenuFrg_binder;
import com.datecs.fiscalprinter.SDK.BuildInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdReceipt;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdReport;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdService;
import com.datecs.testApp.R;
import static com.datecs.demo.PrinterManager.getFiscalDevice;
import static com.datecs.demo.ui.main.tools.DemoUtil.*;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdReceipt.BarcodeType.EAN13;


public class MenuFragment extends Fragment {

    private MenuFrg_binder binder;
    private cmdReceipt.FiscalReceipt fiscalReceipt;
    private cmdReceipt.NonFiscalReceipt noFiscalReceipt;
    private ProgressDialog progress;
    private Double mMultiplier = 100.00;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binder = DataBindingUtil.inflate(inflater, R.layout.menu_fragment, container, false);
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fiscalReceipt = new cmdReceipt.FiscalReceipt();
        noFiscalReceipt = new cmdReceipt.NonFiscalReceipt();
        binder.tvSDKBuild.setText("Datecs JSDK Build: " + BuildInfo.VERSION);

        //To check the status of the device, send a command to fill in the status bits
        try {
            //To check the status of the device, send a command to fill in the status bits
            new cmdInfo().GetDeviceSerialNumber();
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }
        binder.btnFiscRecordInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (getFiscalDevice().isConnectedECR())
                        postToast("Not Supported on DP-05, DP-25, DP-35, WP-50, DP-150");
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new FiscalPeriodInfoFrag(), "fiscal_period_frg");
                    ft.addToBackStack(null);
                    ft.commit();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        binder.btnClientsInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(!getFiscalDevice().isConnectedECR()) postToast("Supported only on  DP-05, DP-25, DP-35, WP-50, DP-150");
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new ClientsListFragment(), "clients_list_frg");
                    ft.addToBackStack(null);
                    ft.commit();


                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        binder.btnSetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new DateTimeFragment(), "date_time_frg");
                    ft.addToBackStack(null);
                    ft.commit();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });


        binder.btnHeaderFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new HeaderFooterFragment(), "header_footer_frg");
                    ft.addToBackStack(null);
                    ft.commit();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });

        binder.btnPrintConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new PrintConfigFragment(), "print_config_frg");
                    ft.addToBackStack(null);
                    ft.commit();

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        binder.btnDeviceStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new DeviceStatusFragment(), "device_status_frg");
                    ft.addToBackStack(null);
                    ft.commit();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });

        binder.btnDeviceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new DeviceInfoFragment(), "device_info_frg");
                    ft.addToBackStack(null);
                    ft.commit();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });


        binder.btnZReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progress = new ProgressDialog(getContext());
                progress.setCancelable(true);
                progress.setTitle("Z report is starting !!!");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
                final int[] reportNumber = {0};
                final cmdReport.ReportSummary reportSummary = new cmdReport.ReportSummary();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            cmdReport cmd = new cmdReport();
                            cmd.PrintZreport(reportSummary);

                        } catch (Exception e) {
                            postToast(e.getMessage());
                            e.printStackTrace();
                        } finally {
                            progress.dismiss();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DialogZXReportsSummary dialogSummary = new DialogZXReportsSummary(getActivity(), reportSummary);
                                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                lp.copyFrom(dialogSummary.getWindow().getAttributes());
                                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                dialogSummary.show();
                                dialogSummary.getWindow().setAttributes(lp);
                            }
                        });
                    }
                }).start();

            }

        });


        binder.btnXReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progress = new ProgressDialog(getContext());
                progress.setCancelable(true);
                progress.setTitle("X report is starting !!!");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
                final int[] reportNumber = {0};
                final cmdReport.ReportSummary reportSummary = new cmdReport.ReportSummary();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            cmdReport cmd = new cmdReport();
                            cmd.PrintXreport(reportSummary);
                        } catch (Exception e) {
                            postToast(e.getMessage());
                            e.printStackTrace();
                        } finally {
                            progress.dismiss();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DialogZXReportsSummary dialogSummary = new DialogZXReportsSummary(getActivity(), reportSummary);
                                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                lp.copyFrom(dialogSummary.getWindow().getAttributes());
                                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                dialogSummary.show();
                                dialogSummary.getWindow().setAttributes(lp);
                            }
                        });
                    }
                }).start();

            }

        });


        binder.btnExSaleTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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


                        fiscalReceipt.openFiscalReceipt(operatorCode, operatorPassword, salePoint, unp);    //Internal generated  unp="".
                        //РЕГИСТРИРАНЕ (ПРОДАЖБА) НА СТОКА
                        cmdReceipt.FiscalReceipt.FiscalSale testSale = new cmdReceipt.FiscalReceipt.FiscalSale();

                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                        fiscalReceipt.printFreeText("Тест продажба без параметри!", true, true, true, cmdReceipt.FiscalReceipt.FreeFiscalTextType.type32dpiA);

                        testSale.add(
                                "Нектар Вишна 35 % стъкло",
                                "Нектар Вишна 35% 2HoReCa",
                                "Б", //А, Б, В...
                                "0.01",
                                "",
                                "",
                                cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.noCorecction,
                                "");

                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash_space);
                        testSale.add(
                                "Демонстрация",
                                "продажба с количество",
                                "Б",  //А, Б, В...
                                "0.01",
                                "1",
                                "Кг", //Note! Units is not supported on DP-05, DP-25, DP-35 , WP-50, DP-150
                                cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.noCorecction,
                                "");

                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                        testSale.add(
                                "Демонстрация",
                                "Процентна отстъпака",
                                "Б", //А, Б, В...
                                "0.11",
                                "10",
                                "Кг",  //Note! Units is not supported on DP-05, DP-25, DP-35 , WP-50, DP-150
                                cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.discountPercentage,
                                "-10"); // Use sign !
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash_space);
                        testSale.add(
                                "Демонстрация",
                                "Отстъпака по стойност",
                                "Б", //А, Б, В...
                                "0.11",
                                "10",
                                "Литри", //Note! Units is not supported on DP-05, DP-25, DP-35 , WP-50, DP-150
                                cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.discountSum,
                                ".5");
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.equal);
                        testSale.add(
                                "Демонстрация",
                                "Надбавка в процент",
                                "Б",  //А, Б, В...
                                "0.1",
                                "10",
                                "Литри", //Note! Units is not supported on DP-05, DP-25, DP-35 , WP-50, DP-150
                                cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.surchargePercentage,
                                "1.5");

                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.equal);
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);

                        testSale.add(
                                "",
                                "VOID !!!",
                                "Б",  //А, Б, В...
                                "-0.1",
                                "",
                                "",
                                cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.noCorecction,
                                "");
                        //SUBTOTAL
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.equal);
                        testSale.subtotal(true, true, cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.noCorecction, "");
                        fiscalReceipt.printFreeText("Отстъпка в междинната сума", true, true, true, cmdReceipt.FiscalReceipt.FreeFiscalTextType.type32dpiB);
                        testSale.subtotal(true, true, cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.discountSum, "2.52");

                        //TOTAL
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                        cmdReceipt.TotalResult totalResult = testSale.saleTotal(
                                "Тотал:",
                                "",
                                cmdReceipt.FiscalReceipt.FiscalSale.PaidMode.fromOrdinal(0).getId(),
                                "0.01");

                        fiscalReceipt.printFreeText("Не се дължи плащане!", true, true, true, cmdReceipt.FiscalReceipt.FreeFiscalTextType.type32dpiA);
                        //CLOSE
                        fiscalReceipt.closeFiscalReceipt();


                    } else cancelSale();

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        binder.btnInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    DialogInvoice dialogWin =
                            new DialogInvoice(getActivity());
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int width = (int) ((int) displaymetrics.widthPixels * 0.9);
                    int height = (int) ((int) displaymetrics.heightPixels * 0.9);
                    dialogWin.show();

                    dialogWin.getWindow().setLayout(width, height);
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });


        binder.btnCancelAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cancelSale();

            }
        });

        binder.btnCustomCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new CommandFragment(), "custom_cmd_frg");
                    ft.addToBackStack(null);
                    ft.commit();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });


        binder.btnStorno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new StornoFragment(), "storno_frg");
                    ft.addToBackStack(null);
                    ft.commit();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });

        binder.btnNonFiscalReceipt.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                try {

                    //Uncomment to testing customer display
                    /*
                    new cmdDisplay().Clear();
                    Boolean res = new cmdDisplay().SetCodePage1251(true);
                    new cmdDisplay().SetUpperLineText("АБВГДЕЖЗИЙКЛМНОП");
                    new cmdDisplay().SetLowerLineText("РСТУФХЦЧШЩЪьЮЯ");
                    */

                    noFiscalReceipt.open();
                    //Uncomment to testing printing of Barcode:
                    noFiscalReceipt.printNonFiscalText("Баркод EAN8");
                    noFiscalReceipt.printBarcode("0123456", cmdReceipt.BarcodeType.EAN8, "", false);

                    noFiscalReceipt.setPrintBarcodeData(false);
                    noFiscalReceipt.printNonFiscalText("Баркод EAN13");
                    noFiscalReceipt.printBarcode("012345678901", EAN13, "", false);
                    noFiscalReceipt.setPrintBarcodeData(true);
                    noFiscalReceipt.printBarcode("123456789191", cmdReceipt.BarcodeType.Code128, "", false);
                   /* Supported on device group "А" only
                    DATECS FP-800 / FP-2000 / FP-650 / SK1-21F / SK1-31F/ FMP-10 / FP-550
                    noFiscalReceipt.setPrintBarcodeData(true);
                    noFiscalReceipt.printNonFiscalText("Баркод Interleaved 2 of 5 (ITF)");
                    noFiscalReceipt.printBarcode("012345678985", cmdReceipt.BarcdeType.ITF1, "", false);

                    noFiscalReceipt.setPrintBarcodeData(false);
                    noFiscalReceipt.printNonFiscalText("Баркод Interleaved 2 of 5 (ITF)");
                    noFiscalReceipt.printBarcode("012345678986", cmdReceipt.BarcdeType.ITF2, "", false);

                    noFiscalReceipt.setPrintBarcodeData(true);
                    noFiscalReceipt.printNonFiscalText("Баркод Data Matrix");
                    noFiscalReceipt.printBarcode("Welcome to Datecs !!!", cmdReceipt.BarcdeType.DataMatrix, "", false);

                    noFiscalReceipt.setPrintBarcodeData(false);
                    noFiscalReceipt.printNonFiscalText("Баркод QR Code");
                    noFiscalReceipt.printBarcode("Fiscal printer JavaSDK, 2019", cmdReceipt.BarcdeType.QRcode, "", false);
                 */
                    noFiscalReceipt.printNonFiscalText("Благодарим Ви!");
                    noFiscalReceipt.closeNonFiscalReceipt();

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        binder.btnInfoEJ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new EJ_Fragment(), "ej_frg");
                    ft.addToBackStack(null);
                    ft.commit();

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        binder.btnItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new ItemsFragment(), "items_frg");
                    ft.addToBackStack(null);
                    ft.commit();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });
        binder.btnListItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new ItemsListFragment(), "items_list_frg");
                    ft.addToBackStack(null);
                    ft.commit();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });
        binder.btnCashInOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                try {
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    DialogCashInOut dialogWin =
                            new DialogCashInOut(getActivity());

                    lp.copyFrom(dialogWin.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    dialogWin.show();
                    dialogWin.getWindow().setAttributes(lp);
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });
        binder.btnVatsDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    DialogVAT d = new DialogVAT(getActivity());
                    d.show();
                    d.setTitle("Changing VAT rates");
                    d.show();
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int width = (int) ((int) displaymetrics.widthPixels * 0.9);
                    int height = (int) ((int) displaymetrics.heightPixels * 0.9);
                    d.getWindow().setLayout(width, height);

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        binder.btnDepartments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new DepartmentsFragment(), "departments_frg");
                    ft.addToBackStack(null);
                    ft.commit();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });

        binder.btnSetNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new NetworkFragment(), "network_frg");
                    ft.addToBackStack(null);
                    ft.commit();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });


    }

    //DIALOG  ZXReportsSummary
    public class DialogZXReportsSummary extends Dialog implements
            android.view.View.OnClickListener {
        cmdReport.ReportSummary summary;

        private DialogZXReportsSummary(Activity a, cmdReport.ReportSummary summary) {
            //super(a, R.style.Dialog);
            super(a);
            this.summary = summary;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_xz_summary);
            try {

                TextView tvRepNumber = findViewById(R.id.tvReportNumber);
                TextView tvTaxA = findViewById(R.id.tvTaxA);
                TextView tvTaxB = findViewById(R.id.tvTaxB);
                TextView tvTaxC = findViewById(R.id.tvTaxC);
                TextView tvTaxD = findViewById(R.id.tvTaxD);
                TextView tvTaxE = findViewById(R.id.tvTaxE);
                TextView tvTaxF = findViewById(R.id.tvTaxF);
                TextView tvTaxG = findViewById(R.id.tvTaxG);
                TextView tvTaxH = findViewById(R.id.tvTaxH);
                TextView tvTotal = findViewById(R.id.tvTotal);
                Button btnOk = findViewById(R.id.btn_dialogOk);
                btnOk.setOnClickListener(this);
                //Read local curreny name
                cmdService.VAT myVatInfo = new cmdService.VAT();
                myVatInfo.readVatRates();
                String localCurrency = " " + myVatInfo.getCurrencyName();
                tvRepNumber.setText(String.valueOf(summary.reportNumber));
                tvTaxA.setText(amountToString(summary.totalA / mMultiplier) + localCurrency);
                tvTaxB.setText(amountToString(summary.totalB / mMultiplier) + localCurrency);
                tvTaxC.setText(amountToString(summary.totalC / mMultiplier) + localCurrency);
                tvTaxD.setText(amountToString(summary.totalD / mMultiplier) + localCurrency);
                tvTaxE.setText(amountToString(summary.totalE / mMultiplier) + localCurrency);
                tvTaxF.setText(amountToString(summary.totalF / mMultiplier) + localCurrency);
                tvTaxG.setText(amountToString(summary.totalG / mMultiplier) + localCurrency);
                tvTaxH.setText(amountToString(summary.totalH / mMultiplier) + localCurrency);
                tvTotal.setText(amountToString(summary.getFM_Total() / mMultiplier) + localCurrency);

            } catch (Exception e) {
                postToast(e.getMessage());
                e.printStackTrace();
            }

        }

        //DIALOG ON CLICK OK
        @Override
        public void onClick(View v) {
            dismiss();

        }
    }

    /**
     * This method attempts to refuse a fiscal or non-fiscal receipt.
     * And if there is a startup payment on the amount and it is not fully paid,
     * it issues a payment message.
     */
    private void cancelSale() {

        try {
            if (noFiscalReceipt.isOpen()) {
                noFiscalReceipt.closeNonFiscalReceipt();
                return;
            }
            if (new cmdReceipt.FiscalReceipt.Storno().isOpen()) fiscalReceipt.cancel();
            if (fiscalReceipt.isOpen()) {
                final Double owedSum = new cmdReceipt.FiscalReceipt.FiscalTransaction().getNotPaid();//owedSum=Amount-Tender
                Double payedSum = new cmdReceipt.FiscalReceipt.FiscalTransaction().getPaid();
                //If a TOTAL in the opened receipt has not been set, it will be canceled
                if (payedSum == 0.0) {
                    fiscalReceipt.cancel();
                    return;
                }
                //If a TOTAL is set with a partial payment, there is a Amount and Tender is positive.
                //Offer payment of the amount and completion of the sale.
                if (owedSum > 0.0) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle(getString(R.string.app_name));
                    String sQuestion = String.format("Cannot cancel receipt, payment has already started.\n\r" +
                            "Do you want to pay the owed sum: %2.2f -and close it?", owedSum / 100.0);

                    dialog.setMessage(sQuestion);
                    dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                new cmdReceipt.FiscalReceipt.FiscalSale().saleTotal(
                                        "Тотал:",
                                        "",
                                        cmdReceipt.FiscalReceipt.FiscalSale.PaidMode.fromOrdinal(0).getId(),
                                        ""); //We pays a full amount

                                fiscalReceipt.closeFiscalReceipt();
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
                                fiscalReceipt.cancel();
                            } catch (Exception e) {
                                postToast(e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });
                    dialog.show();

                } else fiscalReceipt.closeFiscalReceipt();

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
