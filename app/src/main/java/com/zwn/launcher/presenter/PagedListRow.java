package com.zwn.launcher.presenter;

import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ObjectAdapter;

public class PagedListRow extends ListRow {
    public PagedListRow(HeaderItem header, ObjectAdapter adapter) {
        super(header, adapter);
    }
}
