
package com.clipper.video;

import android.app.*;
import android.os.*;
import android.net.Uri;
import android.content.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.concurrent.*;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.media3.common.MediaItem;
import androidx.media3.transformer.*;

public class MainActivity extends ComponentActivity {

    VideoView videoView;
    EditText startTime, endTime;
    Button selectButton, exportButton;
    TextView status;

    Uri videoUri;
    ExecutorService executor =
        Executors.newSingleThreadExecutor();

    ActivityResultLauncher<String> picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20,20,20,20);

        TextView title = new TextView(this);
        title.setText("CLIPPER VIDEO APP");
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        videoView = new VideoView(this);
        layout.addView(videoView,
            new LinearLayout.LayoutParams(
                -1, 250));

        selectButton = new Button(this);
        selectButton.setText("PILIH VIDEO");
        layout.addView(selectButton);

        startTime = new EditText(this);
        startTime.setHint("Waktu mulai (detik)");
        startTime.setInputType(8194);
        layout.addView(startTime);

        endTime = new EditText(this);
        endTime.setHint("Waktu selesai (detik)");
        endTime.setInputType(8194);
        layout.addView(endTime);

        exportButton = new Button(this);
        exportButton.setText("POTONG & EKSPOR MP4");
        layout.addView(exportButton);

        status = new TextView(this);
        status.setText("Siap digunakan");
        layout.addView(status);

        setContentView(layout);

        picker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    videoUri = uri;
                    videoView.setVideoURI(uri);
                    videoView.start();
                    status.setText("Video berhasil dipilih");
                }
            }
        );

        selectButton.setOnClickListener(v -> {
            picker.launch("video/*");
        });

        exportButton.setOnClickListener(v -> {
            exportVideo();
        });
    }

    private void exportVideo() {

        if (videoUri == null) {
            status.setText("Pilih video dahulu!");
            return;
        }

        long start;
        long end;

        try {
            start = Long.parseLong(
                startTime.getText().toString()) * 1000;

            end = Long.parseLong(
                endTime.getText().toString()) * 1000;

        } catch (Exception e) {
            status.setText("Masukkan waktu mulai dan selesai");
            return;
        }

        if (start < 0 || end <= start) {
            status.setText("Waktu pemotongan tidak valid");
            return;
        }

        exportButton.setEnabled(false);
        status.setText("Sedang memproses video...");

        executor.execute(() -> {

            try {
                File output = new File(
                    getCacheDir(),
                    "clipper_output.mp4"
                );

                MediaItem mediaItem =
                    new MediaItem.Builder()
                        .setUri(videoUri)
                        .setClippingConfiguration(
                            new MediaItem.ClippingConfiguration.Builder()
                                .setStartPositionMs(start)
                                .setEndPositionMs(end)
                                .build()
                        )
                        .build();

                EditedMediaItem edited =
                    new EditedMediaItem.Builder(mediaItem)
                        .build();

                Transformer transformer =
                    new Transformer.Builder(this)
                        .addListener(
                            new Transformer.Listener() {

                                @Override
                                public void onCompleted(
                                    Composition composition,
                                    ExportResult result) {

                                    saveVideo(output);
                                }

                                @Override
                                public void onError(
                                    Composition composition,
                                    ExportResult result,
                                    ExportException exception) {

                                    runOnUiThread(() -> {
                                        status.setText(
                                            "Gagal: " +
                                            exception.getMessage()
                                        );
                                        exportButton.setEnabled(true);
                                    });
                                }
                            }
                        )
                        .build();

                transformer.start(
                    edited,
                    output.getAbsolutePath()
                );

            } catch (Exception e) {
                runOnUiThread(() -> {
                    status.setText("Error: " + e.getMessage());
                    exportButton.setEnabled(true);
                });
            }
        });
    }

    private void saveVideo(File file) {

        try {
            ContentValues values = new ContentValues();
            values.put(
                MediaStore.Video.Media.DISPLAY_NAME,
                "Clipper_" + System.currentTimeMillis() + ".mp4"
            );
            values.put(
                MediaStore.Video.Media.MIME_TYPE,
                "video/mp4"
            );
            values.put(
                MediaStore.Video.Media.RELATIVE_PATH,
                "Movies/Clipper"
            );

            Uri uri = getContentResolver().insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values
            );

            if (uri == null) {
                throw new IOException("Gagal menyimpan video");
            }

            try (InputStream in = new FileInputStream(file);
                 OutputStream out =
                    getContentResolver().openOutputStream(uri)) {

                if (out == null) {
                    throw new IOException("Gagal membuka penyimpanan");
                }

                byte[] buffer = new byte[8192];
                int length;

                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }
            }

            runOnUiThread(() -> {
                status.setText("BERHASIL! Video tersimpan di Movies/Clipper");
                exportButton.setEnabled(true);
            });

        } catch (Exception e) {
            runOnUiThread(() -> {
                status.setText("Gagal menyimpan: " + e.getMessage());
                exportButton.setEnabled(true);
            });
        }
    }
}
