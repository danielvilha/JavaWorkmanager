package com.danielvilha.javaworkmanager.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.danielvilha.javaworkmanager.workers.BlurWorker;
import com.danielvilha.javaworkmanager.workers.CleanupWorker;
import com.danielvilha.javaworkmanager.workers.SaveImageToFileWorker;

import java.util.List;

import static com.danielvilha.javaworkmanager.Constants.IMAGE_MANIPULATION_WORK_NAME;
import static com.danielvilha.javaworkmanager.Constants.KEY_IMAGE_URI;
import static com.danielvilha.javaworkmanager.Constants.TAG_OUTPUT;

/**
 * Created by danielvilha on 2019-08-18
 */
public class BlurViewModel extends AndroidViewModel {

    //region Variables
    private Uri mImageUri;
    private Uri mOutputUri;
    private WorkManager mWorkManager;
    private LiveData<List<WorkInfo>> mSavedWorkInfo;
    //endregion

    //region BlurViewModel
    public BlurViewModel(@NonNull Application application) {
        super(application);
        mWorkManager = WorkManager.getInstance(application);

        // This transformation makes sure that whenever the current work Id changes the WorkInfo
        // the UI is listening to changes
        mSavedWorkInfo = mWorkManager.getWorkInfosByTagLiveData(TAG_OUTPUT);
    }
    //endregion

    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    //region applyBlur
    public void applyBlur(int blurLevel) {
        // Add WorkRequest to Cleanup temporary images
        WorkContinuation continuation = mWorkManager
                .beginUniqueWork(IMAGE_MANIPULATION_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        OneTimeWorkRequest.from(CleanupWorker.class));

        // Add WorkRequests to blur the image the number of times requested
        for (int i = 0; i < blurLevel; i++) {
            OneTimeWorkRequest.Builder blurBuilder =
                    new OneTimeWorkRequest.Builder(BlurWorker.class);

            // Input the Uri if this is the first blur operation
            // After the first blur operation the input will be the output of previous
            // blur operations.
            if ( i == 0 ) {
                blurBuilder.setInputData(createInputDataForUri());
            }

            continuation = continuation.then(blurBuilder.build());
        }

        // Create charging constraint
        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(true)
                .build();

        // Add WorkRequest to save the image to the filesystem
        OneTimeWorkRequest save = new OneTimeWorkRequest.Builder(SaveImageToFileWorker.class)
                .setConstraints(constraints) // This adds the Constraints
                .addTag(TAG_OUTPUT)
                .build();

        continuation = continuation.then(save);

        // Actually start the work
        continuation.enqueue();
    }
    //endregion

    //region uriOrNull
    private Uri uriOrNull(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }
    //endregion

    /**
     * Creates the input data bundle which includes the Uri to operate on
     * @return Data which contains the Image Uri as a String
     */
    //region createInputDataForUri
    private Data createInputDataForUri() {
        Data.Builder builder = new Data.Builder();
        if (mImageUri != null) {
            builder.putString(KEY_IMAGE_URI, mImageUri.toString());
        }
        return builder.build();
    }
    //endregion

    /**
     * Setters
     */
    //region setImageUri
    public void setImageUri(String uri) {
        mImageUri = uriOrNull(uri);
    }
    //endregion

    /**
     * Getters
     */
    //region getImageUri
    public Uri getImageUri() {
        return mImageUri;
    }
    //endregion

    //region getOutputWorkInfo
    public LiveData<List<WorkInfo>> getOutputWorkInfo() { return mSavedWorkInfo; }
    //endregion

    // Add a getter and setter for mOutputUri
    //region setOutputUri
    public void setOutputUri(String outputImageUri) {
        mOutputUri = uriOrNull(outputImageUri);
    }
    //endregion

    //region getOutputUri
    public Uri getOutputUri() { return mOutputUri; }
    //endregion

    /**
     * Cancel work using the work's unique name
     */
    //region cancelWork
    public void cancelWork() {
        mWorkManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME);
    }
    //endregion
}
