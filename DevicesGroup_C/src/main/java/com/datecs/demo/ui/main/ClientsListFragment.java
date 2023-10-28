package com.datecs.demo.ui.main;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.datecs.demo.ClientsListFragment_binding;
import com.datecs.fileselector.FileOperation;
import com.datecs.fileselector.FileSelector;
import com.datecs.fileselector.OnHandleFileListener;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdClients;
import com.datecs.testApp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static com.datecs.fileselector.FileUtils.rescanFolder;
import static com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdClients.ClientInfoModel.ItemState.ITEM_NOT_SAVED;


public class ClientsListFragment extends Fragment {

    private static final int LOAD_CSV_RESULTS = 11890;
    String[] headerCSVfile = {"FIRM", "Name", "TypeTAXN", "TAXN", "RecName", "VATN", "Addr1", "Addr2"};
    private ClientsListFragment_binding binder;
    private ClientsListAdapter adapter;
    private ProgressDialog progress;
    private String[] mFileFilter = {".csv", ".txt"};
    private File lastOpenFolder = Environment.getExternalStorageDirectory(); //TODO:Read Settings from file !
    private int currentCursor = 0;
    private final int SET_READ_RANGE = 0;
    private final int SET_DELETE_RANGE = 1;
    private ArrayList<cmdClients.ClientInfoModel> items;
    private cmdClients myClients = new cmdClients();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("clientsListItems", items);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binder = DataBindingUtil.inflate(inflater, R.layout.clients_info_frag, container, false);

        if (savedInstanceState == null) {
            items = new ArrayList<>();
            adapter = new ClientsListAdapter(getContext(), items);

        } else {
            items = (ArrayList<cmdClients.ClientInfoModel>) savedInstanceState.getSerializable("clientsListItems");
            adapter = new ClientsListAdapter(getContext(), items);
            binder.lvClients.setAdapter(adapter);
        }
        BottomNavigationView navigation = binder.naviBottomClients;
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //Register of messages
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mSetItemMessageReceiver, new IntentFilter("setClient"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDeleteItemMessageReceiver, new IntentFilter("deleteClient"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mAddItemMessageReceiver, new IntentFilter("addClient"));
        binder.lvClients.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        /**
         * Capture ListView item click:
         *  a short click opens a dialog to edit the item on position
         *   through long clicks, you can select rows from the list to delete from the device
         */
        binder.lvClients.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
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
                final int checkedCount = binder.lvClients.getCheckedItemCount();
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
                                        cmdClients.ClientInfoModel selecteditem = adapter.getItem(selected.keyAt(i));
                                        // Remove selected items following the ids
                                        int itemToDelete = Integer.valueOf(selecteditem.getFIRM());
                                        try {
                                            myClients.DelClientsInRange(itemToDelete, itemToDelete);
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
                cmdClients.ClientInfoModel temp = (cmdClients.ClientInfoModel) intent.getSerializableExtra("itemData");
                myClients.SetItem(temp);
                items.add(0, temp);//Add position on the top of table
                if (adapter == null) {
                    adapter = new ClientsListAdapter(getContext(), items);
                    binder.lvClients.setAdapter(adapter);
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
                cmdClients.ClientInfoModel temp = (cmdClients.ClientInfoModel) intent.getSerializableExtra("itemData");
                myClients.SetItem(temp);
                // ToDo: Rom
                if (currentCursor < items.size()) items.set(currentCursor, temp);  // Redakcia
                else items.add(0, temp);  // Dobavia

                if (adapter == null) {
                    adapter = new ClientsListAdapter(getContext(), items);
                    binder.lvClients.setAdapter(adapter);
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
                if (myClients.DelClientsInRange(temp, temp)) {
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

        binder.lvClients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adpterView, View view, int position, long id) {
                StartItemEditDialog(position, items.get(position));
                currentCursor = position;

            }
        });


    }

    private void StartItemEditDialog(int position, cmdClients.ClientInfoModel itemToEdit) {
        try {
            DialogClientEdit d = new DialogClientEdit(getActivity(), itemToEdit);
            d.show();

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(d.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            d.show();
            d.getWindow().setAttributes(lp);
//            DisplayMetrics displaymetrics = new DisplayMetrics();
//            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//            int width = (int) ((int) displaymetrics.widthPixels * 0.90);
//            int height = (int) ((int) displaymetrics.heightPixels * 0.90);
//            d.getWindow().setLayout(width, height);
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
                                    readAllItems();
                                    break;
                                case R.id.mnu_read_range_items:
                                    Dialog_ItemsRange cdd =
                                            new Dialog_ItemsRange(getActivity(), SET_READ_RANGE, "1", "100");
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
                                    cdd = new Dialog_ItemsRange(
                                            getActivity(),
                                            SET_DELETE_RANGE,
                                            String.valueOf(items.get(0).getFIRM()),
                                            String.valueOf(items.get(items.size() - 1).getFIRM()));
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


            }
            return false;
        }

    };

    private void addNewItem() {
        currentCursor = items.size(); // Add to end
        try {
            StartItemEditDialog(currentCursor,
                    new cmdClients.ClientInfoModel(
                            myClients.GetFirstNotProgrammed(0),
                            "",
                            "",
                            cmdClients.ClientInfoModel.TypeTAXN.BULSTAT,
                            "",
                            "",
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
        final ArrayList<cmdClients.ClientInfoModel> mItems = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Reader in = new FileReader(filePath);
                    Iterable<CSVRecord> records = CSVFormat.EXCEL.withDelimiter(';').withHeader(headerCSVfile).parse(in);

                    for (CSVRecord record : records) {
                        if (record.get("FIRM").equals("FIRM")) continue;
                        mItems.add(
                                new cmdClients.ClientInfoModel(
                                        Integer.valueOf(record.get("FIRM")),
                                        record.get("Name"),
                                        record.get("TAXN"),
                                        cmdClients.ClientInfoModel.TypeTAXN.fromOrdinal(Integer.parseInt(record.get("TypeTAXN"))),
                                        record.get("RecName"),
                                        record.get("VATN"),
                                        record.get("Addr1"),
                                        record.get("Addr2"),
                                        ITEM_NOT_SAVED
                                ));
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
                        adapter = new ClientsListAdapter(getContext(), items);
                        binder.lvClients.setAdapter(adapter);
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
                    //String[] headerCSVfile = {"FIRM", "Name", "TypeTAXN", "TAXN", "RecName", "VATN", "Addr1", "Addr2"};
                    for (cmdClients.ClientInfoModel item : items) {
                        csvPrinter.printRecord(
                                item.getFIRM(),
                                item.getName(),
                                item.getTypeTAXN().ordinal(),
                                item.getEIK(),
                                item.getRecName(),
                                item.getVATN(),
                                item.getAddr1(), item.getAddr2());
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
                        Snackbar.make(getView(), R.string.msg_file_saved, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
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
                    for (cmdClients.ClientInfoModel temp : items) {
                        myClients.SetItem(temp);
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
                        binder.lvClients.setAdapter(adapter);
                        binder.lvClients.setSelection(prog[0]);
                    }
                });
            }
        }).start();
    }


    private void readItemsInRange(final int fromPlu, final int toPlu) {
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
        final ArrayList<cmdClients.ClientInfoModel> mItems = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // do the thing that takes a long time
                try {
                    int F = fromPlu;
                    int L = toPlu;
                    progress.setMax(L);
                    for (int i = F; i <= L; i++) {
                        cmdClients.ClientInfoModel temp = myClients.ReadItemByID(i);
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
                        adapter = new ClientsListAdapter(getContext(), items);
                        binder.lvClients.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }

    private void readAllItems() {

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
        final ArrayList<cmdClients.ClientInfoModel> mItems = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // do the thing that takes a long time
                try {
                    int F = 1;
                    int L = myClients.GetItemsInformation().getTotalCountOfProgrammedClients();
                    progress.setMax(L);
                    cmdClients.ClientInfoModel temp = myClients.GetFirstFoundProgrammed(F);
                    mItems.add(temp);
                    for (int i = F; i < L; i++) {
                        temp = myClients.GetNextProgrammed();
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
                        adapter = new ClientsListAdapter(getContext(), items);
                        binder.lvClients.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }


    private void deleteItemsInRange(final int f, final int l) {
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
        final ArrayList<cmdClients.ClientInfoModel> mItems = new ArrayList<>();
        mItems.addAll(items);
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    for (int pluCode = f; pluCode <= l; pluCode++) {
                        if (!runTh[0]) {
                            break;
                        }

                        if (!myClients.DelClientsInRange(pluCode, pluCode))
                            postToast(getString(R.string.msg_item_not_found));

                        for (int gridPos = 0; gridPos < mItems.size(); gridPos++) {
                            if (pluCode == Integer.valueOf(mItems.get(gridPos).getFIRM())) {// ima li takova PLU v grida
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
                        adapter = new ClientsListAdapter(getContext(), items);
                        binder.lvClients.setAdapter(adapter);

                    }
                });
            }
        }).start();


    }


    public void setLastOpenFolder(File lastOpenFolder) {
        this.lastOpenFolder = new File(lastOpenFolder.getParent());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////DIALOG
    public class Dialog_ItemsRange extends Dialog implements
            View.OnClickListener {
        private Button yes, no;
        private EditText edFrom;
        private EditText edTo;
        private CheckBox chbAll;
        private int typeOfDialog;
        private String fItem;
        private String lItem;


        public Dialog_ItemsRange(Activity a, int operation, String fromItem, String toItem) {
            super(a);
            // TODO Auto-generated constructor stub
            typeOfDialog = operation;
            fItem = fromItem;
            lItem = toItem;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_items_range);
            yes = findViewById(R.id.btn_yes);
            no = findViewById(R.id.btn_no);
            chbAll = findViewById(R.id.chbxAll);
            edFrom = findViewById(R.id.ed_FromItem); //set s nalichinite
            edTo = findViewById(R.id.ed_ToItem);     //set
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
                            if (!chbAll.isChecked()) readItemsInRange(f, l);
                            else readAllItems();
                        if (typeOfDialog == SET_DELETE_RANGE)
                            if (!chbAll.isChecked()) deleteItemsInRange(f, l);
                            else deleteAllItems();

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

    private void deleteAllItems() throws Exception {
        myClients.DelAllItems();
        clearTable();
        adapter = new ClientsListAdapter(getContext(), items);
        binder.lvClients.setAdapter(adapter);
    }


    private void postToast(final String text) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
            }
        });

    }


    public class ClientsListAdapter extends ArrayAdapter<cmdClients.ClientInfoModel> {

        private List<cmdClients.ClientInfoModel> items;
        private Context context;
        private SparseBooleanArray mSelectedItemsIds;


        public ClientsListAdapter(@NonNull Context context, @NonNull ArrayList<cmdClients.ClientInfoModel> list) {
            super(context, R.layout.custom_client_info_item, list);
            mSelectedItemsIds = new SparseBooleanArray();
            this.context = context;
            items = list;
        }


        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public cmdClients.ClientInfoModel getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public void removeSelection() {
            mSelectedItemsIds = new SparseBooleanArray();
            notifyDataSetChanged();
        }

        public int getSelectedCount() {
            return mSelectedItemsIds.size();
        }

        public SparseBooleanArray getSelectedIds() {
            return mSelectedItemsIds;
        }

        private void selectView(int position, boolean value) {
            if (value)
                mSelectedItemsIds.put(position, value);
            else
                mSelectedItemsIds.delete(position);
            notifyDataSetChanged();
        }


        public void toggleSelection(int position) {
            selectView(position, !mSelectedItemsIds.get(position));
        }

        @Override
        public void remove(cmdClients.ClientInfoModel object) {
            items.remove(object);
            notifyDataSetChanged();
        }

        class ViewHolder {

            TextView tvFIRM;
            TextView tvName;
            TextView tvType;
            TextView tvTAXN;
            TextView tvRecName;
            TextView tvVATN;
            TextView tvAddr;
        }

        @NonNull
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = new ViewHolder();
            View v = view;
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (v == null) {
                v = inflater.inflate(R.layout.custom_client_info_item, null);

            }
            cmdClients.ClientInfoModel item = items.get(i);
            holder.tvFIRM = v.findViewById(R.id.tvCliInfoFIRM);
            holder.tvName = v.findViewById(R.id.tvCliInfoName);
            holder.tvType = v.findViewById(R.id.tvCliInfoTypeTAXN);
            holder.tvTAXN = v.findViewById(R.id.tvCliInfoTAXN);
            holder.tvRecName = v.findViewById(R.id.tvCliInfoRecName);
            holder.tvVATN = v.findViewById(R.id.tvCliInfoVATN);
            holder.tvAddr = v.findViewById(R.id.tvCliInfoAddr);

            switch (item.getState()) {
                case ITEM_SAVED:
                    holder.tvFIRM.setBackgroundResource(R.drawable.bg_blue_item);
                    break;
                case ITEM_NOT_SAVED:
                    holder.tvFIRM.setBackgroundResource(R.drawable.bg_red_item);
                    break;
                case ITEM_IS_READ:
                    holder.tvFIRM.setBackgroundResource(R.drawable.bg_table_family);
                    break;
            }

            final String[] typeTAXN = {"BULSTAT", "EGN", "LNCH", "SERVICE"};
            holder.tvFIRM.setText(String.valueOf(item.getFIRM()));
            holder.tvName.setText(item.getName());
            holder.tvType.setText(typeTAXN[item.getTypeTAXN().ordinal()]);
            holder.tvTAXN.setText(item.getEIK());
            holder.tvRecName.setText(item.getRecName());
            holder.tvVATN.setText(item.getVATN());
            holder.tvAddr.setText(item.getAddr1() + "\n\r" + item.getAddr2());


            return v;
        }

        class StateListItem {
            private String itemTitle;
            public long id;

            /*     private Boolean isItemSelected;

                 public StateListItem(String name, long id) {
                     this.itemTitle = name;
                     this.isItemSelected = false;
                     this.id = id;
                 }
         */
            @Override
            public String toString() {
                return this.itemTitle;
            }
        }
    }


}
