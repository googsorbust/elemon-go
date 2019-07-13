package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.frozendevs.periodictable.model.TableElementItem;
import com.frozendevs.periodictable.model.TableItem;
import com.frozendevs.periodictable.model.TableTextItem;
import com.frozendevs.periodictable.view.PeriodicTableView;
import org.tensorflow.lite.examples.detection.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class TableAdapter extends PeriodicTableView.Adapter implements Parcelable {

    public static final Creator<TableAdapter> CREATOR = new Creator<TableAdapter>() {
        @Override
        public TableAdapter createFromParcel(Parcel in) {
            return new TableAdapter(in);
        }

        @Override
        public TableAdapter[] newArray(int size) {
            return new TableAdapter[size];
        }
    };

    private static final int GREYED_OUT = Color.GRAY;

    private static final int[] COLORS = {
            R.color.category_diatomic_nonmetals_bg,
            R.color.category_noble_gases_bg,
            R.color.category_alkali_metals_bg,
            R.color.category_alkaline_earth_metals_bg,
            R.color.category_metalloids_bg,
            R.color.category_polyatomic_nonmetals_bg,
            R.color.category_other_metals_bg,
            R.color.category_transition_metals_bg,
            R.color.category_lanthanides_bg,
            R.color.category_actinides_bg,
            R.color.category_unknown_bg
    };
    private static final Object[][] TEXT_ITEMS = {
            {4, R.string.category_actinides, 9},
            {5, R.string.category_alkali_metals, 2},
            {6, R.string.category_alkaline_earth_metals, 3},
            {7, R.string.category_diatomic_nonmetals, 0},
            {8, R.string.category_lanthanides, 8},
            {9, R.string.category_metalloids, 4},
            {22, R.string.category_noble_gases, 1},
            {23, R.string.category_polyatomic_nonmetals, 5},
            {24, R.string.category_other_metals, 6},
            {25, R.string.category_transition_metals, 7},
            {26, R.string.category_unknown, 10},
            {92, "57 - 71", 8},
            {110, "89 - 103", 9}
    };
    private Typeface mTypeface;
    private int mGroupsCount;
    private int mPeriodsCount;
    private Map<Integer, TableItem> mItems = new HashMap<>();
    private Set<Integer> seenElements;

    public TableAdapter() {
    }

    protected TableAdapter(Parcel in) {
        mGroupsCount = in.readInt();
        mPeriodsCount = in.readInt();

        seenElements = new HashSet<>();
        final int elSize = in.readInt();
        for (int i = 0; i < elSize; i++) {
            seenElements.add(in.readInt());
        }

        final int size = in.readInt();
        for (int i = 0; i < size; i++) {
            final int key = in.readInt();
            final TableItem value = in.readParcelable(TableItem.class.getClassLoader());

            mItems.put(key, value);
        }
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public TableItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        initialize(context);

        final TableItem item = getItem(position);

        if (item instanceof TableTextItem) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.table_text,
                        parent, false);
            }

            if (("57 - 71".equals(((TableTextItem) item).getText()) && seenElements.stream().noneMatch(i -> i >= 57 && i <= 71)) ||
                    ("89 - 103".equals(((TableTextItem) item).getText()) && seenElements.stream().noneMatch(i -> i >= 89 && i <= 103))) {
                convertView.setBackgroundColor(GREYED_OUT);
            } else {
                convertView.setBackgroundColor(getBackgroundColor(context, item));
            }

            ((TextView) convertView).setText(((TableTextItem) item).getText());

            return convertView;
        } else if (item instanceof TableElementItem) {
            return getView((TableElementItem) item, convertView, parent);
        }

        return null;
    }

    public View getView(TableElementItem item, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        initialize(context);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.table_item,
                    parent, false);
        }

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        if (viewHolder == null) {
            viewHolder = new ViewHolder();

            viewHolder.symbol = convertView.findViewById(R.id.element_symbol_found);
            viewHolder.symbol.setTypeface(mTypeface);
            viewHolder.number = convertView.findViewById(R.id.element_number_found);
            viewHolder.number.setTypeface(mTypeface);
            viewHolder.name = convertView.findViewById(R.id.element_name_found);
            viewHolder.name.setTypeface(mTypeface);
            viewHolder.weight = convertView.findViewById(R.id.element_weight_found);
            viewHolder.weight.setTypeface(mTypeface);

            convertView.setTag(viewHolder);
        }

        String atomicWeight = item.getStandardAtomicWeight();

        try {
            BigDecimal bigDecimal = new BigDecimal(atomicWeight);
            atomicWeight = bigDecimal.setScale(3, RoundingMode.HALF_UP).toString();
        } catch (NumberFormatException ignored) {
        }

        if (seenElements.contains(item.getNumber())) {
            convertView.setBackgroundColor(getBackgroundColor(context, item));
        } else {
            convertView.setBackgroundColor(Color.GRAY);
        }

        viewHolder.symbol.setText(item.getSymbol());
        viewHolder.number.setText(String.valueOf(item.getNumber()));
        viewHolder.name.setTextSize(12f);
        viewHolder.name.setText(item.getName());
        viewHolder.weight.setText(atomicWeight);

        return convertView;
    }

    @Override
    public View getActiveView(Bitmap bitmap, View convertView, ViewGroup parent) {
        initialize(parent.getContext());

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.table_active_item, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.bitmap);
        imageView.setImageBitmap(bitmap);

        return convertView;
    }

    private int getBackgroundColor(Context context, TableItem item) {
        return context.getResources().getColor(COLORS[item.getCategory()]);
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof TableTextItem) {
            return ViewType.TEXT.ordinal();
        }

        return ViewType.ITEM.ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.values().length;
    }

    public void setSeenElements(Set<Integer> seenElements) {
        this.seenElements = seenElements;
    }

    public void setItems(Context context, List<TableElementItem> items, Set<Integer> seenElements) {
        mItems.clear();
        this.seenElements = seenElements;

        if (items == null) {
            return;
        }

        int groups = 0, periods = 0;

        for (TableElementItem item : items) {
            groups = Math.max(item.getGroup(), groups);
            periods = Math.max(item.getPeriod(), periods);
        }

        mGroupsCount = groups;
        mPeriodsCount = periods + 2;

        for (TableElementItem item : items) {
            final int position;

            if (item.getNumber() >= 57 && item.getNumber() <= 71) {
                position = (mGroupsCount * periods) + 2 + item.getNumber() - 57;
            } else if (item.getNumber() >= 89 && item.getNumber() <= 103) {
                position = (mGroupsCount * periods) + 20 + item.getNumber() - 89;
            } else {
                position = ((item.getPeriod() - 1) * mGroupsCount) + item.getGroup() - 1;
            }

            mItems.put(position, item);
        }

        for (Object[] item : TEXT_ITEMS) {
            final String text;

            if (item[1] instanceof Integer) {
                text = context.getString((int) item[1]);
            } else {
                text = (String) item[1];
            }

            mItems.put((int) item[0], new TableTextItem(text, (int) item[2]));
        }
    }

    @Override
    public int getGroupsCount() {
        return mGroupsCount;
    }

    @Override
    public int getPeriodsCount() {
        return mPeriodsCount;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position) instanceof TableElementItem;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mGroupsCount);
        dest.writeInt(mPeriodsCount);

        dest.writeInt(seenElements.size());
        for (int el : seenElements) {
            dest.writeInt(el);
        }

        dest.writeInt(mItems.size());
        for (Map.Entry<Integer, TableItem> item : mItems.entrySet()) {
            dest.writeInt(item.getKey());
            dest.writeParcelable((Parcelable) item.getValue(), 0);
        }
    }

    private void initialize(Context context) {
        if (mTypeface == null) {
            mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");
        }
    }

    private enum ViewType {
        ITEM,
        TEXT
    }

    private class ViewHolder {
        TextView symbol, number, name, weight;
    }
}
