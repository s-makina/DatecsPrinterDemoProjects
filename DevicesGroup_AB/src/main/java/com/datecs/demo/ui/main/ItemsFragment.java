/**
 * This fragment provides a custom graphical user interface to edit and add items (articles) in the
 * device built-in database of articles items.
 *
 * @author Datecs Ltd. Software Department
 */
package com.datecs.demo.ui.main;

import android.content.DialogInterface;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.datecs.demo.ItemsFrg_bind;
import com.datecs.demo.MainActivity;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdItems;
import com.datecs.testApp.R;

import org.apache.commons.lang3.math.NumberUtils;

public class ItemsFragment extends Fragment {
    private ItemsFrg_bind binder;

    private enum TaxGr {
        А, Б, В, Г, Д, Е, Ж, З
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binder = DataBindingUtil.inflate(inflater, R.layout.items_fragment, container, false);

        //Hides the buttons if they are not supported
        if (MainActivity.myFiscalDevice.isEditItemQTY())
            binder.btnSaveQTY.setVisibility(View.VISIBLE);
        else binder.btnSaveQTY.setVisibility(View.INVISIBLE);
        //Hides the buttons if they are not supported
        if (MainActivity.myFiscalDevice.isEditItemPrice())
            binder.btnSavePrice.setVisibility(View.VISIBLE);
        binder.btnSavePrice.setVisibility(View.INVISIBLE);

        Integer stockGrCnt = Integer.valueOf(MainActivity.myFiscalDevice.getMaxStockGroup());
        //Init Stock Groups Spinner
        String[] items = new String[stockGrCnt];
        for (int i = 0; i < stockGrCnt; i++)
            items[i] = String.valueOf(i + 1); //1...stockGrCnt
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binder.spStockGr.setAdapter(adapter);
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final cmdItems myItems = new cmdItems();
        binder.btnReadItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmdItems.ItemModel item;
                Integer PLU = NumberUtils.toInt(binder.edItemID.getText().toString(), 1);
                try {
                    item = myItems.ReadItem(PLU);
                    if (item.getPlu()<1) {
                        binder.edItemName.setText(item.getName());
                        binder.spItemVAT.setSelection(TaxGr.valueOf(item.getTaxGr()).ordinal());
                        binder.edItemPrice.setText(item.getsPrice());
                        binder.edItemTurnover.setText(item.getTotal());
                        binder.edStockQTY.setText(item.getQuantity());
                        binder.edItemSoldQTY.setText(item.getSold());
                    } else postToast(getString(R.string.msg_not_found));
                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }

        });
        binder.btnItemDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setMessage(R.string.msg_delete_all);
                dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (binder.chcxDelAll.isChecked())
                                myItems.DelAllItems();
                            else
                                myItems.DelItemsInRange(Integer.valueOf(binder.edItemID.getText().toString()), 0);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.setNegativeButton(R.string.no, null);
                dialog.show();

            }

        });

        binder.btnItemSaveToDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final cmdItems.ItemModel myItemModel = new cmdItems.ItemModel(
                            Integer.valueOf(binder.edItemID.getText().toString()),
                            binder.spItemVAT.getSelectedItem().toString(),
                            binder.spStockGr.getSelectedItem().toString(),
                            binder.edItemPrice.getText().toString(),
                            binder.edStockQTY.getText().toString(),
                            true,
                            binder.edItemName.getText().toString(),
                            "",//Read only
                            "",//Read only
                            cmdItems.ItemModel.ItemState.ITEM_SAVED
                    );
                    myItems.SaveItem(myItemModel);
                    binder.edItemID.setText(
                            String.valueOf(Integer.valueOf(binder.edItemID.getText().toString()) + 1
                            )
                    );
                    binder.edItemName.setText("Item " + binder.edItemID.getText().toString());

                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }


        });

        binder.btnFindItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Integer PLU = NumberUtils.toInt(binder.edItemID.getText().toString(), 1); // Start from PLU
                    if (binder.chkFindFromTop.isChecked())
                        binder.edItemID.setText(String.valueOf(myItems.GetLastNotProgrammed(PLU)));
                    else
                        binder.edItemID.setText(String.valueOf(myItems.GetFirstNotProgrammed(PLU)));

                    binder.edItemName.setText("New item " + binder.edItemID.getText().toString());
                    //Clear all
                    binder.spItemVAT.setSelection(0);
                    binder.spStockGr.setSelection(0);
                    binder.chkWithTurnover.setChecked(false);
                    binder.edStockQTY.setText("");
                    binder.edItemSoldQTY.setText("");
                    binder.edItemTurnover.setText("");
                    binder.edItemPrice.setText("0.01");
                    binder.edItemPrice.requestFocus();

                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }

        });

        binder.btnReadFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmdItems.ItemModel item;
                try {
                    Integer PLU = NumberUtils.toInt(binder.edItemID.getText().toString(), 1);

                    if (binder.chkWithTurnover.isChecked())
                        item = myItems.GetFirstFoundWithSales(PLU);
                    else
                        item = myItems.GetFirstFoundProgrammed(PLU);
                    if (item.getPlu()>0) {
                        binder.edItemID.setText(item.getPlu());
                        binder.edItemName.setText(item.getName());
                        binder.edItemPrice.setText(item.getsPrice());
                        binder.edItemTurnover.setText(item.getTotal());
                        binder.edStockQTY.setText(item.getQuantity());
                        binder.edItemSoldQTY.setText(item.getSold());
                    } else postToast(getString(R.string.msg_not_found));
                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }

        });

        binder.btnReadNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmdItems.ItemModel item;
                try {
                    if (binder.chkWithTurnover.isChecked()) item = myItems.GetNextFoundWithSales();
                    else item = myItems.GetNextProgrammed();

                    if (item.getPlu()<1) postToast(getString(R.string.msg_not_found));
                    else {
                        binder.edItemID.setText(item.getPlu());
                        binder.edItemName.setText(item.getName());
                        binder.edItemPrice.setText(item.getsPrice());
                        binder.edItemTurnover.setText(item.getTotal());
                        binder.edStockQTY.setText(item.getQuantity());
                        binder.edItemSoldQTY.setText(item.getSold());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }

        });

        binder.btnReadLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmdItems.ItemModel item;
                try {
                    Integer PLU = NumberUtils.toInt(binder.edItemID.getText().toString(), 0);

                    if (binder.chkWithTurnover.isChecked())
                        item = myItems.GetLastFoundWithSales(PLU);
                    else
                        item = myItems.GetLastFoundProgrammed(PLU);
                    if (item.getPlu()<1) postToast(getString(R.string.msg_not_found));
                    else {

                        binder.edItemID.setText(item.getPlu());
                        binder.edItemName.setText(item.getName());
                        binder.edItemPrice.setText(item.getsPrice());
                        binder.edItemTurnover.setText(item.getTotal());
                        binder.edStockQTY.setText(item.getQuantity());
                        binder.edItemSoldQTY.setText(item.getSold());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }

        });
        binder.btnSaveQTY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmdItems.ItemModel item;
                try {
                    Integer PLU = NumberUtils.toInt(binder.edItemID.getText().toString(), 1);

                    myItems.SetItemQuantity(Integer.valueOf(binder.edItemID.getText().toString()),
                            Double.valueOf(binder.edStockQTY.getText().toString()));
                    item = myItems.ReadItem(PLU);
                    binder.edStockQTY.setText(item.getQuantity());
                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                }
            }

        });

        binder.btnSavePrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmdItems.ItemModel item;
                try {
                    Integer PLU = NumberUtils.toInt(binder.edItemID.getText().toString(), 1);

                    myItems.SetItemPrice(Integer.valueOf(binder.edItemID.getText().toString()),
                            Double.valueOf(binder.edItemPrice.getText().toString()));
                    item = myItems.ReadItem(PLU);
                    binder.edStockQTY.setText(item.getQuantity());
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
