package com.zwn.launcher.presenter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.FocusHighlightHelper;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.Presenter;

import com.zwn.launcher.R;

public class MineHeaderCustomRowPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mine_header_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof ListRow) {
            ListRow listRow = (ListRow) item;
            ViewHolder vh = (ViewHolder) viewHolder;
            vh.itemBridgeAdapter = new ItemBridgeAdapter(listRow.getAdapter());
            vh.horizontalGridView.setAdapter(vh.itemBridgeAdapter);
            FocusHighlightHelper.setupBrowseItemFocusHighlight(vh.itemBridgeAdapter,
                    FocusHighlight.ZOOM_FACTOR_MEDIUM, false);
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    private static class ViewHolder extends Presenter.ViewHolder {
        public View interactionsView;
        public View practiceDaysView;
        public View interactionTimeView;
        public View calorieView;

        public ItemBridgeAdapter itemBridgeAdapter;
        public HorizontalGridView horizontalGridView;

        @SuppressLint("SetTextI18n")
        public ViewHolder(View view) {
            super(view);
            interactionsView = view.findViewById(R.id.include_mine_header_interactions);
            practiceDaysView = view.findViewById(R.id.include_mine_header_practice_days);
            interactionTimeView = view.findViewById(R.id.include_mine_header_interaction_time);
            calorieView = view.findViewById(R.id.include_mine_header_calorie);

            interactionsView.setBackgroundResource(R.mipmap.icon_interaction);
            practiceDaysView.setBackgroundResource(R.mipmap.icon_practice);
            interactionTimeView.setBackgroundResource(R.mipmap.icon_duration);
            calorieView.setBackgroundResource(R.mipmap.icon_calorie);

            ((TextView) interactionsView.findViewById(R.id.tv_mine_data_item_num)).setText("333");
            ((TextView) interactionsView.findViewById(R.id.tv_mine_data_item_unit)).setText("次");

            ((TextView) practiceDaysView.findViewById(R.id.tv_mine_data_item_num)).setText("22");
            ((TextView) practiceDaysView.findViewById(R.id.tv_mine_data_item_unit)).setText("天");

            ((TextView) interactionTimeView.findViewById(R.id.tv_mine_data_item_num)).setText("999");
            ((TextView) interactionTimeView.findViewById(R.id.tv_mine_data_item_unit)).setText("分钟");

            ((TextView) calorieView.findViewById(R.id.tv_mine_data_item_num)).setText("555");
            ((TextView) calorieView.findViewById(R.id.tv_mine_data_item_unit)).setText("千卡");

            horizontalGridView = view.findViewById(R.id.hgv_mine_header_acct);
        }
    }
}
