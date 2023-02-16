package com.zwn.launcher.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.zwn.launcher.data.model.MainCategoryMo;
import com.zwn.launcher.ui.home.CareListFragment;
import com.zwn.launcher.ui.home.ProductListFragment;
import com.zwn.launcher.ui.mine.MineFragment;

import java.util.List;

public class ProductCategoryViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<MainCategoryMo> dataList;
    private Fragment instantFragment;

    public ProductCategoryViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position == 0){
            return MineFragment.newInstance();
        }else if(position == 1){
            return CareListFragment.newInstance(dataList.get(position).getCategoryId(), position);
        }
        return ProductListFragment.newInstance(dataList.get(position).getCategoryId(), position);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        instantFragment = (Fragment) object;
        super.setPrimaryItem(container, position, object);
    }

    public Fragment getInstantFragment(){
        return instantFragment;
    }

    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    public void setDataList(List<MainCategoryMo> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }
}
