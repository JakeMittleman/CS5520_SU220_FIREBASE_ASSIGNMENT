package com.example.firebaseassignment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used to extend the FirebaseMessagingService. It should be thought of as a "helper"
 * class of sorts, for the StickerActivity class. Once a FCM message is sent, this class exists
 * to receive and process the message
 */
public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";


    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(token);
    }

    /**
     * The skeleton of this function (basic functionality) is referenced from from Dr. Feinberg
     * sample code, Class DemoMessagingService, as well as google's official documentation/sample code
     * - Per google's documentation: Data messages are handled here, in both foreground and background.
     * - Notification messages are only handled here when the app is in the foreground.
     * - When the app is in the background, an automatically generated notification is displayed.
     * - Messages containing both notification and data payloads are treated as notifications.
     * - https://firebase.google.com/docs/cloud-messaging/concept-options
     * Not getting messages here? See why this may be: https://goo.gl/39bRNJ
     *
     * @param remoteMessage the message being received
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload. THIS WORKS IN THE FOREGROUND
        // Referenced from Dr. Feinberg Sample code, DemoMessagingService
        if (remoteMessage.getData() != null) {

            //TODO: Implement what we actually want to do with the data sent in the
            // remoteMessage. postToastMessage() should only be a placeholder.
//            postToastMessage(remoteMessage.getData().get("title") + ", " +
//                    "from remoteMessage's data paylaod, in MessagingService onMessageReceived()");


        }

        // Check if message contains a notification payload, generate custom notification
        // Referenced from Dr. Feinberg Sample code, DemoMessagingService
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTag());
        }

    }

    /**
     * This message is copied from Dr. Feinberg example code, Class DemoMessagingService.
     */
    public void postToastMessage(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }


//    // Copied from Dr. Feinberg sample code
//    private void  extractPayloadDataForegroundCase(RemoteMessage remoteMessage){
//        if(remoteMessage.getData() != null){
//            postToastMessage(remoteMessage.getData().get("title"));
//        }
//    }



        /**
     * Create and show a simple notification containing the received FCM message.
     * This method is used if you want to generate a custom notification message, vs. the
     * system's automatically generated notification message
     * This method was referenced from: https://github.com/firebase/quickstart-android/blob/8a3169ae7f75e38665a62c520ccf8960609ab815/messaging/app/src/main/java/com/google/firebase/quickstart/fcm/java/MyFirebaseMessagingService.java#L58-L101
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody, String imageName) {
        Intent intent = new Intent(this, StickerActivityWithImages.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        // Sourced from https://mobikul.com/imagebanner-in-android-notification/
        // and https://stackoverflow.com/questions/4955268/how-to-set-a-bitmap-from-resource
        /*
        This is what allows us to send an image in the notification. This is probably similar
        to how we'll do it elsewhere.
         */
        Bitmap remote_picture = null;
        NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle();

        remote_picture = BitmapFactory.decodeResource(getResources(), Integer.parseInt(imageName));
        notiStyle.bigPicture(remote_picture);

        // ===================

        String channelId = "MY CUSTOM DEFAULT NOTIFICATION CHANNEL ID";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentTitle("Your Friend Sent You A New Sticker!")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setStyle(notiStyle);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


}
