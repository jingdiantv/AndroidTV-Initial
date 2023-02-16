package com.zwn.launcher.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.ListRowView;
import androidx.leanback.widget.RowHeaderPresenter;
import androidx.leanback.widget.RowPresenter;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.base.model.LoadState;
import com.zeewain.base.utils.DisplayUtil;
import com.zwn.launcher.MainActivity;
import com.zwn.launcher.MainViewModel;
import com.zwn.launcher.R;
import com.zwn.launcher.config.ProdConstants;
import com.zwn.launcher.data.model.ProductListMo;
import com.zwn.launcher.ui.detail.DetailActivity;
import com.zwn.user.data.config.UserCenterConf;
import com.zwn.user.ui.UserCenterActivity;

import java.util.List;


public class ModuleListPagedPresenter extends BaseListRowPresenter {
    private final LifecycleOwner lifecycleOwner;
    private final String categoryId;
    private final String moduleType;
    private MainViewModel viewModel;

    public ModuleListPagedPresenter(LifecycleOwner lifecycleOwner, String categoryId, String moduleType){
        this.lifecycleOwner = lifecycleOwner;
        this.categoryId = categoryId;
        this.moduleType = moduleType;
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void initializeRowViewHolder(RowPresenter.ViewHolder holder) {
        super.initializeRowViewHolder(holder);
        ViewHolder rowViewHolder = (ViewHolder) holder;
        rowViewHolder.getGridView().setHorizontalSpacing(DisplayUtil.dip2px(rowViewHolder.getGridView().getContext(), 18));
        rowViewHolder.getGridView().setFocusScrollStrategy(HorizontalGridView.FOCUS_SCROLL_ALIGNED);
        MainActivity attachedFragmentActivity = (MainActivity) rowViewHolder.getGridView().getContext();
        viewModel = new ViewModelProvider(attachedFragmentActivity).get(MainViewModel.class);

        rowViewHolder.setOnKeyListener((view, keyCode, event) -> {
            if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_UP){
                HorizontalGridView horizontalGridView = ((ListRowView)view).getGridView();
                if(horizontalGridView.getSelectedPosition() > 0) {
                    RecyclerView.LayoutManager gridViewLayoutManager = horizontalGridView.getLayoutManager();
                    if (gridViewLayoutManager != null) {
                        int itemCount = gridViewLayoutManager.getItemCount();
                        if(itemCount < viewModel.getCareModuleTotalRecordSize(moduleType)) {
                            if (horizontalGridView.getSelectedPosition() >= itemCount - 3) {
                                viewModel.reqCareModuleListPaged(categoryId, moduleType);
                            }
                        }
                    }
                }
            } else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN){
                HorizontalGridView horizontalGridView = ((ListRowView)view).getGridView();
                RecyclerView.LayoutManager gridViewLayoutManager = horizontalGridView.getLayoutManager();
                if(gridViewLayoutManager != null && horizontalGridView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE){
                    int itemCount = gridViewLayoutManager.getItemCount();
                    if(horizontalGridView.getSelectedPosition() == (itemCount -1) && itemCount == viewModel.getCareModuleTotalRecordSize(moduleType)){
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
                if(ProdConstants.SKU_ID_LOADED_ERR.equals(record.getSkuId())){
                    viewModel.reqCareModuleListPaged(categoryId, moduleType);
                }else if(!ProdConstants.SKU_ID_LOADING.equals(record.getSkuId())){
                    Intent intent;
                    if (ProdConstants.LATEST_INTERACT_ITEM.equals(record.getSpuId())) {
                        intent = new Intent(holder.view.getContext(), UserCenterActivity.class);
                        intent.putExtra(UserCenterConf.INTENT_KEY_USER_CENTER_FUNC, UserCenterConf.FUNC_USER_HISTORY);
                    } else {
                        intent = new Intent(context, DetailActivity.class);
                        intent.putExtra("skuId", record.getSkuId());
                    }
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    protected RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        ViewHolder listRowPresenterViewHolder =
                (ViewHolder) super.createRowViewHolder(parent);

        return new PagedListRowPresenterViewHolder(
                listRowPresenterViewHolder.view,
                listRowPresenterViewHolder.getGridView(),
                listRowPresenterViewHolder.getListRowPresenter());
    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);

        ListRow listRow = (ListRow) item;
        final ArrayObjectAdapter adapter = (ArrayObjectAdapter) listRow.getAdapter();

        final PagedListRowPresenterViewHolder pagedListRowPresenterViewHolder = (PagedListRowPresenterViewHolder) holder;
        if(pagedListRowPresenterViewHolder.getAdapter() == null){
            pagedListRowPresenterViewHolder.setAdapter(adapter);

            viewModel.mldCareModuleListPagedLoadState.observe(lifecycleOwner, pagedListLoadState -> {
                if(moduleType.equals(pagedListLoadState.type)) {
                    if(pagedListLoadState.pageNum > 1){
                        if(pagedListLoadState.loadState == LoadState.Loading){
                            String skuId = ((ProductListMo.Record)adapter.get(adapter.size() -1)).getSkuId();
                            if(ProdConstants.SKU_ID_LOADED_ERR.equals(skuId)){
                                adapter.removeItems(adapter.size() -1, 1);
                            }
                            adapter.add(getCustomItem(ProdConstants.SKU_ID_LOADING));
                            adapter.notifyItemRangeChanged(adapter.size() -1, 1);
                        }else if(pagedListLoadState.loadState == LoadState.Success) {
                            String skuId = ((ProductListMo.Record)adapter.get(adapter.size() -1)).getSkuId();
                            if(ProdConstants.SKU_ID_LOADING.equals(skuId) || ProdConstants.SKU_ID_LOADED_ERR.equals(skuId)){
                                adapter.removeItems(adapter.size() -1, 1);
                            }
                            List<ProductListMo.Record> recordList = viewModel.getCareModuleListFromCache(moduleType).getRecords();
                            int size = adapter.size();
                            int addSize = recordList.size() - size;
                            adapter.addAll(size, recordList.subList(size, recordList.size()));
                            adapter.notifyItemRangeChanged(size, addSize);

                        }else{
                            String skuId = ((ProductListMo.Record)adapter.get(adapter.size() -1)).getSkuId();
                            if(ProdConstants.SKU_ID_LOADING.equals(skuId)){
                                adapter.removeItems(adapter.size() -1, 1);
                            }
                            adapter.add(getCustomItem(ProdConstants.SKU_ID_LOADED_ERR));
                            adapter.notifyItemRangeChanged(adapter.size() -1, 1);
                        }
                    }
                }
            });
        }
    }

    private ProductListMo.Record getCustomItem(String skuId){
        ProductListMo.Record record = new ProductListMo.Record();
        record.setSkuId(skuId);
        record.setProductTitle("");
        return record;
    }

    private static class PagedListRowPresenterViewHolder extends ViewHolder {
        private ArrayObjectAdapter adapter;

        PagedListRowPresenterViewHolder(View rootView, HorizontalGridView gridView, ListRowPresenter listRowPresenter) {
            super(rootView, gridView, listRowPresenter);
        }

        public ArrayObjectAdapter getAdapter() {
            return adapter;
        }

        public void setAdapter(ArrayObjectAdapter adapter) {
            this.adapter = adapter;
        }
    }
}
