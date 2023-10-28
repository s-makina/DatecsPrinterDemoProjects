package com.datecs.demo.ui.main;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.datecs.demo.DeviceInfo_frg_binding;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdConfig;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdService;
import com.datecs.testApp.R;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.datecs.demo.ui.main.tools.DemoUtil.amountToString;


public class DeviceInfoFragment extends Fragment {
    DeviceInfo_frg_binding binder;
    private cmdInfo myInfo = new cmdInfo();
    private cmdService.VAT myServiceInfo = new cmdService.VAT();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.device_info_fragment, container, false);
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
         try {
            myServiceInfo.readVatRates(); // To read Info: Decimal position, Local currency
            //int decPlace = Integer.valueOf(myServiceInfo.getDecimals());
            String localCurrency = " " + myServiceInfo.getCurrencyName();
            //Registration
            binder.tvDeviceSerialNumber.setText(myInfo.GetDeviceSerialNumber());
            binder.tvTaxNumber.setText(myInfo.GetTaxNumber());


            DateFormat deviceFormat = null;
            if (myInfo.isConnectedECR()) deviceFormat = new SimpleDateFormat("ddMMyy");
            if (myInfo.isConnectedPrinter()) deviceFormat = new SimpleDateFormat("ddMMyyHHmmss");
            Date deviceDate = deviceFormat.parse(myInfo.GetLastFiscalRecordDate());
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            binder.tvLastFiscalRecord.setText(dateFormat.format(deviceDate));
            if (myInfo.isConnectedPrinter())
                binder.tvDateOfFiscalization.setText(myInfo.GetDateTimeOfFiscalization());

            int closue = myInfo.GetDailyPayments().getClosure(); //  to get Closure

            //Fiscal receipts by Z period:
            cmdInfo.DailyInfoModel info = myInfo.GetDailyNumAndSumsOfSells();
            binder.tvNumOfFiscalReceiptsByDay.setText(info.getNumberOfClients() + "/" + info.getSumOfSells() + localCurrency);
            StringBuilder st = new StringBuilder();
            //Turnover and VAT by TAX Group
            for (int i = 0; i < 8; i++)
                st.append((char) (65 + i) + ":" + info.getTurnoverByTaxGroup()[i] + " ");
            binder.tvTurnoverByTaxGroups.setText(st);
            st = new StringBuilder();
            info = myInfo.GetDailyAccumulatedVATByTaxGroup();

            for (int i = 0; i < 8; i++)
                st.append((char) (65 + i) + ":" + info.getAccumulatedVATrByTaxGroup()[i] + " ");
            binder.tvVATByTaxGroups.setText(st);


            //Not supported  FP-800 / FP-2000 / FP-650 / SK1-21F / SK1-31F / FMP-10 / FP-700
            if (!myInfo.isConnectedPrinter()) {
                //Storno turnover and VAT by TAX Group
                st = new StringBuilder();
                info = myInfo.GetDailyStornoTurnoverByTaxGroup();
                for (int i = 0; i < 8; i++)
                    st.append((char) (65 + i) + ":" + info.getTurnoverByTaxGroup()[i] + " ");
                binder.tvStornoTurnoverByTaxGroups.setText(st);
                st = new StringBuilder();
                info = myInfo.GetDailyStornoAccumulatedVATByTaxGroup();
                for (int i = 0; i < 8; i++)
                    st.append((char) (65 + i) + ":" + info.getAccumulatedVATrByTaxGroup()[i] + " ");
                binder.tvStornoVATByTaxGr.setText(st);
            }


                //Number and total of VOID items by day
                info = myInfo.GetDailyNumAndSumsOfVoided();
                binder.tvNumAndSumsOfVoided.setText(info.getNumberOfCorrections() + "/" + amountToString(info.getSumOfCorrections()) + localCurrency);
                //Number and total of canceled receipt by day
                binder.tvNumAndSumsOfCanceld.setText(info.getNumberOfAnnulled() + "/" + amountToString(info.getSumOfAnnulled()) + localCurrency);
                //Number and total of discounts by day
                info = myInfo.GetDailyNumAndSumsOfDiscountsSurcharges();
                binder.tvNumAndSumsOfDiscounts.setText(info.getNumberOfDiscounts() + "/" + amountToString(info.getSumOfDiscounts()) + localCurrency);
                //Number and total of Surcharges by day
                binder.tvNumAndSumsOfSurcharges.setText(info.getNumberOfSurcharges() + "/" + amountToString(info.getSumOfSurcharges()) + localCurrency);

                info = myInfo.GetDailyPayments();
                binder.tvDailyPaymentsCash.setText(amountToString(info.getCash()) + localCurrency);
                binder.tvDailyPaymentCredit.setText(amountToString(info.getCredit()) + localCurrency);
                binder.tvDailyPaymentDebit.setText(amountToString(info.getDebit()) + localCurrency);
                binder.tvDailyPaymentCheque.setText(amountToString(info.getCheque()) + localCurrency);//Not supported on DP-05, DP-25, DP-35, WP-50, DP-150

            if (myInfo.isConnectedPrinter()) {
                info = myInfo.GetDailyPaymentsEx();
                populate_AdditionalPaymentTablePrinter(info.getAdditionalPaymentEX(), localCurrency);
            } else populate_AdditionalPaymentTableECR(info, localCurrency);
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }
    }

    private void populate_AdditionalPaymentTableECR(cmdInfo.DailyInfoModel info, String localCurrency) throws Exception {
        TableRow tr = new TableRow(getContext());
        TextView tvID = new TextView(getContext());
        TextView tvAmount = new TextView(getContext());
        tvID.setText(new cmdConfig().GetPayName(1));
        tvAmount.setText(info.getPay1() + localCurrency);
        tr.addView(tvID);
        tr.addView(tvAmount);
        binder.mytable.addView(tr);
        tr = new TableRow(getContext());
        tvID = new TextView(getContext());
        tvAmount = new TextView(getContext());
        tvID.setText(new cmdConfig().GetPayName(2));
        tvAmount.setText(info.getPay2() + localCurrency);
        tr.addView(tvID);
        tr.addView(tvAmount);
        binder.mytable.addView(tr);
        tr = new TableRow(getContext());
        tvID = new TextView(getContext());
        tvAmount = new TextView(getContext());
        tvID.setText(new cmdConfig().GetPayName(3));
        tvAmount.setText(info.getPay3() + localCurrency);
        tr.addView(tvID);
        tr.addView(tvAmount);
        binder.mytable.addView(tr);
    }

    //Used on  FP-800 / FP-2000 / FP-650 / SK1-21F / SK1-31F / FMP-10 / FP-700
    public void populate_AdditionalPaymentTablePrinter(double[] additionalPaymentEx, String localCurr) {
        ProgressDialog progress = ProgressDialog.show(getContext(), getString(R.string.title_reading_items), getString(R.string.msg_please_wait), true);
        binder.mytable.setStretchAllColumns(true);
        binder.mytable.bringToFront();
        int numOfPayNames = myInfo.getAdditionalPayNamesCount();
        List<String> paymnetNames = new ArrayList<String>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // do the thing that takes a long time
                try {
                    for (int i = 1; i <= numOfPayNames; i++)
                        paymnetNames.add(new cmdConfig().GetPayName(i));
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
                        try {
                            for (int i = 0; i < numOfPayNames; i++) {
                                TableRow tr = new TableRow(getContext());
                                TextView tvID = new TextView(getContext());
                                TextView tvAmount = new TextView(getContext());
                                tvID.setText(paymnetNames.get(i));
                                tvAmount.setText(amountToString(additionalPaymentEx[i]) + localCurr);
                                tr.addView(tvID);
                                tr.addView(tvAmount);
                                binder.mytable.addView(tr);
                            }
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
