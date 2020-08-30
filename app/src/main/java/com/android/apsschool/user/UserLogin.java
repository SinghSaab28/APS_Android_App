package com.android.apsschool.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.apsschool.beans.Student;
import com.android.apsschool.database.DBActivities;
import com.android.apsschool.home.SelectSubject;
import com.android.apsschool.staticutilities.StaticUtilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class UserLogin extends AppCompatActivity {

    private EditText password;
    private EditText rollno;
    final FirebaseFirestore db = StaticUtilities.DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        password = findViewById(R.id.pwd);
        rollno = findViewById(R.id.rollno);
    }

    public void login(View v) {
        db.collection("students").whereEqualTo("ROLLNUMBER", rollno.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DBActivities dbActivities = new DBActivities(getApplicationContext());
                            List<Student> students = task.getResult().toObjects(Student.class);
                            if(students.get(0).PERMISSION==true) {
                                if (students.get(0).LOGIN_FLAG == false) {
                                    if (password.getText().toString().equals(students.get(0).PASSWORD)) {
                                        updateLoginFlag();
                                        Log.d("UserLogin", "onComplete: " + students.get(0).NAME);
                                        if (!dbActivities.verifyStudent(rollno.getText().toString())) {
                                            dbActivities.addStudent(getApplicationContext(), students.get(0));
                                        }
                                        Intent i = new Intent(getApplicationContext(), SelectSubject.class);
                                        startActivity(i);
                                        finish();
                                    } else {
                                        Log.d("UserLogin", "login : Password is incorrect for : " + students.get(0).ROLLNUMBER);
                                        Toast toast = Toast.makeText(getApplicationContext(), "Roll number or Password incorrect. Please try again with correct credentials...", Toast.LENGTH_LONG);
                                        toast.show();
                                    }
                                } else {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Only one device is allowed to login. This user is already logged in...", Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            }else{
                                Toast toast = Toast.makeText(getApplicationContext(), "You are not permitted to login. Please contact school admin...", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        } else {
                            Log.w("UserLogin", "Error getting documents.", task.getException());
                            Log.d("UserLogin", "login : Roll number is incorrect.");
                            Toast toast = Toast.makeText(getApplicationContext(), "Roll number or Password incorrect. Please try again with correct credentials...", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
    }

    private void updateLoginFlag() {
        db.collection("students").whereEqualTo("ROLLNUMBER", rollno.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().size()==1) {
                        System.out.println("jagga");
                        for (DocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            db.collection("students").document(id).update("LOGIN_FLAG", true) //Set student object
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(UserLogin.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                            Log.d("UserLogin", "updateLoginFlag : LOGIN_FLAG updated to true.");
                                        }
                                    });
                        }
                    }
                }
            }
        });
    }
}