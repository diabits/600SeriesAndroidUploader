package info.nightscout.android.upload.diabits;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import info.nightscout.android.upload.nightscout.NightscoutUploadReceiver;

public class DiabitsUploadReceiver extends WakefulBroadcastReceiver {
    private static final String Tah = NightscoutUploadReceiver.class.getCanonicalName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        Intent service = new Intent(context, DiabitsUploadIntentService.class);
        startWakefulService(context, service);
    }
}
