package com.datecs.demo.ui.main;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.datecs.demo.DialogClientEdit_biniing;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdClients;
import com.datecs.testApp.R;
import com.datecs.util.RegExpr;
import com.google.android.material.snackbar.Snackbar;

import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdClients.ClientInfoModel.ItemState.ITEM_NOT_SAVED;


public class DialogClientEdit extends Dialog implements View.OnClickListener {
    private DialogClientEdit_biniing binder;
    private cmdClients myClients = new cmdClients();
    private Activity a;
    private cmdClients.ClientInfoModel itemToEdit;
    private View mDialogView;

    public DialogClientEdit(Activity a, cmdClients.ClientInfoModel itemToEdit) {
        super(a, R.style.Dialog);
        this.a = a;
        this.itemToEdit = itemToEdit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = this.getLayoutInflater();
        binder = DataBindingUtil.inflate(inflater, R.layout.dialog_client_edit, null, false);
        updateView(itemToEdit);
        mDialogView = binder.getRoot();
        setContentView(mDialogView);
        binder.btnReadItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cmdClients.ClientInfoModel readedItem = myClients.ReadItemByID(Integer.valueOf(binder.edCliFIRM.getText().toString()));
                    updateView(readedItem);
                    Snackbar.make(mDialogView, R.string.msg_item_was_found, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }

        });

        binder.btnItemSaveToDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cmdClients.ClientInfoModel clietnData = updateItem();
                    if (validateClientData(clietnData)) {
                        sendBroadCastItemIsSet(clietnData);
                        dismiss();
                    }
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        binder.btnItemAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String freePLUslot = String.valueOf(myClients.GetFirstNotProgrammed(0));
                    binder.edCliFIRM.setText(freePLUslot);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(a);
                    dialog.setMessage(a.getString(R.string.new_client) + freePLUslot);
                    dialog.setPositiveButton(R.string.yes, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            cmdClients.ClientInfoModel clietnData = updateItem();
                            if (validateClientData(clietnData)) {
                                sendBroadCastAddItem(clietnData);
                                dismiss();
                            }
                        }
                    });
                    dialog.setNegativeButton(R.string.no, null);
                    dialog.show();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        binder.btnItemDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(a);
                    dialog.setMessage(a.getString(R.string.msg_remove_client) + binder.edCliFIRM.getText().toString());
                    dialog.setPositiveButton(R.string.yes, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendBroadCastItemIsDeleted(binder.edCliFIRM.getText().toString());
                            dialog.dismiss();
                            dismiss();
                        }
                    });
                    dialog.setNegativeButton(R.string.no, null);
                    dialog.show();

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });




    }

    private void sendBroadCastItemIsSet(cmdClients.ClientInfoModel itemData) {
        if (itemData == null) return;
        Intent intent = new Intent("setClient");
        intent.putExtra("itemData", itemData);
        LocalBroadcastManager.getInstance(a.getBaseContext()).sendBroadcast(intent);
    }

    private void sendBroadCastItemIsDeleted(String itemId) {
        Intent intent = new Intent("deleteClient");
        intent.putExtra("itemId", itemId);
        LocalBroadcastManager.getInstance(a.getBaseContext()).sendBroadcast(intent);
    }

    private void sendBroadCastAddItem(cmdClients.ClientInfoModel itemData) {
        if (itemData == null) return;
        Intent intent = new Intent("addClient");
        intent.putExtra("itemData", itemData);
        LocalBroadcastManager.getInstance(a.getBaseContext()).sendBroadcast(intent);
    }

    private void postToast(final String text) {
        a.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(a, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateView(cmdClients.ClientInfoModel item) {

        binder.edCliFIRM.setText(String.valueOf(item.getFIRM()));
        binder.edCliName.setText(item.getName());
        binder.edCliVAT.setText(item.getVATN());
        binder.spTypeTAXN.setSelection(item.getTypeTAXN().ordinal());
        binder.edCliTAXN.setText(item.getEIK());
        binder.edCliRecName.setText(item.getRecName());
        binder.edCliAddr1.setText(item.getAddr1());
        binder.edCliAddr2.setText(item.getAddr2());

    }


    private cmdClients.ClientInfoModel updateItem() {

        cmdClients.ClientInfoModel.TypeTAXN dd = cmdClients.ClientInfoModel.TypeTAXN.BULSTAT;
        cmdClients.ClientInfoModel res = new cmdClients.ClientInfoModel(
                Integer.valueOf(binder.edCliFIRM.getText().toString()),
                binder.edCliName.getText().toString(),
                binder.edCliTAXN.getText().toString(),
                dd.fromOrdinal((int) binder.spTypeTAXN.getSelectedItemId()),
                binder.edCliRecName.getText().toString(),
                binder.edCliVAT.getText().toString(),
                binder.edCliAddr1.getText().toString(),
                binder.edCliAddr2.getText().toString(),
                ITEM_NOT_SAVED
        );
        return res;

    }

    private boolean validateClientData(cmdClients.ClientInfoModel res) {

        String textMessage = "";
        boolean ret = true;
        if (res.getFIRM() > 1000) {
            textMessage = "Use index of record (1...1000)";
            binder.edCliFIRM.requestFocus();
            ret = false;
        }

        if (!res.getName().matches(RegExpr._UpTo36Symbol)) {
            textMessage = "Use client's name (up to 36 chars)";
            binder.edCliName.requestFocus();
            ret = false;
        }
        if (!res.getEIK().matches(RegExpr._9_14Symbol)) {
            textMessage = "Use Client's tax number (9...13 chars);";
            binder.edCliTAXN.requestFocus();
            ret = false;
        }

        if (!res.getRecName().matches(RegExpr._UpTo36Symbol)) {
            textMessage = "Use receiver name (up to 36 chars);";
            binder.edCliRecName.requestFocus();
            ret = false;
        }

        if (!res.getVATN().matches(RegExpr._UpTo14Symbol)) {
            textMessage = "Use VAT number of the client (up to 14 chars);";
            binder.edCliVAT.requestFocus();
            ret = false;
        }

        if (!res.getAddr1().matches(RegExpr._UpTo36Symbol)) {
            textMessage = "Use Client's address - line 1 (up to 36 chars);";
            binder.edCliAddr1.requestFocus();
            ret = false;
        }

        if (!res.getAddr2().matches(RegExpr._UpTo36Symbol)) {
            textMessage = "Use Client's address - line 2 (up to 36 chars);";
            binder.edCliAddr2.requestFocus();
            ret = false;
        }

        if (!ret)
            Snackbar.make(mDialogView, textMessage, Snackbar.LENGTH_LONG).setAction("Action", null).show();

        return ret;
    }


    @Override
    public void onClick(View v) {

    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
