package com.example.deadline;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Calendar;

public class EditTransactionActivity extends AppCompatActivity implements TransactionTypeBottomSheet.OnTypeSelectedListener {

    private EditText edtAmount, edtDate, edtNote;
    private TextView tvTransactionType;
    private ImageView ivTransactionIcon;
    private Button btnUpdate;
    private LinearLayout llTransactionType;

    private DatabaseReference ref;
    private String transactionId;
    private String uid;

    private String selectedIcon = null;
    private String selectedName = null;
    private String selectedType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ref = FirebaseDatabase.getInstance().getReference();
        transactionId = getIntent().getStringExtra("transactionId");

        // Ánh xạ View
        ImageButton btnBack = findViewById(R.id.btnBack);
        tvTransactionType = findViewById(R.id.tvTransactionType);
        ivTransactionIcon = findViewById(R.id.ivTransactionIcon);
        llTransactionType = findViewById(R.id.llTransactionType);
        edtAmount = findViewById(R.id.edtAmount);
        edtDate = findViewById(R.id.edtDate);
        edtNote = findViewById(R.id.edtNote);
        btnUpdate = findViewById(R.id.btnSave);

        btnBack.setOnClickListener(v -> finish());

        edtDate.setOnClickListener(v -> showDatePicker());

        // Load dữ liệu từ Firebase
        if (transactionId != null) {
            ref.child("users").child(uid).child("transactions").child(transactionId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GiaoDich giaoDich = dataSnapshot.getValue(GiaoDich.class);
                            if (giaoDich != null) {
                                DecimalFormat formatter = new DecimalFormat("#,###");
                                edtAmount.setText(formatter.format(giaoDich.getAmount()));
                                edtNote.setText(giaoDich.getNote());
                                edtDate.setText(giaoDich.getDate());
                                tvTransactionType.setText(giaoDich.getName());

                                selectedIcon = giaoDich.getIcon();
                                selectedName = giaoDich.getName();
                                selectedType = giaoDich.getType();

                                if (selectedIcon != null) {
                                    int iconResId = getResources().getIdentifier(
                                            selectedIcon, "drawable", getPackageName()
                                    );
                                    if (iconResId != 0) {
                                        ivTransactionIcon.setImageResource(iconResId);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("FirebaseError", "Lỗi khi lấy dữ liệu: " + databaseError.getMessage());
                        }
                    });
        } else {
            Log.e("Error", "transactionId is null");
        }

        btnUpdate.setOnClickListener(v -> updateTransaction());

        tvTransactionType.setOnClickListener(v -> showTransactionTypeBottomSheet());
        llTransactionType.setOnClickListener(v -> showTransactionTypeBottomSheet());
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                EditTransactionActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    edtDate.setText(selectedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void updateTransaction() {
        String updatedAmount = edtAmount.getText().toString();
        String updatedNote = edtNote.getText().toString();
        String updatedDate = edtDate.getText().toString();

        if (!TextUtils.isEmpty(updatedAmount) && !TextUtils.isEmpty(updatedNote) && !TextUtils.isEmpty(updatedDate)) {
            try {
                updatedAmount = updatedAmount.replace(",", "");
                updatedAmount = updatedAmount.replace(".", "");
                double amount = Double.parseDouble(updatedAmount);

                DatabaseReference transRef = ref.child("users").child(uid).child("transactions").child(transactionId);
                transRef.child("amount").setValue(amount);
                transRef.child("note").setValue(updatedNote);
                transRef.child("date").setValue(updatedDate);

                if (selectedName != null && selectedIcon != null && selectedType != null) {
                    transRef.child("name").setValue(selectedName);
                    transRef.child("icon").setValue(selectedIcon);
                    transRef.child("type").setValue(selectedType);
                }

                Toast.makeText(this, "Cập nhật giao dịch thành công", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTransactionTypeBottomSheet() {
        TransactionTypeBottomSheet bottomSheet = new TransactionTypeBottomSheet();
        bottomSheet.setOnTypeSelectedListener(this);
        bottomSheet.setUid(uid);
        bottomSheet.show(getSupportFragmentManager(), "TransactionTypeBottomSheet");
    }

    @Override
    public void onTypeSelected(String name, String type, String iconName) {
        selectedName = name;
        selectedType = type;
        selectedIcon = iconName;
        tvTransactionType.setText(name);

        int iconResId = getResources().getIdentifier(iconName, "drawable", getPackageName());
        if (iconResId != 0) {
            ivTransactionIcon.setImageResource(iconResId);
        }
    }
}
