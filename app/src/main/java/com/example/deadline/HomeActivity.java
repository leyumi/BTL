package com.example.deadline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private TextView tvChiTieu, tvThuNhap, tvSoDu;
    private RecyclerView rvGiaoDich;
    private Spinner spinnerMonths;
    private GiaoDichAdapter adapter;

    private final List<GiaoDich> giaoDichList = new ArrayList<>();
    private final List<String> monthList = new ArrayList<>();
    private ArrayAdapter<String> monthAdapter;

    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupAdapters();
        checkAuthentication();
        setupEventListeners();
    }

    private void initViews() {
        tvChiTieu = findViewById(R.id.tvChiTieu);
        tvThuNhap = findViewById(R.id.tvThuNhap);
        tvSoDu = findViewById(R.id.tvSoDu);
        spinnerMonths = findViewById(R.id.spinnerMonths);
        rvGiaoDich = findViewById(R.id.rvGiaoDich);
        rvGiaoDich.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupAdapters() {
        adapter = new GiaoDichAdapter(giaoDichList, new GiaoDichAdapter.OnItemActionListener() {
            @Override
            public void onEdit(int position) {
                Toast.makeText(HomeActivity.this, "Chức năng sửa chưa triển khai", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(int position) {
                deleteTransaction(position);
            }
        });
        rvGiaoDich.setAdapter(adapter);

        monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthList);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonths.setAdapter(monthAdapter);
    }

    private void checkAuthentication() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        databaseRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        loadInitialData();
    }

    private void setupEventListeners() {
        findViewById(R.id.btnViewAll).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AllTransactionsActivity.class));
        });

        spinnerMonths.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadGiaoDichTheoThang(monthList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_calendar) {
                startActivity(new Intent(HomeActivity.this, CalendarActivity.class));
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(HomeActivity.this, AddTransactionActivity.class));
            } else if (id == R.id.nav_statistics) {
                startActivity(new Intent(HomeActivity.this, StaticsticsActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            }
            return false;
        });
    }

    private void loadInitialData() {
        loadThangGiaoDich();
    }

    private void loadGiaoDichTheoThang(String thangNam) {
        databaseRef.child("transactions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                giaoDichList.clear();
                int tongThuNhap = 0;
                int tongChiTieu = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    GiaoDich gd = data.getValue(GiaoDich.class);
                    if (gd != null && gd.getDate() != null) {
                        String[] parts = gd.getDate().split("/");
                        if (parts.length == 3) {
                            String gdThangNam = parts[1] + "/" + parts[2];
                            if (gdThangNam.equals(thangNam)) {
                                giaoDichList.add(gd);

                                // Phân loại thu nhập/chi tiêu
                                if (gd.getAmount() >= 0) {
                                    tongThuNhap += gd.getAmount();
                                } else {
                                    tongChiTieu += Math.abs(gd.getAmount());
                                }
                            }
                        }
                    }
                }

                // Cập nhật UI
                updateTongQuan(tongThuNhap, tongChiTieu);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Lỗi khi tải giao dịch: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTongQuan(int thuNhap, int chiTieu) {
        tvThuNhap.setText(formatCurrency(thuNhap));
        tvChiTieu.setText(formatCurrency(chiTieu));
        tvSoDu.setText(formatCurrency(thuNhap - chiTieu));
    }

    private String formatCurrency(int amount) {
        return String.format(Locale.getDefault(), "%,d VND", amount);
    }

    private void loadThangGiaoDich() {
        databaseRef.child("transactions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> monthSet = new HashSet<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    String date = data.child("date").getValue(String.class);
                    if (date != null && date.contains("/")) {
                        String[] parts = date.split("/");
                        if (parts.length == 3) {
                            monthSet.add(parts[1] + "/" + parts[2]); // MM/yyyy
                        }
                    }
                }

                updateMonthList(monthSet);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Lỗi khi tải tháng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMonthList(Set<String> monthSet) {
        monthList.clear();
        monthList.addAll(monthSet);

        Collections.sort(monthList, (a, b) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
                return sdf.parse(b).compareTo(sdf.parse(a));
            } catch (ParseException e) {
                return 0;
            }
        });

        if (monthList.isEmpty()) {
            monthList.add("Không có giao dịch");
        }

        monthAdapter.notifyDataSetChanged();

        // Tự động load dữ liệu cho tháng đầu tiên
        if (!monthList.isEmpty() && !monthList.get(0).equals("Không có giao dịch")) {
            loadGiaoDichTheoThang(monthList.get(0));
        }
    }

    private void deleteTransaction(int position) {
        if (position >= 0 && position < giaoDichList.size()) {
            GiaoDich gd = giaoDichList.get(position);
            databaseRef.child("transactions").child(gd.getId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        giaoDichList.remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(HomeActivity.this, "Đã xoá giao dịch", Toast.LENGTH_SHORT).show();
                        // Cập nhật lại tổng quan
                        loadThangGiaoDich();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(HomeActivity.this, "Lỗi khi xoá giao dịch", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}