package android.mohamedalaa.com.vipreminder.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.mohamedalaa.com.vipreminder.R;
import android.mohamedalaa.com.vipreminder.view.MainActivity;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/7/2018.
 *
 */
public class NotificationUtils {

    private static final String NOTIFICATION_CHANNEL_REMINDER_ID = "NOTIFICATION_CHANNEL_REMINDER_ID";

    private static final int PENDING_INTENT_REMINDER_REQUEST_CODE = 6547;

    private static final int NOTIFICATION_MANAGER_REMINDER_ID = 405;

    /**
     * Makes reminder notification due to time or place.
     *
     * @param context to build the notification.
     * @param title either came from time or from place.
     * @param message the label in the database of that reminder.
     */
    public static void makeReminderNotification(Context context,
                                                String title,
                                                String message){
        Intent intent = new Intent(context, MainActivity.class);
        // Create the pending intent to launch the activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context, PENDING_INTENT_REMINDER_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel channel = new NotificationChannel(
                        NOTIFICATION_CHANNEL_REMINDER_ID,
                        context.getString(R.string.notification_channel_reminder_name),
                        NotificationManager.IMPORTANCE_HIGH);

                notificationManager.createNotificationChannel(channel);
            }

            int smallIcon = R.drawable.ic_event_note_24px;
            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),
                    R.mipmap.ic_app_launcher_circle);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_REMINDER_ID)
                    .setSmallIcon(smallIcon)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            Timber.v("Before notify of notifManag");

            notificationManager.notify(NOTIFICATION_MANAGER_REMINDER_ID, notificationBuilder.build());
        }
    }

}
