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

        PumpStatusEvent record = records.last();

        SensorData glucoseData = new SensorData("glucose");
        glucoseData.addValue("type", "glucose");
        glucoseData.addValue("date", record.getSgvDate().getTime());
        glucoseData.addValue("device", record.getDeviceName());
        glucoseData.addValue("level", record.getSgv());
        glucoseData.addValue("trend", EntriesSerializer.getDirectionStringStatus(record.getCgmTrend()));
        sendSensorData(glucoseData);

        SensorData bolusData = new SensorData("bolus");
        bolusData.addValue("type", "bolus");
        bolusData.addValue("date", record.getSgvDate().getTime());
        bolusData.addValue("device", record.getDeviceName());
        bolusData.addValue("glucose", record.getBolusWizardBGL());
        bolusData.addValue("running", record.isBolusing());
        sendSensorData(bolusData);

        SensorData basalData = new SensorData("basal");
        basalData.addValue("type", "basal");
        basalData.addValue("date", record.getSgvDate().getTime());
        basalData.addValue("device", record.getDeviceName());
        basalData.addValue("rate", record.getBasalRate());
        basalData.addValue("tempRate", record.getTempBasalRate());
        basalData.addValue("tempPercentage", record.getTempBasalPercentage());
        sendSensorData(basalData);

        SensorData insulinData = new SensorData("insulin");
        insulinData.addValue("type", "insulin");
        insulinData.addValue("date", record.getSgvDate().getTime());
        insulinData.addValue("device", record.getDeviceName());
        insulinData.addValue("active", record.getActiveInsulin());
        sendSensorData(insulinData);

        SensorData statusData = new SensorData("status");
        statusData.addValue("battery", record.getBatteryPercentage());
        statusData.addValue("reservoir", record.getReservoirAmount());
        sendSensorData(statusData);
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

            connection.publish("diabits", payload.getBytes(), QoS.AT_LEAST_ONCE, true);
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
