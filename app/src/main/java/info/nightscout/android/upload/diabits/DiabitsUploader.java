package info.nightscout.android.upload.diabits;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import info.nightscout.android.model.medtronicNg.PumpStatusEvent;
import info.nightscout.android.upload.nightscout.serializer.EntriesSerializer;
import io.realm.RealmResults;

class DiabitsUploader {
    private static final String Tag = DiabitsUploader.class.getCanonicalName();

    void upload(DiabitsUploadSpecification specification, RealmResults<PumpStatusEvent> records) {
        if (records.size() <= 0) {
            return;
        }

        Log.i(Tag, String.format("Starting upload of %s record to Diabits Platform using %s", records.size(), specification.getUri()));

        for (PumpStatusEvent record : records) {
            SensorData glucoseData = new SensorData("glucose");
            glucoseData.addValue("type", "sgv");
            glucoseData.addValue("date", record.getSgvDate().getTime());
            glucoseData.addValue("direction", EntriesSerializer.getDirectionStringStatus(record.getCgmTrend()));
            glucoseData.addValue("device", record.getDeviceName());
            glucoseData.addValue("sgv", record.getSgv());
            sendSensorData(glucoseData);

            if (record.getBolusWizardBGL() != 0) {
                SensorData bolusData = new SensorData("bolus");
                bolusData.addValue("type", "mbg");
                glucoseData.addValue("date", record.getSgvDate().getTime());
                glucoseData.addValue("device", record.getDeviceName());
                glucoseData.addValue("bolus", record.getBolusWizardBGL());
                sendSensorData(bolusData);
            }
        }
    }

    private void sendSensorData(SensorData sensorData) {
        BlockingConnection connection = null;
        try {
            MQTT mqtt = new MQTT();
            mqtt.setHost("tcp://rabbitmq.marathon.tools.clouderite.io:1883");
            mqtt.setUserName("root");
            mqtt.setPassword("dedede123");
            connection = mqtt.blockingConnection();
            connection.connect();

            ObjectMapper mapper = new ObjectMapper();
            String payload = mapper.writeValueAsString(sensorData);

            connection.publish("diabits", payload.getBytes(), QoS.AT_LEAST_ONCE, false);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect(connection);
        }
    }

    private void disconnect(BlockingConnection connection) {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
