package com.frozendevs.periodictable.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.frozendevs.periodictable.activity.PropertiesActivity;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.view.RecyclerView;
import com.frozendevs.periodictable.widget.DividerDecoration;
import org.jetbrains.annotations.NotNull;
import org.tensorflow.lite.examples.detection.R;

//import com.frozendevs.periodictable.model.adapter.IsotopesAdapter;
//import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
//import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
//import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
//import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

public class IsotopesFragment extends Fragment {
    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER =
            "RecyclerViewExpandableItemManager";

    private RecyclerView.Adapter mWrappedAdapter;
//    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.properties_fragment, container, false);

        final RecyclerView recyclerView = layout.findViewById(R.id.properties_list);

        final Parcelable savedState = (savedInstanceState != null) ?
                savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
//        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(savedState);

        ElementProperties properties = (ElementProperties) getArguments().get(
                PropertiesActivity.ARGUMENT_PROPERTIES);

//        IsotopesAdapter adapter = new IsotopesAdapter(getActivity(), properties.getIsotopes());

//        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(adapter);

//        GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
//        animator.setSupportsChangeAnimations(false);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mWrappedAdapter);
        recyclerView.addItemDecoration(new DividerDecoration(getActivity()));
//        recyclerView.setItemAnimator(animator);

//        mRecyclerViewExpandableItemManager.attachRecyclerView(recyclerView);

        getActivity().registerForContextMenu(recyclerView);

        return layout;
    }

    @Override
    public void onDestroyView() {
//        if (mRecyclerViewExpandableItemManager != null) {
//            mRecyclerViewExpandableItemManager.release();
//        }
//
//        if (mWrappedAdapter != null) {
//            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
//        }

        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);

//        if (mRecyclerViewExpandableItemManager != null) {
//            outState.putParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER,
//                    mRecyclerViewExpandableItemManager.getSavedState());
//        }
    }
}
