package com.zwn.launcher.presenter;

import androidx.leanback.widget.ListRow;

import com.zwn.launcher.data.model.Footer;

public class ContentPresenterSelector extends BasePresenterSelector {
    public ContentPresenterSelector() {
        CommonListRowPresenter commonListRowPresenter = new CommonListRowPresenter();
        commonListRowPresenter.setShadowEnabled(false);
        commonListRowPresenter.setSelectEffectEnabled(false);
        commonListRowPresenter.setKeepChildForeground(false);

        addClassPresenter(ListRow.class, commonListRowPresenter, TypeMainMasterPresenter.class);
        addClassPresenter(ListRow.class, commonListRowPresenter, TypeMainSlavePresenter.class);
        addClassPresenter(ListRow.class, commonListRowPresenter, TypeRecommendPresenter.class);

        addClassPresenter(Footer.class, new TypeFooterPresenter());
    }

}
