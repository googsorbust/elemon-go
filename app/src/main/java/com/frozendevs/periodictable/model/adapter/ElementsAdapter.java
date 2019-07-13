package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.frozendevs.periodictable.activity.PropertiesActivity;
import com.frozendevs.periodictable.model.ElementListItem;
import com.frozendevs.periodictable.view.RecyclerView;
import org.jetbrains.annotations.NotNull;
import org.tensorflow.lite.examples.detection.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ElementsAdapter extends RecyclerView.Adapter<ElementsAdapter.ViewHolder> {
    private ElementListItem[] mItems = new ElementListItem[0];
    private List<ElementListItem> mFilteredItems = new ArrayList<>();

    public ElementsAdapter() {
        setHasStableIds(true);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.elements_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
        ElementListItem item = mFilteredItems.get(position);

        holder.setName(item.getName());
        holder.setNumber(item.getNumber());
        holder.setSymbol(item.getSymbol());
    }

    @Override
    public int getItemCount() {
        return mFilteredItems.size();
    }

    @Override
    public long getItemId(int i) {
        return mFilteredItems.get(i).hashCode();
    }

    public void filter(Context context, String filter) {
        if (mItems.length > 0 && filter != null) {
            List<ElementListItem> filteredItems = new ArrayList<>();

            Locale locale = context.getResources().getConfiguration().locale;

            int nextPos = 0;
            for (ElementListItem element : mItems) {
                if (element.getSymbol().toLowerCase(locale).equalsIgnoreCase(filter)) {
                    filteredItems.add(0, element);

                    nextPos += 1;
                } else if (element.getSymbol().toLowerCase(locale).startsWith(filter.toLowerCase(
                        locale)) || String.valueOf(element.getNumber()).startsWith(filter)) {
                    filteredItems.add(nextPos, element);

                    nextPos += 1;
                } else if (element.getName().toLowerCase(locale).startsWith(
                        filter.toLowerCase(locale))) {
                    filteredItems.add(element);
                }
            }

            mFilteredItems = new ArrayList<>(filteredItems);

            notifyDataSetChanged();
        }
    }

    public void clearFilter() {
        mFilteredItems = new ArrayList<>(Arrays.asList(mItems));

        notifyDataSetChanged();
    }

    public ElementListItem[] getItems() {
        return mItems;
    }

    public void setItems(List<ElementListItem> items) {
        setItems(items.toArray(new ElementListItem[0]));
    }

    public void setItems(ElementListItem[] items) {
        mItems = items != null ? items.clone() : new ElementListItem[0];

        mFilteredItems = new ArrayList<>(Arrays.asList(mItems));

        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        TextView mSymbolView, mNumberView, mNameView;
        int mNumber;

        public ViewHolder(View itemView) {
            super(itemView);

            mSymbolView = itemView.findViewById(R.id.element_symbol);
            mNumberView = itemView.findViewById(R.id.element_number);
            mNameView = itemView.findViewById(R.id.element_name);

            itemView.setOnClickListener(this);
        }

        public void setName(String name) {
            mNameView.setText(name);
        }

        public void setNumber(int number) {
            mNumberView.setText(Integer.toString(mNumber = number));
        }

        public void setSymbol(String symbol) {
            mSymbolView.setText(symbol);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), PropertiesActivity.class);
            intent.putExtra(PropertiesActivity.EXTRA_ATOMIC_NUMBER, mNumber);

            view.getContext().startActivity(intent);
        }
    }
}
