package com.example.deadline;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllTransactionsActivity extends AppCompatActivity {

    private TextView tvFromDate, tvToDate;
    private ImageButton btnPickFromDate, btnPickToDate;
    private Button btnApplyFilter;
    private Spinner spinnerLoaiGD;
    private RecyclerView rvAllTransactions;
    private GiaoDichAdapter adapter;
    private final List<GiaoDich> allGiaoDichList = new ArrayList<>();
    private final List<GiaoDich> filteredGiaoDichList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Date fromDate, toDate;

    private DatabaseReference databaseRef;
    private ValueEventListener giaoDichListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_transactions);

        // Ánh xạ view
        tvFromDate = findViewById(R.id.tvFromDate);
        tvToDate = findViewById(R.id.tvToDate);
        btnPickFromDate = findViewById(R.id.btnPickFromDate);
        btnPickToDate = findViewById(R.id.btnPickToDate);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        spinnerLoaiGD = findViewById(R.id.spinnerLoaiGD);
        rvAllTransactions = findViewById(R.id.rvAllTransactions);

        // Thiết lập RecyclerView
        rvAllTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GiaoDichAdapter(this, filteredGiaoDichList, new GiaoDichAdapter.OnItemActionListener() {
            @Override
            public void onEdit(GiaoDich giaoDich) {
                Intent intent = new Intent(AllTransactionsActivity.this, EditTransactionActivity.class);
                intent.putExtra("GiaoDichID", giaoDich.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(GiaoDich giaoDich) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    databaseRef.child(giaoDich.getId()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AllTransactionsActivity.this, "Giao dịch đã được xóa", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AllTransactionsActivity.this, "Xóa giao dịch thất bại", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });
        rvAllTransactions.setAdapter(adapter);

        // Khởi tạo ngày mặc định
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        fromDate = calendar.getTime();
        tvFromDate.setText(dateFormat.format(fromDate));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        toDate = calendar.getTime();
        tvToDate.setText(dateFormat.format(toDate));

        // Sự kiện chọn ngày
        btnPickFromDate.setOnClickListener(v -> showDatePicker(true));
        btnPickToDate.setOnClickListener(v -> showDatePicker(false));
        btnApplyFilter.setOnClickListener(v -> applyFilters());

        // Tải dữ liệu realtime
        loadAllGiaoDichRealtime();
    }

    private void showDatePicker(boolean isFromDate) {
        Calendar calendar = Calendar.getInstance();
        if (isFromDate && fromDate != null) {
            calendar.setTime(fromDate);
        } else if (!isFromDate && toDate != null) {
            calendar.setTime(toDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    if (isFromDate) {
                        fromDate = selectedDate.getTime();
                        tvFromDate.setText(dateFormat.format(fromDate));
                    } else {
                        toDate = selectedDate.getTime();
                        tvToDate.setText(dateFormat.format(toDate));
                    }
                    applyFilters(); // Cập nhật lọc khi đổi ngày
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadAllGiaoDichRealtime() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid())
                .child("transactions");

        giaoDichListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allGiaoDichList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    GiaoDich gd = data.getValue(GiaoDich.class);
                    if (gd != null) {
                        allGiaoDichList.add(gd);
                    }
                }
                applyFilters(); // Áp dụng bộ lọc sau khi dữ liệu được tải về
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AllTransactionsActivity.this, "Lỗi khi tải giao dịch: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        databaseRef.addValueEventListener(giaoDichListener);
    }

    private void applyFilters() {
        filteredGiaoDichList.clear();

        String selectedType = spinnerLoaiGD.getSelectedItem().toString();
        boolean filterByType = !selectedType.equals("Tất cả");

        for (GiaoDich gd : allGiaoDichList) {
            try {
                Date gdDate = dateFormat.parse(gd.getDate());
                if (gdDate == null) continue;

                boolean dateInRange = (fromDate == null || !gdDate.before(fromDate)) &&
                        (toDate == null || !gdDate.after(toDate));

                boolean typeMatches = !filterByType ||
                        (selectedType.equals("Thu nhập") && gd.getAmount() >= 0) ||
                        (selectedType.equals("Chi tiêu") && gd.getAmount() < 0);

                if (dateInRange && typeMatches) {
                    filteredGiaoDichList.add(gd);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredGiaoDichList.isEmpty()) {
            Toast.makeText(this, "Không có giao dịch nào phù hợp", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseRef != null && giaoDichListener != null) {
            databaseRef.removeEventListener(giaoDichListener);
        }
    }
}
