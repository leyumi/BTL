package com.example.deadline;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class GiaoDichAdapter extends RecyclerView.Adapter<GiaoDichAdapter.ViewHolder> {

    private final List<GiaoDich> giaoDichList;

    public GiaoDichAdapter(List<GiaoDich> giaoDichList) {
        this.giaoDichList = giaoDichList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_giaodich, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GiaoDich giaoDich = giaoDichList.get(position);

        // Tránh null gây lỗi
        String title = giaoDich.getTitle() != null ? giaoDich.getTitle() : "Không rõ";
        String date = giaoDich.getDate() != null ? giaoDich.getDate() : "--/--/----";

        int amount = giaoDich.getAmount();

        holder.tvTitle.setText(title);
        holder.tvDate.setText(date);

        // Định dạng tiền tệ có dấu phẩy
        String amountFormatted = NumberFormat.getNumberInstance(Locale.US).format(Math.abs(amount)) + " VND";
        holder.tvAmount.setText((amount < 0 ? "- " : "+ ") + amountFormatted);
        holder.tvAmount.setTextColor(amount < 0 ? Color.RED : Color.parseColor("#43A047"));
    }

    @Override
    public int getItemCount() {
        return giaoDichList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
