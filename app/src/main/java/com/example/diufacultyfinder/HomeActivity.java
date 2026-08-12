package com.example.diufacultyfinder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_EMAIL = "real_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final int MIC_PERMISSION_REQUEST_CODE = 101;
    private static final int FILE_CHOOSER_REQUEST_CODE = 102;
    private static final int BACKUP_CREATE_REQUEST_CODE = 103;
    private static final int BACKUP_RESTORE_REQUEST_CODE = 104;

    private ValueCallback<Uri[]> filePathCallback;
    private WebView webView; // now a field so backup/restore callbacks can reach it
    private String pendingBackupJson; // holds the JSON while the user picks a save location

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d("WebViewConsole", "TEST — HomeActivity onCreate is running the NEW code");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MIC_PERMISSION_REQUEST_CODE);
        }

        webView = findViewById(R.id.webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);

        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        WebView.setWebContentsDebuggingEnabled(true);

        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage msg) {
                Log.d("WebViewConsole", msg.message() + " -- From line "
                        + msg.lineNumber() + " of " + msg.sourceId());
                return true;
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams fileChooserParams) {
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }
                filePathCallback = callback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                        "text/csv",
                        "text/comma-separated-values",
                        "application/csv",
                        "application/vnd.ms-excel",
                        "text/plain"
                });

                try {
                    startActivityForResult(Intent.createChooser(intent, "Select CSV file"), FILE_CHOOSER_REQUEST_CODE);
                } catch (Exception e) {
                    filePathCallback = null;
                    Toast.makeText(HomeActivity.this, "Couldn't open file picker.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:") || url.startsWith("mailto:")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } catch (Exception e) {
                        Toast.makeText(HomeActivity.this, "No app found to handle this action", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
                return false;
            }
        });

        webView.loadUrl("file:///android_asset/home.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (filePathCallback == null) return;
            Uri[] result = null;
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                result = new Uri[]{data.getData()};
            }
            filePathCallback.onReceiveValue(result);
            filePathCallback = null;
            return;
        }

        if (requestCode == BACKUP_CREATE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null && pendingBackupJson != null) {
                writeBackupToUri(data.getData(), pendingBackupJson);
            } else {
                Toast.makeText(this, "Backup cancelled.", Toast.LENGTH_SHORT).show();
            }
            pendingBackupJson = null;
            return;
        }

        if (requestCode == BACKUP_RESTORE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                readBackupFromUri(data.getData());
            } else {
                Toast.makeText(this, "Restore cancelled.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void writeBackupToUri(Uri uri, String jsonData) {
        try {
            ContentResolver resolver = getContentResolver();
            OutputStream out = resolver.openOutputStream(uri);
            if (out == null) throw new Exception("Couldn't open output stream");
            out.write(jsonData.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
            Toast.makeText(this, "Backup saved successfully!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("BackupExport", "Failed to write backup", e);
            Toast.makeText(this, "Couldn't save backup: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void readBackupFromUri(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream in = resolver.openInputStream(uri);
            if (in == null) throw new Exception("Couldn't open input stream");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) jsonBuilder.append(line).append("\n");
            reader.close();
            in.close();

            String jsonData = jsonBuilder.toString();
            // Safely pass the raw JSON text into the JS function as a properly-escaped string literal
            String escaped = JSONObject.quote(jsonData);
            runOnUiThread(() -> webView.evaluateJavascript("restoreBackup(" + escaped + ")", null));
        } catch (Exception e) {
            Log.e("BackupRestore", "Failed to read backup", e);
            Toast.makeText(this, "Couldn't read backup file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void onFacultyClick(String email) {
        }

        @JavascriptInterface
        public String getUserName() {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            return prefs.getString(KEY_USER_NAME, "User");
        }

        @JavascriptInterface
        public String getUserEmail() {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            return prefs.getString(KEY_USER_EMAIL, "");
        }

        @JavascriptInterface
        public void setUserName(String name) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putString(KEY_USER_NAME, name).apply();
        }

        @JavascriptInterface
        public void shareText(String text) {
            runOnUiThread(() -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            });
        }

        @JavascriptInterface
        public void logout() {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();

            runOnUiThread(() -> {
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // ================= Backup & Restore (new) =================

        @JavascriptInterface
        public void exportBackup(String jsonData) {
            pendingBackupJson = jsonData;
            runOnUiThread(() -> {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_TITLE, "DIU_FacultyFinder_Backup_" + timestamp + ".json");
                try {
                    startActivityForResult(intent, BACKUP_CREATE_REQUEST_CODE);
                } catch (Exception e) {
                    Toast.makeText(HomeActivity.this, "Couldn't open save dialog.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void openRestorePicker() {
            runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*"); // some file managers mislabel .json as text/plain — accept both
                try {
                    startActivityForResult(intent, BACKUP_RESTORE_REQUEST_CODE);
                } catch (Exception e) {
                    Toast.makeText(HomeActivity.this, "Couldn't open file picker.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}