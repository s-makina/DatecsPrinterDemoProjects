/*
 * @author Datecs Ltd. Software Department
 */

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

import com.datecs.demo.MenuFrgBinding;
import com.datecs.demo.PrinterManager;
import com.datecs.fiscalprinter.SDK.BuildInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReport;
import com.datecs.testApp.R;

import static com.datecs.demo.PrinterManager.getFiscalDevice;
import static com.datecs.demo.ui.main.tools.DemoUtil.amountToString;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt.BarcdeType.Code128;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt.BarcdeType.EAN13;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt.BarcdeType.EAN8;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt.BarcdeType.Interleave;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt.BarcdeType.PDF417normal;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt.BarcdeType.PDF417trunc;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdReceipt.BarcdeType.QRcode;

public class MenuFragment extends Fragment {

    private cmdReceipt.FiscalReceipt fiscalReceipt;
    private cmdReceipt.NonFiscalReceipt noFiscalReceipt;
    private ProgressDialog progress;
    private MenuFrgBinding binder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binder = DataBindingUtil.inflate(inflater, R.layout.menu_fragment, container, false);
        //Create fiscal receipt  instance
        fiscalReceipt = new cmdReceipt.FiscalReceipt();
        noFiscalReceipt = new cmdReceipt.NonFiscalReceipt();
        //Create  non fiscal receipt instance
        binder.tvSDKBuild.setText("Datecs JSDK Build: " + BuildInfo.VERSION);

        //To check the status of the device, send a command to fill in the status bits
        try {
            new cmdInfo().GetDeviceSerialNumber();
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }

        return binder.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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


        binder.btnAdditionalDayInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new AdditionalDayInfo(), "additional_day_info");
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


        binder.btnZReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new ProgressDialog(getContext());
                progress.setCancelable(true);
                progress.setTitle("Z report starting !!!");
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
                            reportNumber[0] = cmd.PrintZreport(reportSummary);
                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
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
                                progress.dismiss();

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
                progress.setTitle("X report starting !!!");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
                final int[] reportNummber = new int[1];
                final cmdReport.ReportSummary reportSummary = new cmdReport.ReportSummary();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            cmdReport cmd = new cmdReport();
                            reportNummber[0] = cmd.PrintXreport(reportSummary);
                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
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
                                progress.dismiss();
                            }
                        });
                    }
                }).start();

            }
        });

/**
 *
 * Here are a set of Java SDK methods for creating and opening a fiscal receipt,
 * creating a sales and add items in the receipt.
 * Payment of purchases, and closing the receipt.
 *
 */

        binder.btnSaleTest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String salePoint = "1"; //Number of point of sale from 1...99999;

                try {
                    //Note: WP-500X, WP-50X, WP-25X, DP-25X, DP-150X, DP-05C: the default password for each operator is
                    //equal to the corresponding number (for example, for Operator1 the password is "1") .
                    // FMP-350X, FMP-55X,FP-700X: the default password for each operator is “0000”
                    //Set Operator and password

                    Integer operatorCode = Integer.valueOf(getFiscalDevice().getConnectedModelV2().getCurrentOpCode());
                    //String operatorPassword = getFiscalDevice().getConnectedModelV2().getDefaultOpPass();
                    String operatorPassword = new cmdInfo().GetOperPasw(operatorCode - 1);//Password of operator. Text up to 8 symbols. ( Require Service jumper )

                    if (!fiscalReceipt.isOpen()) {
                        //Open Fiscal bon in current receipt
                        fiscalReceipt.openFiscalReceipt(
                                String.valueOf(operatorCode),
                                operatorPassword,
                                salePoint,
                                false);
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);

                        //Registration of item for sale with the minimum required set of parameters
                        cmdReceipt.FiscalReceipt.FiscalSale testSale =
                                new cmdReceipt.FiscalReceipt.FiscalSale(
                                        "Бонбон",
                                        "2",
                                        "0.01").add();

                        //TOTAL in Current Currency
                        testSale.saleTotal(cmdReceipt.FiscalSale.PaymentType.cash, "");//Full payment ""
                        cmdReceipt.FiscalTransaction fiscalTransaction = new cmdReceipt.FiscalTransaction();
                        //TOTAL in Foreign Currency
                        //testSale.saleTotalForeignCurrency("0.10", cmdReceipt.FiscalSale.TypeOfChange.currentCurrency);
                        fiscalReceipt.closeFiscalReceipt();
                    } else cancelSale();

                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }
        });

/**
 *
 * Here are a set of Java SDK methods for creating and opening a fiscal receipt,
 * creating a sales and add items in the receipt,
 * void of sales items, printing of text, and separating lines,
 * Payment of purchases, and closing the receipt.
 *
 */
        binder.btnSaleTestEx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String salePoint = "1"; //Number of point of sale from 1...99999;
                String NSale; //Unique sale number (21 chars "LLDDDDDD-CCCC-DDDDDDD"
                try {
                    if (!fiscalReceipt.isOpen()) {
                        //Note: WP-500X, WP-50X, WP-25X, DP-25X, DP-150X, DP-05C: the default password for each operator is
                        //equal to the corresponding number (for example, for Operator1 the password is "1") . FMP-350X, FMP-55X,
                        //FP-700X: the default password for each operator is “0000”
                        Integer operatorCode = Integer.valueOf(getFiscalDevice().getConnectedModelV2().getCurrentOpCode());
                        //String operatorPassword = getFiscalDevice().getConnectedModelV2().getDefaultOpPass();
                        String operatorPassword = new cmdInfo().GetOperPasw(operatorCode - 1);//Password of operator. Text up to 8 symbols. ( Require Service jumper )
                        //Creating the unique sale number "LLDDDDDD-CCCC-DDDDDDD"
                        NSale = new cmdInfo().GetDeviceSerialNumber() + "-" +
                                String.format("%04d", operatorCode) + "-" + // Pad left with trailing zero
                                String.format("%07d", 1 + Integer.parseInt(fiscalReceipt.getAllreceipt()));  //Next Document number pad left with trailing zero

                        //Open Fiscal bon in current receipt and return number of receipt
                        fiscalReceipt.openFiscalReceipt(
                                String.valueOf(operatorCode),
                                operatorPassword,
                                NSale,
                                salePoint,
                                false);
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);


                        //Registration of item for sale
                        cmdReceipt.FiscalReceipt.FiscalSale testSale =
                                new cmdReceipt.FiscalReceipt.FiscalSale(
                                        "Продажба с количество",
                                        "2",
                                        "0.01",
                                        "2.555",
                                        cmdReceipt.FiscalSale.DiscountType.noDiscount,
                                        "",
                                        "",
                                        "kg").add();
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash_space);


                        //VOID of Registered  item for sale (Discount by percentage)
                        testSale.add(
                                "Продажба с количество",
                                "2",
                                "",
                                "-0.01", //Product price, with sign '-' at void operations.
                                "2.555",
                                cmdReceipt.FiscalSale.DiscountType.discountPercentage,
                                "",
                                "kg");
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash_space);

                        //SUBTOTAL
                        testSale.printSubtotal(false, cmdReceipt.FiscalSale.DiscountType.noDiscount, "");// No display
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash_space);

                        //Registration of item for sale (Minimum of parameters)
                        testSale.add(
                                "Продажба с минимум параметри",
                                "2",
                                "",
                                "0.01",
                                "",
                                cmdReceipt.FiscalSale.DiscountType.noDiscount,
                                "");
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash_space);
                        //SUBTOTAL
                        testSale.printSubtotal(false, cmdReceipt.FiscalSale.DiscountType.noDiscount, "");// No display


                        //Registration of item for sale  (Surcharge by value)
                        testSale.add(
                                "Продажба с надбавка по стойност",
                                "2",
                                "",
                                "0.01",
                                "",
                                cmdReceipt.FiscalSale.DiscountType.surchargeSum,
                                "0.01");
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash_space);
                        //SUBTOTAL
                        testSale.printSubtotal(false, cmdReceipt.FiscalSale.DiscountType.noDiscount, "");// No display

                        //Registration of item for sale (Surcharge by percentage )
                        testSale.add(
                                "Продажба процентна надбавка",
                                "2",
                                "",
                                "0.01",
                                "",
                                cmdReceipt.FiscalSale.DiscountType.surchargePercentage,
                                "10");
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash_space);
                        //SUBTOTAL
                        testSale.printSubtotal(false, cmdReceipt.FiscalSale.DiscountType.noDiscount, "");// No display

                        //Registration of item for sale (Discount by percentage)
                        testSale.add(
                                "Продажба с отсъпка по стойнст",
                                "2",
                                "",
                                "0.1",
                                "",
                                cmdReceipt.FiscalSale.DiscountType.discountSum,
                                ".09");
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash_space);
                        //SUBTOTAL
                        testSale.printSubtotal(false, cmdReceipt.FiscalSale.DiscountType.noDiscount, "");// No display

                        //Registration of item for sale (Discount by percentage)
                        testSale.add(
                                "Продажба с отсъпка в %",
                                "2",
                                "",
                                "0.1",
                                "",
                                cmdReceipt.FiscalSale.DiscountType.discountPercentage,
                                "10");
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash_space);
                        //SUBTOTAL
                        testSale.printSubtotal(false, cmdReceipt.FiscalSale.DiscountType.noDiscount, "");// No display

                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash_space);
                        fiscalReceipt.printFreeText("Благодарим Ви!");
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.text);
                        testSale.printSubtotal(false, cmdReceipt.FiscalSale.DiscountType.noDiscount, "");

                        //SUBTOTAL with Discount
                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash_space);
                        fiscalReceipt.printFreeText("Отстъпка в междинна сума");
                        testSale.printSubtotal(false, cmdReceipt.FiscalSale.DiscountType.discountSum, "0.13");

                        //Note! If you use a correction(discount or surcharge) in readSubtotal method, it will be reflected in the subtotal amount
                        String toPay = testSale.readSubtotal(false, cmdReceipt.FiscalSale.DiscountType.noDiscount, "");

                        //TOTAL
                        testSale.saleTotal(
                                cmdReceipt.FiscalSale.PaymentType.cash,
                                toPay    //  Whole sum
                        );
                        fiscalReceipt.closeFiscalReceipt();
                    } else cancelSale();

                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
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


//
//                    DialogCustomCommand d = new DialogCustomCommand(getActivity());
//                    d.show();
//                    d.setTitle("Execute Command");
//                    d.show();
//                    DisplayMetrics displaymetrics = new DisplayMetrics();
//                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//                    int width = (int) ((int) displaymetrics.widthPixels * 0.9);
//                    int height = (int) ((int) displaymetrics.heightPixels * 0.9);
//                    d.getWindow().setLayout(width, height);
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

        binder.btnInvoice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                try {
                    DialogInvoice d = new DialogInvoice(getActivity());
                    d.show();
                    d.setTitle("Invoice Printing Demo");
                    d.show();
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int width = (int) ((int) displaymetrics.widthPixels * 0.95);
                    int height = (int) ((int) displaymetrics.heightPixels * 0.90);
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
        binder.btnNonFiscalReceipt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cmdReceipt.NonFiscalReceipt myTestNonfiscalRec = new cmdReceipt.NonFiscalReceipt();
                try {
                    if (!myTestNonfiscalRec.isOpen()) myTestNonfiscalRec.openNonFiscalReceipt();
                    myTestNonfiscalRec.printBarcode(EAN8, "1234567", 0);
                    myTestNonfiscalRec.printBarcode(EAN13, "123456789191", 0);
                    myTestNonfiscalRec.printBarcode(Code128, "123456789191", 0);
                    myTestNonfiscalRec.printBarcode(Interleave, "123456789191", 0);
                    //bcQRSize - Dots multiplier ( 3...10 ) for QR barcodes and PDF417 barcodes. Default: 4;
                    myTestNonfiscalRec.printBarcode(QRcode, "1234567", 10);
                    myTestNonfiscalRec.printBarcode(PDF417trunc, "123456789191", 10);
                    myTestNonfiscalRec.printBarcode(PDF417normal, "123456789191", 10);
                    noFiscalReceipt.printNonFiscalText("Благодарим Ви!");
                    noFiscalReceipt.closeNonFiscalReceipt();

                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
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
        binder.btnEjStruct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new EjStruct_Fragment(), "ej_struct_frg");
                    ft.addToBackStack(null);
                    ft.commit();

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        binder.btnItemsList.setOnClickListener(new View.OnClickListener() {
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

        binder.btnClients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!PrinterManager.isECR())
                        postToast("Not Supported on FMP-350X, FMP-55X, FP-700X , FP-700XE");
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
        binder.btnPinpadDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new PinpadFragment(), "pinpad_demo_frg");
                    ft.addToBackStack(null);
                    ft.commit();

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
                    int width = (int) ((int) displaymetrics.widthPixels * 0.85);
                    int height = (int) ((int) displaymetrics.heightPixels * 0.75);
                    d.getWindow().setLayout(width, height);

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
        private final cmdReport.ReportSummary summary;

        private DialogZXReportsSummary(Activity a, cmdReport.ReportSummary summary) {
            super(a);
            this.summary = summary;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_xz_summary);

            TextView tvRepNum = findViewById(R.id.tvReportNumber);
            TextView tvTotal = findViewById(R.id.tvTotal);
            TextView tvTaxA = findViewById(R.id.tvTaxA);
            TextView tvTaxB = findViewById(R.id.tvTaxB);
            TextView tvTaxC = findViewById(R.id.tvTaxC);
            TextView tvTaxD = findViewById(R.id.tvTaxD);
            TextView tvTaxE = findViewById(R.id.tvTaxE);
            TextView tvTaxF = findViewById(R.id.tvTaxF);
            TextView tvTaxG = findViewById(R.id.tvTaxG);
            TextView tvTaxH = findViewById(R.id.tvTaxH);

            tvRepNum.setText(String.valueOf(summary.reportNumber));
            tvTaxA.setText(amountToString(summary.totalA));
            tvTaxB.setText(amountToString(summary.totalB));
            tvTaxC.setText(amountToString(summary.totalC));
            tvTaxD.setText(amountToString(summary.totalD));
            tvTaxE.setText(amountToString(summary.totalE));
            tvTaxF.setText(amountToString(summary.totalF));
            tvTaxG.setText(amountToString(summary.totalG));
            tvTaxH.setText(amountToString(summary.totalH));
            tvTotal.setText(amountToString(summary.getTotalSumSell()));
            Button btnOk = findViewById(R.id.btn_dialogOk);
            btnOk.setOnClickListener(this);
        }

        //DIALOG ON CLICK OK
        @Override
        public void onClick(View v) {
            dismiss();

        }
    }

    /**
     * This method attempts to refuse a fiscal or non-fiscal receipt.
     * And if there a startup payment on the amount and it is not fully paid,
     * it issues a payment message.
     */
    private void cancelSale() {

        try {
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
