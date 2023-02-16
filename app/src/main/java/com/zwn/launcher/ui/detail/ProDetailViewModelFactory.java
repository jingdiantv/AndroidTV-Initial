package com.zwn.launcher.ui.detail;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.zwn.launcher.data.DataRepository;

public class ProDetailViewModelFactory implements ViewModelProvider.Factory {

    private final DataRepository dataRepository;

    public ProDetailViewModelFactory(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ProDetailViewModel(dataRepository);
    }
}
