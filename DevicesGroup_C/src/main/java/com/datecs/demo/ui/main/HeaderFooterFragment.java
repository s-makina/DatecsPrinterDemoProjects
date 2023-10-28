/**
 * This fragment provides a custom graphical interface for viewing and editing the text strings
 * defining the Header and Footer of the device,
 *
 * @author Datecs Ltd. Software Department
 */
package com.datecs.demo.ui.main;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.datecs.demo.HeaderFooterFrgBinding;
import com.google.android.material.snackbar.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdConfig;
import com.datecs.testApp.R;

public class HeaderFooterFragment extends Fragment {
    private HeaderFooterFrgBinding binder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.header_footer_fragment, container, false);
        return binder.getRoot();

    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final cmdConfig.HeaderFooter headerFooter = new cmdConfig.HeaderFooter();

        binder.btnSetHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int headerLinesCnt = headerFooter.getHeaderLinesCount();
                String[] sHeader = new String[headerLinesCnt];
                sHeader[0] = binder.edHeader1.getText().toString();
                sHeader[1] = binder.edHeader2.getText().toString();
                sHeader[2] = binder.edHeader3.getText().toString();
                sHeader[3] = binder.edHeader4.getText().toString();
                sHeader[4] = binder.edHeader5.getText().toString();
                sHeader[5] = binder.edHeader6.getText().toString();
                sHeader[6] = binder.edHeader7.getText().toString();
                sHeader[7] = binder.edHeader8.getText().toString();
                sHeader[8] = binder.edHeader9.getText().toString();
                sHeader[9] = binder.edHeader10.getText().toString();

                for (int i = 0; i < headerLinesCnt; i++) {
                    try {
                        headerFooter.setHeaderLines(i, sHeader[i]);
                    } catch (Exception e) {
                        postToast(e.getMessage());
                        e.printStackTrace();
                    }
                    Snackbar.make(getView(), R.string.msg_header_saved, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }

            }

        });

        binder.btnReadHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int headerLinesCnt = headerFooter.getHeaderLinesCount();
                String[] sHeaders = new String[headerLinesCnt];
                sHeaders[0] = "";
                for (int i = 0; i < headerLinesCnt; i++) {
                    try {
                        sHeaders[i] = headerFooter.getHeaderLines(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                        postToast(e.getMessage());
                    }
                }

                binder.edHeader1.setText(sHeaders[0]);
                binder.edHeader2.setText(sHeaders[1]);
                binder.edHeader3.setText(sHeaders[2]);
                binder.edHeader4.setText(sHeaders[3]);
                binder.edHeader5.setText(sHeaders[4]);
                binder.edHeader6.setText(sHeaders[5]);
                binder.edHeader7.setText(sHeaders[6]);
                binder.edHeader8.setText(sHeaders[7]);
                binder.edHeader9.setText(sHeaders[8]);
                binder.edHeader10.setText(sHeaders[9]);

            }

        });

        binder.btnSetFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final int footerLinesCnt = headerFooter.getFooterLinesCount();
                    String[] sFooter = new String[footerLinesCnt];
                    sFooter[0] = binder.edFooter1.getText().toString();
                    sFooter[1] = binder.edFooter2.getText().toString();
                    sFooter[2] = binder.edFooter3.getText().toString();
                    sFooter[3] = binder.edFooter4.getText().toString();
                    sFooter[4] = binder.edFooter5.getText().toString();
                    sFooter[5] = binder.edFooter6.getText().toString();
                    sFooter[6] = binder.edFooter7.getText().toString();
                    sFooter[7] = binder.edFooter8.getText().toString();
                    sFooter[8] = binder.edFooter9.getText().toString();
                    sFooter[9] = binder.edFooter10.getText().toString();

                    for (int i = 0; i < footerLinesCnt; i++) {

                        try {
                            headerFooter.setFooterLines(i, sFooter[i]);
                        } catch (Exception e) {
                            postToast(e.getMessage());
                            e.printStackTrace();
                        }
                        Snackbar.make(getView(), R.string.msg_footer_saved, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }

        });

        binder.btnReadFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final int footerLinesCnt = headerFooter.getFooterLinesCount();
                    String[] sFooter = new String[footerLinesCnt];
                    sFooter[0] = "";
                    for (int i = 0; i < footerLinesCnt; i++) {
                        try {
                            sFooter[i] = headerFooter.getFooterLines(i);

                        } catch (Exception e) {
                            e.printStackTrace();
                            postToast(e.getMessage());
                        }
                        binder.edFooter1.setText(sFooter[0]);
                        binder.edFooter2.setText(sFooter[1]);
                        binder.edFooter3.setText(sFooter[2]);
                        binder.edFooter4.setText(sFooter[3]);
                        binder.edFooter5.setText(sFooter[4]);
                        binder.edFooter6.setText(sFooter[5]);
                        binder.edFooter7.setText(sFooter[6]);
                        binder.edFooter8.setText(sFooter[7]);
                        binder.edFooter9.setText(sFooter[8]);
                        binder.edFooter10.setText(sFooter[9]);


                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }

        });

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
