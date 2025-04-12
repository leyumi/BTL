package com.example.deadline;

import android.app.DatePickerDialog;
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
        adapter = new GiaoDichAdapter(filteredGiaoDichList, new GiaoDichAdapter.OnItemActionListener() {
            @Override
            public void onEdit(int position) {
                // Xử lý sửa
            }

            @Override
            public void onDelete(int position) {
                // Xử lý xóa
            }
        });
        rvAllTransactions.setAdapter(adapter);

        // Khởi tạo ngày mặc định (tháng hiện tại)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        fromDate = calendar.getTime();
        tvFromDate.setText(dateFormat.format(fromDate));

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        toDate = calendar.getTime();
        tvToDate.setText(dateFormat.format(toDate));

        // Sự kiện chọn ngày bắt đầu
        btnPickFromDate.setOnClickListener(v -> showDatePicker(true));

        // Sự kiện chọn ngày kết thúc
        btnPickToDate.setOnClickListener(v -> showDatePicker(false));

        // Sự kiện áp dụng bộ lọc
        btnApplyFilter.setOnClickListener(v -> applyFilters());

        // Tải tất cả giao dịch
        loadAllGiaoDich();
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
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void loadAllGiaoDich() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid())
                .child("transactions");

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allGiaoDichList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    GiaoDich gd = data.getValue(GiaoDich.class);
                    if (gd != null) {
                        allGiaoDichList.add(gd);
                    }
                }
                applyFilters(); // Áp dụng bộ lọc sau khi tải xong
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AllTransactionsActivity.this, "Lỗi khi tải giao dịch: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        filteredGiaoDichList.clear();

        String selectedType = spinnerLoaiGD.getSelectedItem().toString();
        boolean filterByType = !selectedType.equals("Tất cả");

        for (GiaoDich gd : allGiaoDichList) {
            try {
                Date gdDate = dateFormat.parse(gd.getDate());
                if (gdDate == null) continue;

                // Kiểm tra ngày
                boolean dateInRange = (fromDate == null || gdDate.after(fromDate) ||
                        gdDate.equals(fromDate)) &&
                        (toDate == null || gdDate.before(toDate) ||
                                gdDate.equals(toDate));

                // Kiểm tra loại giao dịch
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
}