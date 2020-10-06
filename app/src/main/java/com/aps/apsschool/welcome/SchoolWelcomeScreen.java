package com.aps.apsschool.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aps.apsschool.Dashboard;
import com.aps.apsschool.beans.Student;
import com.aps.apsschool.database.DBActivities;
import com.aps.apsschool.home.SelectSubject;
import com.aps.apsschool.staticutilities.StaticUtilities;
import com.aps.apsschool.user.R;
import com.aps.apsschool.user.UserLogin;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class SchoolWelcomeScreen extends AppCompatActivity {

    private  static Student STUDENT = null;
    private FirebaseAuth mAuth;
    private DBActivities dbActivities = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_welcome_screen);
        dbActivities = new DBActivities(getApplicationContext());
        mAuth = StaticUtilities.mAuth;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if(currentUser!=null && currentUser.getUid()!=null) {
                    Intent i = new Intent(getApplicationContext(), Dashboard.class);
                    startActivity(i);
                    finish();
                }else if((currentUser==null || currentUser.getUid()==null) && (dbActivities.getStudentRollNo()==null || dbActivities.getStudentRollNo().equalsIgnoreCase(""))){
                    Intent i = new Intent(getApplicationContext(), TeacherOrStudent.class);
                    startActivity(i);
                    finish();
                }else {
                    checkUserInLocalDB();
                }
            }
        }, 2000);
    }

    private void checkUserInLocalDB(){
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
                                    if(STUDENT.PERMISSION==true) {
                                        if(STUDENT.LOGIN_FLAG==true){
                                                Intent i = new Intent(getApplicationContext(), SelectSubject.class);
                                                startActivity(i);
                                                finish();
                                        } else {
                                            divertToUserLogin();
                                        }
                                    }else{
                                        Toast toast = Toast.makeText(getApplicationContext(), "You are restricted from login permissions. Please contact school admin...", Toast.LENGTH_LONG);
                                        toast.show();
                                        divertToUserLogin();
                                    }
                                } else {
                                    divertToUserLogin();
                                }
                            }
                        });
            } else {
                divertToUserLogin();
            }
        } catch (Exception e){
            divertToUserLogin();
        }
    }

    private void divertToUserLogin(){
        Intent i = new Intent(getApplicationContext(), UserLogin.class);
        startActivity(i);
        finish();
    }
}