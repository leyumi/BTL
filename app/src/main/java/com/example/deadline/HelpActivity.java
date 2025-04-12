package com.example.deadline;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    private TextView tvHelpContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // Khởi tạo các view
        tvHelpContent = findViewById(R.id.tvHelpContent);

        // Bạn có thể thay đổi nội dung hỗ trợ ở đây
        String helpText = "Hướng dẫn sử dụng:\n\n" +
                "1. Đăng nhập vào tài khoản của bạn.\n" +
                "2. Truy cập vào các chức năng như Quản lý chi tiêu, Quản lý thông tin cá nhân.\n" +
                "3. Để đăng xuất, bạn chỉ cần chọn 'Đăng xuất'.\n\n" +
                "Nếu bạn gặp vấn đề nào trong quá trình sử dụng, vui lòng liên hệ với chúng tôi.";

        tvHelpContent.setText(helpText);
    }
}
