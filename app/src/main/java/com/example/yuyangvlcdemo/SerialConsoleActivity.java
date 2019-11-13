/* Copyright 2011-2013 Google Inc.
 * Copyright 2013 mike wakerly <opensource@hoho.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: https://github.com/mik3y/usb-serial-for-android
 */

package com.example.yuyangvlcdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.yuyangvlcdemo.GraphData.Data.Item;
import com.example.yuyangvlcdemo.GraphData.Data.ItemCO2;
import com.example.yuyangvlcdemo.GraphData.Data.ItemHumidity;
import com.example.yuyangvlcdemo.GraphData.Data.ItemTemprature;
import com.example.yuyangvlcdemo.GraphData.DataController;
import com.example.yuyangvlcdemo.GraphData.DataGraphFragment;
import com.example.yuyangvlcdemo.GraphData.DataType;
import com.example.yuyangvlcdemo.lib.driver.UsbSerialPort;
import com.example.yuyangvlcdemo.lib.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Monitors a single {@link UsbSerialPort} instance, showing all data
 * received.
 *
 * @author mike wakerly (opensource@hoho.com)
 */
public class SerialConsoleActivity extends AppCompatActivity {

    private final String TAG = SerialConsoleActivity.class.getSimpleName();
    /**
     * Driver instance, passed in statically via
     * {@link #show(Context, UsbSerialPort)}.
     * <p>
     * <p/>
     * This is a devious hack; it'd be cleaner to re-create the driver using
     * arguments passed in with the {@link #startActivity(Intent)} intent. We
     * can get away with it because both activities will run in the same
     * process, and this is a simple demo.
     */
    private static UsbSerialPort sPort = null;
    private boolean portOpen = false;
    private TextView mTitleTextView;

    private DataController dataController = DataController.getInstance();
    private HashMap<DataType, Integer> mappingSrc;



    /* Response callback Interface */
    public interface IReceived {
        void getResponseBody(String msg);
    }

    private final ExecutorService mExecutor = Executors.newCachedThreadPool();

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {

                }

                @Override
                public void onNewData(final byte[] data) {
                    SerialConsoleActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Receive VLC Frame
                            processData(data);
                        }
                    });
                }
            };

    private void processData(byte[] data)
    {
        //parse data
        char type = ParsingVLCData.getFrameID(data);

        switch(type)
        {
            case 'I':
                break;
            case 'C':
              // Toast.makeText(this, "Co2:" + ParsingVLCData.getCO2Info(data) + ":" + (data[4] & 0xFF) + ":" + (data[5] & 0xFF) + ":TEST" + (char)data[3], Toast.LENGTH_SHORT).show();
                dataController.addCO2(new ItemCO2(ParsingVLCData.getCO2Info(data)));
                updateGraph(DataType.CO2);
                //printList(DataType.CO2);
                break;
            case 'T':
                dataController.addTemp(new ItemTemprature(ParsingVLCData.getTempInfo(data)));
                dataController.addHumi(new ItemHumidity(ParsingVLCData.getHumiInfo(data)));
                updateGraph(DataType.TEMP);
                updateGraph(DataType.HUMI);
                //printList(DataType.TEMP);
                //Toast.makeText(this, "TEMP:" +  ParsingVLCData.getTempInfo(data) + " HUMI:" + ParsingVLCData.getHumiInfo(data), Toast.LENGTH_SHORT).show();

                break;
            default:
                Toast.makeText(this, "Can't Process the data", Toast.LENGTH_SHORT).show();
        }
    }

    private void printList(DataType t)
    {
        ArrayList<Item> test =  dataController.getList(t);

        String res = "";
        for(int i=0; i<test.size(); i++)
        {
            if(t == DataType.CO2)
                res += test.get(i).getIntValue();
            else
                res += test.get(i).getDoubleValue();
            res+=" ";
        }

        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }



    private void updateGraph(DataType type)
    {
        int rsc = mappingSrc.get(type);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(rsc, new DataGraphFragment((type))).commit();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serial_console);
        mTitleTextView = (TextView) findViewById(R.id.demoTitle);

//        graph = new ArrayList<DataGraphFragment>();
//        graph.add(new DataGraphFragment(DataType.TEMP));
//        graph.add(new DataGraphFragment(DataType.HUMI));
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.graph_co2 , new DataGraphFragment(DataType.CO2)).
                replace(R.id.graph_temp , new DataGraphFragment(DataType.TEMP)).
                replace(R.id.graph_humi , new DataGraphFragment(DataType.HUMI)).commit();

        mappingSrc = new HashMap<DataType, Integer>();
        mappingSrc.put(DataType.CO2, R.id.graph_co2);
        mappingSrc.put(DataType.TEMP, R.id.graph_temp);
        mappingSrc.put(DataType.HUMI, R.id.graph_humi);

        //FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
//        if (sPort != null) {
//            try {
//                sPort.close();
//            } catch (IOException e) {
//                // Ignore.
//            }
//            sPort = null;
//        }
//        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "Resumed, port=" + sPort);
        if (sPort == null) {
            mTitleTextView.setText("No serial device.");
        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            UsbDeviceConnection connection = usbManager.openDevice(sPort.getDriver().getDevice());
            if (connection == null) {
                mTitleTextView.setText("Opening device failed");
                return;
            }

            if (portOpen == false) {
                try {
                    sPort.open(connection);
                    portOpen = true;
                    sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                } catch (IOException e) {
                    Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                    mTitleTextView.setText("Error opening device: " + e.getMessage());
                    try {
                        sPort.close();
                    } catch (IOException e2) {
                        // Ignore.
                    }
                    sPort = null;
                    return;
                }
            }
            //mTitleTextView.setText("Serial device: " + sPort.getClass().getSimpleName());
        }
        onDeviceStateChange();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    /**
     * Starts the activity, using the supplied driver instance.
     *
     * @param context
     * @param
     */
    static void show(Context context, UsbSerialPort port) {
        sPort = port;
        final Intent intent = new Intent(context, SerialConsoleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }
}

