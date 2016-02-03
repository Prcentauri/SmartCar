package com.example.root.myapplication;

import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SlidingDrawer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Locale;


import java.util.UUID;




public class MainActivity extends AppCompatActivity {
   //start slider Object
    private SlidingDrawer drawer;
    private Button Handle,Click_me;
    private TextView text1;
    private Context context;
    //end Slider

    //mySpeech Prop
    private final int REQ_CODE_SPEECH_INPUT = 100;
    public String theStringTosend;

    //end of Prop
    android.os.Handler handler = new android.os.Handler();



    private static final String TAG = "bluetooth1";

    Button btnOn, btnOff;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;


    private OutputStream outStream = null;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "30:14:09:02:20:61";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Slider init

        context = this.getApplicationContext();
        Handle = (Button) findViewById(R.id.handle);
        text1 = (TextView)findViewById(R.id.text1);
        Click_me = (Button)findViewById(R.id.click_me);
        drawer = (SlidingDrawer)findViewById(R.id.slidingDrawer);
        btnOff = (Button)findViewById(R.id.btnOff);
        btnOn = (Button)findViewById(R.id.btnOn);


        drawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                Handle.setText("-");
                //drawer.setBackgroundColor(0xFF00FF00);
                text1.setText("alerdy dragged");

            }
        });
        drawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                Handle.setText("+");
                //drawer.setBackgroundColor(0x00);
                text1.setText("drag me ");
            }
        });

        //end slider init
        btnOn = (Button) findViewById(R.id.btnOn);
        btnOff = (Button) findViewById(R.id.btnOff);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();



    }
    //text Speech start
    //Showing google speech input Dialog

    public void promptSpeechInput(View v)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        }catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_LONG).show();
        }
    }

    //receiving text input
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {

            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    Log.d(result.get(0).toString(), "...ok...");

                    theStringTosend = result.get(0).toString();

                    handler.postDelayed(new Runnable() {
                        public void run() {
                            sendData(theStringTosend);
                        }
                    },0);

                    }


                }
                break;
            }

        }

        public void onClick0(View v)
        {
            Toast.makeText(context,"the button has been clicked ",Toast.LENGTH_SHORT).show();
        }




        public void onClick(View v) {
            sendData("1");
            //Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
        }



            public void onClick2(View v) {
                sendData("0");
                //Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
            }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }


    public void onResume() {
        super.onResume();

        //toast.setGravity(Gravity.LEFT, 0, 0);
        //toast.setDuration(duration);
        //toast.setText("the Bluetooth is Connected");

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e1) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            Toast toast =Toast.makeText(getBaseContext(),"message",Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP,0,70);
            btSocket.connect();
            //toast.setGravity(Gravity.TOP|Gravity.LEFT, 0, 0);
            toast.setText("Bluetooth is Connected!!");
            toast.show();
            Log.d(TAG, "...Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

            try{
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Send data: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            text1.setText("No Client Bleutooth" + msg);
        }
    }
}
