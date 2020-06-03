package com.watt.canpay.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.watt.canpay.R;
import com.watt.canpay.model.MenuItem;

import java.util.ArrayList;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.MyViewHolder>{

    public interface MenuItemListener{
        void onMenuItemChanged(MenuItem menuItem);
    }

    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private ArrayList<MenuItem> menuItems;
    private MenuItemListener menuItemListener;

    public MenuItemAdapter(Context context, ArrayList<MenuItem> menuItems){
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        menuItemListener = (MenuItemListener)context;
        this.menuItems = menuItems;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return  new MyViewHolder(mLayoutInflater.inflate(R.layout.row_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if(menuItems.size()<position)return;
        MenuItem menuItem = menuItems.get(position);
        holder.rootView.setTag(menuItem);
        if(menuItem.getItemQuantity() == 0){
            holder.btnMinus.setEnabled(false);
        }else{
            holder.btnMinus.setEnabled(true);
        }
        holder.txvItemName.setText(menuItem.getItemName());
        holder.txvItemPrice.setText("₹".concat(menuItem.getItemPrice()));
        holder.txvItemCount.setText(String.valueOf(menuItem.getItemQuantity()));
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private LinearLayout rootView;
        private TextView txvItemName, txvItemPrice, txvItemCount;
        private Button btnAdd, btnMinus;
        public MyViewHolder(View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.root_view);
            txvItemName = itemView.findViewById(R.id.txv_item_name);
            txvItemPrice = itemView.findViewById(R.id.txv_item_price);
            txvItemCount = itemView.findViewById(R.id.item_count);
            btnAdd = itemView.findViewById(R.id.item_add);
            btnMinus = itemView.findViewById(R.id.item_minus);

            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MenuItem menuItem = (MenuItem)rootView.getTag();
                    menuItem.setItemQuantity(menuItem.getItemQuantity()+1);
                    menuItem.setAdded(true);
                    menuItemListener.onMenuItemChanged(menuItem);
                    notifyDataSetChanged();
                }
            });

            btnMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MenuItem menuItem = (MenuItem)rootView.getTag();
                    menuItem.setItemQuantity(menuItem.getItemQuantity()-1);
                    menuItem.setAdded(false);
                    menuItemListener.onMenuItemChanged(menuItem);
                    notifyDataSetChanged();
                }
            });
        }
    }
}