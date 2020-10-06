package com.aps.apsschool;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aps.apsschool.beans.Course;
import com.aps.apsschool.staticutilities.StaticUtilities;
import com.aps.apsschool.upload.UploadLectureDetails;
import com.aps.apsschool.user.R;
import com.aps.apsschool.welcome.TeacherOrStudent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class Dashboard extends AppCompatActivity {

    final FirebaseFirestore db = StaticUtilities.DB;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        mAuth = StaticUtilities.mAuth;
        db.collection("courses")
                .whereEqualTo("OWNER",mAuth.getCurrentUser().getUid())
                .orderBy("DATE_TIME_OF_AVAILABILITY")
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
                            if(courses.size()>0){
                                ViewGroup container = (ViewGroup)findViewById(R.id.rootLectureUploadsLayout);
                                View myLayout = null;
                                for (int i = 0; i < courses.size(); i++) {
                                    myLayout = getLayoutInflater().inflate(R.layout.uploaded_lectures_layout, null);
                                    Course course = courses.get(i);
                                    TextView title = myLayout.findViewById(R.id.title);
                                    title.setText("Title : "+course.TITLE);
                                    TextView standard = myLayout.findViewById(R.id.standard);
                                    standard.setText("Class : "+course.CLASS);
                                    TextView subject = myLayout.findViewById(R.id.subject);
                                    subject.setText("Subject : "+course.SUBJECT);
                                    TextView posted_by = myLayout.findViewById(R.id.posted_by);
                                    posted_by.setText("Posted By : "+course.POSTED_BY);
                                    TextView created_at = myLayout.findViewById(R.id.created_at);
                                    created_at.setText("Created at : "+course.CREATED_AT);
                                    TextView avl_dt_time = myLayout.findViewById(R.id.avl_dt_time);
                                    avl_dt_time.setText("Availability Date/Time : "+course.DATE_TIME_OF_AVAILABILITY);
                                    container.addView(myLayout);
                                }
                            }
                        } else {
                            Log.w("SelectCourse", "Error getting documents."+task.getException(), task.getException());
                        }
                    }
                });
    }

    public void goToUploads(View view) {
        Intent i = new Intent(Dashboard.this, UploadLectureDetails.class);
        startActivity(i);
        finish();
    }

    public void logout(View view) {
        mAuth.signOut();
        Intent i = new Intent(Dashboard.this, TeacherOrStudent.class);
        startActivity(i);
        finish();
    }
}