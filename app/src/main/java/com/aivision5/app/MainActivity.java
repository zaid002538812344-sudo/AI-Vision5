package com.aivision5.app;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;
import java.io.*;

public class MainActivity extends Activity {

    TextView status;
    Uri videoUri;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(1);
        layout.setPadding(35, 45, 35, 35);
        layout.setBackgroundColor(0xFF101A30);

        TextView title = new TextView(this);
        title.setText("AI VISION 5");
        title.setTextSize(30);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("نظام تحسين جودة الفيديو");
        subtitle.setTextSize(18);
        subtitle.setTextColor(0xFF64B5F6);
        subtitle.setGravity(Gravity.CENTER);
        layout.addView(subtitle);

        Button choose = new Button(this);
        choose.setText("اختيار فيديو");
        layout.addView(choose);

        status = new TextView(this);
        status.setText("يرجى اختيار فيديو من المعرض");
        status.setTextColor(0xFFFFFFFF);
        status.setPadding(0, 30, 0, 30);
        layout.addView(status);

        choose.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("video/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 100);
        });

        setContentView(layout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            videoUri = data.getData();
            status.setText("تم اختيار الفيديو بنجاح");
        }
    }
}
