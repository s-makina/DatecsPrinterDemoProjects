/**
 * This fragment provides a custom graphical interface for viewing and editing the text strings
 * defining the Header and Footer of the device,
 *
 * @author Datecs Ltd. Software Department
 */
package com.datecs.demo.ui.main;

import android.os.Bundle;
import android.text.Layout;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import com.datecs.demo.CommandFragment_binding;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdService;
import com.datecs.testApp.R;

import java.util.ArrayList;


public class CommandFragment extends Fragment {


    private CommandFragment_binding binder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.custom_cmd_fragment, container, false);
        return binder.getRoot();

    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        binder.btnCustomCmdExec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmdService myCommand = new cmdService();
                String cmdResult = "";
                ArrayList<CommandData> cmdList = null;
                int i = 0;
                try {
                    binder.edCustCmdRes.setText("");
                    cmdList = ScripParser(binder.edCommandScript.getText().toString());

                    binder.edCustCmdRes.setMaxLines(cmdList.size() + 2);
                    binder.edCustCmdRes.setLines(cmdList.size());

                    for (i = 0; i < cmdList.size(); i++) {
                        cmdResult = myCommand.customCommand(cmdList.get(i).getCmdIndex(), cmdList.get(i).getCmdData().replace("[\\t]", "\t"));
                        binder.edCustCmdRes.setText(cmdList.get(i).getCmdIndex() + ":" + cmdResult + "\n\r");
                    }

                } catch (Exception e1) {

                    binder.edCustCmdRes.setText(cmdList.get(i).getCmdIndex() + ":" + e1.getMessage());
                    e1.printStackTrace();
                }


            }

        });

        binder.btnExecSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int startSelection = binder.edCommandScript.getSelectionStart();
                int endSelection = binder.edCommandScript.getSelectionEnd();
                String selectedText = binder.edCommandScript.getText().toString().substring(startSelection, endSelection);

                cmdService myCommand = new cmdService();
                String cmdResult = "";
                ArrayList<CommandData> cmdList = null;
                int i = 0;
                try {
                    binder.edCustCmdRes.setText("");
                    cmdList = ScripParser(selectedText);

                    binder.edCustCmdRes.setMaxLines(cmdList.size() + 2);
                    binder.edCustCmdRes.setLines(cmdList.size());

                    for (i = 0; i < cmdList.size(); i++) {
                        cmdResult = myCommand.customCommand(cmdList.get(i).getCmdIndex(), cmdList.get(i).getCmdData().replace("[\\t]", "\t"));
                        binder.edCustCmdRes.setText(cmdList.get(i).getCmdIndex() + ":" + cmdResult + "\n\r");
                        binder.edCustCmdRes.setLines(9);
                    }

                } catch (Exception e1) {

                    binder.edCustCmdRes.setText(cmdList.get(i).getCmdIndex() + ":" + e1.getMessage());
                    e1.printStackTrace();
                }

            }

        });

        binder.btnCustomCmdTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binder.edCommandScript.requestFocus();
                insertConstantStr("[\\t]", binder.edCommandScript);
            }

        });


    }

    private ArrayList<CommandData> ScripParser(String text) {
        ArrayList<CommandData> res = new ArrayList<>();
        try {
            String lines[] = text.split("\\r?\\n");
            for (String line : lines) {
                CommandData oneCmd = new CommandData();
                String[] sLine = line.split(",");

                if (!sLine[0].matches("^\\d{1,3}$"))
                    throw new Exception("Command code not accepted !!!");

                if (sLine.length > 0) oneCmd.setCmdIndex(sLine[0]);
                if (sLine.length > 1) oneCmd.setCmdData(sLine[1]);
                else oneCmd.setCmdData("");

                res.add(oneCmd);
            }
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }

        return res;
    }

    private void insertConstantStr(String insertStr, EditText editText) {
        String oriContent = editText.getText().toString();
        int index = editText.getSelectionStart() >= 0 ? editText.getSelectionStart() : 0;
        StringBuilder sBuilder = new StringBuilder(oriContent);
        sBuilder.insert(index, insertStr);
        editText.setText(sBuilder.toString());
        editText.setSelection(index + insertStr.length());
    }

    public int getCurrentCursorLine(EditText editText) {
        int selectionStart = Selection.getSelectionStart(editText.getText());
        Layout layout = editText.getLayout();
        if (selectionStart != -1) {
            return layout.getLineForOffset(selectionStart);
        }

        return -1;
    }

    private void postToast(final String message) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private class CommandData {

        private String cmdIndex;
        private String cmdData;

        public String getCmdData() {
            return cmdData;
        }

        public void setCmdData(String cmdData) {
            this.cmdData = cmdData;
        }

        public String getCmdIndex() {
            return cmdIndex;
        }

        public void setCmdIndex(String cmdIndex) {
            this.cmdIndex = cmdIndex;
        }
    }
}
