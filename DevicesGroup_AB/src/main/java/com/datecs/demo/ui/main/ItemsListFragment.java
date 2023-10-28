package com.datecs.demo.ui.main;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.databinding.DataBindingUtil;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;

import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.datecs.demo.ListItems_frg_binding;
import com.datecs.demo.ui.main.adapters.ItemsListAdapter;
import com.datecs.fileselector.FileOperation;
import com.datecs.fileselector.FileSelector;
import com.datecs.fileselector.OnHandleFileListener;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdItems;
import com.datecs.testApp.R;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;


import static com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdItems.ItemModel.ItemState.ITEM_NOT_SAVED;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdItems.ItemModel.ItemState.ITEM_SAVED;

public class ItemsListFragment extends Fragment {

    private static final int LOAD_CSV_RESULTS = 11890;
    private ListItems_frg_binding binder;
    private ItemsListAdapter adapter;
    private ProgressDialog progress;
    private String[] mFileFilter = {".csv", ".txt"};
    private File lastOpenFolder = Environment.getExternalStorageDirectory(); //TODO:Read Settings from file !
    private int currentCursor = 0;
    private final int SET_READ_RANGE = 0;
    private final int SET_DELETE_RANGE = 1;
    private ArrayList<cmdItems.ItemModel> items;
    private cmdItems myItems = new cmdItems();
    private String[] headerCSVfile = {"PLU", "VAT", "StockGr", "Price", "Qty", "OverQty", "Name", "Turnover", "SoldQTY"};

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("pluListItems", items);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binder = DataBindingUtil.inflate(inflater, R.layout.items_list_fragment, container, false);

        if (savedInstanceState == null) {
            items = new ArrayList<>();
            adapter = new ItemsListAdapter(getContext(), items);

        } else {
            items = (ArrayList<cmdItems.ItemModel>) savedInstanceState.getSerializable("pluListItems");
            adapter = new ItemsListAdapter(getContext(), items);
            binder.lvPluList.setAdapter(adapter);
        }

        BottomNavigationView navigation = binder.naviBottomPlu;
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //Register of messages
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mSetItemMessageReceiver, new IntentFilter("setItem"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDeleteItemMessageReceiver, new IntentFilter("deleteItem"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mAddItemMessageReceiver, new IntentFilter("addItem"));
        binder.lvPluList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        /**
         * Capture ListView item click:
         *  a short click opens a dialog to edit the item on position
         *   through long clicks, you can select rows from the list to delete from the device
         */
        binder.lvPluList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.popup_selection, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                adapter.removeSelection();
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Capture total checked items
                final int checkedCount = binder.lvPluList.getCheckedItemCount();
                // Set the CAB title according to total checked items
                mode.setTitle(String.valueOf(checkedCount) + getString(R.string.title_item_selected));
                // Calls toggleSelection method from ListViewAdapter Class
                adapter.toggleSelection(position);
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_selected_items:
                        final SparseBooleanArray selected = adapter.getSelectedIds(); // Calls getSelectedIds method from ListViewAdapter Class

                        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                        dialog.setMessage(R.string.delete_selected);
                        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Captures all selected ids with a loop
                                for (int i = (selected.size() - 1); i >= 0; i--) {
                                    if (selected.valueAt(i)) {
                                        cmdItems.ItemModel selecteditem = adapter.getItem(selected.keyAt(i));
                                        // Remove selected items following the ids
                                        int itemToDelete = Integer.valueOf(selecteditem.getPlu());
                                        try {
                                            myItems.DelItemsInRange(itemToDelete, itemToDelete);
                                            adapter.remove(selecteditem);
                                        } catch (Exception e) {
                                            postToast(e.getMessage());
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                // Close CAB
                                adapter.notifyDataSetChanged();
                            }
                        });
                        dialog.setNegativeButton(R.string.no, null);
                        dialog.show();
                        mode.finish();
                        return true;

              /*      case R.id.add_to_sale:
                        mode.finish();
                        return true;*/

                    default:
                        return false;
                }
            }
        });

        return binder.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSetItemMessageReceiver != null)
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mSetItemMessageReceiver);
        if (mDeleteItemMessageReceiver != null)
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mDeleteItemMessageReceiver);
        if (mAddItemMessageReceiver != null)
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mAddItemMessageReceiver);
    }

    /**
     * Processing the Message from the Item Dialog editor
     */
    BroadcastReceiver mAddItemMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                cmdItems.ItemModel temp = (cmdItems.ItemModel) intent.getSerializableExtra("itemData");
                myItems.SaveItem(temp);
                items.add(0, temp);//Add position on the top of table
                if (adapter == null) {
                    adapter = new ItemsListAdapter(getContext(), items);
                    binder.lvPluList.setAdapter(adapter);
                } else adapter.notifyDataSetChanged();
                Snackbar.make(getView(), R.string.item_added, Snackbar.LENGTH_LONG).setAction("Action", null).show();

            } catch (Exception e) {
                postToast(e.getMessage());
                e.printStackTrace();
            }
        }
    };

    /**
     * Edit Item message
     */
    BroadcastReceiver mSetItemMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                cmdItems.ItemModel temp = (cmdItems.ItemModel) intent.getSerializableExtra("itemData");
                myItems.SaveItem(temp);
                temp.setState(ITEM_SAVED);

                if (currentCursor < items.size()) items.set(currentCursor, temp);  // Redakcia
                else items.add(0, temp);  // Dobavia

                if (adapter == null) {
                    adapter = new ItemsListAdapter(getContext(), items);
                    binder.lvPluList.setAdapter(adapter);
                } else
                    adapter.notifyDataSetChanged();
                Snackbar.make(getView(), R.string.msg_item_saved, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } catch (Exception e) {
                postToast(e.getMessage());
                e.printStackTrace();
            }
        }
    };

    /**
     * Delete Item message
     */
    BroadcastReceiver mDeleteItemMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int temp = Integer.parseInt(intent.getStringExtra("itemId"));
                if (myItems.DelItemsInRange(temp, temp)) {
                    items.remove(currentCursor); //currentCursor ne se e izmenil
                    adapter.notifyDataSetChanged();
                    Snackbar.make(getView(),
                            R.string.msg_item_deleted, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            } catch (Exception e) {
                postToast(e.getMessage());
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binder.lvPluList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adpterView, View view, int position, long id) {

                StartItemEditDialog(position, items.get(position));
                currentCursor = position;

            }
        });

    }

    private void StartItemEditDialog(int position, cmdItems.ItemModel itemToEdit) {
        try {
            DialogItemEdit d = new DialogItemEdit(getActivity(), itemToEdit);
            d.show();
            d.setTitle("Edit Item on Position: " + String.valueOf(position + 1));
            d.show();
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = (int) ((int) displaymetrics.widthPixels * 0.90);
            int height = (int) ((int) displaymetrics.heightPixels * 0.80);
            d.getWindow().setLayout(width, height);
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }

    }


    /////////////////////////////////////////////////////////////////////////////////////////////////BOTTOM NAVIGATOR
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_items_read:
                    /////////////////////////////////////////////////////////////////////////////////////////////////POP UP MENU
                    PopupMenu popup = new PopupMenu(getContext(), getView().findViewById(R.id.navigation_items_read));
                    popup.getMenuInflater().inflate(R.menu.popup_menage_items, popup.getMenu());
                    popup.getMenu().findItem(R.id.mnu_delete).setEnabled(items.size() > 0);
                    popup.getMenu().findItem(R.id.mnu_write_all).setEnabled(items.size() > 0);
                    popup.getMenu().findItem(R.id.mnu_clear_table).setEnabled(items.size() > 0);


//                    if(items.size() ==0)  binder.pluManagerHelp.setVisibility(View.VISIBLE);
//                    else binder.pluManagerHelp.setVisibility(View.INVISIBLE);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {

                                case R.id.mnu_add_plu:
                                    addNewItem();
                                    break;

                                case R.id.mnu_clear_table:
                                    clearTable();
                                    break;

                                case R.id.mnu_read_all:
                                    ReadItems_All();
                                    break;
                                case R.id.mnu_read_items_range:
                                    Dialog_setRangeOfPLU cdd =
                                            new Dialog_setRangeOfPLU(getActivity(), SET_READ_RANGE, 1, 100);
                                    cdd.show();
                                    break;

                                case R.id.mnu_write_all:
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                                    dialog.setMessage(R.string.msg_q_item_overwrite);
                                    dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            WritigAllItems();

                                        }
                                    });
                                    dialog.setNegativeButton(R.string.no, null);
                                    dialog.show();

                                    break;

                                case R.id.mnu_delete:
                                    cdd = new Dialog_setRangeOfPLU(getActivity(), SET_DELETE_RANGE, items.get(0).getPlu(), items.get(items.size() - 1).getPlu());
                                    cdd.show();
                                    break;
                            }
                            return true;
                        }
                    });
                    popup.show(); //showing popup menu
                    return true;

                case R.id.navigation_export_items:
                    popup = new PopupMenu(getContext(), getView().findViewById(R.id.navigation_export_items));
                    popup.getMenuInflater().inflate(R.menu.popup_csv_plu, popup.getMenu());
                    popup.getMenu().findItem(R.id.csv_export_plu).setEnabled(items.size() > 0);

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.csv_import_plu:
                                    ImportItemsFromCSV();
                                    break;
                                case R.id.csv_export_plu:
                                    ExportItemsToCSV();
                                    break;
                            }
                            return true;
                        }
                    });
                    popup.show(); //showing popup menu
                    return true;
            }
            return false;
        }

    };

    private void addNewItem() {
        currentCursor = items.size(); // Add to end
        try {
            StartItemEditDialog(currentCursor,
                    new cmdItems.ItemModel(myItems.GetFirstNotProgrammed(0),
                            "1",
                            "1",
                            "0.1",
                            "1",
                            false,
                            "New Item",
                            "",
                            "",
                            ITEM_NOT_SAVED));

        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearTable() {
        if (null != items) items.clear();
        if (null != adapter) {
            adapter.clear();
        }
    }


    public void ExportItemsToCSV() {
        try {
            try {
                FileSelector fs = new FileSelector(getContext(), FileOperation.SAVE, mExportCSVFileListener, mFileFilter, lastOpenFolder);
                fs.show();
            } catch (Exception e) {
                postToast(e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }
    }


    public void ImportItemsFromCSV() {
        try {
            FileSelector fs = new FileSelector(getActivity(), FileOperation.LOAD, mImportCSVFileListener, mFileFilter, lastOpenFolder);
            fs.show();
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
        }
    }

    OnHandleFileListener mImportCSVFileListener = new OnHandleFileListener() {
        @Override
        public void handleFile(final String filePath) {
            setLastOpenFolder(new File(filePath));
            Load_CSV_File(filePath);
        }
    };

    OnHandleFileListener mExportCSVFileListener = new OnHandleFileListener() {
        @Override
        public void handleFile(final String filePath) {
            setLastOpenFolder(new File(filePath));
            Save_CSV_File(filePath);
        }
    };

    private void Load_CSV_File(final String filePath) {
        progress = ProgressDialog.show(getContext(),
                getString(R.string.title_load_items)
                , getString(R.string.msg_please_wait),
                true);
        clearTable();
        final ArrayList<cmdItems.ItemModel> mItems = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Reader in = new FileReader(filePath);
                    Iterable<CSVRecord> records = CSVFormat.EXCEL.withDelimiter(';').withHeader(headerCSVfile).parse(in);

                    //List<String> arrlst = Arrays.asList(taxRatesValues);

                    // Matching  value of CSV record to item Model
                    //private String[] headerCSVfile = {"PLU", "VAT", "StockGr", "Price", "Qty", "OverQty", "Name", "Turnover", "SoldQTY"};
                    //Items.ItemModel.ItemState state;
                    for (CSVRecord record : records) {
                        if (record.get("PLU").equals("PLU")) continue; //Ignore Header
                        cmdItems.ItemModel item = new cmdItems.ItemModel(
                                Integer.parseInt(record.get("PLU")),
                                //Convert value of TaxGr is A,B....to index 1,2...
                                //String.valueOf(arrlst.lastIndexOf(record.get("VAT")) + 1),//A-H to 1-7
                                record.get("VAT"),
                                record.get("StockGr"),//1-99;
                                record.get("Price"),
                                record.get("Qty"),
                                record.get("OverQty") == "A",
                                record.get("Name"),
                                record.get("Turnover"),
                                record.get("SoldQTY"),
                                ITEM_NOT_SAVED);
                        mItems.add(item);
                    }
                    //mItems.remove(0); // Header

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                } finally {
                    progress.dismiss();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clearTable();
                        items.addAll(mItems);
                        adapter = new ItemsListAdapter(getContext(), items);
                        binder.lvPluList.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }

    private void Save_CSV_File(final String filePath) {

        progress = ProgressDialog.show(getContext(),
                getString(R.string.title_export)
                , getString(R.string.msg_please_wait),
                true);
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL.withDelimiter(';').
                            withHeader(headerCSVfile));
                    // Matching  value of  item Model to CSV record
                    // plu, String taxGr, String group, String sPrice, String quantity, boolean replaceQty, String name, String total, String sold, cmdItems.ItemModel.ItemState state
                    //{"PLU", "VAT", "StockGr", "Price", "Qty", "OverQty", "Name", "Turnover", "SoldQTY"};
                    for (cmdItems.ItemModel item : items) {
                        csvPrinter.printRecord(
                                item.getPlu(),
                                item.getTaxGr(),
                                item.getGroup(),
                                item.getsPrice(),
                                item.getQuantity(),
                                item.isReplaceQty() ? "A" : "",
                                item.getName(),
                                item.getTotal(),
                                item.getSold());
                    }
                    csvPrinter.close();

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                    progress.dismiss();
                    return;
                } finally {
                    progress.dismiss();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //binder.lvPluList.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private cmdItems.ItemModel firsItem(int startItem) throws Exception {
        return myItems.GetFirstFoundProgrammed(startItem);
    }

    private cmdItems.ItemModel nextItem() throws Exception {
        return myItems.GetNextProgrammed();
    }

    private cmdItems.ItemModel readItem(int itemID) throws Exception {
        return myItems.ReadItem(itemID);
    }

    private void WritigAllItems() {
        final boolean[] runThread = {true};
        progress = new ProgressDialog(getContext());
        progress.setTitle(R.string.title_write_items);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setCancelable(false);
        progress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runThread[0] = false;
                dialog.dismiss();
            }
        });
        progress.show();
        final int[] prog = {0};
        new Thread(new Runnable() {
            @Override
            public void run() {
                // do the thing that takes a long time
                try {
                    progress.setMax(items.size());
                    for (cmdItems.ItemModel temp : items) {
                        myItems.SaveItem(temp);
                        temp.setState(ITEM_SAVED);
                        progress.setProgress(prog[0]++);
                        if (!runThread[0]) {
                            return;
                        }
                    }

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                    progress.dismiss();
                    return;
                } finally {
                    progress.dismiss();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binder.lvPluList.setAdapter(adapter);
                        binder.lvPluList.setSelection(prog[0]);
                    }
                });
            }
        }).start();
    }


    private void ReadItems_inRange(final int fromPlu, final int toPlu) {
        final boolean[] runRead = {true};
        items = new ArrayList<>();
        progress = new ProgressDialog(getContext());
        progress.setTitle(R.string.title_reading_items);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setCancelable(false);
        progress.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runRead[0] = false;
                        dialog.dismiss();
                    }
                });
        progress.show();
        clearTable();
        final ArrayList<cmdItems.ItemModel> mItems = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // do the thing that takes a long time
                try {
                    int F = fromPlu;
                    int L = toPlu;
                    progress.setMax(L);
                    for (int i = F; i <= L; i++) {
                        cmdItems.ItemModel temp = readItem(i);
                        if (temp != null) mItems.add(temp);
                        else
                            postToast(getString(R.string.msg_item_not_found));
                        if (!runRead[0]) return;
                        progress.setProgress(i);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                } finally {
                    progress.dismiss();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clearTable();
                        items.addAll(mItems);
                        adapter = new ItemsListAdapter(getContext(), items);
                        binder.lvPluList.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }

    private void ReadItems_All() {
        final boolean[] runRead = {true};
        progress = new ProgressDialog(getContext());
        progress.setTitle(R.string.title_reading_items);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setCancelable(false);
        progress.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runRead[0] = false;
                        dialog.dismiss();
                    }
                });
        progress.show();
        clearTable();
        final ArrayList<cmdItems.ItemModel> mItems = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // do the thing that takes a long time
                try {
                    progress.setMax(getCountProgrammedItems());
                    cmdItems.ItemModel temp = firsItem(1);
                    if (temp == null) return;
                    mItems.add(temp);
                    int i = 0;
                    while (true) {
                        temp = nextItem();
                        if (temp == null) break;
                        mItems.add(temp);
                        if (!runRead[0]) break;
                        progress.setProgress(i++);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    postToast(e.getMessage());
                } finally {
                    progress.dismiss();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clearTable();
                        items.addAll(mItems);
                        adapter = new ItemsListAdapter(getContext(), items);
                        binder.lvPluList.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }


    private void Delete_inRange(final int f, final int l) {
        final boolean[] runThread = {true};
        progress = new ProgressDialog(getContext());
        progress.setTitle(R.string.title_delete_items);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setCancelable(false);
        progress.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runThread[0] = false;
                        dialog.dismiss();
                    }
                });
        progress.setMax(l);
        progress.show();
        final ArrayList<cmdItems.ItemModel> mItems = new ArrayList<>();
        mItems.addAll(items);
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    for (int pluCode = f; pluCode <= l; pluCode++) {
                        if (!runThread[0]) break;

                        if (!myItems.DelItemsInRange(pluCode, pluCode))
                            postToast(getString(R.string.msg_item_not_found));

                        for (int gridPos = 0; gridPos < mItems.size(); gridPos++) {
                            if (pluCode == Integer.valueOf(mItems.get(gridPos).getPlu())) {// ima li takova PLU v grida
                                mItems.remove(gridPos);
                                break;
                            }
                        }
                        progress.setProgress(pluCode);
                    }

                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                } finally {
                    progress.dismiss();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clearTable();
                        items.addAll(mItems);
                        adapter = new ItemsListAdapter(getContext(), items);
                        binder.lvPluList.setAdapter(adapter);

                    }
                });
            }
        }).start();


    }

    private int getCountProgrammedItems() throws Exception {
        return myItems.GetItemsInformation().getTotalCountOfProgrammedItems();
    }


    public void setLastOpenFolder(File lastOpenFolder) {
        this.lastOpenFolder = new File(lastOpenFolder.getParent());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////DIALOG
    public class Dialog_setRangeOfPLU extends Dialog implements
            View.OnClickListener {
        private Button yes, no;
        private EditText edFrom;
        private EditText edTo;
        private CheckBox chkAll;
        private int typeOfDialog;
        private int fItem;
        private int lItem;


        public Dialog_setRangeOfPLU(Activity a, int operation, int minPLUcode, int maxPLUcode) {
            super(a);
            // TODO Auto-generated constructor stub
            typeOfDialog = operation;
            fItem = minPLUcode;
            lItem = maxPLUcode;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_get_range);
            chkAll = findViewById(R.id.chbxAll);
            yes = findViewById(R.id.btn_yes);
            no = findViewById(R.id.btn_no);
            edFrom = findViewById(R.id.ed_rangeFrom); //set s nalichinite
            edTo = findViewById(R.id.ed_rangeTo);     //set
            edFrom.setText(String.valueOf(fItem));
            edTo.setText(String.valueOf(lItem));
            yes.setOnClickListener(this);
            no.setOnClickListener(this);

        }

        ////////////////////////////////////////////////////////////////////////////////////////////DIALOG ON CLICK
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.btn_yes:
                    dismiss();
                    try {
                        int f = Integer.valueOf(edFrom.getText().toString().trim());
                        int l = Integer.valueOf(edTo.getText().toString().trim());
                        if (typeOfDialog == SET_READ_RANGE)
                            if (!chkAll.isChecked()) ReadItems_inRange(f, l);
                            else ReadItems_All();
                        if (typeOfDialog == SET_DELETE_RANGE)
                            if (!chkAll.isChecked()) Delete_inRange(f, l);
                            else Delete_AllItems();
                    } catch (Exception e) {
                        postToast(e.getMessage());
                        e.printStackTrace();
                    }

                    break;
                case R.id.btn_no:
                    dismiss();
                    break;
                default:
                    break;
            }
            dismiss();
        }
    }

    private void Delete_AllItems() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    myItems.DelAllItems();
                } catch (Exception e) {
                    postToast(e.getMessage());
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clearTable();
                        items.clear();
                        adapter = new ItemsListAdapter(getContext(), items);
                        binder.lvPluList.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }

    private void postToast(final String text) {
        //       try {

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
            }
        });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


}