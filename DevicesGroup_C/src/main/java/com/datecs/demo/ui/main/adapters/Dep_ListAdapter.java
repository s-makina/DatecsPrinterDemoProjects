package com.datecs.demo.ui.main.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.datecs.testApp.R;

import java.util.ArrayList;


public class Dep_ListAdapter extends ArrayAdapter<DepartmentListModel> {
    private Context context;
    private ArrayList<DepartmentListModel> items;

    public Dep_ListAdapter(@NonNull Context context, @NonNull ArrayList<DepartmentListModel> list) {
        super(context, R.layout.custom_department_item_lv, list);
        this.context = context;
        items = list;
    }

    public ArrayList<DepartmentListModel> getItems() {
        return items;
    }

    class ViewHolder {
        TextView tvDeptID;
        TextView tvDeptVAT;
        TextView tvDeptPrice;
        TextView tvDeptName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView =
            inflater.inflate(R.layout.custom_department_item_lv, null);
            holder.tvDeptID = convertView.findViewById(R.id.tvDeptID);
            holder.tvDeptName = convertView.findViewById(R.id.tvDeptName);
            holder.tvDeptVAT = convertView.findViewById(R.id.tvDeptVAT);
            holder.tvDeptPrice = convertView.findViewById(R.id.tvDeptPice);
            convertView.setTag(holder);
        }else   holder=(ViewHolder)convertView.getTag();
         DepartmentListModel item  = items.get(position);
        //holder.tvDeptID.setText(position);
        holder.tvDeptID.setText(String.valueOf(item.getDeptID()+1));
        holder.tvDeptVAT.setText(String.valueOf(item.getDeptVAT()));
        holder.tvDeptPrice.setText(String.valueOf(item.getDeptPrice()));
        holder.tvDeptName.setText(item.getDeptName());
        return convertView;
    }

}


