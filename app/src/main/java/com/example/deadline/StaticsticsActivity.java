package com.example.deadline;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StaticsticsActivity extends AppCompatActivity {
    private PieChart pieChart;
    private RecyclerView recyclerView;
    private TextView txtTotalIncome, txtTotalExpense;
    private RadioGroup radioGroup;
    private List<ThongKeModel> data = new ArrayList<>();
    private ThongKeAdapter adapter;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staticstics);

        mAuth = FirebaseAuth.getInstance();
        initViews();
        setupRecyclerView();
        loadData("expense");
        setupRadioGroup();
    }

    private void initViews() {
        pieChart = findViewById(R.id.pieChart);
        recyclerView = findViewById(R.id.recyclerCategoryStats);
        txtTotalIncome = findViewById(R.id.txtTotalIncome);
        txtTotalExpense = findViewById(R.id.txtTotalExpense);
        radioGroup = findViewById(R.id.radioGroupType);
        configurePieChart();
    }

    private void configurePieChart() {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(android.R.color.transparent);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(getResources().getColor(android.R.color.black));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ThongKeAdapter(data);
        recyclerView.setAdapter(adapter);
    }

    private void setupRadioGroup() {
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbExpense) {
                loadData("expense");
            } else {
                loadData("income");
            }
        });
    }

    private void loadData(String type) {
        data.clear();
        adapter.notifyDataSetChanged();

        if (mAuth.getCurrentUser() == null) {
            showNoData();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("transactions");

        ref.orderByChild("type").equalTo(type).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total = 0;
                Map<String, Integer> categoryMap = new HashMap<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        Map<String, Object> transaction = (Map<String, Object>) ds.getValue();
                        if (transaction != null) {
                            int amount = getAmountFromTransaction(transaction);
                            // Đảm bảo chi tiêu là số âm
                            if (type.equals("expense")) {
                                amount = -Math.abs(amount);
                            } else {
                                amount = Math.abs(amount);
                            }

                            String category = transaction.get("category").toString();
                            total += amount;

                            if (categoryMap.containsKey(category)) {
                                categoryMap.put(category, categoryMap.get(category) + amount);
                            } else {
                                categoryMap.put(category, amount);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("STATS", "Error processing transaction", e);
                    }
                }

                for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
                    data.add(new ThongKeModel(entry.getKey(), entry.getValue()));
                }

                updateUI(type, total);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("STATS", "Failed to read value.", error.toException());
                showNoData();
            }
        });
    }

    private int getAmountFromTransaction(Map<String, Object> transaction) {
        try {
            Object amountObj = transaction.get("amount");
            if (amountObj instanceof Long) {
                return ((Long) amountObj).intValue();
            } else if (amountObj instanceof Double) {
                return (int) Math.round((Double) amountObj);
            } else if (amountObj instanceof Integer) {
                return (Integer) amountObj;
            }
            return 0;
        } catch (Exception e) {
            Log.e("STATS", "Error getting amount", e);
            return 0;
        }
    }

    private void updateUI(String type, int total) {
        runOnUiThread(() -> {
            NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedTotal = format.format(Math.abs(total));

            if (type.equals("expense")) {
                txtTotalExpense.setText(String.format("Chi tiêu\n-%s", formattedTotal));
                txtTotalExpense.setTextColor(Color.RED);
            } else {
                txtTotalIncome.setText(String.format("Thu nhập\n+%s", formattedTotal));
                txtTotalIncome.setTextColor(Color.parseColor("#FF4CAF50")); // Màu xanh lá
            }

            if (data.isEmpty()) {
                showNoData();
            } else {
                drawPieChart(data, type);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void drawPieChart(List<ThongKeModel> list, String type) {
        List<PieEntry> entries = new ArrayList<>();

        for (ThongKeModel item : list) {
            // Sử dụng giá trị tuyệt đối cho biểu đồ
            entries.add(new PieEntry(Math.abs(item.getAmount()), item.getCategory()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Thống kê theo danh mục");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(getResources().getColor(android.R.color.black));

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
                return format.format((int) value);
            }
        });

        pieChart.setData(pieData);
        pieChart.setCenterText(type.equals("expense") ? "Biểu đồ chi tiêu" : "Biểu đồ thu nhập");
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void showNoData() {
        pieChart.clear();
        pieChart.setNoDataText("Không có dữ liệu để hiển thị");
        pieChart.invalidate();
    }
}