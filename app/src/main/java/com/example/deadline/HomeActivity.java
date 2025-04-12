package com.example.deadline;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> addTransactionLauncher;
    private TextView tvChiTieu, tvThuNhap, tvSoDu;
    private RecyclerView rvGiaoDich;
    private Spinner spinnerMonths;
    private GiaoDichAdapter adapter;

    private final List<GiaoDich> giaoDichList = new ArrayList<>();
    private final List<String> monthList = new ArrayList<>();
    private ArrayAdapter<String> monthAdapter;

    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;

    private final SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault());

    private ActivityResultLauncher<Intent> editTransactionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        addTransactionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadThangGiaoDich();
                    }
                });

        editTransactionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadThangGiaoDich(); // gọi lại hàm load dữ liệu
                    }
                }
        );


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
        adapter = new GiaoDichAdapter(HomeActivity.this, giaoDichList, new GiaoDichAdapter.OnItemActionListener() {

            @Override
            public void onEdit(GiaoDich giaoDich) {
                // Xử lý khi nhấn Sửa
                Intent intent = new Intent(HomeActivity.this, EditTransactionActivity.class);
                intent.putExtra("transactionId", giaoDich.getId());
                editTransactionLauncher.launch(intent); // dùng launcher thay vì startActivity
            }

            @Override
            public void onDelete(GiaoDich giaoDich) {
                // Xử lý khi nhấn Xóa
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setMessage("Bạn có chắc chắn muốn xóa giao dịch này?")
                        .setPositiveButton("Xóa", (dialog, id) -> {
                            // Xóa giao dịch từ Firebase
                            FirebaseDatabase.getInstance().getReference("users/" + currentUser.getUid() + "/transactions")
                                    .child(giaoDich.getId()).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        giaoDichList.remove(giaoDich);
                                        adapter.notifyDataSetChanged();  // Cập nhật lại RecyclerView
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(HomeActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
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
        databaseRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid())
                .child("transactions");
        loadThangGiaoDich();
    }

    private void setupEventListeners() {
        findViewById(R.id.btnViewAll).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AllTransactionsActivity.class));
        });

        spinnerMonths.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                giaoDichList.clear();
                adapter.notifyDataSetChanged();
                loadGiaoDichTheoThang(monthList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            else if (id == R.id.nav_calendar) {
                startActivity(new Intent(HomeActivity.this, CalendarActivity.class));
            } else if (id == R.id.nav_add) {
                Intent intent = new Intent(HomeActivity.this, AddTransactionActivity.class);
                addTransactionLauncher.launch(intent);
            } else if (id == R.id.nav_statistics) {
                startActivity(new Intent(HomeActivity.this, StaticsticsActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            }
            return false;
        });
    }

    private void loadThangGiaoDich() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> monthSet = new HashSet<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String date = data.child("date").getValue(String.class);
                    if (date != null && date.contains("/")) {
                        String[] parts = date.split("/");
                        if (parts.length == 3) {
                            String thang = parts[1];
                            String nam = parts[2];
                            if (thang.length() == 1) thang = "0" + thang;
                            monthSet.add(thang + "/" + nam);
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

    private void loadGiaoDichTheoThang(String thangNam) {
        final int[] tongThuNhap = {0};
        final int[] tongChiTieu = {0};

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                giaoDichList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    GiaoDich gd = data.getValue(GiaoDich.class);
                    if (gd != null && gd.getDate() != null) {
                        String[] parts = gd.getDate().split("/");
                        if (parts.length == 3) {
                            String thang = parts[1];
                            String nam = parts[2];
                            if (thang.length() == 1) thang = "0" + thang;
                            String gdThangNam = thang + "/" + nam;
                            if (gdThangNam.equals(thangNam)) {
                                giaoDichList.add(gd);
                                String type = gd.getType() != null ? gd.getType().trim().toLowerCase() : "";
                                if (type.equals("income")) {
                                    tongThuNhap[0] += gd.getAmount();
                                } else if (type.equals("expense")) {
                                    tongChiTieu[0] += gd.getAmount();
                                }
                            }
                        }
                    }
                }
                Collections.reverse(giaoDichList);
                adapter.notifyDataSetChanged();
                updateTongQuan(tongThuNhap[0], tongChiTieu[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Lỗi khi tải giao dịch: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMonthList(Set<String> monthSet) {
        monthList.clear();
        monthList.addAll(monthSet);
        Collections.sort(monthList, (a, b) -> {
            try {
                return sdf.parse(b).compareTo(sdf.parse(a));
            } catch (ParseException e) {
                return 0;
            }
        });
        if (monthList.isEmpty()) {
            monthList.add("Không có giao dịch");
        }
        monthAdapter.notifyDataSetChanged();
        if (!monthList.get(0).equals("Không có giao dịch")) {
            loadGiaoDichTheoThang(monthList.get(0));
        }
    }

    private void updateTongQuan(int thuNhap, int chiTieu) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        tvThuNhap.setText(numberFormat.format(thuNhap) + " đ");
        tvChiTieu.setText(numberFormat.format(chiTieu) + " đ");

        int soDu = thuNhap - chiTieu;
        tvSoDu.setText(numberFormat.format(soDu) + " đ");
    }

    private void deleteTransaction(int position) {
        GiaoDich gd = giaoDichList.get(position);
        String key = gd.getId();
        if (key != null) {
            databaseRef.child(key).removeValue().addOnSuccessListener(unused -> {
                Toast.makeText(HomeActivity.this, "Đã xoá giao dịch", Toast.LENGTH_SHORT).show();
                giaoDichList.remove(position);
                adapter.notifyItemRemoved(position);
            }).addOnFailureListener(e -> {
                Toast.makeText(HomeActivity.this, "Lỗi xoá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
