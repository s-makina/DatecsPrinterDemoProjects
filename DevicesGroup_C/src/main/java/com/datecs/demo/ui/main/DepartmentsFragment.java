package com.datecs.demo.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import com.datecs.demo.Page_Departments_frg_binding;
import com.datecs.demo.ui.main.adapters.Dep_ListAdapter;
import com.datecs.demo.ui.main.adapters.DepartmentListModel;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdConfig;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdService;
import com.datecs.testApp.R;


import java.util.ArrayList;

public class DepartmentsFragment extends Fragment {
    private ArrayList<DepartmentListModel> items;
    private Dep_ListAdapter adapter;
    private Page_Departments_frg_binding binder;
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
        binder = DataBindingUtil.inflate(inflater, R.layout.departments_frag, container, false);

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
                deptIndexToEdit = items.get(position).getDeptID();
                DialogDepartments d = new DialogDepartments(getActivity(), selectedItem);
                d.setTitle("Edit Department");
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(d.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                d.show();
                d.getWindow().setAttributes(lp);

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
                progress = ProgressDialog.show(getContext(), getString(R.string.title_reading_items), getString(R.string.msg_please_wait), true);
                final ArrayList<DepartmentListModel> mItems = new ArrayList<>();
                items.clear();
                adapter.notifyDataSetChanged();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // do the thing that takes a long time
                        try {
                            final int cntDepartment = new cmdService().getMaxDepartments();
                            for (int i = 0; i < cntDepartment; i++) {
                                DepartmentListModel temp = new DepartmentListModel(i,
                                        myInfo.GetDeptName(i),
                                        Integer.valueOf(myInfo.GetDeptVat(i)),
                                        Double.valueOf(myInfo.GetDeptPrice(i))/100.00);
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
        });
    }

    private void updateDepartmentInfo(int i) {
        try {
            DepartmentListModel temp = new DepartmentListModel(i,
                    myInfo.GetDeptName(i),
                    Integer.valueOf(myInfo.GetDeptVat(i)),
                    Double.valueOf(myInfo.GetDeptPrice(i))/100.00);
            if (deptIndexToEdit != i) items.add(temp); //To add new element
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
        private EditText edName;
        private EditText edPrice;
        private Spinner spNum;
        private Spinner spVAT;
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
            edPrice = findViewById(R.id.edDepPrice);
            edName = findViewById(R.id.edDepName);
            spNum = findViewById(R.id.spDepNum);
            spVAT = findViewById(R.id.spDepTaxGr);
            yes = findViewById(R.id.btn_set_department);
            no = findViewById(R.id.btn_cancel_department);
            yes.setOnClickListener(this);
            no.setOnClickListener(this);
            edName.setText(selectedItem.getDeptName());
            edPrice.setText(String.valueOf(selectedItem.getDeptPrice()));
            String[] items = new String[myConfig.getMaxDepartments()];
            for (int i = 0; i < myConfig.getMaxDepartments(); i++)
                items[i] = String.valueOf((i + 1));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
            spNum.setAdapter(adapter);
            spNum.setSelection(selectedItem.getDeptID());
            spVAT.setSelection(selectedItem.getDeptVAT()-1);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.btn_set_department:
                    try {
                        int depIndex = (int) spNum.getSelectedItemId();
                        myConfig.SetDeptName(depIndex, edName.getText().toString());
                        myConfig.SetDeptVAT(depIndex, String.valueOf(spVAT.getSelectedItemId()+1));
                        Double priceForDepartmen = Double.valueOf(edPrice.getText().toString());
                        String priceInStot=String.valueOf(Math.round(priceForDepartmen*100.00));
                        myConfig.SetDeptPrice(depIndex,priceInStot);
                        updateDepartmentInfo((int) (spNum.getSelectedItemId()));
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



