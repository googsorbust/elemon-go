package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private ArrayList<PageInfo> mPages = new ArrayList<PageInfo>();

    public ViewPagerAdapter(AppCompatActivity activity) {
        super(activity.getSupportFragmentManager());

        mContext = activity;
    }

    public void addPage(int title, Class<?> cls, Bundle args) {
        addPage(mContext.getString(title), cls, args);
    }

    public void addPage(String title, Class<?> cls, Bundle args) {
        mPages.add(new PageInfo(title, cls, args));

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPages.size();
    }

    @Override
    public Fragment getItem(int position) {
        PageInfo info = mPages.get(position);

        return Fragment.instantiate(mContext, info.cls.getName(), info.args);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mPages.get(position).title;
    }

    static final class PageInfo {
        private final String title;
        private final Class<?> cls;
        private final Bundle args;

        PageInfo(String _title, Class<?> _class, Bundle _args) {
            title = _title;
            cls = _class;
            args = _args;
        }
    }
}
