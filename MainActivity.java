package com.aivision5.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.os.Build;
import android.os.Environment;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;

public class MainActivity extends Activity {
    private static final int PICK_VIDEO = 41;
    private Uri selectedUri;
    private TextView selectedText, statusText;
    private Spinner resolutionSpinner;
    private Button processButton;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(8,13,24));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(24));
        scroll.addView(root);

        TextView logo = new TextView(this);
        logo.setText("AI VISION 5");
        logo.setTextColor(Color.rgb(100,181,246));
        logo.setTextSize(28);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);
        root.addView(logo, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("AI-STYLE VIDEO ENHANCER");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(12);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(6), 0, dp(24));
        root.addView(subtitle, matchWrap());

        TextView title = new TextView(this);
        title.setText("تحسين الفيديو");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setGravity(Gravity.RIGHT);
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title, matchWrap());

        selectedText = new TextView(this);
        selectedText.setText("لم يتم اختيار فيديو بعد");
        selectedText.setTextColor(Color.LTGRAY);
        selectedText.setTextSize(14);
        selectedText.setPadding(dp(14), dp(16), dp(14), dp(16));
        selectedText.setBackgroundColor(Color.rgb(20,29,46));
        LinearLayout.LayoutParams box = matchWrap();
        box.topMargin = dp(14);
        root.addView(selectedText, box);

        Button choose = button("اختيار فيديو من الهاتف");
        root.addView(choose, matchWrap());
        choose.setOnClickListener(v -> pickVideo());

        TextView resLabel = label("دقة الإخراج");
        root.addView(resLabel, matchWrap());
        resolutionSpinner = new Spinner(this);
        String[] options = {"1080p", "4K"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options);
        resolutionSpinner.setAdapter(adapter);
        root.addView(resolutionSpinner, matchWrap());

        TextView note = new TextView(this);
        note.setText("المعالجة الحالية: تكبير الدقة وإعادة ترميز الفيديو. هذا الإصدار لا يشغّل DLSS 5 ولا يتضمن تحسين AI للوجوه.");
        note.setTextColor(Color.rgb(180,190,205));
        note.setTextSize(12);
        note.setPadding(0, dp(12), 0, dp(12));
        root.addView(note, matchWrap());

        processButton = button("بدء التحسين");
        root.addView(processButton, matchWrap());
        processButton.setEnabled(false);
        processButton.setOnClickListener(v -> processVideo());

        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        progress.setVisibility(View.GONE);
        root.addView(progress, matchWrap());

        statusText = new TextView(this);
        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, dp(12), 0, dp(12));
        root.addView(statusText, matchWrap());

        setContentView(scroll);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private TextView label(String text) {
        TextView t = new TextView(this);
        t.setText(text); t.setTextColor(Color.WHITE); t.setTextSize(16);
        t.setGravity(Gravity.RIGHT); t.setPadding(0, dp(18), 0, dp(8));
        return t;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(25,118,210)));
        return b;
    }

    private void pickVideo() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("video/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, PICK_VIDEO);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO && resultCode == RESULT_OK && data != null) {
            selectedUri = data.getData();
            if (selectedUri != null) {
                try { getContentResolver().takePersistableUriPermission(selectedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
                catch (Exception ignored) {}
                selectedText.setText("تم اختيار: " + displayName(selectedUri));
                processButton.setEnabled(true);
                statusText.setText("");
            }
        }
    }

    private String displayName(Uri uri) {
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return c.getString(idx);
            }
        } catch (Exception ignored) {}
        return "video";
    }

    private void processVideo() {
        if (selectedUri == null) return;
        processButton.setEnabled(false);
        statusText.setText("جارٍ تجهيز الفيديو...");
        executor.execute(() -> {
            File input = new File(getCacheDir(), "source_video");
            try (InputStream in = getContentResolver().openInputStream(selectedUri);
                 OutputStream out = new FileOutputStream(input)) {
                byte[] buffer = new byte[1024 * 1024];
                int n;
                while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
            } catch (Exception e) {
                runOnUiThread(() -> { statusText.setText("تعذر قراءة الفيديو: " + e.getMessage()); processButton.setEnabled(true); });
                return;
            }

            int height = resolutionSpinner.getSelectedItemPosition() == 1 ? 2160 : 1080;
            String name = "AIVision5_" + System.currentTimeMillis() + ".mp4";
            File tempOut = new File(getCacheDir(), name);
            String vf = "scale=-2:" + height + ":flags=lanczos,eq=contrast=1.04:saturation=1.06:brightness=0.01,unsharp=5:5:0.35:3:3:0.0";
            String cmd = "-y -i " + quote(input.getAbsolutePath()) + " -vf " + quote(vf) +
                    " -c:v libx264 -preset ultrafast -crf 20 -c:a aac -b:a 128k " + quote(tempOut.getAbsolutePath());

            FFmpegKit.executeAsync(cmd, session -> {
                if (ReturnCode.isSuccess(session.getReturnCode()) && tempOut.exists() && tempOut.length() > 0) {
                    try {
                        Uri saved = saveToGallery(tempOut, name);
                        runOnUiThread(() -> {
                            statusText.setText("اكتمل التحسين وحُفظ الفيديو في المعرض.\n" + name);
                            processButton.setEnabled(true);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> { statusText.setText("اكتملت المعالجة لكن تعذر الحفظ في المعرض: " + e.getMessage()); processButton.setEnabled(true); });
                    }
                } else {
                    String log = session.getFailStackTrace();
                    runOnUiThread(() -> { statusText.setText("فشلت المعالجة. قد يكون الفيديو أو الترميز غير مدعوم.\n" + (log == null ? "" : log)); processButton.setEnabled(true); });
                }
            });
        });
    }

    private String quote(String s) { return "'" + s.replace("'", "'\\''") + "'"; }

    private Uri saveToGallery(File file, String name) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, name);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        if (Build.VERSION.SDK_INT >= 29) values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/AI Vision 5");
        Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) throw new IOException("MediaStore returned null");
        try (InputStream in = new FileInputStream(file); OutputStream out = getContentResolver().openOutputStream(uri)) {
            byte[] buffer = new byte[1024 * 1024];
            int n;
            while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
        }
        return uri;
    }
}
