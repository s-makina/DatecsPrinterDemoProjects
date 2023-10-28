/**
 * This fragment demonstrated the SDК methods for changing the date and time of the Fiscal Device.
 * Attention !!!
 * Changing the clock settings is an operation that may blocking your device's fiscal operation.
 *
 * @author Datecs Ltd. Software Department
 */
package com.datecs.demo.ui.main;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.datecs.demo.DateTimeFrg_binding;
import com.google.android.material.snackbar.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.datecs.demo.ui.main.tools.SetTime;
import com.datecs.demo.ui.main.tools.TextViewDatePicker;
import com.datecs.fiscalprinter.SDK.FiscalException;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdConfig;
import com.datecs.testApp.R;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeFragment extends Fragment {
    private DateTimeFrg_binding binder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.data_time_fragment, container, false);
        showAndroidDT();
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextViewDatePicker editTextDatePicker = new TextViewDatePicker(getContext(), binder.edDate);
        SetTime toTime = new SetTime(getContext(), binder.edTime);

        binder.btnReadDateTimeAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAndroidDT();
            }
        });

        binder.btnReadDateTimeFiscalDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cmdConfig.DateTime myClock = new cmdConfig.DateTime();
                    binder.edDate.setText(myClock.getDate());
                    binder.edTime.setText(myClock.getTime());

                } catch (FiscalException e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                } catch (IOException e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }

            }
        });

        binder.btnSetDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cmdConfig.DateTime myClock = new cmdConfig.DateTime();
                    myClock.setDateTime(binder.edDate.getText().toString(), binder.edTime.getText().toString());
                    Snackbar.make(view, R.string.data_time_is_set, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } catch (FiscalException e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isResumed() && isVisibleToUser) {
            showAndroidDT();

        }
    }

    private void showAndroidDT() {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
            String currentDate = sdf.format(new Date());
            binder.edDate.setText(currentDate);
            sdf = new SimpleDateFormat("HH:mm:ss");
            String currentTime = sdf.format(new Date());
            binder.edTime.setText(currentTime);
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
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
