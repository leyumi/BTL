package com.example.deadline;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TransactionTypeAdapter extends RecyclerView.Adapter<TransactionTypeAdapter.ViewHolder> {

    private final List<TransactionType> transactionTypeList;
    private final OnItemSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnItemSelectedListener {
        void onItemSelected(TransactionType selectedItem);
    }

    public TransactionTypeAdapter(List<TransactionType> transactionTypeList, OnItemSelectedListener listener) {
        this.transactionTypeList = transactionTypeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_type, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionType type = transactionTypeList.get(position);
        holder.name.setText(type.getName());

        // Lấy icon từ drawable theo tên
        Context context = holder.itemView.getContext();
        int iconResId = context.getResources().getIdentifier(type.getIc_name(), "drawable", context.getPackageName());

        if (iconResId != 0) {
            holder.icon.setImageResource(iconResId);
        }

        holder.radioButton.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
            listener.onItemSelected(type);
        });
    }

    @Override
    public int getItemCount() {
        return transactionTypeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        RadioButton radioButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            radioButton = itemView.findViewById(R.id.radioButton);
        }
    }
}
