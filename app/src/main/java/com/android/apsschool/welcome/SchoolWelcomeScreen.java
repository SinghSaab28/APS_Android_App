package com.android.apsschool.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.apsschool.beans.Student;
import com.android.apsschool.database.DBActivities;
import com.android.apsschool.home.SelectSubject;
import com.android.apsschool.staticutilities.StaticUtilities;
import com.android.apsschool.user.R;
import com.android.apsschool.user.UserLogin;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class SchoolWelcomeScreen extends AppCompatActivity {

    private  static Student STUDENT = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_welcome_screen);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUserInLocalDB();
            }
        }, 2000);
    }

    private void checkUserInLocalDB(){
        DBActivities dbActivities = new DBActivities(getApplicationContext());
        try {
            if (dbActivities.getStudentRollNo() != null &&
                    !dbActivities.getStudentRollNo().equalsIgnoreCase("") &&
                    dbActivities.verifyStudent(dbActivities.getStudentRollNo())) {
                FirebaseFirestore db = StaticUtilities.DB;
                db.collection("students").whereEqualTo("ROLLNUMBER", dbActivities.getStudentRollNo())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    List<Student> students = task.getResult().toObjects(Student.class);
                                    STUDENT = students.get(0);
                                    if(STUDENT.LOGIN_FLAG==true){
                                        Log.d("SchoolWelcomeScreen", "onCreate: Student login flag = true");
                                        Intent i = new Intent(getApplicationContext(), SelectSubject.class);
                                        startActivity(i);
                                        finish();
                                    } else {
                                        Log.d("SchoolWelcomeScreen", "onCreate: Diverted to Login Page : 1");
                                        divertToUserLogin();
                                    }
                                } else {
                                    Log.d("SchoolWelcomeScreen", "onCreate: Diverted to Login Page : 2");
                                    divertToUserLogin();
                                }
                            }
                        });
            } else {
                Log.d("SchoolWelcomeScreen", "onCreate: Diverted to Login Page : 3");
                divertToUserLogin();
            }
        } catch (Exception e){
            Log.d("SchoolWelcomeScreen", "onCreate: Diverted to Login Page : 4");
            divertToUserLogin();
        }
    }

    private void divertToUserLogin(){
        Intent i = new Intent(getApplicationContext(), UserLogin.class);
        startActivity(i);
        finish();
    }
}