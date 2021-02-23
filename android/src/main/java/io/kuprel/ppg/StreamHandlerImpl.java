package io.kuprel.ppg;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import io.flutter.plugin.common.EventChannel;

class StreamHandlerImpl implements EventChannel.StreamHandler {
    private SensorEventListener sensorEventListener;
    private final SensorManager sensorManager;
    private final Sensor sensor;
    private FileSaver fileSaver;
    private Context context;
    private Queue<double[]> ppgDataQueue;

    class FileSaverRunnable implements Runnable {
        public void run() {
            fileSaver.saveFile(context, ppgDataQueue);
        }
    }

    StreamHandlerImpl(SensorManager sensorManager, int sensorType, Context context) {
        this.sensorManager = sensorManager;
        this.fileSaver = new FileSaver();
        this.ppgDataQueue = new LinkedList<>();
        this.context = context;
        sensor = sensorManager.getDefaultSensor(sensorType);
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        sensorEventListener = createSensorEventListener(events);
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onCancel(Object arguments) {
        sensorManager.unregisterListener(sensorEventListener, sensor);
    }

    private SensorEventListener createSensorEventListener(final EventChannel.EventSink events) {
        return new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                double[] sensorValues = new double[event.values.length + 2];
                for (int i = 0; i < event.values.length; i++) {
                    sensorValues[i] = (double) event.values[i];
                }
                sensorValues[sensorValues.length - 2] = System.currentTimeMillis();
                sensorValues[sensorValues.length - 1] = ((double) event.accuracy);
                ppgDataQueue.add(sensorValues);
                if (ppgDataQueue.size()>=500) {
//                    TODO(thread pool 로 구현하기)
                    Thread backgroundThread = new Thread(new FileSaverRunnable());
                    Log.d("streamhandlerimpl", backgroundThread.getState().toString());
                    Log.d("streamhandlerimpl", String.valueOf(backgroundThread.getState().equals(Thread.State.RUNNABLE)));
                    backgroundThread.start();
                }
                events.success(sensorValues);
            }
        };
    }

}
