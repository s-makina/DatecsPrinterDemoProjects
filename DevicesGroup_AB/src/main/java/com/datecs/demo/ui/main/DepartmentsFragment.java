package com.datecs.demo.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.datecs.demo.DepartmentsFrgBinding;
import com.datecs.demo.ui.main.adapters.Dep_ListAdapter;
import com.datecs.demo.ui.main.adapters.DepartmentListModel;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdConfig;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdInfo;
import com.datecs.testApp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DepartmentsFragment extends Fragment {
    private ArrayList<DepartmentListModel> items;
    private Dep_ListAdapter adapter;
    private DepartmentsFrgBinding binder;
    private ProgressDialog progress;
    private cmdInfo myInfo = new cmdInfo();
    private int deptIndexToEdit;
    private int clickPosition;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("deptItems", items);
        super.onSaveInstanceState(outState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.fragment_departments, container, false);

        if (savedInstanceState == null) {
            items = new ArrayList<>();
            adapter = new Dep_ListAdapter(getContext(), items);
        } else {
            items = (ArrayList<DepartmentListModel>) savedInstanceState.getSerializable("deptItems");
            adapter = new Dep_ListAdapter(getContext(), items);
            binder.lvDepartments.setAdapter(adapter);
        }

        binder.lvDepartments.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adpterView, View view, final int position, long id) {
                DepartmentListModel selectedItem = items.get(position);
                clickPosition = position;
                deptIndexToEdit = Integer.parseInt(items.get(position).getDeptID());
                DialogDepartments d = new DialogDepartments(getActivity(), selectedItem);
                d.show();
                d.setTitle("Edit Department");
                d.show();
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int width = (int) ((int) displaymetrics.widthPixels * 0.6);
                int height = (int) ((int) displaymetrics.heightPixels * 0.6);
                d.getWindow().setLayout(width, height);
            }
        });
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binder.btnReadDept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_setRangeToRead cdd =
                        new Dialog_setRangeToRead(getActivity(), 1, 9);
                cdd.show();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////DIALOG RANGE
    public class Dialog_setRangeToRead extends Dialog implements
            View.OnClickListener {
        private Button yes, no;
        private EditText edFrom;
        private EditText edTo;
        private CheckBox chkAll;
        private int fItem;
        private int lItem;

        public Dialog_setRangeToRead(Activity a, int intiStart, int initEnd) {
            super(a);
            // TODO Auto-generated constructor stub
            fItem = intiStart;
            lItem = initEnd;
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
            chkAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                  @Override
                                                  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                      if (isChecked) {
                                                          edFrom.setText("1");
                                                          edTo.setText(String.valueOf(myInfo.getMaxDepartments()));
                                                      } else {
                                                          edFrom.setText(String.valueOf(fItem));
                                                          edTo.setText(String.valueOf(lItem));
                                                      }
                                                  }
                                              }
            );

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
                        readDepartmentRange(f, l);

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

    private void readDepartmentRange(int from, int to) {
        progress = ProgressDialog.show(getContext(), getString(R.string.title_reading_items), getString(R.string.msg_please_wait), true);
        final ArrayList<DepartmentListModel> mItems = new ArrayList<>();
        items.clear();
        adapter.notifyDataSetChanged();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // do the thing that takes a long time
                try {
                    for (int i = from; i <= to; i++) { //
                        cmdInfo.DepartmentData dd = myInfo.GetDepartmentData(i);
                        DepartmentListModel temp = new DepartmentListModel(
                                String.valueOf(i),
                                dd.getTaxGr(),
                                dd.getRecSales() + "/" + dd.getRecSum(),
                                dd.getTotSum() + "/" + dd.getTotSum(),
                                dd.getLine1() + "/" + dd.getLine2());
                        if (dd.getTaxGr().length() == 1)//If department is used, insert in table
                            mItems.add(temp);
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
                        items.addAll(mItems);
                        adapter = new Dep_ListAdapter(getContext(), items);
                        binder.lvDepartments.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }

    private void updateDepartmentInfo(int depIndex) {
        try {
            cmdInfo.DepartmentData dd = myInfo.GetDepartmentData(depIndex); //read Dept info
            DepartmentListModel temp = new DepartmentListModel(
                    String.valueOf(depIndex),
                    dd.getTaxGr(),
                    dd.getRecSales() + "/" + dd.getRecSum(),
                    dd.getTotSum() + "/" + dd.getTotSum(),
                    dd.getLine1() + "/" + dd.getLine2());
            if (deptIndexToEdit != depIndex) items.add(temp); //To add new element
            else items.set(clickPosition, temp); // Update element on position of clicking

            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            postToast(e.getMessage());
        }

    }

    public void postToast(final String text) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
            }
        });
    }


    public class DialogDepartments extends Dialog implements
            View.OnClickListener {
        private ImageButton yes, no;
        private EditText edLine1Text;
        private EditText edLine2Text;
        private Spinner spDeptNum;
        private Spinner spDeptTaxGr;
        private cmdConfig myConfig = new cmdConfig();
        private DepartmentListModel selectedItem;
        private Activity a;

        public DialogDepartments(Activity a, DepartmentListModel selectedItem) {
            super(a, R.style.Dialog);
            this.a = a;
            this.selectedItem = selectedItem;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_edit_department);
            edLine1Text = findViewById(R.id.edDepLine1);
            edLine2Text = findViewById(R.id.edDepLine2);
            spDeptNum = findViewById(R.id.spDepNum);
            spDeptTaxGr = findViewById(R.id.spDepTaxGr);
            yes = findViewById(R.id.btn_set_department);
            no = findViewById(R.id.btn_cancel_department);
            yes.setOnClickListener(this);
            no.setOnClickListener(this);

            String[] items = new String[myConfig.getMaxDepartments()];
            for (int i = 0; i < myConfig.getMaxDepartments(); i++)
                items[i] = "Department " + (i + 1);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
            spDeptNum.setAdapter(adapter);

            String[] lines = selectedItem.getDeptNameLines().split("/");
            if (lines.length > 0) edLine1Text.setText(lines[0]);
            if (lines.length > 1) edLine2Text.setText(lines[1]);

            spDeptNum.setSelection(Integer.parseInt(selectedItem.getDeptID()) - 1);
            List<String> taxList = Arrays.asList(new String[]{"А", "Б", "В", "Г", "Д", "Е", "Ж", "З"});
            spDeptTaxGr.setSelection(taxList.indexOf(selectedItem.getDeptTaxGr()));

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.btn_set_department:
                    try {
                        myConfig.setDepartment(
                                String.valueOf(1 + (int) spDeptNum.getSelectedItemId()),
                                (String) spDeptTaxGr.getSelectedItem(),
                                String.valueOf(edLine1Text.getText()),
                                String.valueOf(edLine2Text.getText()));
                        updateDepartmentInfo((int) (spDeptNum.getSelectedItemId() + 1));
                        dismiss();
                    } catch (Exception e) {
                        postToast(e.getMessage());
                        e.printStackTrace();
                    }

                    break;
                case R.id.btn_cancel_department:
                    dismiss();
                    break;
                default:
                    break;
            }

        }


        private void postToast(final String message) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(a, message, Toast.LENGTH_LONG).show();
                }
            });
        }

    }


}



