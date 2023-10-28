package com.datecs.demo.ui.main.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.datecs.demo.ui.main.ItemsListFragment;
import com.datecs.fiscalprinter.SDK.model.UserLayerV2.cmdItems;
import com.datecs.testApp.R;
import java.util.ArrayList;
import java.util.List;


public class ItemsListAdapter extends ArrayAdapter<cmdItems.ItemModel> {
    private final String[] priceTypeValues = {"Fixed", "Free", "Max"};
    private final String[] taxRatesValues = {"A", "B", "C", "D", "E", "F", "G", "H"};

    private List<cmdItems.ItemModel> items;
    private Context context;
    private SparseBooleanArray mSelectedItemsIds;


    public ItemsListAdapter(@NonNull Context context, @NonNull ArrayList<cmdItems.ItemModel> list) {
        super(context, R.layout.custom_plu_list_lv, list);
        mSelectedItemsIds = new SparseBooleanArray();
        this.context = context;
        items = list;
    }


    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public cmdItems.ItemModel getItem(int i) {
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
    public void remove(cmdItems.ItemModel object) {
        items.remove(object);
        notifyDataSetChanged();
    }
    class ViewHolder {

        TextView vPLUID;
        TextView dPLUName;
     //   TextView dPLUDept;
        TextView dPLUMUnit;
        TextView dPLUPrice;
        TextView dPLUPriceType;
        TextView dPLUSoldQty;
        TextView dPLUStockQTY;
        TextView dPLUTurnover;
     //   TextView dPLUVAT;
//        TextView dPLUStockGr;
//        TextView dPLUBar1;
//        TextView dPLUBar2;
//        TextView dPLUBar3;
//        TextView dPLUBar4;
    }

    @NonNull
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = new ViewHolder();
        View v = view;
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (v == null) {
            v = inflater.inflate(R.layout.custom_plu_list_lv, null);

        }
        cmdItems.ItemModel item = items.get(i);
        holder.vPLUID = v.findViewById(R.id.tv_PLU_ID);
        holder.dPLUName = v.findViewById(R.id.ed_PLU_Name);
        //holder.dPLUVAT = (TextView) v.findViewById(R.id.ed_PLU_Name);
      //  holder.dPLUStockGr = (TextView) v.findViewById(R.id.ed_PLU_StockGr);
        //holder.dPLUDept = (TextView) v.findViewById(R.id.ed_PLU_Name);
        holder.dPLUPriceType = v.findViewById(R.id.ed_PLU_PriceType);
        holder.dPLUPrice = v.findViewById(R.id.ed_PLU_Price);
        holder.dPLUSoldQty = v.findViewById(R.id.ed_PLU_SoldQty);
        holder.dPLUStockQTY = v.findViewById(R.id.ed_PLU_StockQTY);
        holder.dPLUTurnover = v.findViewById(R.id.ed_PLU_Turnover);
        holder.dPLUMUnit = v.findViewById(R.id.ed_PLU_MUnit);
//        holder.dPLUBar1 = (TextView) v.findViewById(R.id.ed_PLU_Bar1);
//        holder.dPLUBar2 = (TextView) v.findViewById(R.id.ed_PLU_Bar2);
//        holder.dPLUBar3 = (TextView) v.findViewById(R.id.ed_PLU_Bar3);
//        holder.dPLUBar4 = (TextView) v.findViewById(R.id.ed_PLU_Bar4);

        holder.vPLUID.setText(item.getPLU());

        switch (item.getState()) {
            case ITEM_SAVED:
                holder.vPLUID.setBackgroundResource(R.drawable.bg_blue_item);
                break;
            case ITEM_NOT_SAVED:
                holder.vPLUID.setBackgroundResource(R.drawable.bg_red_item);
                break;
            case ITEM_IS_READ:
                holder.vPLUID.setBackgroundResource(R.drawable.bg_table_family);
                break;
        }


        holder.dPLUName.setText(item.getName());
        holder.dPLUPrice.setText(item.getPrice());
        holder.dPLUPriceType.setText(priceTypeValues[Integer.parseInt(item.getPriceType())]);
        holder.dPLUMUnit.setText(ItemsListFragment.mUnitNames[Integer.parseInt(item.getUnit())]);
        holder.dPLUStockQTY.setText(item.getQuantity());
        holder.dPLUSoldQty.setText(item.getSoldQty());
        holder.dPLUTurnover.setText(item.getTurnover());


        return v;
    }


}


