package com.android.apsschool.uninstall;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.apsschool.database.DBActivities;
import com.android.apsschool.staticutilities.StaticUtilities;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

class ListenActivities extends Thread{
    boolean exit = false;
    ActivityManager am = null;
    Context context = null;

    public ListenActivities(Context con){
        context = con;
        am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    public void run(){

        Looper.prepare();

        while(!exit){

            // get the info from the currently running task
            List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(MAX_PRIORITY);

            String activityName = taskInfo.get(0).topActivity.getClassName();


            Log.d("topActivity", "CURRENT Activity :: " + activityName);

            if (activityName.equals("com.android.packageinstaller.UninstallerActivity")) {
                // User has clicked on the Uninstall button under the Manage Apps settings

                //do whatever pre-uninstallation task you want to perform here
                // show dialogue or start another activity or database operations etc..etc..
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                final FirebaseFirestore db = StaticUtilities.DB;
                                DocumentReference student = db.collection("students").document((new DBActivities(context)).getStudentRollNo());
                                student.update("LOGIN_FLAG", false)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                                Log.d("SelectSubject", "updateLoginFlag : LOGIN_FLAG updated to false.");
                                            }
                                        });
                                DBActivities dbActivities = new DBActivities(context);
                                dbActivities.deleteStudentTable(context);
                                dbActivities.deleteSubjectTable(context);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to log-out?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

//                context.startActivity(new Intent(context, MyPreUninstallationMsgActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                exit = true;
                Toast.makeText(context, "Done with preuninstallation tasks... Exiting Now", Toast.LENGTH_SHORT).show();
            } else if(activityName.equals("com.android.settings.ManageApplications")) {
                // back button was pressed and the user has been taken back to Manage Applications window
                // we should close the activity monitoring now
                exit=true;
            }
        }
        Looper.loop();
    }
}
