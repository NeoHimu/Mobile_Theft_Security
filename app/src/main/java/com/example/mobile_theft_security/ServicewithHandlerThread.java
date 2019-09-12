package com.example.mobile_theft_security;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import java.lang.reflect.Method;
import java.util.Set;

import static android.content.ContentValues.TAG;
import static com.example.mobile_theft_security.App.CHANNEL_ID;

public class ServicewithHandlerThread extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    String SPEAKER_NAME = "CRER2087";


    static int number_of_display;
    DisplayManager displayManager;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    static MediaPlayer mp = new MediaPlayer();


    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            while (true) {
                synchronized (this) {
                    try {
                        //YOUR BACKGROUND WORK HERE
                        number_of_display = displayManager.getDisplays().length ;

                        //switch on the bluetooth
                        if (mBluetoothAdapter == null) {
                            // Device does not support Bluetooth
                        } else {
                            if (!mBluetoothAdapter.isEnabled()) {
                                // Bluetooth is not enable :)
                                mBluetoothAdapter.enable();
                            }
                        }

                        // unpair all the devices except the speaker
//                        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//                        if (pairedDevices.size() > 0) {
//                            for (BluetoothDevice device : pairedDevices) {
//                                try {
//                                    String device_name = device.getName();
//                                    if(!device_name.contentEquals(SPEAKER_NAME)) // unpair other devices
//                                    {
//                                        Method m = device.getClass().getMethod("removeBond", (Class[]) null);
//                                        m.invoke(device, (Object[]) null);
//                                    }
////                                    else // pair with the speaker
////                                    {
////                                        try{
////                                            Method m = device.getClass()
////                                                    .getMethod("createBond", (Class[]) null);
////                                            m.invoke(device, (Object[]) null);
////
////                                        } catch (Exception e) {
////                                            Log.e(TAG, e.getMessage());
////                                        }
////                                    }
//
//                                } catch (Exception e) {
//                                    Log.e("Removing has failed.", e.getMessage());
//                                }
//                            }
//                        }

                        if(isConnected())
                        {
                            if(mp.isPlaying())
                                mp.stop();
                            else {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        else
                        {
                            //set the volume to maximum if gets disconnected
                            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

//                            boolean isEnabled = (Settings.System.getInt(getContentResolver(), Settings.ACTION_AIRPLANE_MODE_SETTINGS , 0) == 1);
//
//                            if(isEnabled)
//                            {
//                                // turn airplane mode off
//                                Settings.System.putInt(getContentResolver(), Settings.ACTION_AIRPLANE_MODE_SETTINGS, 0);
//
//                            }

                            if(!mp.isPlaying())
                            {
                                mp.reset();// stops any current playing song
                                mp = MediaPlayer.create(getApplicationContext(), R.raw.song);//Settings.System.DEFAULT_ALARM_ALERT_URI);
                                mp.setLooping(true);
                                mp.start(); // starting mediaplayer
                            }
                        }

                    } catch (Exception e) {
                    }
                }
            }
            //stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        displayManager = (DisplayManager) getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Welcome to OnePlus!")
                .setContentText("OnePlus 7 pro is the most disruptive smartphone in the world!")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean isConnected() {
        if(number_of_display>1)
            return true;
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
