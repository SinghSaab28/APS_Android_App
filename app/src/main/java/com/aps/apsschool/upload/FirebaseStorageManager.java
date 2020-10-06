package com.aps.apsschool.upload;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


public class FirebaseStorageManager {
//XDAj8jtxZBMqgBW7NUV281p114i2
    public interface UploadListener {
        void onComplete(boolean isSuccess, String uploadedFileUrl);
    }

    public static void UploadFile(final File file, String serverFileName, final UploadListener completionHandler) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        // Create a reference to the file you want to upload
        String directory = "lectures/";
        final StorageReference fileRef = storageRef.child(directory + serverFileName);

        try {
            InputStream stream = new FileInputStream(file);
            UploadTask uploadTask = fileRef.putStream(stream);
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / file.length();
                    Log.d("MainActivity", "Upload is " + progress + "% done");
                    UploadLectureDetails.uploadProgressText.setText((int)progress + "% uploaded");
                    Log.d("MainActivity", String.format("onProgress: bytes=%d total=%d",
                            taskSnapshot.getBytesTransferred(), file.length()));
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("MainActivity", "Upload is paused");
                        }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.e("oops","error in file uploading");
                    completionHandler.onComplete(false, "");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            });
            // Now we need to download url of uploaded file using a separate task
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String stringUrl = downloadUri.toString();
                        completionHandler.onComplete(true, stringUrl);
                    } else {
                        // Handle failures
                        completionHandler.onComplete(false, "");
                    }
                }
            });
        }catch (Exception e){
            Log.e("FirebaseStorageManager", "UploadFile: ", e);
            completionHandler.onComplete(false, "");
        }
    }

}
