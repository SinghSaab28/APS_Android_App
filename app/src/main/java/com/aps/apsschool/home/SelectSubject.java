package com.aps.apsschool.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aps.apsschool.beans.Division;
import com.aps.apsschool.beans.Student;
import com.aps.apsschool.database.DBActivities;
import com.aps.apsschool.staticutilities.StaticUtilities;
import com.aps.apsschool.user.Blank;
import com.aps.apsschool.user.R;
import com.aps.apsschool.user.UserLogin;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelectSubject extends AppCompatActivity {

    private Button newBtn;
    public static String SUBJECT_SELECTED = "";
    private List<String> arr = new ArrayList<>();
    private TextView class_id;
    final FirebaseFirestore db = StaticUtilities.DB;
    private ProgressBar pb;
    private ImageView logOut;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_subject);
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
                                    Intent i = new Intent(SelectSubject.this, UserLogin.class);
                                    startActivity(i);
                                    finish();
                                }
                            }
                        }
                    }
                });
        logOut = findViewById(R.id.log_out);
        pb = findViewById(R.id.progressBar);
        pb.setVisibility(View.VISIBLE);
        DBActivities dbActivities = new DBActivities(getApplicationContext());
        List<String> subjectList = dbActivities.getSubjectList();
        if(subjectList.size()>0){
            arr = subjectList;
        }else {
            db.collection("class")
                    .whereEqualTo("CLASS", dbActivities.getStudentClass())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("SelectSubject", document.getId() + " => " + document.getData());
                                }
                                List<Division> divisions = task.getResult().toObjects(Division.class);
                                arr = divisions.get(0).SUBJECTS;
                                DBActivities dbActivities = new DBActivities(getApplicationContext());
                                dbActivities.addSubjects(getApplicationContext(), arr);
                            } else {
                                Log.w("SelectSubject", "Error getting documents.", task.getException());
                            }
                        }
                    });
        }
        if(subjectList.size()>0){
            pb.setVisibility(View.GONE);
            for (int i = 0; i < arr.size(); i++) {
                createNewButton(i, arr.get(i), arr.size()-1);
            }
        }else {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pb.setVisibility(View.GONE);
                    for (int i = 0; i < arr.size(); i++) {
                        createNewButton(i, arr.get(i), arr.size() - 1);
                    }
                }
            }, 2000);
        }

        class_id = findViewById(R.id.class_id);
        class_id.setText("Class : "+dbActivities.getStudentClass());

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

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ctx);
                builder.setMessage("Are you sure you want to log-out?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
    }

    private void createNewButton(int i, String btnText, Integer noOfSubjects) {
            LinearLayout layout = findViewById(R.id.rootLayout);
            newBtn = new Button(this);
            newBtn.setId(i);
            newBtn.setText(btnText);
            newBtn.setBackgroundResource(R.drawable.active_button);
            newBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    newBtn.setBackgroundResource(R.drawable.button_clicked);
                    onBtnClick(view.getId());
                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    500, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 50, 0, 0);
            newBtn.setLayoutParams(params);
            layout.addView(newBtn);

    }

    public void onBtnClick(int i){

        SUBJECT_SELECTED=arr.get(i);
        Intent intent = new Intent(getApplicationContext(), SelectCourse.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent launchNextActivity;
        launchNextActivity = new Intent(this, Blank.class);
//        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(launchNextActivity);
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

    private void confirmationAlert(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
//                        deleteDBTables(ctx);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage("Are you sure you want to log-out?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
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
                                                    Toast.makeText(SelectSubject.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }
}