/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.thalmic.android.sample.helloworld;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public class HelloWorldActivity extends Activity {
    //Myo識別用変数
    final int attachingCount = 3;
    int ConnectMyoCount = 0;
    private ArrayList<Myo> mKnownMyos = new ArrayList<Myo>();
    private CheckBox Myo1,Myo2, Myo3;

    //記録用ボタンと状態識別変数
    private Button record;
    boolean isRecord = false;

    //取得データの種類選択用変数
    private  CheckBox checkAccel, checkGyro, checkOrient, checkQuater;
    boolean isAccel = false, isGyro = false, isOrient = false, isQuater = false;

    //記録用変数
    String AccelData, GyroData, OrientData, QuaterData;
    File AccelDataFile, GyroDataFile, OrientDataFile, QuaterDataFile;
    FileOutputStream AccelFileOutputStream, GyroFileOutputStream, OrientFileOutputStream, QuaterFileOutputStream;
    OutputStreamWriter AccelOutputStreamWriter, GyroOutputStreamWriter, OrientOutputStreamWriter, QuaterOutputStreamWriter;
    BufferedWriter bwAccel, bwGyro, bwOrient, bwQuater;
    //Myo2用
    File AccelDataFile2, GyroDataFile2, OrientDataFile2, QuaterDataFile2;
    FileOutputStream AccelFileOutputStream2, GyroFileOutputStream2, OrientFileOutputStream2, QuaterFileOutputStream2;
    OutputStreamWriter AccelOutputStreamWriter2, GyroOutputStreamWriter2, OrientOutputStreamWriter2, QuaterOutputStreamWriter2;
    BufferedWriter bwAccel2, bwGyro2, bwOrient2, bwQuater2;
    //Myo3用
    File AccelDataFile3, GyroDataFile3, OrientDataFile3, QuaterDataFile3;
    FileOutputStream AccelFileOutputStream3, GyroFileOutputStream3, OrientFileOutputStream3, QuaterFileOutputStream3;
    OutputStreamWriter AccelOutputStreamWriter3, GyroOutputStreamWriter3, OrientOutputStreamWriter3, QuaterOutputStreamWriter3;
    BufferedWriter bwAccel3, bwGyro3, bwOrient3, bwQuater3;

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {
        @Override
        public void onAttach(Myo myo, long timestamp) {
            // The object for a Myo is unique - in other words, it's safe to compare two Myo references to
            // see if they're referring to the same Myo.

            // Add the Myo object to our list of known Myo devices. This list is used to implement identifyMyo() below so
            // that we can give each Myo a nice short identifier.
            if (identifyMyo(myo) == 0) {
                mKnownMyos.add(myo);
                ConnectMyoCount++;
            }
        }

        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            switch (identifyMyo(myo)) {
                case 1:
                    Myo1.setTextColor(Color.CYAN);
                    Myo1.setText(myo.getName()+" is connected");
                    Myo1.setEnabled(true);
                    break;

                case 2:
                    Myo2.setTextColor(Color.CYAN);
                    Myo2.setText(myo.getName()+" is connected");
                    Myo2.setEnabled(true);
                    break;

                case 3:
                    Myo3.setTextColor(Color.CYAN);
                    Myo3.setText(myo.getName()+" is connected");
                    Myo3.setEnabled(true);
                    break;
            }
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            switch (identifyMyo(myo)) {
                case 1:
                    Myo1.setTextColor(Color.RED);
                    Myo1.setText(myo.getName()+" is disconnected");
                    break;

                case 2:
                    Myo2.setTextColor(Color.RED);
                    Myo2.setText(myo.getName()+" is disconnected");
                    break;

                case 3:
                    Myo3.setTextColor(Color.RED);
                    Myo3.setText(myo.getName()+" is disconnected");
                    break;
            }
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            long time = System.currentTimeMillis();
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
            float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));

            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (myo.getXDirection() == XDirection.TOWARD_ELBOW) {
                roll *= -1;
                pitch *= -1;
            }

            //Orientationデータの書き込み
            if (isRecord && isExternalStorageWritable() && isOrient) {
                OrientData = time + "," + roll + "," + pitch + "," + yaw + "\n";
                try {
                    if (identifyMyo(myo) == 1) {
                        bwOrient.write(OrientData);
                        bwOrient.flush();
                    }
                    if (identifyMyo(myo) == 2) {
                        bwOrient2.write(OrientData);
                        bwOrient2.flush();
                    }
                    if (identifyMyo(myo) == 3) {
                        bwOrient3.write(OrientData);
                        bwOrient3.flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //四元ベクトルデータの書き込み
            if (isRecord && isExternalStorageWritable() && isQuater) {
                QuaterData = time + "," + rotation.x() + "," + rotation.y() + "," + rotation.z() + "," + rotation.w() + "\n";
                try {
                    if (identifyMyo(myo) == 1) {
                        bwQuater.write(QuaterData);
                        bwQuater.flush();
                    }
                    if (identifyMyo(myo) == 2) {
                        bwQuater2.write(QuaterData);
                        bwQuater2.flush();
                    }
                    if (identifyMyo(myo) == 3) {
                        bwQuater3.write(QuaterData);
                        bwQuater3.flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // onAccelerometerData() is called when an attached Myo has provided new accelerometer data
        //単位は g (1G = 9.80665 m/s^2)
        @Override
        public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
            long time = System.currentTimeMillis();

            //加速度データの書き込み
            if (isRecord && isExternalStorageWritable() && isAccel) {
                AccelData = time + "," + accel.x() + "," + accel.y() + "," + accel.z() + "\n";
                try {
                    if (identifyMyo(myo) == 1) {
                        bwAccel.write(AccelData);
                        bwAccel.flush();
                    }
                    if (identifyMyo(myo) == 2) {
                        bwAccel2.write(AccelData);
                        bwAccel2.flush();
                    }
                    if (identifyMyo(myo) == 3) {
                        bwAccel3.write(AccelData);
                        bwAccel3.flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // onGyroscopeData() is called when an attached Myo has provided new gyroscope data
        @Override
        public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {
            long time = System.currentTimeMillis();
            //ジャイロデータの書き込み
            if (isRecord && isExternalStorageWritable() && isGyro) {
                GyroData = time + "," + gyro.x() + "," + gyro.y() + "," + gyro.z() + "\n";
                try {
                    if (identifyMyo(myo) == 1) {
                        bwGyro.write(GyroData);
                        bwGyro.flush();
                    }
                    if (identifyMyo(myo) == 2) {
                        bwGyro2.write(GyroData);
                        bwGyro2.flush();
                    }
                    if (identifyMyo(myo) == 3) {
                        bwGyro3.write(GyroData);
                        bwGyro3.flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public HelloWorldActivity() throws FileNotFoundException, UnsupportedEncodingException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        Myo1 = (CheckBox) findViewById(R.id.myo1);
        Myo2 = (CheckBox) findViewById(R.id.myo2);
        Myo3 = (CheckBox) findViewById(R.id.myo3);

        checkAccel = (CheckBox) findViewById(R.id.checkAccel);
        checkGyro = (CheckBox) findViewById(R.id.checkGyro);
        checkOrient = (CheckBox) findViewById(R.id.checkOrientation);
        checkQuater = (CheckBox) findViewById(R.id.checkQuaternion);

        record = (Button) findViewById(R.id.button);
        //クリック時の処理
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //記録状態変数の切り替え
                if (isRecord) {
                    //データ保存の終了
                    isRecord = false;
                    record.setTextColor(Color.BLACK);
                    record.setText("Record Finish!");

                    //チェックボックスの開放
                    checkAccel.setClickable(true);
                    checkGyro.setClickable(true);
                    checkOrient.setClickable(true);
                    checkQuater.setClickable(true);

                    try {
                        if (checkAccel.isChecked()) {
                            if (Myo1.isChecked())bwAccel.close();
                            if (Myo2.isChecked())bwAccel2.close();
                            if (Myo3.isChecked())bwAccel3.close();
                        }
                        if (checkOrient.isChecked()) {
                            if (Myo1.isChecked())bwOrient.close();
                            if (Myo2.isChecked())bwOrient2.close();
                            if (Myo3.isChecked())bwOrient3.close();
                        }
                        if (checkGyro.isChecked()){
                            if (Myo1.isChecked())bwGyro.close();
                            if (Myo2.isChecked())bwGyro2.close();
                            if (Myo3.isChecked())bwGyro3.close();
                        }
                        if (checkQuater.isChecked()) {
                            if (Myo1.isChecked())bwQuater.close();
                            if (Myo2.isChecked())bwQuater2.close();
                            if (Myo3.isChecked())bwQuater3.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    //データ保存の開始
                    isRecord = true;
                    record.setTextColor(Color.RED);
                    record.setText("Data Recording...");

                    //データ保存中にチェックボックスが変わらないようにする
                    checkAccel.setClickable(false);
                    checkGyro.setClickable(false);
                    checkOrient.setClickable(false);
                    checkQuater.setClickable(false);

                    makefile();
                    try {
                        if (checkAccel.isChecked()) {
                            if (Myo1.isChecked()) {
                                AccelFileOutputStream = new FileOutputStream(AccelDataFile, true);
                                AccelOutputStreamWriter = new OutputStreamWriter(AccelFileOutputStream, "UTF-8");
                                bwAccel = new BufferedWriter(AccelOutputStreamWriter);
                                bwAccel.write("UNIX-TimeStamp,x,y,z\n");
                            }
                            if (Myo2.isChecked()) {
                                AccelFileOutputStream2 = new FileOutputStream(AccelDataFile2, true);
                                AccelOutputStreamWriter2 = new OutputStreamWriter(AccelFileOutputStream2, "UTF-8");
                                bwAccel2 = new BufferedWriter(AccelOutputStreamWriter2);
                                bwAccel2.write("UNIX-TimeStamp,x,y,z\n");
                            }
                            if (Myo3.isChecked()) {
                                AccelFileOutputStream3 = new FileOutputStream(AccelDataFile3, true);
                                AccelOutputStreamWriter3 = new OutputStreamWriter(AccelFileOutputStream3, "UTF-8");
                                bwAccel3 = new BufferedWriter(AccelOutputStreamWriter3);
                                bwAccel3.write("UNIX-TimeStamp,x,y,z\n");
                            }
                        }
                        if (checkGyro.isChecked()) {
                            if (Myo1.isChecked()) {
                                GyroFileOutputStream = new FileOutputStream(GyroDataFile, true);
                                GyroOutputStreamWriter = new OutputStreamWriter(GyroFileOutputStream, "UTF-8");
                                bwGyro = new BufferedWriter(GyroOutputStreamWriter);
                                bwGyro.write("UNIX-TimeStamp,x,y,z\n");
                            }
                            if (Myo2.isChecked()) {
                                GyroFileOutputStream2 = new FileOutputStream(GyroDataFile2, true);
                                GyroOutputStreamWriter2 = new OutputStreamWriter(GyroFileOutputStream2, "UTF-8");
                                bwGyro2 = new BufferedWriter(GyroOutputStreamWriter2);
                                bwGyro2.write("UNIX-TimeStamp,x,y,z\n");
                            }
                            if (Myo3.isChecked()) {
                                GyroFileOutputStream3 = new FileOutputStream(GyroDataFile3, true);
                                GyroOutputStreamWriter3 = new OutputStreamWriter(GyroFileOutputStream3, "UTF-8");
                                bwGyro3 = new BufferedWriter(GyroOutputStreamWriter3);
                                bwGyro3.write("UNIX-TimeStamp,x,y,z\n");
                            }
                        }
                        if (checkOrient.isChecked()) {
                            if (Myo1.isChecked()) {
                                OrientFileOutputStream = new FileOutputStream(OrientDataFile, true);
                                OrientOutputStreamWriter = new OutputStreamWriter(OrientFileOutputStream, "UTF-8");
                                bwOrient = new BufferedWriter(OrientOutputStreamWriter);
                                bwOrient.write("UNIX-TimeStamp,roll,pitch,yaw\n");
                            }
                            if (Myo2.isChecked()) {
                                OrientFileOutputStream2 = new FileOutputStream(OrientDataFile2, true);
                                OrientOutputStreamWriter2 = new OutputStreamWriter(OrientFileOutputStream2, "UTF-8");
                                bwOrient2 = new BufferedWriter(OrientOutputStreamWriter2);
                                bwOrient2.write("UNIX-TimeStamp,roll,pitch,yaw\n");
                            }
                            if (Myo3.isChecked()) {
                                OrientFileOutputStream3 = new FileOutputStream(OrientDataFile3, true);
                                OrientOutputStreamWriter3 = new OutputStreamWriter(OrientFileOutputStream3, "UTF-8");
                                bwOrient3 = new BufferedWriter(OrientOutputStreamWriter3);
                                bwOrient3.write("UNIX-TimeStamp,roll,pitch,yaw\n");
                            }
                        }
                        if (checkQuater.isChecked()) {
                            if (Myo1.isChecked()) {
                                QuaterFileOutputStream = new FileOutputStream(QuaterDataFile, true);
                                QuaterOutputStreamWriter = new OutputStreamWriter(QuaterFileOutputStream, "UTF-8");
                                bwQuater = new BufferedWriter(QuaterOutputStreamWriter);
                                bwQuater.write("UNIX-TimeStamp,x,y,z,w\n");
                            }
                            if (Myo2.isChecked()) {
                                QuaterFileOutputStream2 = new FileOutputStream(QuaterDataFile2, true);
                                QuaterOutputStreamWriter2 = new OutputStreamWriter(QuaterFileOutputStream2, "UTF-8");
                                bwQuater2 = new BufferedWriter(QuaterOutputStreamWriter2);
                                bwQuater2.write("UNIX-TimeStamp,x,y,z,w\n");
                            }
                            if (Myo3.isChecked()) {
                                QuaterFileOutputStream3 = new FileOutputStream(QuaterDataFile3, true);
                                QuaterOutputStreamWriter3 = new OutputStreamWriter(QuaterFileOutputStream3, "UTF-8");
                                bwQuater3 = new BufferedWriter(QuaterOutputStreamWriter3);
                                bwQuater3.write("UNIX-TimeStamp,x,y,z,w\n");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //hubにmyoの接続数を指定する
        hub.setMyoAttachAllowance(attachingCount);

        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        if (isFinishing()) {
            // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
            Hub.getInstance().shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.action_scan == id) {
            onScanActionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onScanActionSelected() {
        // Launch the ScanActivity to scan for Myos to connect to.
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    //ファイル作り
    public void makefile() {
        Calendar calendar = Calendar.getInstance();
        String RecordTime = calendar.get(Calendar.YEAR) + "-"
                + (calendar.get(Calendar.MONTH) + 1) + "-"
                + calendar.get(Calendar.DAY_OF_MONTH) + "_"
                + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                + calendar.get(Calendar.MINUTE) + ":"
                + calendar.get(Calendar.SECOND) + "_";

        if (checkAccel.isChecked()) {
            if (Myo1.isChecked()) {
                AccelDataFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + mKnownMyos.get(0).getName() + "_Accel.csv");
                AccelDataFile.getParentFile().mkdir();
            }
            if (Myo2.isChecked()) {
                AccelDataFile2 = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + mKnownMyos.get(1).getName() + "_Accel.csv");
                AccelDataFile2.getParentFile().mkdir();
            }
            if (Myo3.isChecked()) {
                AccelDataFile3 = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + mKnownMyos.get(2).getName() + "_Accel.csv");
                AccelDataFile3.getParentFile().mkdir();
            }
        }
        if (checkGyro.isChecked()) {
            if (Myo1.isChecked()) {
                GyroDataFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + mKnownMyos.get(0).getName() + "_Gyro.csv");
                GyroDataFile.getParentFile().mkdir();
            }
            if (Myo2.isChecked()) {
                GyroDataFile2 = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + mKnownMyos.get(1).getName() + "_Gyro.csv");
                GyroDataFile2.getParentFile().mkdir();
            }
            if (Myo3.isChecked()) {
                GyroDataFile3 = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + mKnownMyos.get(2).getName() + "_Gyro.csv");
                GyroDataFile3.getParentFile().mkdir();
            }
        }
        if (checkOrient.isChecked()) {
            if (Myo1.isChecked()) {
                OrientDataFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + mKnownMyos.get(0).getName() + "_Orient.csv");
                OrientDataFile.getParentFile().mkdir();
            }
            if (Myo2.isChecked()) {
                OrientDataFile2 = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + mKnownMyos.get(1).getName() + "_Orient.csv");
                OrientDataFile2.getParentFile().mkdir();
            }
            if (Myo3.isChecked()) {
                OrientDataFile3 = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + mKnownMyos.get(2).getName() + "_Orient.csv");
                OrientDataFile3.getParentFile().mkdir();
            }
        }
        if (checkQuater.isChecked()) {
            if (Myo1.isChecked()) {
                QuaterDataFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + mKnownMyos.get(0).getName() + "_Quater.csv");
                QuaterDataFile.getParentFile().mkdir();
            }
            if (Myo2.isChecked()) {
                QuaterDataFile2 = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + mKnownMyos.get(1).getName() + "_Quater.csv");
                QuaterDataFile2.getParentFile().mkdir();
            }
            if (Myo3.isChecked()) {
                QuaterDataFile3 = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + mKnownMyos.get(2).getName() + "_Quater.csv");
                QuaterDataFile3.getParentFile().mkdir();
            }
        }
    }

    //内部ストレージが空いているかどうか
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //保存するデータの種類を選ぶチェックボックスの処理
    public void  onCheckboxClicked(View view) {
        switch(view.getId()) {
            case R.id.checkAccel:
                if (isAccel) isAccel = false;
                else isAccel = true;
                break;

            case R.id.checkGyro:
                if (isGyro) isGyro = false;
                else isGyro = true;
                break;

            case R.id.checkQuaternion:
                if (isQuater) isQuater = false;
                else isQuater = true;
                break;

            case R.id.checkOrientation:
                if (isOrient) isOrient = false;
                else isOrient = true;
                break;
        }
    }

    //リストの何番目のmyoかを返す
    private int identifyMyo(Myo myo) {
        return mKnownMyos.indexOf(myo) + 1;
    }
}
