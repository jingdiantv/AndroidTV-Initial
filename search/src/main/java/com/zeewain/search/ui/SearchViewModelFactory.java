package com.zeewain.search.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.zeewain.search.data.SearchRepository;

public class SearchViewModelFactory implements ViewModelProvider.Factory {

    private final SearchRepository searchRepository;

    public SearchViewModelFactory(SearchRepository dataRepository) {
        this.searchRepository = dataRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SearchViewModel(searchRepository);
    }
}
