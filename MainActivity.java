package com.aivision5.app;

import android.app.;
import android.os.;
import android.view.;
import android.widget.;
import android.content.*;
import android.net.Uri;

public class MainActivity extends Activity {

TextView status;
Uri videoUri;
Spinner quality;
ProgressBar progress;
Button startButton;

@Override
public void onCreate(Bundle b) {
    super.onCreate(b);

    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(1);
    layout.setPadding(30, 35, 30, 30);
    layout.setBackgroundColor(0xFF101A30);

    TextView title = new TextView(this);
    title.setText("AI VISION 5");
    title.setTextSize(30);
    title.setTextColor(0xFFFFFFFF);
    title.setGravity(Gravity.CENTER);
    layout.addView(title);

    TextView subtitle = new TextView(this);
    subtitle.setText("نظام تحسين جودة الفيديو");
    subtitle.setTextSize(17);
    subtitle.setTextColor(0xFF64B5F6);
    subtitle.setGravity(Gravity.CENTER);
    layout.addView(subtitle);

    Button choose = new Button(this);
    choose.setText("📁 اختيار فيديو");
    layout.addView(choose);

    status = new TextView(this);
    status.setText("لم يتم اختيار فيديو");
    status.setTextColor(0xFFFFFFFF);
    status.setPadding(0, 20, 0, 20);
    layout.addView(status);

    TextView qualityTitle = new TextView(this);
    qualityTitle.setText("دقة الفيديو الناتج");
    qualityTitle.setTextColor(0xFFFFFFFF);
    qualityTitle.setTextSize(16);
    layout.addView(qualityTitle);

    quality = new Spinner(this);
    String[] options = {"1080p - Full HD", "4K - Ultra HD"};
    ArrayAdapter<String> adapter = new ArrayAdapter<>(
        this,
        android.R.layout.simple_spinner_item,
        options
    );
    adapter.setDropDownViewResource(
        android.R.layout.simple_spinner_dropdown_item
    );
    quality.setAdapter(adapter);
    layout.addView(quality);

    startButton = new Button(this);
    startButton.setText("▶ بدء تحسين الفيديو");
    layout.addView(startButton);

    progress = new ProgressBar(this, null,
        android.R.attr.progressBarStyleHorizontal);
    progress.setMax(100);
    progress.setProgress(0);
    layout.addView(progress);

    TextView footer = new TextView(this);
    footer.setText("AI Vision 5 | Video Enhancement");
    footer.setTextColor(0xFF64B5F6);
    footer.setGravity(Gravity.CENTER);
    footer.setPadding(0, 35, 0, 0);
    layout.addView(footer);

    choose.setOnClickListener(v -> {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 100);
    });

    startButton.setOnClickListener(v -> {
        if (videoUri == null) {
            status.setText("يرجى اختيار فيديو أولاً");
            return;
        }

        status.setText("تم اختيار الفيديو. محرك المعالجة يحتاج إلى الربط.");
    });

    setContentView(layout);
}

@Override
protected void onActivityResult(int requestCode,
        int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == 100 &&
        resultCode == RESULT_OK && data != null) {

        videoUri = data.getData();

        getContentResolver().takePersistableUriPermission(
            videoUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        );

        status.setText("تم اختيار الفيديو بنجاح ✓");
    }
}

}
