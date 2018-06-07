package com.vastukosh.android.localist.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vastukosh.android.localist.DataStructures.AllItems;
import com.vastukosh.android.localist.Interfaces.CustomItemClickListener;
import com.vastukosh.android.localist.R;

import java.util.ArrayList;
import java.util.List;

public class AllItemsAdapter extends RecyclerView.Adapter<AllItemsAdapter.ItemHolder> {

    private Context context;
    private List<AllItems> item;
    CustomItemClickListener listener;

    public AllItemsAdapter(Context applicationContext, ArrayList<AllItems> listItems, CustomItemClickListener listener) {
        context = applicationContext;
        item = listItems;
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.bindItem(item.get(position), context);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_item, parent, false);
        final ItemHolder itemHolder = new ItemHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(v, itemHolder.getPosition());
            }
        });
        return itemHolder;
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        public View itemView;
        public TextView itemNameText;

        public ItemHolder(View itemView) {
            super(itemView);
            itemNameText = itemView.findViewById(R.id.itemNameText);
        }

        public void bindItem(AllItems allItems, Context context) {
            itemNameText.setText(allItems.itemName + " : " + allItems.storeName);
        }

    }

}
