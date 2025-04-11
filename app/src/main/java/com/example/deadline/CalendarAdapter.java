package com.example.deadline;

import android.content.Context;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarAdapter extends BaseAdapter {

    private final Context context;
    private final List<String> dateList;
    private final String currentDay;

    public CalendarAdapter(Context context, List<String> dateList) {
        this.context = context;
        this.dateList = dateList;
        this.currentDay = new SimpleDateFormat("dd", Locale.getDefault()).format(new Date());
    }

    @Override
    public int getCount() {
        return dateList.size();
    }

    @Override
    public Object getItem(int position) {
        return dateList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String day = dateList.get(position);
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        }

        TextView tvDay = view.findViewById(R.id.tvDay);
        tvDay.setText(day);

        // Nếu là tiêu đề thứ (Sun, Mon, ...)
        if (position < 7) {
            tvDay.setTextColor(0xFF000000); // Đen
            tvDay.setBackgroundColor(0xFFE0E0E0); // Xám nhạt
        } else if (day.isEmpty()) {
            tvDay.setBackgroundColor(0x00000000); // Transparent
        } else if (day.equals(currentDay)) {
            tvDay.setBackgroundColor(0xFFE91E63); // Hồng cho ngày hiện tại
            tvDay.setTextColor(0xFFFFFFFF); // Trắng
        } else {
            tvDay.setBackgroundColor(0xFFFFFFFF); // Trắng
            tvDay.setTextColor(0xFF000000); // Đen
        }

        return view;
    }
}
