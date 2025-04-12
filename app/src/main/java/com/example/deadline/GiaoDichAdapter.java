package com.example.deadline;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class GiaoDichAdapter extends RecyclerView.Adapter<GiaoDichAdapter.ViewHolder> {

    private final List<GiaoDich> giaoDichList;
    private final OnItemActionListener listener;
    private final Context context;  // Thêm context

    public GiaoDichAdapter(Context context, List<GiaoDich> giaoDichList, OnItemActionListener listener) {
        this.context = context;
        this.giaoDichList = giaoDichList;
        this.listener = listener;
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

        // Tiêu đề giao dịch
        String displayName = giaoDich.getName() != null && !giaoDich.getName().isEmpty()
                ? giaoDich.getName()
                : "Không có tên";
        holder.tvTitle.setText(displayName);

        // Định dạng số tiền
        String amountFormatted = NumberFormat.getNumberInstance(Locale.US)
                .format(Math.abs(giaoDich.getAmount())) + " VND";
        boolean isExpense = "expense".equals(giaoDich.getType());
        holder.tvAmount.setText((isExpense ? "- " : "+ ") + amountFormatted);
        holder.tvAmount.setTextColor(isExpense ? Color.parseColor("#6FA3EF") : Color.parseColor("#43A047"));

        // Hiển thị ngày
        holder.tvDate.setText(giaoDich.getDate());

        // Hiển thị phân loại (thu nhập / chi tiêu)
        String typeText = "income".equals(giaoDich.getType()) ? "Thu nhập" : "Chi tiêu";
        holder.tvCategory.setText(typeText);

        // Hiển thị icon nếu có
        String iconResourceName = giaoDich.getIcon();
        int iconResId = holder.itemView.getContext().getResources().getIdentifier(
                iconResourceName, "drawable", holder.itemView.getContext().getPackageName());

        if (iconResId != 0) {
            holder.imgIcon.setImageResource(iconResId);
        }

        // Menu chỉnh sửa/xoá
        holder.imgMenu.setOnClickListener(v -> showPopupMenu(v, holder.getAdapterPosition()));
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.menu_giaodich, popup.getMenu());

        GiaoDich giaoDich = giaoDichList.get(position); // ✅ Lấy object theo vị trí

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit) {
                listener.onEdit(giaoDich);  // ✅ Truyền object
                return true;
            } else if (id == R.id.action_delete) {
                listener.onDelete(giaoDich);  // ✅ Truyền object
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return giaoDichList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvAmount, tvCategory;
        ImageView imgMenu, imgIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            imgMenu = itemView.findViewById(R.id.imgMenu);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }

    // Interface listener
    public interface OnItemActionListener {
        void onEdit(GiaoDich giaoDich);  // Truyền object GiaoDich
        void onDelete(GiaoDich giaoDich);  // Truyền object GiaoDich
    }
}
