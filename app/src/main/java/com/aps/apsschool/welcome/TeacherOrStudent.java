package com.aps.apsschool.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.aps.apsschool.user.R;
import com.aps.apsschool.user.TeacherLogin;
import com.aps.apsschool.user.UserLogin;

public class TeacherOrStudent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_or_student);
    }

    public void student(View view) {
        Intent i = new Intent(TeacherOrStudent.this, UserLogin.class);
        startActivity(i);
        finish();
    }

    public void teacher(View view) {
        Intent i = new Intent(TeacherOrStudent.this, TeacherLogin.class);
        startActivity(i);
        finish();
    }
}