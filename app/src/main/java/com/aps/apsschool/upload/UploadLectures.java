package com.aps.apsschool.upload;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.aps.apsschool.upload.file.FileUtils;
import com.aps.apsschool.upload.video.MediaController;
import com.aps.apsschool.user.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class UploadLectures extends AppCompatActivity {

    private static final int RESULT_CODE_COMPRESS_VIDEO = 3;
    private static final String TAG = "UploadLectures";
    private EditText editText;
    private ProgressBar progressBar;
    private File tempFile;
    private StorageReference mStorageRef;
    private static final int SELECT_VIDEO = 1;
    private String selectedVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_lectures);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        editText = (EditText) findViewById(R.id.editText);

        findViewById(R.id.btnSelectVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("video/*");
//                startActivityForResult(intent, RESULT_CODE_COMPRESS_VIDEO);
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_CODE_COMPRESS_VIDEO);
            }
        });


    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if (resCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (reqCode == RESULT_CODE_COMPRESS_VIDEO) {
                if (uri != null) {
                    Cursor cursor = getContentResolver().query(uri, null, null, null, null, null);

                    try {
                        if (cursor != null && cursor.moveToFirst()) {

                            String displayName = cursor.getString(
                                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            Log.i(TAG, "Display Name: " + displayName);

                            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                            String size = null;
                            if (!cursor.isNull(sizeIndex)) {
                                size = cursor.getString(sizeIndex);
                            } else {
                                size = "Unknown";
                            }
                            Log.i(TAG, "Size: " + size);

                            tempFile = FileUtils.saveTempFile(displayName, this, uri);
                            editText.setText(tempFile.getPath());

                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }
        } else if (resCode == RESULT_OK && reqCode == SELECT_VIDEO) {
//            if (requestCode == SELECT_VIDEO) {
                selectedVideoPath = getPath(data.getData());
                if (selectedVideoPath == null) {
//                        Log.e("selected video path = null!");
                    finish();
                } else {
                    /**
                     * try to do something there
                     * selectedVideoPath is path to the selected video
                     */
                    // File or Blob
                    File file = new File(selectedVideoPath);
                    FirebaseStorageManager.UploadFile(file, "test12345.mp4", new FirebaseStorageManager.UploadListener() {
                        @Override
                        public void onComplete(boolean isSuccess, String uploadedFileUrl) {
                            Log.d("fileupload",isSuccess + "," + uploadedFileUrl);
                        }
                    });
                }
//            }
            finish();
        }
    }

    private void deleteTempFile(){
        if(tempFile != null && tempFile.exists()){
            tempFile.delete();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteTempFile();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteTempFile();
    }

    public void compress(View v) {
//        MediaController.getInstance().scheduleVideoConvert(tempFile.getPath());
        new VideoCompressor().execute();
    }

    class VideoCompressor extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            Log.d(TAG,"Start video compression");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return MediaController.getInstance().convertVideo(tempFile.getPath());
        }

        @Override
        protected void onPostExecute(Boolean compressed) {
            super.onPostExecute(compressed);
            progressBar.setVisibility(View.GONE);
            if(compressed){
                Log.d(TAG,"Compression successfully!");
//                upload();
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }

//    public void upload() {
//        Intent i = new Intent(Intent.ACTION_SEND, FileProvider.getUriForFile(getApplicationContext(), "com.aps.apsschool.fileprovider", MediaController.finalFile));
//        startActivityForResult(i, SELECT_VIDEO);
////        Intent intent = new Intent(this, UploadLectures.class);
////        intent.putExtra("videoURI", FileProvider.getUriForFile(getApplicationContext(), "com.aps.apsschool.fileprovider", MediaController.finalFile).toString());
////        startActivityForResult(intent, SELECT_VIDEO);
//    }
}