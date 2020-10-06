package com.aps.apsschool.upload;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aps.apsschool.Dashboard;
import com.aps.apsschool.staticutilities.StaticUtilities;
import com.aps.apsschool.upload.file.FileUtils;
import com.aps.apsschool.upload.video.MediaController;
import com.aps.apsschool.user.R;
import com.aps.apsschool.welcome.TeacherOrStudent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UploadLectureDetails extends AppCompatActivity implements View.OnClickListener {

    private static final int SELECT_VIDEO = 1;
    private static final int RESULT_CODE_COMPRESS_VIDEO = 3;
    private static String selectedVideoPath;
    private static String selectedMedium;
    private static String selectedClass;
    private static String selectedSection;
    private static String selectedSubject;
    final String[] MEDIUM = {"Select Medium", "English", "Hindi"};
    final String[] CLASS = {"Select Class", "12Sci", "12Art", "12Com"};
    final String[] SECTION = {"Select Section", "A", "B", "D", "E", "GM", "GB"};
    final String[] SUBJECT = {"Select Subject", "Hindi", "English", "Physics", "Chemistry", "Biology", "Maths"};
    Spinner mediumSpinner;
    Spinner classSpinner;
    Spinner sectionSpinner;
    Spinner subjectSpinner;
    EditText titleText, descText;
    ProgressBar progressBar;
    TextView videoURI,txtDate, txtTime;
    public static TextView uploadProgressText;
    private int mYear, mMonth, mDay, mHour, mMinute, mAM_PM;
    final FirebaseFirestore db = StaticUtilities.DB;
    private File tempFile;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_lecture_details);
        mAuth = StaticUtilities.mAuth;
        mediumSpinner = (Spinner) findViewById(R.id.spinnerMedium);
        classSpinner = (Spinner) findViewById(R.id.spinnerClass);
        sectionSpinner = (Spinner) findViewById(R.id.spinnerSection);
        subjectSpinner = (Spinner) findViewById(R.id.spinnerSubject);
        progressBar = findViewById(R.id.progressBar);
        videoURI = findViewById(R.id.videoURI);
        titleText = findViewById(R.id.titleText);
        descText = findViewById(R.id.descText);
        uploadProgressText = findViewById(R.id.uploadProgressText);
        setMediumSpinner(mediumSpinner, MEDIUM);
        setClassSpinner(classSpinner, CLASS);
        setSectionSpinner(sectionSpinner, SECTION);
        setSubjectSpinner(subjectSpinner, SUBJECT);
        txtDate=findViewById(R.id.in_date);
        txtTime=findViewById(R.id.in_time);
        txtDate.setOnClickListener(this);
        txtTime.setOnClickListener(this);
    }

    private void setMediumSpinner(Spinner spinner, String[] MEDIUM){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, MEDIUM);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                selectedMedium = MEDIUM[position];
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setClassSpinner(Spinner spinner, String[] CLASS){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, CLASS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                selectedClass = CLASS[position];
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setSectionSpinner(Spinner spinner, String[] SECTION){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, SECTION);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                selectedSection = SECTION[position];
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setSubjectSpinner(Spinner spinner, String[] SUBJECT){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, SUBJECT);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                selectedSubject = SUBJECT[position];
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onClick(View v) {

        if (v == txtDate) {

            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            txtDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        if (v == txtTime) {

            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR);
            mMinute = c.get(Calendar.MINUTE);
            mAM_PM = c.get(Calendar.AM_PM);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            txtTime.setText(hourOfDay + ":" + minute);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
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

    public void selectVideo(View view) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_CODE_COMPRESS_VIDEO);
    }

    @ Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {

            Uri uri = data.getData();

            if (requestCode == RESULT_CODE_COMPRESS_VIDEO) {
                if (uri != null) {
                    Cursor cursor = getContentResolver().query(uri, null, null, null, null, null);

                    try {
                        if (cursor != null && cursor.moveToFirst()) {

                            String displayName = cursor.getString(
                                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            Log.i("UploadLectureDetails", "Display Name: " + displayName);

                            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                            String size = null;
                            if (!cursor.isNull(sizeIndex)) {
                                size = cursor.getString(sizeIndex);
                            } else {
                                size = "Unknown";
                            }
                            Log.i("UploadLectureDetails", "Size: " + size);
                            tempFile = FileUtils.saveTempFile(displayName, this, uri);
                            compress();
                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }
        }
    }

    public void upload(View view) {
            progressBar.setVisibility(View.VISIBLE);
            uploadProgressText.setVisibility(View.VISIBLE);
            // File or Blob
            File file = MediaController.finalFile;
            if (file != null) {
                FirebaseStorageManager.UploadFile(file, file.getName(), new FirebaseStorageManager.UploadListener() {
                    @Override
                    public void onComplete(boolean isSuccess, String uploadedFileUrl) {
                        Log.d("fileupload", isSuccess + "," + uploadedFileUrl);
                        Map<String, Object> lecture = new HashMap<>();
                        lecture.put("CLASS", selectedClass);
                        lecture.put("MEDIUM", selectedMedium);
                        lecture.put("SECTION", selectedSection);
                        lecture.put("SUBJECT", selectedSubject);
                        lecture.put("CHAPTER_DESCRIPTION", descText.getText().toString());
                        lecture.put("CREATED_AT", new Date(String.valueOf(Calendar.getInstance().getTime())));
                        SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy;HH:mm");
                        Date date = null;
                        try {
                            date = ft.parse(txtDate.getText().toString() + ";" + txtTime.getText().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        lecture.put("DATE_TIME_OF_AVAILABILITY", date);
                        lecture.put("POSTED_BY", mAuth.getCurrentUser().getDisplayName());
                        lecture.put("TITLE", titleText.getText().toString());
                        lecture.put("VIDEO_NAME", uploadedFileUrl);
                        lecture.put("OWNER", mAuth.getCurrentUser().getUid());

                        db.collection("courses").add(lecture)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d("UploadLectureDetails", "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("UploadLectureDetails", "Error writing document", e);
                                    }
                                });
                        progressBar.setVisibility(View.GONE);
                        uploadProgressText.setVisibility(View.GONE);
                        selectedMedium = null;
                        selectedClass = null;
                        selectedSection = null;
                        selectedSubject = null;
                        Intent i = new Intent(UploadLectureDetails.this, Dashboard.class);
                        startActivity(i);
                        finish();
                    }
                });
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "No lecture video provided, Please select a video before uploading...", Toast.LENGTH_LONG);
                toast.show();
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
        Intent i = new Intent(UploadLectureDetails.this, Dashboard.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteTempFile();
    }

    public void compress() {
        new VideoCompressor().execute();
    }

    public void goToDashboard(View view) {
        Intent i = new Intent(UploadLectureDetails.this, Dashboard.class);
        startActivity(i);
        finish();
    }

    class VideoCompressor extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            uploadProgressText.setVisibility(View.VISIBLE);
            uploadProgressText.setText("Video compression started");
            Log.d("UploadLectureDetails","Start video compression");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return MediaController.getInstance().convertVideo(tempFile.getPath());
        }

        @Override
        protected void onPostExecute(Boolean compressed) {
            super.onPostExecute(compressed);
            progressBar.setVisibility(View.GONE);
            uploadProgressText.setVisibility(View.GONE);
            if(compressed){
                Log.d("UploadLectureDetails","Compression successfully!");
                uploadProgressText.setText("Compression successful!");
                selectedVideoPath = MediaController.finalFile.getAbsolutePath()+MediaController.finalFile.getName();
                videoURI.setText(selectedVideoPath);
            }
        }
    }

    public void logout(View view) {
        mAuth.signOut();
        Intent i = new Intent(UploadLectureDetails.this, TeacherOrStudent.class);
        startActivity(i);
        finish();
    }
}