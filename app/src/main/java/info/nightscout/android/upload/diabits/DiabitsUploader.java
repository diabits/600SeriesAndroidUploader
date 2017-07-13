package info.nightscout.android.upload.diabits;

import android.util.Log;

import org.fusesource.mqtt.client.MQTT;

import java.net.URISyntaxException;

import info.nightscout.android.model.medtronicNg.PumpStatusEvent;
import io.realm.RealmResults;

class DiabitsUploader {
    private static final String Tag = DiabitsUploader.class.getCanonicalName();

    void upload(DiabitsUploadSpecification specification, RealmResults<PumpStatusEvent> records) {
        if (records.size() <= 0) {
            return;
        }

        try {
            Log.i(Tag, String.format("Starting upload of %s record using a Diabits API", records.size()));
            MQTT mqtt = new MQTT();
            mqtt.setHost("tcp://localhost:1883");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
