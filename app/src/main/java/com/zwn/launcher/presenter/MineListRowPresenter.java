package com.zwn.launcher.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.widget.TextView;

import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.RowHeaderPresenter;
import androidx.leanback.widget.RowPresenter;

import com.zeewain.base.model.MineCommonMo;
import com.zwn.launcher.R;
import com.zwn.launcher.config.ProdConstants;
import com.zwn.launcher.ui.detail.DetailActivity;
import com.zwn.launcher.ui.upgrade.VersionUpdateActivity;
import com.zwn.user.data.config.UserCenterConf;
import com.zwn.user.ui.UserCenterActivity;

public class MineListRowPresenter extends BaseListRowPresenter {
    private final Resources mResources;

    public MineListRowPresenter(Resources resources) {
        mResources = resources;
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void initializeRowViewHolder(RowPresenter.ViewHolder holder) {
        super.initializeRowViewHolder(holder);

        final ViewHolder listRowViewHolder = (ViewHolder) holder;
        listRowViewHolder.getGridView().setHorizontalSpacing(mResources.getDimensionPixelSize(R.dimen.src_dp_8));
        listRowViewHolder.getGridView().setFocusScrollStrategy(HorizontalGridView.FOCUS_SCROLL_ITEM);
        listRowViewHolder.getGridView().setPadding(mResources.getDimensionPixelSize(R.dimen.src_dp_50),
                0, 0, mResources.getDimensionPixelSize(R.dimen.src_dp_25));

        RowHeaderPresenter.ViewHolder vh = listRowViewHolder.getHeaderViewHolder();
        TextView textView = vh.view.findViewById(R.id.row_header);
        textView.setTextSize(mResources.getDimension(R.dimen.src_font_20) / mResources.getDisplayMetrics().scaledDensity);
        textView.setTextColor(textView.getContext().getResources().getColor(R.color.white));
        textView.setPadding(mResources.getDimensionPixelSize(R.dimen.src_dp_55), 0, 0,
                mResources.getDimensionPixelSize(R.dimen.src_dp_9));

        setOnItemViewClickedListener((itemViewHolder, item, rowViewHolder, row) -> {
            Context context = ((ViewHolder) rowViewHolder).getGridView().getContext();
            if (item instanceof MineCommonMo) {
                MineCommonMo model = (MineCommonMo) item;
                Intent intent;
                switch (model.type) {
                    case ProdConstants.INTERACTIVE_RECORD:
                        intent = new Intent(context, UserCenterActivity.class);
                        intent.putExtra(UserCenterConf.INTENT_KEY_USER_CENTER_FUNC, UserCenterConf.FUNC_USER_HISTORY);
                        break;
                    case ProdConstants.MY_FAVORITES:
                        intent = new Intent(context, UserCenterActivity.class);
                        intent.putExtra(UserCenterConf.INTENT_KEY_USER_CENTER_FUNC, UserCenterConf.FUNC_USER_COLLECT);
                        break;
                    case ProdConstants.MY_DOWNLOADS:
//                        intent = new Intent(context, MyDownloadActivity.class);
                        intent = new Intent(context, UserCenterActivity.class);
                        intent.putExtra(UserCenterConf.INTENT_KEY_USER_CENTER_FUNC, UserCenterConf.FUNC_USER_DOWNLOAD);
                        break;
                    case ProdConstants.VERSION_UPDATE:
                        intent = new Intent(context, VersionUpdateActivity.class);
                        break;
                    case ProdConstants.ABOUT_US:
                        intent = new Intent(context, UserCenterActivity.class);
                        intent.putExtra(UserCenterConf.INTENT_KEY_USER_CENTER_FUNC, UserCenterConf.FRAGMENT_ABOUT_US);
                        break;
                    default:
                        intent = new Intent(context, DetailActivity.class);
                        intent.putExtra("skuId", model.skuId);
                        break;
                }
                context.startActivity(intent);
            }
        });
    }
}
