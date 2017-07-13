package info.nightscout.android.upload.diabits;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import info.nightscout.android.model.medtronicNg.PumpStatusEvent;
import info.nightscout.android.upload.nightscout.NightscoutUploadIntentService;
import io.realm.Realm;
import io.realm.RealmResults;

public class DiabitsUploadIntentService extends IntentService {
    private static final String Tag = DiabitsUploadIntentService.class.getCanonicalName();
    private Context mContext;
    private SharedPreferences prefs;

    public DiabitsUploadIntentService() {
        super(NightscoutUploadIntentService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getBaseContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Boolean uploadEnabled = prefs.getBoolean("EnableDiabitsUpload", false);
        if (!uploadEnabled) {
            return;
        }

        Log.i(Tag, "Uploading to Diabits Platform");

        Realm mRealm = Realm.getDefaultInstance();
        RealmResults<PumpStatusEvent> records = mRealm
                .where(PumpStatusEvent.class)
                .equalTo("uploaded", false)
                .notEqualTo("sgv", 0)
                .findAll();

        DiabitsUploader uploader = new DiabitsUploader();
        uploader.upload(records);

        mRealm.close();
        DiabitsUploadReceiver.completeWakefulIntent(intent);
    }
}
