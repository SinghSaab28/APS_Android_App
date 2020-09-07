package com.aps.apsschool.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aps.apsschool.beans.Course;
import com.aps.apsschool.beans.Student;
import com.aps.apsschool.database.DBActivities;
import com.aps.apsschool.staticutilities.StaticUtilities;
import com.aps.apsschool.user.R;
import com.aps.apsschool.user.UserLogin;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SelectCourse extends AppCompatActivity {

    private static List<Course> COURSES = new ArrayList<>();
    final FirebaseFirestore db = StaticUtilities.DB;
    public static String VIDEO_SELECTED = "";
    public static Boolean SELECTED_VIDEO_AVAILABILITY = false;
    private TextView countDownTimer;
    private ImageView logOut;
    private Context ctx;
    private TextView subject_id;
    private TextView noCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_course);
        ctx = this;
        db.collection("students").whereEqualTo("ROLLNUMBER", (new DBActivities(getApplicationContext()).getStudentRollNo()))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.isSuccessful() && !task.getResult().isEmpty()) {
                                List<Student> students = task.getResult().toObjects(Student.class);
                                if(students.get(0).PERMISSION==false) {
                                    updateLoginFlag();
                                    Intent i = new Intent(SelectCourse.this, UserLogin.class);
                                    startActivity(i);
                                    finish();
                                }
                            }
                        }
                    }
                });
        logOut = findViewById(R.id.log_out);
        subject_id = findViewById(R.id.subject_id);
        subject_id.setText(SelectSubject.SUBJECT_SELECTED);
        noCourse = findViewById(R.id.noCourse);
        DBActivities dbActivities = new DBActivities(getApplicationContext());
        System.out.println("Subject selected : "+ SelectSubject.SUBJECT_SELECTED);
        System.out.println("Student Class : "+dbActivities.getStudentClass());
        db.collection("courses")
                .whereEqualTo("CLASS",dbActivities.getStudentClass())
                .whereEqualTo("SUBJECT", SelectSubject.SUBJECT_SELECTED)
                .whereEqualTo("MEDIUM", dbActivities.getStudentMedium())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("SelectCourse", document.getId() + " => " + document.getData());
                            }
                            List<Course> courses = task.getResult().toObjects(Course.class);
                            System.out.println("No of Courses present : "+courses.size());
                            if(courses.size()==0){
                                createNewTextField();
                            }else {
                                for (int i = 0; i < courses.size(); i++) {
                                    Course course = courses.get(i);
                                    if(course.DATE_TIME_OF_AVAILABILITY.compareTo(new Date())<=0) {
                                        noCourse.setVisibility(View.GONE);
                                        COURSES.add(course);
                                        createNewButton(course);
                                    }
                                }
                            }
                        } else {
                            Log.w("SelectCourse", "Error getting documents."+task.getException(), task.getException());
                            createNewTextField();
                        }
                    }
                });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                deleteDBTables(ctx);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setMessage("Are you sure you want to log-out?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
    }

    private void createNewTextField() {
        TextView tv = findViewById(R.id.noCourse);
        tv.setVisibility(View.VISIBLE);
    }

    private void createNewButton(final Course course) {
        ViewGroup container = (ViewGroup)findViewById(R.id.rootCourseLayout);
        final View myLayout = getLayoutInflater().inflate(R.layout.course_view, null);
        countDownTimer = myLayout.findViewById(R.id.countDownTimer);
        TextView chapter_name = myLayout.findViewById(R.id.chapter_name);
        chapter_name.setText("Title : "+course.TITLE);
        chapter_name.setTextColor(Color.parseColor("#ffffff"));
        chapter_name.setTextSize(20);

        TextView desc = myLayout.findViewById(R.id.description);
        desc.setText("Description : "+course.CHAPTER_DESCRIPTION);
        desc.setTextColor(Color.parseColor("#ffffff"));
        desc.setTextSize(15);

        TextView postedBy = myLayout.findViewById(R.id.posted_by);
        postedBy.setText("Posted By : "+course.POSTED_BY);
        postedBy.setTextColor(Color.parseColor("#ffffff"));
        postedBy.setTextSize(15);

        Date availabilityDate = course.DATE_TIME_OF_AVAILABILITY;
        Calendar start_calendar = Calendar.getInstance();
        Calendar end_calendar;

        // convert date to calendar
        Calendar c = Calendar.getInstance();
        c.setTime(availabilityDate);

        // manipulate date
        c.add(Calendar.DATE, 2); //same with c.add(Calendar.DAY_OF_MONTH, 1);

        // convert calendar to date
        end_calendar = c;
        Date currentDatePlusTwo = c.getTime();
        System.out.println("Date & Time Plus 2 days : "+currentDatePlusTwo);
        long start_millis = start_calendar.getTimeInMillis(); //get the start time in milliseconds
        long end_millis = end_calendar.getTimeInMillis(); //get the end time in milliseconds
        long total_millis = (end_millis - start_millis); //total time in milliseconds
        CountDownTimer cdt = new CountDownTimer(total_millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                millisUntilFinished -= TimeUnit.DAYS.toMillis(days);

                long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                millisUntilFinished -= TimeUnit.HOURS.toMillis(hours);

                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes);

                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
                if(days>0){
                    countDownTimer.setText("Expires in : "+ days + "Days " + hours + "Hrs");
                    countDownTimer.setTextColor(Color.parseColor("#00FF00"));
                } else if(hours>0){
                    countDownTimer.setText("Expires in : "+ hours + "Hrs " + minutes + "Mins");
                    countDownTimer.setTextColor(Color.parseColor("#FF7F50"));
                } else if (minutes>0){
                    countDownTimer.setText("Expires in : "+ minutes + "Mins " + seconds + "Secs");
                }
            }

            @Override
            public void onFinish() {
                countDownTimer.setText("Course Expired!");
            }
        };
        cdt.start();
        myLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCourseClick(course);
                System.out.println("Course URL : "+course.VIDEO_NAME);
            }
        });
        container.addView(myLayout); // you can pass extra layout params here too
    }

    private void onCourseClick(Course course) {
        VIDEO_SELECTED = course.VIDEO_NAME;
        Date currentDateTime = Calendar.getInstance().getTime();
        Log.d("SelectCourse", "onCourseClick: currentDateTime = "+currentDateTime);
        Date availabilityDate = course.DATE_TIME_OF_AVAILABILITY;
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zzzz");
        if(currentDateTime.compareTo(availabilityDate) >= 0) {
            SELECTED_VIDEO_AVAILABILITY=true;
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }else{
            Toast toast = Toast.makeText(getApplicationContext(), "This video is unavailable right now. Please try opening once it is available...", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, SelectSubject.class);
        startActivity(i);
        finish();
    }

    private void deleteDBTables(Context ctx){
        updateLoginFlag();
        DBActivities dbActivities = new DBActivities(getApplicationContext());
        dbActivities.deleteSubjectTable(ctx);
        dbActivities.deleteStudentTable(ctx);
        Intent i = new Intent(this, UserLogin.class);
        startActivity(i);
        finish();
    }

    private void updateLoginFlag() {
        db.collection("students").whereEqualTo("ROLLNUMBER", (new DBActivities(getApplicationContext())).getStudentRollNo())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().size()==1) {
                                System.out.println("jagga");
                                for (DocumentSnapshot document : task.getResult()) {
                                    String id = document.getId();
                                    db.collection("students").document(id).update("LOGIN_FLAG", false) //Set student object
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(SelectCourse.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                                    Log.d("SelectCourse", "updateLoginFlag : LOGIN_FLAG updated to false.");
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }
}