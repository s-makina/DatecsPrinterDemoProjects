/**
 * This fragment provides a custom graphical user interface for document search in the device's
 * INFORMATION ON A FISCAL RECORD OR FISCAL PERIOD
 * <p>
 *
 * @author Datecs Ltd. Software Department
 */

package com.datecs.demo.ui.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import com.datecs.demo.FiscalPeriodFrgBinding;
import com.datecs.demo.ui.main.tools.DemoUtil;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdConfig;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdService;
import com.datecs.testApp.R;

import java.util.ArrayList;
import java.util.List;

import static androidx.core.content.ContextCompat.getMainExecutor;
import static com.datecs.demo.ui.main.tools.DemoUtil.amountToString;

public class FiscalPeriodInfoFrag extends Fragment {
    private FiscalPeriodFrgBinding binder;
    private ProgressDialog progress;
    private String localCurrency;
    private cmdInfo myInfo = new cmdInfo();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.fisc_period_frag, container, false);
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            int lastReportNum = myInfo.GetDailyPayments().getClosure(); //  to get Closure
            binder.edFromRecord.setText(String.valueOf(lastReportNum));
            binder.edToRecord.setText(String.valueOf(lastReportNum));
            cmdService.VAT myVATinfo = new cmdService.VAT();
            myVATinfo.readVatRates();//Execute command
            localCurrency = " " + myVATinfo.getCurrencyName();
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }


        binder.btnReadPeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog progress = ProgressDialog.show(getContext(), getString(R.string.title_reading_items), getString(R.string.msg_please_wait), true);
                getMainExecutor(getContext()).execute(new Runnable() {
                    @Override
                    public void run() {
                        // Code will run on the main thread
                        try {
                            int Rec1 = Integer.valueOf(binder.edFromRecord.getText().toString());
                            int Rec2 = Integer.valueOf(binder.edToRecord.getText().toString());
                            populate_Table0(myInfo.GetFiscRecord_ActiveTaxARates(Rec2));
                            populate_Table1(myInfo.GetFiscRecord_TotalSales(Rec1, Rec2));
                            populate_Table2(myInfo.GetFiscRecord_NetAmounts(Rec1, Rec2));
                            populate_Table3(myInfo.GetFiscRecord_VatAssessed(Rec1, Rec2));
                            populate_Table4(myInfo.GetFiscRecord_AdditionalInfo(Rec1));
                            populate_Table7(myInfo.GetFiscRecord_AmountsByPaymentType(Rec1, Rec2));
                            populate_Table8(myInfo.GetFiscRecord_Adjustments(Rec1, Rec2));
                        } catch (Exception e) {
                            postToast(e.getMessage());
                            e.printStackTrace();
                        } finally {
                            progress.dismiss();
                        }
                    }
                });

            }
        });


    }

    /**
     * Information on the active tax rates for the Z-report record in question.
     *
     * @throws Exception
     */
    private void populate_Table0(cmdInfo.FiscalPeriodInfo info) throws Exception {

        TableRow tr = new TableRow(getContext());
        TextView tvID = new TextView(getContext());
        TextView tvAmount = new TextView(getContext());
        tvID.setText("Last (active) record with tax rates.");
        tvAmount.setText(String.valueOf(info.getDecRec()));
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table0.addView(tr);
        tr = new TableRow(getContext());
        tvID = new TextView(getContext());
        tvAmount = new TextView(getContext());
        tvID.setText("Number of decimal digits for the specified Z-report record.");
        tvAmount.setText(String.valueOf(info.getDec()));
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table0.addView(tr);
        tr = new TableRow(getContext());
        tvID = new TextView(getContext());
        tvAmount = new TextView(getContext());
        tvID.setText("Tax rate for the respective tax group, as a percentage.");
        String sb = "";
        for (int i = 0; i < info.getPerX().length; i++)
            sb += ((char) (65 + i)) + ":" + String.valueOf(info.getPerX()[i]) + "% ";
        tvAmount.setText(sb);
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table0.addView(tr);
        binder.table0.addView(rowSeparator());
    }

    private TableRow rowSeparator() {
        TableRow tr = new TableRow(getContext());
        TextView tvID = new TextView(getContext());
        tvID.setText("REPORTS INFO_________________________from:" + binder.edFromRecord.getText() + " to:" + binder.edToRecord.getText().toString());
        tr.addView(tvID);

        tr.setGravity(Gravity.CENTER);
        return tr;
    }

    /**
     * IInformation on the sales for the specified record or period.
     *
     * @throws Exception
     */
    private void populate_Table1(cmdInfo.FiscalPeriodInfo info) throws Exception {
        TableRow tr = new TableRow(getContext());
        TextView tvID = new TextView(getContext());
        TextView tvAmount = new TextView(getContext());
        tvID.setText("Number of fiscal receipts/clients");
        tvAmount.setText(String.valueOf(info.getReceipts()) + "/" + info.getClCnt());
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table1.addView(tr);
        tr = new TableRow(getContext());
        tvID = new TextView(getContext());
        tvAmount = new TextView(getContext());
        tvID.setText("Sales for the respective tax group.");
        String sb = "";
        for (int i = 0; i < info.getTotX().length; i++)
            sb += ((char) (65 + i)) + ":" + String.valueOf(info.getTotX()[i]) + " ";
        tvAmount.setText(sb);
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table1.addView(tr);
        binder.table1.addView(rowSeparator());
    }

    /**
     * Information on the net amounts for the specified record or period.
     *
     * @throws Exception
     */
    private void populate_Table2(cmdInfo.FiscalPeriodInfo info) throws Exception {
        TableRow tr = new TableRow(getContext());
        TextView tvID = new TextView(getContext());
        TextView tvAmount = new TextView(getContext());
        tvID.setText("Net amount for the respective tax group.");
        String sb = "";
        for (int i = 0; i < info.getNetX().length; i++)
            sb += ((char) (65 + i)) + ":" + String.valueOf(info.getNetX()[i]) + " ";
        tvAmount.setText(sb);
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table2.addView(tr);
        binder.table2.addView(rowSeparator());
    }

    /**
     * Information on the VAT assessed for the specified record or period.
     *
     * @throws Exception
     */
    private void populate_Table3(cmdInfo.FiscalPeriodInfo info) throws Exception {
        TableRow tr = new TableRow(getContext());
        TextView tvID = new TextView(getContext());
        TextView tvAmount = new TextView(getContext());
        tvID.setText("VAT assessed for the respective tax group.");
        String sb = "";
        for (int i = 0; i < info.getTaxX().length; i++)
            sb += ((char) (65 + i)) + ":" + String.valueOf(info.getTaxX()[i]) + " ";
        tvAmount.setText(sb);
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table3.addView(tr);
        binder.table3.addView(rowSeparator());
    }

    /**
     * Additional information on the specified record.
     *
     * @throws Exception
     */
    private void populate_Table4(cmdInfo.FiscalPeriodInfo info) throws Exception {
        TableRow tr = new TableRow(getContext());
        TextView tvID = new TextView(getContext());
        TextView tvAmount = new TextView(getContext());
        tvID.setText("EJ number for this fiscal block.");
        tvAmount.setText(String.valueOf(info.getKLEN()));
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table4.addView(tr);

        tr = new TableRow(getContext());
        tvID = new TextView(getContext());
        tvAmount = new TextView(getContext());
        tvID.setText("Последен (активен) запис с данъчни ставки.");
        tvAmount.setText(String.valueOf(info.getDecRec()));
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table4.addView(tr);

        tr = new TableRow(getContext());
        tvID = new TextView(getContext());
        tvAmount = new TextView(getContext());
        tvID.setText("Fiscal record number.");
        tvAmount.setText(String.valueOf(info.getClosure()));
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table4.addView(tr);

        tr = new TableRow(getContext());
        tvID = new TextView(getContext());
        tvAmount = new TextView(getContext());
        tvID.setText("Last RAM reset before this fiscal block.");
        tvAmount.setText(String.valueOf(info.getReset()));
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table4.addView(tr);
        binder.table4.addView(rowSeparator());
    }

    /**
     * Information on the specified FM record with RAM reset.
     *
     * @param info
     * @param localCurrency
     * @throws Exception
     */
    private void populate_Table6(cmdInfo.DailyInfoModel info) throws Exception {
//        TableRow tr = new TableRow(getContext());
//        TextView tvID = new TextView(getContext());
//        TextView tvAmount = new TextView(getContext());
//        tvID.setText("Date and time.");
//        tvAmount.setText("Hello");
//        tr.addView(tvID);
//        tr.addView(tvAmount);
//        tr.setGravity(Gravity.CENTER);
//        binder.table6.addView(tr);

    }

    /**
     * Information on the amounts by payment type for the specified record or period.
     *
     * @param info
     * @param localCurrency
     * @throws Exception
     */
    private void populate_Table7(cmdInfo.FiscalPeriodInfo info) throws Exception {
        TableRow tr = new TableRow(getContext());
        TextView tvID = new TextView(getContext());
        TextView tvAmount = new TextView(getContext());
        tvID.setText("Paid in cash." + localCurrency);
        tvAmount.setText(DemoUtil.amountToString(info.getCashP()));
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table7.addView(tr);

        tr = new TableRow(getContext());
        tvID = new TextView(getContext());
        tvAmount = new TextView(getContext());
        tvID.setText("Paid by debit card." + localCurrency);
        tvAmount.setText(DemoUtil.amountToString(info.getCardP()));
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table7.addView(tr);

        tr = new TableRow(getContext());
        tvID = new TextView(getContext());
        tvAmount = new TextView(getContext());
        tvID.setText("Paid by credit card." + localCurrency);
        tvAmount.setText(DemoUtil.amountToString(info.getCredP())
        );
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table7.addView(tr);

        tr = new TableRow(getContext());
        tvID = new TextView(getContext());
        tvAmount = new TextView(getContext());
        tvID.setText("Paid by check." + localCurrency);
        tvAmount.setText(DemoUtil.amountToString(info.getCheqP()));
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table7.addView(tr);
        populate_AdditionalPaymentTable(info.getAPayd(), localCurrency);

    }


    public void populate_AdditionalPaymentTable(double[] additionalPaymentEx, String localCurr) {
        ProgressDialog progress = ProgressDialog.show(getContext(), getString(R.string.title_reading_items), getString(R.string.msg_please_wait), true);
        binder.table7.setStretchAllColumns(true);
        binder.table7.bringToFront();
        List<String> paymnetNames = new ArrayList<String>();
 /*

        new Thread(new Runnable() {
            @Override
            public void run() {
                // do the thing that takes a long time
                try {
                    for (int i = 1; i <= additionalPaymentEx.length; i++)
                        paymnetNames.add(new cmdConfig().GetPayName(i));
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
                            for (int i = 0; i < additionalPaymentEx.length; i++) {
                                TableRow tr = new TableRow(getContext());
                                TextView tvID = new TextView(getContext());
                                TextView tvAmount = new TextView(getContext());
                                tvID.setText(paymnetNames.get(i));
                                tvAmount.setText(amountToString(additionalPaymentEx[i]) + localCurr);
                                tr.addView(tvID);
                                tr.addView(tvAmount);
                                tr.setGravity(Gravity.CENTER);
                                binder.table7.addView(tr);
                            }
                            binder.table7.addView(rowSeparator());
                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                        } finally {
                            progress.dismiss();
                        }
                    }
                });
            }
        }).start();
 */

        try {
            for (int i = 1; i <= additionalPaymentEx.length; i++)
                paymnetNames.add(new cmdConfig().GetPayName(i));
        } catch (Exception e) {
            e.printStackTrace();
            postToast(e.getMessage());
        } finally {
            progress.dismiss();
        }


        try {
            for (int i = 0; i < additionalPaymentEx.length; i++) {
                TableRow tr = new TableRow(getContext());
                TextView tvID = new TextView(getContext());
                TextView tvAmount = new TextView(getContext());
                tvID.setText(paymnetNames.get(i));
                tvAmount.setText(amountToString(additionalPaymentEx[i]) + localCurr);
                tr.addView(tvID);
                tr.addView(tvAmount);
                tr.setGravity(Gravity.CENTER);
                binder.table7.addView(tr);
            }
            binder.table7.addView(rowSeparator());
        } catch (Exception e) {
            e.printStackTrace();
            postToast(e.getMessage());
        } finally {
            progress.dismiss();
        }


    }

    /**
     * Information on the amounts by payment type for the specified record or period.
     *
     * @param info
     * @param localCurrency
     * @throws Exception
     */
    private void populate_Table8(cmdInfo.FiscalPeriodInfo info) {
        TableRow tr = new TableRow(getContext());
        TextView tvID = new TextView(getContext());
        TextView tvAmount = new TextView(getContext());
        tvID.setText("Mark-ups (surcharges) number/sum" + localCurrency);
        tvAmount.setText(info.getMkUpC() + "/" + DemoUtil.amountToString(info.getMkUpS()));
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table8.addView(tr);

        tr = new TableRow(getContext());
        tvID = new TextView(getContext());
        tvAmount = new TextView(getContext());
        tvID.setText("Discounts number/sum" + localCurrency);
        tvAmount.setText(info.getDiscC() + "/" + DemoUtil.amountToString(info.getDiscC()));
        tvAmount.setText(DemoUtil.amountToString(info.getCardP()));
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table8.addView(tr);

        tr = new TableRow(getContext());
        tvID = new TextView(getContext());
        tvAmount = new TextView(getContext());
        tvID.setText("VOID number/sum" + localCurrency);
        tvAmount.setText(info.getVoidC() + "/" + DemoUtil.amountToString(info.getVoidS()));

        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table8.addView(tr);

        tr = new TableRow(getContext());
        tvID = new TextView(getContext());
        tvAmount = new TextView(getContext());
        tvID.setText("Cancelled receipts number/total" + localCurrency);
        tvAmount.setText(info.getCanC() + "/" + DemoUtil.amountToString(info.getCanS()));
        tr.addView(tvID);
        tr.addView(tvAmount);
        tr.setGravity(Gravity.CENTER);
        binder.table8.addView(tr);
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

