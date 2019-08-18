package com.danielvilha.javaworkmanager.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.danielvilha.javaworkmanager.Constants;

/**
 * Created by danielvilha on 2019-08-18
 */
public class BlurWorker extends Worker {

    //region Variables
    private static final String TAG = BlurWorker.class.getSimpleName();
    //endregion

    //region BlurWorker
    public BlurWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    //endregion

    //region doWork
    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);
        WorkerUtils.makeStatusNotification("Doing <WORK_NAME>", applicationContext);
        WorkerUtils.sleep();

        try {
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri");
                throw new IllegalArgumentException("Invalid input uri");
            }

            ContentResolver resolver = applicationContext.getContentResolver();
            // Create a bitmap
            Bitmap picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)));

            // Blur the bitmap
            Bitmap output = WorkerUtils.blurBitmap(picture, applicationContext);

            // Write bitmap to a temp file
            Uri outputUri = WorkerUtils.writeBitmapToFile(applicationContext, output);

            WorkerUtils.makeStatusNotification("Output is " + outputUri.toString(), applicationContext);

            Data outputData = new Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, outputUri.toString())
                    .build();

            // If there were no errors, return SUCCESS
            return Result.success(outputData);
        } catch (Throwable throwable) {
            // Technically WorkManager will return Result.failure()
            // but it's best to be explicit about it.
            // Thus if there were errors, we're return FAILURE
            Log.e(TAG, "Error applying blur", throwable);

            return Result.failure();
        }
    }
    //endregion
}
