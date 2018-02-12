package tutoringproject.behaviors;

import android.content.Intent;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/************************************************************************************************
 * this file contains code to start up the app, connect to the server, and begin the tutoring session
 * it is almost entirely borrowed from the previous app versions.
 * Code relevant to the behavoir study specifically is in QuestionActivity
 * *********************************************************************************/
public class MainActivity extends Activity implements View.OnClickListener {

    private EditText iPandPort;
    private TCPClient mTcpClient;
    private Button connectButton;
    private Button startMathButton;
    private EditText message;
    private EditText participantID;
    private EditText sessionNumberBox;
    private TextView connectionStatus;
//    private EditText startQuestionNum;
    private EditText conditionNum;
    private EditText maxTime;
    private String sessionNum;
    private int expGroup;
    private int difficultyGroup;
//    private EditText fixedBreakInterval;
//    private EditText breaksGiven;
    private int breakTaken = 0; //default to not having taken break yet



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_screen);

        iPandPort = (EditText) findViewById(R.id.IPandPort);
        iPandPort.setText("192.168.42.117:9090");
        connectButton = (Button) findViewById(R.id.ConnectButton);
        connectionStatus = (TextView) findViewById(R.id.ConnectionStatus);
        startMathButton = (Button)findViewById(R.id.startMathButton);
        participantID = (EditText) findViewById(R.id.ParticipantID);
        sessionNumberBox = (EditText) findViewById(R.id.SessionNumber);

        startMathButton.setOnClickListener(this);

        maxTime = (EditText) findViewById(R.id.MaxTime);
        maxTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maxTime.setText("");
            }
        });

    }

    public void startMathQuestions(View view) {

        Intent intent = new Intent(this, QuestionActivity.class);

        try {
            sessionNum = sessionNumberBox.getText().toString();
            if (sessionNum.equals("") || Integer.parseInt(sessionNum)> 5) {
                sessionNum = "1";
            }
        } catch(NumberFormatException e) {
           sessionNum = "1";
        }


        String pid;

        try {
            pid = participantID.getText().toString();
            Integer.parseInt(pid);
        } catch(NumberFormatException e) {
            pid = "600001";
            //return;
        }

        String maxTimeString = maxTime.getText().toString();
        boolean load = false;
        if (maxTimeString.equals("-1")) {
            load = false;
        }
        else {
            load = true;
        }

        //try {
        //    Integer.parseInt(maxTimeString);
        //} catch (NumberFormatException e) {  //if nothing entered into field
        //    maxTimeString = "-1";
        //}
        intent.putExtra("maxTime", maxTimeString);

        //send message to computer to convey session starting
        if (TCPClient.singleton != null) {
            String startMessage = "";

            if (load){
                System.out.println("SENDING LOAD MESSAGE TO ROS NODES");
                startMessage = "LOAD;" + pid + ";" + sessionNum + ";" + expGroup + ";" + difficultyGroup;
            }

            else {
                startMessage = "START;" + pid + ";" + sessionNum + ";" + expGroup + ";" + difficultyGroup;
            }

            mTcpClient.sendMessage(startMessage);
        }

        intent.putExtra("SessionNum", Integer.parseInt(sessionNum));
        intent.putExtra("DifficultyGroup", difficultyGroup);
        intent.putExtra("ExpGroup", expGroup);
        if (difficultyGroup == 1) { //harder questions
            intent.putExtra("QuestionLevel", 3);
            intent.putExtra("QuestionNumber", 0);
        }
        else {
            intent.putExtra("QuestionLevel", 1);
            intent.putExtra("QuestionNumber", 0);
        }

        intent.putExtra("BreakTaken", breakTaken);
        startActivity(intent);
    }

    public void connected() {
        connectionStatus.setText("Connected.");
    }

    @Override
    public void onClick(View v) {
        if (v == startMathButton) {
            sessionNum = "1";
        }
        startMathQuestions(v);
    }

    public void onRadioButtonClicked(View view){
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.easierRB:
                if (checked)
                    difficultyGroup = 0;
                break;
            case R.id.harderRB:
                if (checked)
                    difficultyGroup = 1;
                break;
            case R.id.controlRB:
                if (checked)
                    expGroup = 0;
                break;
            case R.id.modelRB:
                if (checked)
                    expGroup = 1;
        }
    }

    public void onBreakRadioButtonClicked(View view){
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.yesRB:
                if (checked)
                    breakTaken = 1;
                break;
            case R.id.noRB:
                if (checked)
                    breakTaken = 0;
                break;
        }
    }

    public void connectTablet(View view){
        String ipInput = iPandPort.getText().toString();
        String ipaddress = ipInput.split(":")[0];
        String ipport = ipInput.split(":")[1];
        ConnectTask connectTask = new ConnectTask();
        connectTask.owner = this;
        connectTask.execute(ipaddress, ipport);

        connectionStatus.setText("Trying to connect to server");
    }

    public class ConnectTask extends AsyncTask<String,String,TCPClient> {

        private String ipaddress;
        public MainActivity owner;
        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                    onProgressUpdate(message);

                    Log.e("MainActivity", "Message received from server: "+message);


                }
            }, owner);

            if (this.validIP(message[0])){
                mTcpClient.setIpAddress(message[0]);
                mTcpClient.setIpPortVar(Integer.parseInt(message[1]));
                //if valid, write ip in text file
                BufferedWriter writer = null;
                try
                {
                    writer = new BufferedWriter(new FileWriter("/sdcard/Movies/ip.txt"));
                    writer.write(message[0]);
                }
                catch (IOException e)
                {
                }
                finally
                {
                    try
                    {
                        if ( writer != null)
                            writer.close( );
                    }
                    catch ( IOException e)
                    {
                    }
                }

            } else {  //if not valid IP, try to read the one from the text file
                String ipaddress = null;
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader("/sdcard/Movies/ip.txt"));
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    String savedIP = br.readLine();
                    br.close();
                    if (this.validIP(savedIP))
                        mTcpClient.setIpAddress(savedIP);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // mTcpClient.setIpAddress(message[0]);
            TCPClient.singleton = mTcpClient;
            mTcpClient.run();

            return null;
        }

        public boolean validIP(String ip) {
            if (ip == null || ip.isEmpty()) return false;
            ip = ip.trim();
            if ((ip.length() < 6) & (ip.length() > 15)) return false;

            try {
                Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
                Matcher matcher = pattern.matcher(ip);
                return matcher.matches();
            } catch (PatternSyntaxException ex) {
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //in the arrayList we add the messaged received from server
            // arrayList.add(values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            //mAdapter.notifyDataSetChanged();
        }
    }
}