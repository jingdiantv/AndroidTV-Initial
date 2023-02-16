package com.zwn.launcher.presenter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.leanback.widget.Presenter;

import com.zwn.launcher.R;
import com.zwn.launcher.widgets.TabVerticalGridView;
import com.zeewain.search.ui.SearchActivity;


public class TypeFooterPresenter extends Presenter {
    private Context mContext;

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_type_footer_layout, parent, false);
        view.findViewById(R.id.cl_type_footer_back)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v.getParent().getParent() instanceof TabVerticalGridView) {
                            ((TabVerticalGridView) v.getParent().getParent()).backToTop();
                        }
                    }
                });
        view.findViewById(R.id.cl_type_footer_search).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       // Toast.makeText(v.getContext(), R.string.not_support_now, Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(v.getContext(), SearchActivity.class);
                        v.getContext().startActivity(intent);
                    }
                });
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {

    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    public static class ViewHolder extends Presenter.ViewHolder {

        private final ConstraintLayout mBackToTopView;
        private final ConstraintLayout mSearchView;

        public ViewHolder(View view) {
            super(view);
            mBackToTopView = view.findViewById(R.id.cl_type_footer_back);
            mSearchView = view.findViewById(R.id.cl_type_footer_search);
        }
    }
}
