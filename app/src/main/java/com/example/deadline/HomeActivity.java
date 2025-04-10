package com.example.deadline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

        // Ánh xạ
        tvChiTieu = findViewById(R.id.tvChiTieu);
        tvThuNhap = findViewById(R.id.tvThuNhap);
        tvSoDu = findViewById(R.id.tvSoDu);
        spinnerMonths = findViewById(R.id.spinnerMonths);
        rvGiaoDich = findViewById(R.id.rvGiaoDich);
        rvGiaoDich.setLayoutManager(new LinearLayoutManager(this));

        // Adapter cho RecyclerView kèm listener xử lý edit/delete
        adapter = new GiaoDichAdapter(giaoDichList, new GiaoDichAdapter.OnItemActionListener() {
            @Override
            public void onEdit(int position) {
                Toast.makeText(HomeActivity.this, "Chức năng sửa chưa triển khai", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(int position) {
                giaoDichList.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(HomeActivity.this, "Đã xoá giao dịch", Toast.LENGTH_SHORT).show();
                // Cập nhật lại Firebase nếu cần
            }
        });
        rvGiaoDich.setAdapter(adapter);

        // Adapter cho spinner tháng
        monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthList);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonths.setAdapter(monthAdapter);

        // Kiểm tra đăng nhập
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy UID từ Firebase
        String userId = currentUser.getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        loadTongQuan();
        loadThangGiaoDich();

        spinnerMonths.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMonth = monthList.get(position);
                loadGiaoDichTheoThang(selectedMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_calendar) {
                startActivity(new Intent(HomeActivity.this, CalendarActivity.class));
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(HomeActivity.this, AddTransactionActivity.class));
                return true;
            } else if (id == R.id.nav_statistics) {
                startActivity(new Intent(HomeActivity.this, StaticsticsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadTongQuan() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer chiTieu = snapshot.child("expense").getValue(Integer.class);
                Integer thuNhap = snapshot.child("income").getValue(Integer.class);
                Integer soDu = snapshot.child("balance").getValue(Integer.class);

                tvChiTieu.setText((chiTieu != null ? chiTieu : 0) + " VND");
                tvThuNhap.setText((thuNhap != null ? thuNhap : 0) + " VND");
                tvSoDu.setText((soDu != null ? soDu : 0) + " VND");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Lỗi khi tải tổng quan: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                            String monthYear = parts[1] + "/" + parts[2]; // MM/yyyy
                            monthSet.add(monthYear);
                        }
                    }
                }

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Lỗi khi tải tháng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGiaoDichTheoThang(String thangNam) {
        databaseRef.child("transactions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                giaoDichList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    GiaoDich gd = data.getValue(GiaoDich.class);
                    if (gd != null && gd.getDate() != null) {
                        String[] parts = gd.getDate().split("/");
                        if (parts.length == 3) {
                            String gdThangNam = parts[1] + "/" + parts[2];
                            if (gdThangNam.equals(thangNam)) {
                                giaoDichList.add(gd);
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Lỗi khi tải giao dịch: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
