package com.danielvilha.javaworkmanager.workers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.danielvilha.javaworkmanager.Constants;

import java.io.File;

/**
 * Created by danielvilha on 2019-08-18
 */
public class CleanupWorker extends Worker {

    //region Variables
    private static final String TAG = CleanupWorker.class.getSimpleName();
    //endregion

    //region CleanupWorker
    public CleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    //endregion

    //region doWork
    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        WorkerUtils.makeStatusNotification("Doing <WORK_NAME>", applicationContext);
        WorkerUtils.sleep();

        try {
            File outputDirectory = new File(applicationContext.getFilesDir(), Constants.OUTPUT_PATH);

            if (outputDirectory.exists()) {
                File[] entries = outputDirectory.listFiles();

                if (entries != null && entries.length > 0) {
                    for (File entry : entries) {
                        String name = entry.getName();

                        if (!TextUtils.isEmpty(name) && name.endsWith(".png")) {
                            boolean deleted = entry.delete();
                            Log.i(TAG, String.format("Deleted %s - %s", name, deleted));
                        }
                    }
                }
            }

            return Worker.Result.success();
        } catch (Exception exception) {
            Log.e(TAG, "Error cleaning up", exception);
            return Worker.Result.failure();
        }
    }
    //endregion
}
