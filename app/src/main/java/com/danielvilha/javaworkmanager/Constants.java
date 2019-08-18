package com.danielvilha.javaworkmanager;

/**
 * Created by danielvilha on 2019-08-18
 */
public final class Constants {

    // Notification Channel constants

    // Name of Notification Channel for verbose notifications of background work
    public static final CharSequence VERBOSE_NOTIFICATION_CHANNEL_NAME = "Verbose WorkManager Notifications";
    public static String VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION = "Shows notifications whenever work starts";
    public static final CharSequence NOTIFICATION_TITLE = "WorkRequest Starting";
    public static final String CHANNEL_ID = "VERBOSE_NOTIFICATION" ;
    public static final int NOTIFICATION_ID = 1;

    // The name of the image manipulation work
    public static final String IMAGE_MANIPULATION_WORK_NAME = "image_manipulation_work";

    // Other keys
    public static final String OUTPUT_PATH = "blur_filter_outputs";
    public static final String KEY_IMAGE_URI = "KEY_IMAGE_URI";
    public static final String TAG_OUTPUT = "OUTPUT";

    public static final long DELAY_TIME_MILLIS = 3000;

    // Ensures this class is never instantiated
    private Constants() {}
}
