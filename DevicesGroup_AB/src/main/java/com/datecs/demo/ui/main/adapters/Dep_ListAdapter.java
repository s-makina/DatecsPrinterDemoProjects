package com.datecs.demo.ui.main.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
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
        TextView tvTaxGr;
        TextView tvRecSalesSum;
        TextView tvTotalSalesSum;
        TextView tvDeptLines;
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
            holder.tvTaxGr = convertView.findViewById(R.id.tvDeptTaxGr);
            holder.tvRecSalesSum = convertView.findViewById(R.id.tvRecSalesSum);
            holder.tvTotalSalesSum = convertView.findViewById(R.id.tvTotSalesSum);
            holder.tvDeptLines = convertView.findViewById(R.id.tvTextLines);
            convertView.setTag(holder);
        }else   holder=(ViewHolder)convertView.getTag();
         DepartmentListModel item  = items.get(position);
        //holder.tvDeptID.setText(position);
        holder.tvDeptID.setText(item.getDeptID());
        holder.tvTaxGr.setText(item.getDeptTaxGr());
        holder.tvRecSalesSum.setText(item.getDeptRecSalesSum());
        holder.tvTotalSalesSum.setText(item.getDeptTotalSalesSum());
        holder.tvDeptLines.setText(item.getDeptNameLines());

        return convertView;
    }

}


