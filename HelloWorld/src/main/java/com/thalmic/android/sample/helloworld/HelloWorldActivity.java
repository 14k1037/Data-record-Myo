/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.thalmic.android.sample.helloworld;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.util.Calendar;

public class HelloWorldActivity extends Activity {

    private TextView mLockStateView;
    private TextView mTextView;

    //記録用ボタンと状態識別変数
    private Button record;
    boolean isRecord = false;

    //取得データの種類選択用変数
    private  CheckBox checkAccel, checkGyro, checkOrient, checkQuater;
    boolean isAccel = false, isGyro = false, isOrient = false, isQuater = false;

    //記録用変数
    Calendar calendar = Calendar.getInstance();
    String RecordTime = calendar.get(Calendar.YEAR) + "-"
                      + (calendar.get(Calendar.MONTH) + 1) + "-"
                      + calendar.get(Calendar.DAY_OF_MONTH) + "_"
                      + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                      + calendar.get(Calendar.MINUTE) + ":"
                      + calendar.get(Calendar.SECOND);
    String AccelData, GyroData, OrientData, QuaterData;
    File AccelDataFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + "_Accel.csv"),
         GyroDataFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + "_Gyro.csv"),
         OrientDataFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + "_Orient.csv"),
         QuaterDataFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + RecordTime + "_Quater.csv");
    FileOutputStream AccelFileOutputStream, GyroFileOutputStream, OrientFileOutputStream, QuaterFileOutputStream;
    OutputStreamWriter AccelOutputStreamWriter, GyroOutputStreamWriter, OrientOutputStreamWriter, QuaterOutputStreamWriter;
    BufferedWriter bwAccel, bwGyro, bwOrient, bwQuater;

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {

        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            mTextView.setTextColor(Color.CYAN);
            mTextView.setText("Myo is connected!");
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            mTextView.setTextColor(Color.RED);
            mTextView.setText("Myo is disconnected!");
        }

        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            mTextView.setText(myo.getArm() == Arm.LEFT ? R.string.arm_left : R.string.arm_right);
        }

        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            mTextView.setText(R.string.hello_world);
        }

        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {
            mLockStateView.setText(R.string.unlocked);
        }

        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {
            mLockStateView.setText(R.string.locked);
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
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
                OrientData = timestamp + "," + roll + "," + pitch + "," + yaw + "\n";
                try {
                    bwOrient.write(OrientData);
                    bwOrient.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //四元ベクトルデータの書き込み
            if (isRecord && isExternalStorageWritable() && isQuater) {
                QuaterData = timestamp + "," + rotation.x() + "," + rotation.y() + "," + rotation.z() + "," + rotation.w() + "\n";
                try {
                    bwQuater.write(QuaterData);
                    bwQuater.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // onAccelerometerData() is called when an attached Myo has provided new accelerometer data
        //単位は g (1G = 9.80665 m/s^2)
        @Override
        public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
            //加速度データの書き込み
            if (isRecord && isExternalStorageWritable() && isAccel) {
                AccelData = timestamp + "," + accel.x() + "," + accel.y() + "," + accel.z() + "\n";
                try {
                    bwAccel.write(AccelData);
                    bwAccel.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // onGyroscopeData() is called when an attached Myo has provided new gyroscope data
        @Override
        public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {
            //ジャイロデータの書き込み
            if (isRecord && isExternalStorageWritable() && isGyro) {
                GyroData = timestamp + "," + gyro.x() + "," + gyro.y() + "," + gyro.z() + "\n";
                try {
                    bwGyro.write(GyroData);
                    bwGyro.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            switch (pose) {
                case UNKNOWN:
                    mTextView.setText(getString(R.string.hello_world));
                    break;
                case REST:
                case DOUBLE_TAP:
                    int restTextId = R.string.hello_world;
                    switch (myo.getArm()) {
                        case LEFT:
                            restTextId = R.string.arm_left;
                            break;
                        case RIGHT:
                            restTextId = R.string.arm_right;
                            break;
                    }
                    mTextView.setText(getString(restTextId));
                    break;
                case FIST:
                    mTextView.setText(getString(R.string.pose_fist));
                    break;
                case WAVE_IN:
                    mTextView.setText(getString(R.string.pose_wavein));
                    break;
                case WAVE_OUT:
                    mTextView.setText(getString(R.string.pose_waveout));
                    break;
                case FINGERS_SPREAD:
                    mTextView.setText(getString(R.string.pose_fingersspread));
                    break;
            }

            if (pose != Pose.UNKNOWN && pose != Pose.REST) {
                // Tell the Myo to stay unlocked until told otherwise. We do that here so you can
                // hold the poses without the Myo becoming locked.
                myo.unlock(Myo.UnlockType.HOLD);

                // Notify the Myo that the pose has resulted in an action, in this case changing
                // the text on the screen. The Myo will vibrate.
                myo.notifyUserAction();
            } else {
                // Tell the Myo to stay unlocked only for a short period. This allows the Myo to
                // stay unlocked while poses are being performed, but lock after inactivity.
                myo.unlock(Myo.UnlockType.TIMED);
            }
        }
    };

    public HelloWorldActivity() throws FileNotFoundException, UnsupportedEncodingException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        mLockStateView = (TextView) findViewById(R.id.lock_state);
        mTextView = (TextView) findViewById(R.id.text);

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
                        if (checkAccel.isChecked()) bwAccel.close();
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
                            AccelFileOutputStream = new FileOutputStream(AccelDataFile, true);
                            AccelOutputStreamWriter = new OutputStreamWriter(AccelFileOutputStream, "UTF-8");
                            bwAccel = new BufferedWriter(AccelOutputStreamWriter);
                            bwAccel.write("TimeStamp,x,y,z\n");
                        }
                        if (checkGyro.isChecked()) {
                            GyroFileOutputStream = new FileOutputStream(GyroDataFile, true);
                            GyroOutputStreamWriter = new OutputStreamWriter(GyroFileOutputStream, "UTF-8");
                            bwGyro = new BufferedWriter(GyroOutputStreamWriter);
                            bwGyro.write("TimeStamp,x,y,z\n");
                        }
                        if (checkOrient.isChecked()) {
                            OrientFileOutputStream = new FileOutputStream(OrientDataFile, true);
                            OrientOutputStreamWriter = new OutputStreamWriter(OrientFileOutputStream, "UTF-8");
                            bwOrient = new BufferedWriter(OrientOutputStreamWriter);
                            bwOrient.write("TimeStamp,roll,pitch,yaw\n");
                        }
                        if (checkQuater.isChecked()) {
                            QuaterFileOutputStream = new FileOutputStream(QuaterDataFile, true);
                            QuaterOutputStreamWriter = new OutputStreamWriter(QuaterFileOutputStream, "UTF-8");
                            bwQuater = new BufferedWriter(QuaterOutputStreamWriter);
                            bwQuater.write("TimeStamp,x,y,z,w\n");
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
        if (checkAccel.isChecked())AccelDataFile.getParentFile().mkdir();
        if (checkGyro.isChecked())GyroDataFile.getParentFile().mkdir();
        if (checkOrient.isChecked())OrientDataFile.getParentFile().mkdir();
        if (checkQuater.isChecked())QuaterDataFile.getParentFile().mkdir();
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
}
