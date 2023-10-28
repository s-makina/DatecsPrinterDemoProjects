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

import com.datecs.demo.ListItemsFrgBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import android.widget.PopupMenu;
import android.widget.Toast;

import com.datecs.demo.ui.main.adapters.ItemsListAdapter;
import com.datecs.fileselector.FileOperation;
import com.datecs.fileselector.FileSelector;
import com.datecs.fileselector.OnHandleFileListener;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdItems;
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
import java.util.Arrays;
import java.util.List;

import static com.datecs.fileselector.FileUtils.rescanFolder;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdItems.ItemModel.ItemState.ITEM_NOT_SAVED;

public class ItemsListFragment extends Fragment {

    private static final int LOAD_CSV_RESULTS = 11890;
    public static final String[] taxRatesValues = {"A", "B", "C", "D", "E", "F", "G", "H"};
    private final String[] headerCSVfile = {"PLU", "Name", "Price", "PriceType", "Qty", "AddQty", "StockGr", "VAT", "Dept",
            "Turnover", "SoldQty", "Bar1", "Bar2", "Bar3", "Bar4", "Unit"};
    private ListItemsFrgBinding binder;
    private ItemsListAdapter adapter;
    private ProgressDialog progress;
    private String[] mFileFilter = {".csv", ".txt"};
    private File lastOpenFolder = Environment.getExternalStorageDirectory(); //TODO:Read Settings from file !
    private int currentCursor = 0;
    private final int SET_READ_RANGE = 0;
    private final int SET_DELETE_RANGE = 1;
    private ArrayList<cmdItems.ItemModel> items;
    private cmdItems myItems = new cmdItems();
    public static String[] mUnitNames;

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

        mUnitNames = initUnitNamesFromDevice(); // Use this array in to init unit column in table and spinners in Items editor

        BottomNavigationView navigation = binder.naviBottomPlu;
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //Register of messages
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mSetItemMessageReceiver, new IntentFilter("setItem"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDeleteItemMessageReceiver, new IntentFilter("deleteItem"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mAddItemMessageReceiver, new IntentFilter("addItem"));
        /**
         * Capture ListView item click:
         *  a short click opens a dialog to edit the item on position
         *   through long clicks, you can select rows from the list to delete from the device
         */
        binder.lvPluList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.popup_item_selection, menu);
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
                                        int itemToDelete = Integer.valueOf(selecteditem.getPLU());
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
                myItems.SetItem(temp);
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
                myItems.SetItem(temp);
                // ToDo: Rom
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
//        binder.lvPluList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
////            @Override
////            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
////
////                return true;
////            }
////        });

        binder.lvPluList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adpterView, View view, int position, long id) {

                StartItemEditDialog(position, items.get(position));
                currentCursor = position;

            }
        });

/*
        binder.lvPluList.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if(mLastFirstVisibleItem<firstVisibleItem)
                {
                    binder.naviBottomPlu.setVisibility(View.GONE);
                }
                if(mLastFirstVisibleItem>firstVisibleItem)
                {
                    binder.naviBottomPlu.setVisibility(View.VISIBLE);
                    // postToast("SCROLLING UP");
                }
                mLastFirstVisibleItem=firstVisibleItem;

            }
        });
*/

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
            int height = (int) ((int) displaymetrics.heightPixels * 0.90);
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
                    popup.getMenuInflater().inflate(R.menu.popup_menage_plu, popup.getMenu());
                    popup.getMenu().findItem(R.id.mnu_delete_all).setEnabled(items.size() > 0);
                    popup.getMenu().findItem(R.id.mnu_write_all_items).setEnabled(items.size() > 0);
                    popup.getMenu().findItem(R.id.mnu_clear_table).setEnabled(items.size() > 0);


//                    if(items.size() ==0)  binder.pluManagerHelp.setVisibility(View.VISIBLE);
//                    else binder.pluManagerHelp.setVisibility(View.INVISIBLE);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {

                                case R.id.mnu_add_item:
                                    addNewItem();
                                    break;

                                case R.id.mnu_clear_table:
                                    clearTable();
                                    break;

                                case R.id.mnu_read_all_items:
                                    ReadItems_All();
                                    break;
                                case R.id.mnu_read_range_items:
                                    Dialog_setRangeOfPLU cdd =
                                            new Dialog_setRangeOfPLU(getActivity(), SET_READ_RANGE, "1", "100");
                                    cdd.show();
                                    break;

                                case R.id.mnu_write_all_items:
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                                    dialog.setMessage(R.string.msg_q_item_overwrite);
                                    dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            WritingAllItems();

                                        }
                                    });
                                    dialog.setNegativeButton(R.string.no, null);
                                    dialog.show();
                                    break;

                                case R.id.mnu_delete_all:
                                    cdd = new Dialog_setRangeOfPLU(getActivity(), SET_DELETE_RANGE,
                                            items.get(0).getPLU(), items.get(items.size() - 1).getPLU()
                                    );
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
                    popup.getMenuInflater().inflate(R.menu.popup_csv_in_ex, popup.getMenu());
                    popup.getMenu().findItem(R.id.csv_export).setEnabled(items.size() > 0);

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.csv_import:
                                    ImportItemsFromCSV();
                                    break;
                                case R.id.csv_export:
                                    ExportItemsToCSV();
                                    break;
                            }
                            return true;
                        }
                    });
                    popup.show(); //showing popup menu

                    return true;

/*                case R.id.navigation_sale_mode:
                    //         FragmentTransaction fragmentTransaction;
                    //         fragmentTransaction = MainActivity.manager.beginTransaction();
                    //         fragmentTransaction.replace(R.id.fragment_container, new Page_Sales_frg());
                    //         fragmentTransaction.commit();
                    ViewPager viewPager = getActivity().findViewById(R.id.pageSaleMode);
                    viewPager.setCurrentItem(FRAGMET_SALES_LIST);
                    return true;*/
            }
            return false;
        }

    };

    private void addNewItem() {
        currentCursor = items.size(); // Add to end
        try {
            StartItemEditDialog(currentCursor,
                    new cmdItems.ItemModel(
                            String.valueOf(myItems.GetFirstNotProgrammed(0)),
                            "1",
                            "0",
                            "1",
                            "0",
                            "0.01",
                            "A",
                            "1",
                            "",
                            "",
                            "",
                            "",
                            "New Item",
                            "",
                            "",
                            "0",
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

                    //"PLU", "Name", "Price", "PriceType", "Qty", "AddQty", "StockGr", "VAT", "Dept","Turnover", "SoldQty", "Bar1", "Bar2", "Bar3", "Bar4", "Unit"
                    //PLU,//taxGr,//dep,//group,//priceType,//price,//addQty,//quantity,//bar1,//bar2,//bar3,//bar4,//name,//turnover,//soldQty,//unit; //state;
                    for (CSVRecord record : records) {
                        if (record.get("PLU").equals("PLU")) continue; //Ignore Header
                        cmdItems.ItemModel item = new cmdItems.ItemModel();
                        item.setPLU(record.get("PLU"));
                        List<String> arrlst = Arrays.asList(taxRatesValues);
                        //Convert value of TaxGr is A,B....to index 1,2...
                        item.setTaxGr(String.valueOf(arrlst.lastIndexOf(record.get("VAT")) + 1));//A-H to 1-7
                        item.setDep(record.get("Dept"));//0-99
                        item.setGroup(record.get("StockGr"));//1-99;
                        item.setPriceType(record.get("PriceType")); //0,1,2;
                        item.setPrice(record.get("Price"));
                        item.setAddQty(record.get("AddQty"));
                        item.setQuantity(record.get("Qty"));
                        item.setBar1(record.get("Bar1"));
                        item.setBar2(record.get("Bar2"));
                        item.setBar3(record.get("Bar3"));
                        item.setBar4(record.get("Bar4"));
                        item.setName(record.get("Name"));
                        item.setTurnover(record.get("Turnover"));
                        item.setSoldQty(record.get("SoldQty"));
                        item.setUnit(record.get("Unit"));
                        item.setState(ITEM_NOT_SAVED);
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
                    //"PLU", "Name", "Price", "PriceType", "Qty", "AddQty", "StockGr", "VAT", "Dept","Turnover", "SoldQty", "Bar1", "Bar2", "Bar3", "Bar4", "Unit"
                    for (cmdItems.ItemModel item : items) {
                        csvPrinter.printRecord(
                                item.getPLU(),
                                item.getName(),
                                item.getPrice(),
                                item.getPriceType(),
                                item.getQuantity(),
                                item.getAddQty(),
                                item.getGroup(),
                                taxRatesValues[Integer.valueOf(item.getTaxGr()) - 1],//1..2... to A -H
                                item.getDep(),
                                item.getTurnover(),
                                item.getSoldQty(),
                                item.getBar1(),
                                item.getBar2(),
                                item.getBar3(),
                                item.getBar4(),
                                item.getUnit());
                    }
                    csvPrinter.close();
                    rescanFolder(getContext(), new File(filePath).getParent());
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
                        rescanFolder(getContext(), new File(filePath).getParent());
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

    private cmdItems.ItemModel readItem(int itemID) {
        try {
            return myItems.ReadItem(itemID);
        } catch (Exception e) {
            postToast(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void WritingAllItems() {
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
                        myItems.SetItem(temp);
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
        items = new ArrayList<>();
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
                    int F = fromPlu;
                    int L = toPlu;
                    progress.setMax(L);
                    for (int i = F; i <= L; i++) {
                        cmdItems.ItemModel temp = readItem(i);
                        if (temp != null) mItems.add(temp);
                        else
                            postToast(getString(R.string.msg_item_not_found));
                        if (!runRead[0]) {
                            return;
                        }
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
                    int F = 1;
                    int L = getCountProgrammedItems();
                    progress.setMax(L);
                    cmdItems.ItemModel temp = firsItem(F);
                    mItems.add(temp);
                    for (int i = F; i < L; i++) {
                        temp = nextItem();
                        if (temp == null) break;
                        mItems.add(temp);
                        if (!runRead[0]) {
                            break;
                        }
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


    private void Delete_inRange(final int f, final int l) {
        final boolean[] runTh = {true};
        progress = new ProgressDialog(getContext());
        progress.setTitle(R.string.title_delete_items);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setCancelable(false);
        progress.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runTh[0] = false;
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
                        if (!runTh[0]) {
                            break;
                        }

                        if (!myItems.DelItemsInRange(pluCode, pluCode))
                            postToast(getString(R.string.msg_item_not_found));

                        for (int gridPos = 0; gridPos < mItems.size(); gridPos++) {
                            if (pluCode == Integer.valueOf(mItems.get(gridPos).getPLU())) {// ima li takova PLU v grida
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
            android.view.View.OnClickListener {
        private Button yes, no;
        private EditText edFrom;
        private EditText edTo;
        private CheckBox chbAll;
        private int typeOfDialog;
        private String fItem;
        private String lItem;


        public Dialog_setRangeOfPLU(Activity a, int operation, String minPLUcode, String maxPLUcode) {
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
            setContentView(R.layout.dialog_items_range);
            yes = findViewById(R.id.btn_yes);
            no = findViewById(R.id.btn_no);
            edFrom = findViewById(R.id.ed_FromItem); //set s nalichinite
            edTo = findViewById(R.id.ed_ToItem);     //set
            chbAll = findViewById(R.id.chbxAll);     //set
            edFrom.setText(fItem);
            edTo.setText(lItem);
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
                            if (!chbAll.isChecked()) ReadItems_inRange(f, l);
                            else ReadItems_All();
                        if (typeOfDialog == SET_DELETE_RANGE)
                            if (!chbAll.isChecked()) Delete_inRange(f, l);
                            else DeleteItems_All();
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

    private void DeleteItems_All() throws Exception {
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
                        adapter = new ItemsListAdapter(getContext(), items);
                        binder.lvPluList.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }


    private String[] initUnitNamesFromDevice() {
        String[] unitNames = new String[20];
        for (int i = 0; i < 20; i++) {
            try {
                unitNames[i] = new cmdInfo().GetUnitName(i);
            } catch (Exception e) {
                postToast(e.getMessage());
                e.printStackTrace();
            }

        }
        return unitNames;
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