package com.zwn.launcher.presenter;

import android.content.res.Resources;

import androidx.leanback.widget.ListRow;

public class MinePresenterSelector extends BasePresenterSelector {
    public MinePresenterSelector(Resources resources) {
        MineHeaderCustomRowPresenter mineHeaderCustomRowPresenter = new MineHeaderCustomRowPresenter();
        addClassPresenter(ListRow.class, mineHeaderCustomRowPresenter, MineHeaderPresenter.class);

        MineListRowPresenter mineListRowPresenter = new MineListRowPresenter(resources);

        mineListRowPresenter.setShadowEnabled(false);
        mineListRowPresenter.setSelectEffectEnabled(false);
        mineListRowPresenter.setKeepChildForeground(false);

        addClassPresenter(ListRow.class, mineListRowPresenter, MineHistoryPresenter.class);
        addClassPresenter(ListRow.class, mineListRowPresenter, MineFavoritesPresenter.class);
        addClassPresenter(ListRow.class, mineListRowPresenter, MineDownloadsPresenter.class);

        addClassPresenter(ListRow.class, mineListRowPresenter, MineFooterPresenter.class);
    }
}
