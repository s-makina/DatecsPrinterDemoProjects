package com.datecs.demo.ui.main;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;

import com.datecs.demo.DialogEditItemBinding;
import com.google.android.material.snackbar.Snackbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdItems;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdItems.ItemModel;
import com.datecs.testApp.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DialogItemEdit extends Dialog implements View.OnClickListener {

    private DialogEditItemBinding binder;
    private cmdItems myItems = new cmdItems();
    private Activity a;
    private ItemModel itemToEdit;
    String[] taxRatesValues = {"А", "Б", "В", "Г", "Д", "Е", "Ж", "З"};

    public DialogItemEdit(Activity a, ItemModel itemToEdit) {
        super(a, R.style.Dialog);
        this.a = a;
        this.itemToEdit = itemToEdit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = this.getLayoutInflater();
        binder = DataBindingUtil.inflate(inflater, R.layout.dialog_item_edit, null, false);
        initializeDeptartmentSpinner();
        initializeStockGroupSpinner();

        initializeSVAT();
        updateView(itemToEdit);
        final View mDialogView = binder.getRoot();
        setContentView(mDialogView);
        binder.btnReadItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ItemModel readedItem = myItems.ReadItem(Integer.valueOf(binder.edItemID.getText().toString()));
                    updateView(readedItem);
                    Snackbar.make(mDialogView, R.string.msg_item_was_found, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } catch (Exception e) {
                    Snackbar.make(mDialogView, R.string.msg_item_not_found, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
            }

        });


        binder.btnItemSaveToDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendBroadCastItemIsSet(updateItem());
                    dismiss();
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
                    String freePLUslot = String.valueOf(myItems.GetFirstNotProgrammed(0));
                    binder.edItemID.setText(freePLUslot);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(a);
                    dialog.setMessage(a.getString(R.string.new_item_with_plu) + freePLUslot);
                    dialog.setPositiveButton(R.string.yes, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendBroadCastAddItem(updateItem());
                            dialog.dismiss();
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
                    dialog.setMessage(a.getString(R.string.msg_q_remove_item) + binder.edItemID.getText().toString());
                    dialog.setPositiveButton(R.string.yes, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendBroadCastItemIsDeleted(binder.edItemID.getText().toString());
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

    private void sendBroadCastItemIsSet(ItemModel itemData) {
        Intent intent = new Intent("setItem");
        intent.putExtra("itemData", itemData);
        LocalBroadcastManager.getInstance(a.getBaseContext()).sendBroadcast(intent);
    }

    private void sendBroadCastItemIsDeleted(String itemId) {
        Intent intent = new Intent("deleteItem");
        intent.putExtra("itemId", itemId);
        LocalBroadcastManager.getInstance(a.getBaseContext()).sendBroadcast(intent);
    }

    private void sendBroadCastAddItem(ItemModel itemData) {
        Intent intent = new Intent("addItem");
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

    private void updateView(ItemModel item) {

        List<String> arrlst = Arrays.asList(taxRatesValues);
        binder.edItemID.setText(String.valueOf(item.getPlu()));
        binder.edItemName.setText(item.getName());
        binder.edItemPrice.setText(item.getsPrice());
        binder.edStockQTY.setText(item.getQuantity());
        binder.edItemSoldQTY.setText(item.getSold());
        binder.edItemTurnover.setText(item.getTotal());
        binder.spStockGr.setSelection(Integer.valueOf(item.getGroup()) - 1); //1-99
        binder.spItemVAT.setSelection(arrlst.lastIndexOf(item.getTaxGr())); //A-H to 1-7

    }


    //int plu,
    //String taxGr,
    //String group,
    //String sPrice,
    //String quantity,
    //boolean replaceQty,
    //String name,
    //String total,
    //String sold,
    //cmdItems.ItemModel.ItemState state
    private ItemModel updateItem() {
        return new ItemModel(Integer.valueOf(binder.edItemID.getText().toString()),
                taxRatesValues[binder.spItemVAT.getSelectedItemPosition()],//A-H
                String.valueOf(binder.spStockGr.getSelectedItemPosition() + 1),//1-99;
                binder.edItemPrice.getText().toString(),
                binder.edStockQTY.getText().toString(),
                binder.chkAddQTY.isChecked(),
                binder.edItemName.getText().toString(),
                "", "",
                ItemModel.ItemState.ITEM_NOT_SAVED
        );

    }


    private void initializeDeptartmentSpinner() {
        //Dep - Department ( 0...99 );
        ArrayList<String> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    private void initializeStockGroupSpinner() {
        //Group - Item group ( 1...99 );
        ArrayList<String> items = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            items.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binder.spStockGr.setAdapter(adapter);
    }

    private void initializeSVAT() {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"А", "Б", "В", "Г", "Д", "Е", "Ж", "З"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binder.spItemVAT.setAdapter(adapter);
    }


    @Override
    public void onClick(View v) {

    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
