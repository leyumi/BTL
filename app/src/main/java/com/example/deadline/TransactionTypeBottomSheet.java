package com.example.deadline;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class TransactionTypeBottomSheet extends BottomSheetDialogFragment {

    private Button btnExpense, btnIncome;
    private RecyclerView recyclerView;
    private TransactionTypeAdapter adapter;
    private ArrayList<TransactionType> transactionTypeList = new ArrayList<>();
    private String selectedCategory = "expense";
    private OnTypeSelectedListener listener;

    private String uid = "";

    public void setUid(String uid) {
        this.uid = uid;
    }

    // ✅ Interface mới: thêm iconName
    public interface OnTypeSelectedListener {
        void onTypeSelected(String name, String type, String iconName);
    }

    public void setOnTypeSelectedListener(OnTypeSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_transaction_type, container, false);

        btnExpense = view.findViewById(R.id.btnExpense);
        btnIncome = view.findViewById(R.id.btnIncome);
        recyclerView = view.findViewById(R.id.recyclerViewTypes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // ✅ Gọi listener với 3 tham số: name, type, iconName
        adapter = new TransactionTypeAdapter(transactionTypeList, (selectedItem) -> {
            if (listener != null) {
                listener.onTypeSelected(
                        selectedItem.getName(),
                        selectedItem.getType(),
                        selectedItem.getIc_name() // icon name
                );
            }
            dismiss();
        });
        recyclerView.setAdapter(adapter);

        btnExpense.setOnClickListener(v -> {
            selectedCategory = "expense";
            loadTransactionTypes(selectedCategory);
            highlightButton(btnExpense, btnIncome);
        });

        btnIncome.setOnClickListener(v -> {
            selectedCategory = "income";
            loadTransactionTypes(selectedCategory);
            highlightButton(btnIncome, btnExpense);
        });

        highlightButton(btnExpense, btnIncome); // mặc định là expense
        loadTransactionTypes(selectedCategory);

        return view;
    }

    private void loadTransactionTypes(String category) {
        FirebaseDatabase.getInstance().getReference("TransactionTypes").child(category)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        transactionTypeList.clear();
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            TransactionType type = itemSnapshot.getValue(TransactionType.class);
                            if (type != null) {
                                transactionTypeList.add(type);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Không tải được loại giao dịch", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void highlightButton(Button selected, Button unselected) {
        selected.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rose_500));
        selected.setTextColor(Color.WHITE);

        unselected.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray));
        unselected.setTextColor(ContextCompat.getColor(requireContext(), R.color.rose_500));
    }
}
