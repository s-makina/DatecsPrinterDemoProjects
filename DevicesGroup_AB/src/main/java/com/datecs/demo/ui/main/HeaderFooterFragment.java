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
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.datecs.demo.HeaderFooterFrg_bind;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdConfig;
import com.datecs.testApp.R;

public class HeaderFooterFragment extends Fragment {
    private HeaderFooterFrg_bind binder;

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

            }

        });

        binder.btnSetfooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final int footerLinesCnt = headerFooter.getFooterLinesCount();
                    String[] sFooter = new String[footerLinesCnt];
                    sFooter[0] = binder.edFooter1.getText().toString();
                    sFooter[1] = binder.edFooter2.getText().toString();
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
