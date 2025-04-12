package com.example.deadline;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.*;

public class AddTransactionActivity extends AppCompatActivity implements TransactionTypeBottomSheet.OnTypeSelectedListener {

    private TextView tvTransactionType;
    private ImageView ivTransactionIcon;
    private EditText edtAmount, edtDate, edtNote;
    private ImageButton btnBack;
    private Button btnAdd;

    // Biến lưu lại lựa chọn người dùng
    private String selectedTransactionName = "";
    private String selectedTransactionType = "";
    private String selectedTransactionIcon = "";

    // Biến toàn cục lưu UID người dùng
    private String uid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Kiểm tra đăng nhập và lấy UID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm giao dịch!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        uid = user.getUid(); // Gán UID

//        insertSampleTransactionTypes(); // Chèn mẫu loại giao dịch cho người dùng mới

        // Ánh xạ view
        tvTransactionType = findViewById(R.id.tvTransactionType);
        ivTransactionIcon = findViewById(R.id.ivTransactionIcon);
        edtAmount = findViewById(R.id.edtAmount);
        edtDate = findViewById(R.id.edtDate);
        edtNote = findViewById(R.id.edtNote);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);

        edtDate.setOnClickListener(v -> showDatePicker());
        btnBack.setOnClickListener(v -> finish());

        tvTransactionType.setOnClickListener(v -> {
            TransactionTypeBottomSheet bottomSheet = new TransactionTypeBottomSheet();
            bottomSheet.setOnTypeSelectedListener(this);
            bottomSheet.setUid(uid); // Truyền UID vào BottomSheet
            bottomSheet.show(getSupportFragmentManager(), "TransactionTypeBottomSheet");
        });

        btnAdd.setOnClickListener(v -> addTransaction());
    }

    @Override
    public void onTypeSelected(String name, String type, String iconName) {
        selectedTransactionName = name;
        selectedTransactionType = type;
        selectedTransactionIcon = iconName;

        tvTransactionType.setText(name);
        int iconResId = getResources().getIdentifier(iconName, "drawable", getPackageName());
        if (iconResId != 0) {
            ivTransactionIcon.setImageResource(iconResId);
        }
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    edtDate.setText(date);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void addTransaction() {
        String amountStr = edtAmount.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String note = edtNote.getText().toString().trim();

        if (selectedTransactionName.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn loại giao dịch!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền và ngày!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra nếu số tiền nhập vào có phải là một số hợp lệ
        double amount = 0;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                throw new NumberFormatException("Số tiền phải lớn hơn 0");
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ! Vui lòng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu giao dịch vào Firebase
        saveTransactionToFirebase(selectedTransactionName, selectedTransactionType, selectedTransactionIcon, amount, date, note);
    }

    // ✅ Chèn loại giao dịch mẫu vào theo UID riêng
    private void insertSampleTransactionTypes() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TransactionTypes").child(uid);

        // Kiểm tra nếu đã tồn tại thì không chèn nữa
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) return; // đã có, không chèn nữa

                Map<String, String> expenses = new LinkedHashMap<>();
                expenses.put("Đi chợ / Siêu thị", "ic_groceries");
                expenses.put("Ăn uống", "ic_food");
                expenses.put("Mua sắm online", "ic_online_shopping");
                expenses.put("Vật dụng hàng ngày", "ic_daily_essentials");
                expenses.put("Đi lại", "ic_transport");
                expenses.put("Xăng xe", "ic_fuel");
                expenses.put("Tiệc / Sự kiện", "ic_event");
                expenses.put("Giặt ủi", "ic_laundry");
                expenses.put("Tiền thuê nhà", "ic_rent");
                expenses.put("Trả nợ", "ic_debt");
                expenses.put("Quần áo / Phụ kiện", "ic_clothing");
                expenses.put("Chăm sóc sức khỏe", "ic_healthcare");
                expenses.put("Tiện ích (Điện, Nước)", "ic_utilities");
                expenses.put("Giải trí", "ic_entertainment");
                expenses.put("Du lịch", "ic_travel");
                expenses.put("Học tập", "ic_education");
                expenses.put("Gas", "ic_gas");
                expenses.put("Bảo hiểm", "ic_insurance");
                expenses.put("Chăm sóc cá nhân", "ic_personal_care");
                expenses.put("Quà tặng / Từ thiện", "ic_gift");
                expenses.put("Tiết kiệm / Đầu tư", "ic_saving");
                expenses.put("Chi tiêu khác", "ic_other");

                for (Map.Entry<String, String> entry : expenses.entrySet()) {
                    String id = ref.child("expense").push().getKey();
                    if (id != null) {
                        ref.child("expense").child(id).setValue(new TransactionType(entry.getKey(), entry.getValue(), "expense"));
                    }
                }

                Map<String, String> incomes = new LinkedHashMap<>();
                incomes.put("Lương", "ic_salary");
                incomes.put("Thưởng", "ic_bonus");
                incomes.put("Lãi đầu tư", "ic_investment");
                incomes.put("Thưởng / Quà tặng", "ic_reward");
                incomes.put("Lãi ngân hàng", "ic_interest");
                incomes.put("Hoàn tiền / Đền bù", "ic_cashback");
                incomes.put("Tiền trợ cấp", "ic_allowance");
                incomes.put("Thu nhập khác", "ic_other");

                for (Map.Entry<String, String> entry : incomes.entrySet()) {
                    String id = ref.child("income").push().getKey();
                    if (id != null) {
                        ref.child("income").child(id).setValue(new TransactionType(entry.getKey(), entry.getValue(), "income"));
                    }
                }

                Toast.makeText(AddTransactionActivity.this, "Đã chèn dữ liệu mẫu!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    // ✅ Lưu giao dịch vào bảng Transactions của UID
    private void saveTransactionToFirebase(String name, String type, String iconName, double amount, String date, String note) {
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("transactions");

        String transactionId = transactionsRef.push().getKey();  // Tạo ID tự động

        if (transactionId != null) {
            // Gọi constructor với 9 tham số, truyền ID vào
            GiaoDich giaoDich = new GiaoDich(transactionId, date, amount, type, note, "Không có tiêu đề", name, iconName);
            transactionsRef.child(transactionId).setValue(giaoDich)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Thêm giao dịch thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi lưu giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Không thể tạo ID giao dịch!", Toast.LENGTH_SHORT).show();
        }
    }

}