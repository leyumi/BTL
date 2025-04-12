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

public class ThongKeAdapter extends RecyclerView.Adapter<ThongKeAdapter.ThongKeViewHolder> {
    private List<ThongKeModel> list;

    public ThongKeAdapter(List<ThongKeModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ThongKeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thongke, parent, false);
        return new ThongKeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThongKeViewHolder holder, int position) {
        ThongKeModel item = list.get(position);
        holder.txtCategory.setText(item.getCategory());

        // Xác định dấu và màu sắc
        boolean isExpense = item.getAmount() < 0;
        String sign = isExpense ? "-" : "+";
        int color = isExpense ? Color.RED : Color.parseColor("#FF4CAF50"); // Xanh lá

        // Định dạng số tiền
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        String amountStr = format.format(Math.abs(item.getAmount()));

        holder.txtAmount.setText(sign + " " + amountStr);
        holder.txtAmount.setTextColor(color);
    }

    // Phương thức định dạng số tiền
    private String formatAmount(int amount) {
        // Sử dụng NumberFormat để định dạng số
        String amountStr = NumberFormat.getNumberInstance(Locale.US).format(Math.abs(amount));

        // Thêm dấu + hoặc -
        return (amount >= 0 ? "+ " : "- ") + amountStr;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ThongKeViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategory, txtAmount;

        public ThongKeViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtAmount = itemView.findViewById(R.id.txtAmount);
        }
    }
}