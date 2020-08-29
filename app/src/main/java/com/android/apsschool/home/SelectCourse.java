package com.android.apsschool.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.apsschool.beans.Course;
import com.android.apsschool.database.DBActivities;
import com.android.apsschool.staticutilities.StaticUtilities;
import com.android.apsschool.user.R;
import com.android.apsschool.user.UserLogin;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SelectCourse extends AppCompatActivity {

    private static List<Course> COURSES = new ArrayList<>();
    final FirebaseFirestore db = StaticUtilities.DB;
    private static final String ADMOB_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110";
    private UnifiedNativeAd nativeAd;
    public static String VIDEO_SELECTED = "";
    public static Boolean SELECTED_VIDEO_AVAILABILITY = false;
    private TextView countDownTimer;
    private ImageView logOut;
    private FrameLayout fl;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_course);
        ctx = this;
        logOut = findViewById(R.id.log_out);
        DBActivities dbActivities = new DBActivities(getApplicationContext());
        System.out.println("Subject selected : "+SelectSubject.SUBJECT_SELECTED);
        System.out.println("Student Class : "+dbActivities.getStudentClass());
        db.collection("courses").whereEqualTo("CLASS",dbActivities.getStudentClass()).whereEqualTo("SUBJECT", SelectSubject.SUBJECT_SELECTED)
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
                                    COURSES.add(course);
                                    decideVideoPlacement(i, course, courses.size()-1);
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

        MobileAds.initialize(getApplicationContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
    }

    private void createNewTextField() {
        TextView tv = findViewById(R.id.noCourse);
        tv.setVisibility(View.VISIBLE);
    }

    private void decideVideoPlacement(int i, final Course course, Integer nOfCourses) {
        if(i<nOfCourses){
            createNewButton(course);
        }else if(i==nOfCourses){
            createNewButton(course);
            LinearLayout layout = findViewById(R.id.rootCourseLayout);
            fl = new FrameLayout(getApplicationContext());
            LinearLayout.LayoutParams flparams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            flparams.setMargins(0, 50, 0, 0);
            fl.setLayoutParams(flparams);
            layout.addView(fl);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    refreshAd();
                }
            }, 5000);
            refreshAd();
        }
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

        String availabilityDate = course.DATE_TIME_OF_AVAILABILITY;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy;HH:mm");
        try {
            Date date = format.parse(availabilityDate);
            System.out.println("Date & Time : "+date);

            Calendar start_calendar = Calendar.getInstance();
            Calendar end_calendar;

            // convert date to calendar
            Calendar c = Calendar.getInstance();
            c.setTime(date);

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
//                    countDownTimer.setText(days + ":" + hours + ":" + minutes + ":" + seconds); //You can compute the millisUntilFinished on hours/minutes/seconds
                }

                @Override
                public void onFinish() {
                    countDownTimer.setText("Course Expired!");
                }
            };
            cdt.start();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        String availabilityDateStr = course.DATE_TIME_OF_AVAILABILITY;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy;HH:mm");
        Date availabilityDate = null;
        try {
            availabilityDate = format.parse(availabilityDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        Intent i = new Intent(this,SelectSubject.class);
        startActivity(i);
        finish();
    }

    private void deleteDBTables(Context ctx){
        updateLoginFlag();
        DBActivities dbActivities = new DBActivities(getApplicationContext());
        dbActivities.deleteSubjectTable(ctx);
        dbActivities.deleteStudentTable(ctx);
    }

    private void updateLoginFlag() {
        DocumentReference student = db.collection("students").document((new DBActivities(getApplicationContext())).getStudentRollNo());
        student.update("LOGIN_FLAG", false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SelectCourse.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                        Log.d("SelectCourse", "updateLoginFlag : LOGIN_FLAG updated to false.");
                    }
                });
        Intent i = new Intent(this, UserLogin.class);
        startActivity(i);
        finish();
    }

    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        // Set the media view.
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        VideoController vc = nativeAd.getVideoController();

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {

            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
                    super.onVideoEnd();
                }
            });
        }
    }

    /**
     * Creates a request for a new native ad based on the boolean parameters and calls the
     * corresponding "populate" method when one is successfully returned.
     *
     */
    private void refreshAd() {
        AdLoader.Builder builder = new AdLoader.Builder(getApplicationContext(), ADMOB_AD_UNIT_ID);

        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            // OnUnifiedNativeAdLoadedListener implementation.
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                // You must call destroy on old ads when you are done with them,
                // otherwise you will have a memory leak.
                if (nativeAd != null) {
                    nativeAd.destroy();
                }
                nativeAd = unifiedNativeAd;
                FrameLayout frameLayout = fl;
                UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater()
                        .inflate(R.layout.ad_unified, null);
                populateUnifiedNativeAdView(unifiedNativeAd, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);
            }

        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Toast.makeText(getApplicationContext(), "Failed to load native ad: "
                        + errorCode, Toast.LENGTH_SHORT).show();
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onDestroy() {
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        super.onDestroy();
    }
}