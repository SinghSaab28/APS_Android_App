package com.aps.apsschool.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aps.apsschool.Dashboard;
import com.aps.apsschool.staticutilities.StaticUtilities;
import com.aps.apsschool.welcome.TeacherOrStudent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TeacherLogin extends AppCompatActivity {

    private EditText password;
    private EditText username;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_login);
        password = findViewById(R.id.pwd);
        username = findViewById(R.id.username);
        mAuth = StaticUtilities.mAuth;
    }

    public void login(View view) {
        mAuth.signInWithEmailAndPassword(username.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TeacherLogin", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.getUid();
                            Intent i = new Intent(getApplicationContext(), Dashboard.class);
                            startActivity(i);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TeacherLogin", "signInWithEmail:failure", task.getException());
                            Toast.makeText(TeacherLogin.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getApplicationContext(), TeacherLogin.class);
                            startActivity(i);
                            finish();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(getApplicationContext(), TeacherOrStudent.class);
        startActivity(i);
        finish();
    }
}