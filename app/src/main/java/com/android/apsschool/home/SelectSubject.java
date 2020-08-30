package com.android.apsschool.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.apsschool.beans.Division;
import com.android.apsschool.database.DBActivities;
import com.android.apsschool.staticutilities.StaticUtilities;
import com.android.apsschool.user.R;
import com.android.apsschool.user.UserLogin;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
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
    private FrameLayout fl;
    public static String SUBJECT_SELECTED = "";
    private List<String> arr = new ArrayList<>();
    private static final String ADMOB_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110";
    private UnifiedNativeAd nativeAd;
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
        fl = findViewById(R.id.fl_adplaceholder);
        logOut = findViewById(R.id.log_out);
        pb = findViewById(R.id.progressBar);
        pb.setVisibility(View.VISIBLE);
        DBActivities dbActivities = new DBActivities(getApplicationContext());
        List<String> subjectList = dbActivities.getSubjectList();
        if(subjectList.size()>0){
            System.out.println("if me andr aaya");
//            for (String sub:subjectList) {
//                arr.add(sub);
//            }
            arr = subjectList;
        }else {
            System.out.println("else me andr aaya");
            db.collection("class").whereEqualTo("CLASS", dbActivities.getStudentClass())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                System.out.println("Firestore DB if me aaya");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("SelectSubject", document.getId() + " => " + document.getData());
                                }
                                List<Division> divisions = task.getResult().toObjects(Division.class);
//                                for (String sub: divisions.get(0).SUBJECTS) {
//                                    arr.add(sub);
//                                }
                                arr = divisions.get(0).SUBJECTS;
                                DBActivities dbActivities = new DBActivities(getApplicationContext());
                                dbActivities.addSubjects(getApplicationContext(), arr);
                            } else {
                                System.out.println("Firestore DB else me aaya");
                                Log.w("SelectSubject", "Error getting documents.", task.getException());
                            }
                        }
                    });
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("array Size : "+arr.size());
                pb.setVisibility(View.GONE);
                for (int i = 0; i < arr.size(); i++) {
                    createNewButton(i, arr.get(i), arr.size()-1);
                }
            }
        }, 2000);

        class_id = findViewById(R.id.class_id);
        class_id.setText("Class : "+dbActivities.getStudentClass());

//        MobileAds.initialize(getApplicationContext(), new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {
//            }
//        });
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                refreshAd();
//            }
//        }, 60000);
//        refreshAd();

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

    }

    private void createNewButton(int i, String btnText, Integer noOfSubjects) {
//        if(i<noOfSubjects) {
            LinearLayout layout = findViewById(R.id.rootLayout);
            newBtn = new Button(this);
            newBtn.setId(i);
            newBtn.setText(btnText);
            newBtn.setBackgroundResource(R.drawable.active_button);
            newBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBtnClick(view.getId());
                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    500, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 50, 0, 0);
            newBtn.setLayoutParams(params);
            layout.addView(newBtn);
//        }else if(i==noOfSubjects){
//            LinearLayout layout = findViewById(R.id.rootLayout);
//            newBtn = new Button(this);
//            newBtn.setId(i);
//            newBtn.setText(btnText);
//            newBtn.setBackgroundResource(R.drawable.active_button);
//            newBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    onBtnClick(view.getId());
//                }
//            });
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    500, LinearLayout.LayoutParams.WRAP_CONTENT);
//            params.setMargins(0, 50, 0, 0);
//            newBtn.setLayoutParams(params);
//            layout.addView(newBtn);
//
//            fl = new FrameLayout(getApplicationContext());
//            LinearLayout.LayoutParams flparams = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            flparams.setMargins(0, 50, 0, 0);
//            fl.setLayoutParams(flparams);
//            layout.addView(fl);
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                public void run() {
//                    refreshAd();
//                }
//            }, 5000);
//            refreshAd();
//        }
    }

    public void onBtnClick(int i){
        SUBJECT_SELECTED=arr.get(i);
        Intent intent = new Intent(getApplicationContext(), SelectCourse.class);
        startActivity(intent);
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

    public void logout(View view) {
        confirmationAlert();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        confirmationAlert();
    }

    private void deleteDBTables(Context ctx){
        updateLoginFlag();
        DBActivities dbActivities = new DBActivities(getApplicationContext());
        dbActivities.deleteSubjectTable(ctx);
        dbActivities.deleteStudentTable(ctx);
    }

    private void confirmationAlert(){

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

    private void updateLoginFlag() {
        System.out.println("updateLoginFlag me aaya");
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
                                                    Toast.makeText(SelectSubject.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                                    Log.d("SelectSubject", "updateLoginFlag : LOGIN_FLAG updated to false.");
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
        Intent i = new Intent(this, UserLogin.class);
        startActivity(i);
        finish();
    }


}