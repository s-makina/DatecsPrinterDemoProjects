package com.datecs.demo.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.datecs.demo.AdditionalDayInfoFrgBinding;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.DailyInfoModel;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdInfo;
import com.datecs.testApp.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AdditionalDayInfo extends Fragment {
    AdditionalDayInfoFrgBinding binder;
    private cmdInfo myInfo = new cmdInfo();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.device_info_fragment, container, false);
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binder.chkStorno.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                        @Override
                                                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                            try {
                                                                //Populate tables
                                                                if (!isChecked) {
                                                                    populateDailyTaxation(binder.tableTaxationTurnover, "Turnover on TAX group:", myInfo.GetDailyTurnoverByTaxGroup(false));
                                                                    populateDailyTaxation(binder.tableTaxationVAT, "Amount on TAX group:", myInfo.GetAmountOnTAXgroup(false));
                                                                } else {

                                                                    populateDailyTaxation(binder.tableTaxationTurnover, "Storno Turnover on TAX group:", myInfo.GetDailyTurnoverByTaxGroup(true));
                                                                    populateDailyTaxation(binder.tableTaxationVAT, "Storno Amount on TAX group:", myInfo.GetAmountOnTAXgroup(true));

                                                                }
                                                            } catch (Exception e) {
                                                                postToast(e.getMessage());
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }
        );

        try {
            String localCurrency = " " + myInfo.GetCurrNameLocal();
            //Registration
            binder.tvDeviceSerialNumber.setText(myInfo.GetDeviceSerialNumber());
            binder.tvTaxNumber.setText(myInfo.GetTaxNumber());

            DateFormat deviceFormat = new SimpleDateFormat("dd-MM-yy");
            Date deviceDate = deviceFormat.parse(myInfo.GetLastFiscalRecordDate());

            binder.tvLastFiscalRecord.setText(deviceFormat.format(deviceDate));
            binder.tvDateOfFiscalization.setText(myInfo.GetActiveTaxRatesDate());
            //Fiscal receipts by Z period:
            DailyInfoModel info = myInfo.GetDailyNumAndSumsOfSells();
            binder.tvNumOfFiscalReceiptsByDay.setText(info.getNumberOfClients() + "/" + info.getSumOfSells() + localCurrency);
            //Number and total of VOID items by day
            info = myInfo.GetDailyNumAndSumsOfVoided();
            binder.tvNumAndSumsOfVoided.setText(info.getNumberOfCorrections() + "/" + info.getSumOfCorrections() + localCurrency);
            //Number and total of canceled receipt by day
            binder.tvNumAndSumsOfCanceld.setText(info.getNumberOfAnnulled() + "/" + info.getSumOfAnnulled() + localCurrency);
            //Number and total of discounts by day
            info = myInfo.GetDailyNumAndSumsOfDiscountsSurcharges();
            binder.tvNumAndSumsOfDiscounts.setText(info.getNumberOfDiscounts() + "/" + info.getSumOfDiscounts() + localCurrency);
            //Number and total of Surcharges by day
            binder.tvNumAndSumsOfSurcharges.setText(info.getNumberOfSurcharges() + "/" + info.getSumOfSurcharges() + localCurrency);
            info = myInfo.GetDailyPayments();
            List<String> paymentTotals = Arrays.asList(
                    info.getPay1() + localCurrency,
                    info.getPay2() + localCurrency,
                    info.getPay3() + localCurrency,
                    info.getPay4() + localCurrency,
                    info.getPay5() + localCurrency,
                    info.getPay6() + localCurrency,
                    info.getForeignPay() + localCurrency);
//Populate table
            binder.tablePayments.removeAllViews();
            for (int i = 0; i < 6; i++) {
                TableRow tr = new TableRow(getContext());
                TextView tvID = new TextView(getContext());
                TextView tvAmount = new TextView(getContext());

                tvAmount.setPadding(11, 0, 11, 0);
                tvID.setText(myInfo.GetPayName(i));
                tvAmount.setText(paymentTotals.get(i));
                tr.addView(tvID);
                tr.addView(tvAmount);
                binder.tablePayments.addView(tr);
            }

            populateDailyTaxation(binder.tableTaxationTurnover, "Turnover on TAX group:", myInfo.GetDailyTurnoverByTaxGroup(false));
            populateDailyTaxation(binder.tableTaxationVAT, "Amount on TAX group:", myInfo.GetAmountOnTAXgroup(false));

        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }


    }

    private void populateDailyTaxation(TableLayout table, String typeOfinfo, cmdInfo.DailyTaxation dailyAmounts) {
        table.removeAllViews();
        for (int i = 0; i < 8; i++) {
            TableRow tr = new TableRow(getContext());
            TextView tvID = new TextView(getContext());
            TextView tvAmount = new TextView(getContext());
            tvAmount.setPadding(11, 0, 11, 0);
            tvID.setText(typeOfinfo + (char) (65 + i));
            tvAmount.setText(dailyAmounts.getSumX(i));
            tr.addView(tvID);
            tr.addView(tvAmount);
            table.addView(tr);
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
