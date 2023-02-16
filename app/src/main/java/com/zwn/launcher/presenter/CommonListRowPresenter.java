package com.zwn.launcher.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.ListRowView;
import androidx.leanback.widget.RowHeaderPresenter;
import androidx.leanback.widget.RowPresenter;
import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.base.utils.DisplayUtil;
import com.zwn.launcher.ui.detail.DetailActivity;
import com.zwn.launcher.R;
import com.zwn.launcher.config.ProdConstants;
import com.zwn.launcher.data.model.ProductListMo;
import com.zwn.user.data.config.UserCenterConf;
import com.zwn.user.ui.UserCenterActivity;


public class CommonListRowPresenter extends BaseListRowPresenter {
    @SuppressLint("RestrictedApi")
    @Override
    protected void initializeRowViewHolder(RowPresenter.ViewHolder holder) {
        super.initializeRowViewHolder(holder);
        final ViewHolder rowViewHolder = (ViewHolder) holder;

        rowViewHolder.getGridView().setHorizontalSpacing(DisplayUtil.dip2px(rowViewHolder.getGridView().getContext(), 18));
        rowViewHolder.getGridView().setFocusScrollStrategy(HorizontalGridView.FOCUS_SCROLL_ITEM);

        rowViewHolder.setOnKeyListener((view, keyCode, event) -> {
            if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN){
                HorizontalGridView horizontalGridView = ((ListRowView)view).getGridView();
                RecyclerView.LayoutManager gridViewLayoutManager = horizontalGridView.getLayoutManager();
                if(gridViewLayoutManager != null && horizontalGridView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE){
                    int itemCount = gridViewLayoutManager.getItemCount();
                    if(horizontalGridView.getSelectedPosition() == (itemCount -1)){
                        shakeX(gridViewLayoutManager.findViewByPosition((itemCount -1)));
                    }
                }
            } else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN){
                HorizontalGridView horizontalGridView = ((ListRowView)view).getGridView();
                RecyclerView.LayoutManager gridViewLayoutManager = horizontalGridView.getLayoutManager();
                if(gridViewLayoutManager != null && horizontalGridView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE){
                    if(horizontalGridView.getSelectedPosition() == 0){
                        shakeX(gridViewLayoutManager.findViewByPosition(0));
                    }
                }
            }
            return false;
        });

        RowHeaderPresenter.ViewHolder vh = rowViewHolder.getHeaderViewHolder();
        final TextView textView = vh.view.findViewById(R.id.row_header);

        textView.setTextSize(22);
        textView.setTextColor(textView.getContext().getResources().getColor(R.color.colorWhite));
        textView.setPadding(0, 0, 0, DisplayUtil.dip2px(textView.getContext(), 12));
        setOnItemViewClickedListener((itemViewHolder, item, rowViewHolder1, row) -> {
            Context context = ((ViewHolder) rowViewHolder1).getGridView().getContext();
            if (item instanceof ProductListMo.Record) {
                ProductListMo.Record record = (ProductListMo.Record) item;
                Intent intent;
                if(ProdConstants.LATEST_INTERACT_ITEM.equals(record.getSpuId())){
                    intent = new Intent(holder.view.getContext(), UserCenterActivity.class);
                    intent.putExtra(UserCenterConf.INTENT_KEY_USER_CENTER_FUNC, UserCenterConf.FUNC_USER_HISTORY);
                }else {
                    intent = new Intent(context, DetailActivity.class);
                    intent.putExtra("skuId",record.getSkuId());
                }
                context.startActivity(intent);
            }
        });
    }
}
