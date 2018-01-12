package com.example.group14;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.opencsv.CSVWriter;


public class MainActivity extends Activity implements SensorEventListener {
    private double mLastX, mLastY, mLastZ;
    //private boolean mInitialized;
    private String csv1Name;
    private CSVWriter writer;

    public boolean enable = false;


    private SensorManager mSensorManager;

    private Sensor mAccelerometer;
    private ArrayList<Float> AcceListX  = new ArrayList<>();
    private ArrayList<Float> AcceListY  = new ArrayList<>();
    private ArrayList<Float> AcceListZ  = new ArrayList<>();

    private ArrayList<String> TimeStampList = new ArrayList<>();

    private final float NOISE = (float) 3.0;

    public MediaPlayer m_fall;

    /** Called when the activity is first created. */

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0);
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        0);

            }
        }

        Button button1 = (Button) findViewById(R.id.button1);	//ENABLE
        Button button2 = (Button) findViewById(R.id.button2);	//SAVE

        //Enable
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                enable=true;
            }

        });

        //SAVE
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enable=false;
                save();
                AcceListX.clear();
                AcceListY.clear();
                AcceListZ.clear();
                TimeStampList.clear();
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //mSensorManager.registerListener(this, mAccelerometer, rate());
       mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, rate());
    }
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
// can be safely ignored for this demo
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(enable) {

            TextView tvX = (TextView) findViewById(R.id.x_axis);
            TextView tvY = (TextView) findViewById(R.id.y_axis);
            TextView tvZ = (TextView) findViewById(R.id.z_axis);
            ImageView iv = (ImageView) findViewById(R.id.image);


            //SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
            SimpleDateFormat format = new SimpleDateFormat("hhmmssSSS");
            Date date = new Date(System.currentTimeMillis());
            String timeStamp = format.format(date);

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];


            AcceListX.add(x);
            AcceListY.add(y);
            AcceListZ.add(z);

            TimeStampList.add(timeStamp);


                //float deltaX = Math.abs(mLastX - x);
                //float deltaY = Math.abs(mLastY - y);
                //float deltaZ = Math.abs(mLastZ - z);
                //if (deltaX < NOISE) deltaX = (float) 0.0;
                //if (deltaY < NOISE) deltaY = (float) 0.0;
                //if (deltaZ < NOISE) deltaZ = (float) 0.0;
                double currentX = Math.abs(x);
                double currentY = Math.abs(y);
                double currentZ = Math.abs(z);

                double current_sum = Math.abs(currentX*currentX+currentY*currentY+currentZ*currentZ);
                 Log.d("TAG", "current_sum"+current_sum);
                double Last_sum = Math.abs(mLastX*mLastX+mLastY*mLastY+mLastZ*mLastZ);
                double current_dif = Math.abs(current_sum-Last_sum);

                mLastX = currentX;
                mLastY = currentY;
                mLastZ = currentZ;
                //tvX.setText(Float.toString(deltaX));
                //tvY.setText(Float.toString(deltaY));
                //tvZ.setText(Float.toString(deltaZ));
                //iv.setVisibility(View.VISIBLE);



                if (current_sum > 900) {

                    Toast.makeText(MainActivity.this, "Falling", Toast.LENGTH_LONG).show();

                    //MediaPlayer m_fall= new MediaPlayer();
                   // m_fall=MediaPlayer.create(this,R.raw.fall);
                    //m_fall.start();
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                            r.play();
                        }
                    }, 1000);

                }



        }
    }

    // Frequency
    public int rate(){

       int SENSOR_RATE = 10;

        return SENSOR_RATE;
    }


    public void save() {
        csv1Name = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.csv";

        File file = new File(csv1Name);
        if (!file.exists()) {
            try {
                writer = new CSVWriter(new FileWriter(csv1Name, true), ',', CSVWriter.NO_QUOTE_CHARACTER);
                String[] entry = new String[]{
                        "TimeStamp",
                        "TYPE_ACCELEROMETER_X","TYPE_ACCELEROMETER_Y","TYPE_ACCELEROMETER_Z"};

                writer.writeNext(entry);
                for(int i = 0; i < AcceListX.size(); ++i) {
                    String[] dataEntry = new String[] {
                            TimeStampList.get(i)+"",
                            AcceListX.get(i)+"", AcceListY.get(i)+"",AcceListZ.get(i)+""
                    };
                    writer.writeNext(dataEntry);
                }
                writer.close();
            } catch (Exception ex) {
                Log.e("Err.", ex.toString());
            }
        }

    }
}