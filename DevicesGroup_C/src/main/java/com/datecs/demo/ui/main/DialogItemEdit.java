package com.datecs.demo.ui.main;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;

import com.datecs.demo.EditItemDialogBinding;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdItems;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdItems.ItemModel;
import com.datecs.testApp.R;
import java.util.ArrayList;



public class DialogItemEdit extends Dialog implements View.OnClickListener {

    private EditItemDialogBinding binder;
    private cmdItems myItems = new cmdItems();
    private Activity a;
    private ItemModel itemToEdit;

    public DialogItemEdit(Activity a, ItemModel itemToEdit) {
        super(a, R.style.Dialog);
        this.a = a;
        this.itemToEdit=itemToEdit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = this.getLayoutInflater();
        binder = DataBindingUtil.inflate(inflater, R.layout.dialog_item_edit, null, false);
        initializeDeptartmentSpinner();
        initializeStockGroupSpinner();
        intiUnitNamesSpinner();
        initializeSVAT();
        updateView(itemToEdit);
        View mDialogView=binder.getRoot();
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
                    dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
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
                    dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
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

        binder.edItemID.setText(item.getPLU());
        binder.edItemName.setText(item.getName());
        binder.edItemPrice.setText(item.getPrice());
        binder.spItemPriceType.setSelection(Integer.valueOf(item.getPriceType()));
        binder.edStockQTY.setText(item.getQuantity());
        binder.chkAddQTY.setChecked(item.getAddQty().equals("A"));
        binder.edItemSoldQTY.setText(item.getSoldQty());
        binder.edItemTurnover.setText(item.getTurnover());
        binder.spStockGr.setSelection(Integer.valueOf(item.getGroup()) - 1); //1-99
        binder.spItemDept.setSelection(Integer.valueOf(item.getDep())); //0-99
        binder.spUnitName.setSelection(Integer.valueOf(item.getUnit())); //0-20
        binder.spItemVAT.setSelection(Integer.valueOf(item.getTaxGr()) - 1); //1-7
        binder.edBarcode1.setText(item.getBar1());
        binder.edBarcode2.setText(item.getBar2());
        binder.edBarcode3.setText(item.getBar3());
        binder.edBarcode4.setText(item.getBar4());

    }


/*
 PLU,taxGr, dep, group, priceType, price, addQty, quantity, bar1, bar2, bar3, bar4, name, turnover,soldQty, unitName, state */
    private ItemModel updateItem() {
        ItemModel item = new ItemModel();
        item.setPLU(binder.edItemID.getText().toString());
        item.setTaxGr(String.valueOf(binder.spItemVAT.getSelectedItemPosition() + 1));//1-7
        item.setDep(String.valueOf(binder.spItemDept.getSelectedItemPosition()));//0-99
        item.setGroup(String.valueOf(binder.spStockGr.getSelectedItemPosition() + 1));//1-99;
        item.setPriceType(String.valueOf(binder.spItemPriceType.getSelectedItemPosition())); //0,1,2;
        item.setPrice(binder.edItemPrice.getText().toString());
        item.setAddQty(binder.chkAddQTY.isChecked() ? "A" : "");
        item.setQuantity(binder.edStockQTY.getText().toString());
        item.setBar1(binder.edBarcode1.getText().toString());
        item.setBar2(binder.edBarcode2.getText().toString());
        item.setBar3(binder.edBarcode3.getText().toString());
        item.setBar4(binder.edBarcode4.getText().toString());
        item.setName(binder.edItemName.getText().toString());
        item.setUnit(String.valueOf(binder.spUnitName.getSelectedItemId()));
        item.setState(ItemModel.ItemState.ITEM_NOT_SAVED);
        return item;
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
        binder.spItemDept.setAdapter(adapter);
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
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, ItemsListFragment.taxRatesValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binder.spItemVAT.setAdapter(adapter);
    }

    private void intiUnitNamesSpinner() {

        try {
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item,ItemsListFragment.mUnitNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binder.spUnitName.setAdapter(adapter);
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }

    }
    @Override
    public void onClick(View v) {

    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
