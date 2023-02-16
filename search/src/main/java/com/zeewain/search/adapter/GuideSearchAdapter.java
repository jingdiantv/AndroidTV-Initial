package com.zeewain.search.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.search.R;
import com.zeewain.search.data.model.CourseInfo;

import java.util.List;


public class GuideSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CourseInfo> courseInfoList;

    public GuideSearchAdapter(List<CourseInfo> courseInfoList) {
        this.courseInfoList = courseInfoList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.guide_search_item, parent, false);
        //创建ViewHolder实例，参数为刚加载进来的子项布局
        GuideRecordHolder viewHolder = new GuideRecordHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        GuideRecordHolder guideRecordHolder = (GuideRecordHolder) holder;
        guideRecordHolder.tvCoursewareName.setText(courseInfoList.get(position).getName());
        guideRecordHolder.tvCoursewareName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
               // controlFocus(v, hasFocus);
                if (hasFocus){
                    if (itemFocusListener!=null){
                        itemFocusListener.onItemFocusSection(position, courseInfoList.get(position).getName());
                    }
                }
            }
        });
        guideRecordHolder.tvCoursewareName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener!=null){
                    listener.onItemClickSection(position,courseInfoList.get(position).getName());
                }
            }
        });
    }

    private void controlFocus(View v, boolean hasFocus) {
        if (hasFocus) {
            ViewCompat.animate(v).setDuration(200).scaleX(1.1f).scaleY(1.1f).start();
        } else {
            ViewCompat.animate(v).setDuration(200).scaleX(1f).scaleY(1f).start();
        }
    }

    @Override
    public int getItemCount() {
        return courseInfoList.size();
    }

    public class GuideRecordHolder extends RecyclerView.ViewHolder {
        TextView tvCoursewareName;

        public GuideRecordHolder(@NonNull View itemView) {
            super(itemView);
            tvCoursewareName = itemView.findViewById(R.id.tv_courseware_name);

        }
    }

    private GuideSearchAdapter.OnItemFocusListener itemFocusListener;

    public void setItemFocusListener(GuideSearchAdapter.OnItemFocusListener itemFocusListener) {
        this.itemFocusListener = itemFocusListener;
    }

    public interface OnItemFocusListener {
        void onItemFocusSection(int position, String data);

    }

    private GuideSearchAdapter.OnItemClickListener listener;

    public void setListener(GuideSearchAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClickSection(int position, String name);

    }
}
