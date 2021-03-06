package com.aps.apsschool.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aps.apsschool.beans.Student;
import com.aps.apsschool.database.DBActivities;
import com.aps.apsschool.home.SelectSubject;
import com.aps.apsschool.staticutilities.StaticUtilities;
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
    private Button button_login;
    final FirebaseFirestore db = StaticUtilities.DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        password = findViewById(R.id.pwd);
        rollno = findViewById(R.id.rollno);
        button_login = findViewById(R.id.button_login);
    }

    public void login(View v) {
        button_login.setBackgroundResource(R.drawable.button_clicked);
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
                                        if (!dbActivities.verifyStudent(rollno.getText().toString())) {
                                            dbActivities.addStudent(getApplicationContext(), students.get(0));
                                        }
                                        Intent i = new Intent(getApplicationContext(), SelectSubject.class);
                                        startActivity(i);
                                        finish();
                                    } else {
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
                        for (DocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            db.collection("students").document(id).update("LOGIN_FLAG", true) //Set student object
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(UserLogin.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }
            }
        });
    }
}