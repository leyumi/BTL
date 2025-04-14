package com.example.deadline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarActivity extends AppCompatActivity {

    private TextView tvDate, tvChiTieu, tvThuNhap, tvSoDu, tvDetailDate, tvChiTieuDetail, tvThuNhapDetail;
    private GridView gridCalendar;
    private Calendar calendar;
    private CalendarAdapter calendarAdapter;
    private List<String> dateList;

    private DatabaseReference giaoDichRef;
    private String currentDateFormatted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Ánh xạ view
        tvDate = findViewById(R.id.tvDate);
        tvChiTieu = findViewById(R.id.tvChiTieu);
        tvThuNhap = findViewById(R.id.tvThuNhap);
        tvSoDu = findViewById(R.id.tvSoDu);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvChiTieuDetail = findViewById(R.id.tvChiTieuDetail);
        tvThuNhapDetail = findViewById(R.id.tvThuNhapDetail);
        gridCalendar = findViewById(R.id.gridCalendar);

        // Thiết lập thanh điều hướng dưới
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_calendar);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_calendar) {
                return true;
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });

        // Khởi tạo Calendar và ngày hiện tại
        calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        currentDateFormatted = sdf.format(calendar.getTime());

        tvDate.setText("Ngày " + currentDateFormatted);
        tvDetailDate.setText("Chi tiết ngày: " + currentDateFormatted);

        // Dữ liệu lịch
        dateList = generateCalendarData();
        calendarAdapter = new CalendarAdapter(this, dateList);
        gridCalendar.setAdapter(calendarAdapter);

        gridCalendar.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDay = dateList.get(position);
            if (position < 7 || selectedDay.isEmpty()) return;

            String newDate = String.format(Locale.getDefault(), "%d/%d/%d",
                    Integer.parseInt(selectedDay), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));

            currentDateFormatted = newDate;
            tvDate.setText("Ngày " + newDate);
            tvDetailDate.setText("Chi tiết ngày: " + newDate);

            loadDataForDate(newDate);
        });

        // Lấy UID người dùng hiện tại
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        giaoDichRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("transactions");

        // Load dữ liệu cho ngày hiện tại
        loadDataForDate(currentDateFormatted);
    }

    private List<String> generateCalendarData() {
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"));

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        for (int i = 1; i < firstDayOfWeek; i++) {
            list.add("");
        }

        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= maxDay; i++) {
            list.add(String.valueOf(i));
        }

        return list;
    }

    private void loadDataForDate(String date) {
        if (giaoDichRef == null) return;

        // Chuyển "01/04/2025" thành "1/4/2025" để khớp Firebase
        String normalizedDate = date.replaceFirst("^0+", "").replaceFirst("/0+", "/");

        giaoDichRef.orderByChild("date").equalTo(normalizedDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long tongChiTieu = 0;
                long tongThuNhap = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    GiaoDich giaoDich = ds.getValue(GiaoDich.class);

                    if (giaoDich != null) {
                        giaoDich.setId(ds.getKey());

                        if ("expense".equals(giaoDich.getType())) {
                            tongChiTieu += giaoDich.getAmount();
                        } else if ("income".equals(giaoDich.getType())) {
                            tongThuNhap += giaoDich.getAmount();
                        }
                    }
                }

                long soDu = tongThuNhap - tongChiTieu;

                tvChiTieu.setText("Chi tiêu: " + tongChiTieu + "đ");
                tvThuNhap.setText("Thu nhập: " + tongThuNhap + "đ");
                tvSoDu.setText("Số dư: " + soDu + "đ");

                tvChiTieuDetail.setText("Chi tiêu\n" + tongChiTieu + "đ");
                tvThuNhapDetail.setText("Thu nhập\n" + tongThuNhap + "đ");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CalendarActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}