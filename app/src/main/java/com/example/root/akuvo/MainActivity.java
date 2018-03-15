package com.example.root.akuvo;



import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;



public class MainActivity extends AppCompatActivity {

    TextView textToSpeechResultField,automatedResponseResultField;
    ImageButton speechToTextButton,textToSpeechButton;
    Button clearButton,copyButton,callButton,scanButton;
    SpeechRecognizer speechRecognizer;
    Intent speechRecognizerIntent;
    MultiAutoCompleteTextView toSpeakField;
    TextToSpeech textToSpeech;
    ConversationService watsonConversationService;
    String userName,userEmailID,userBloodGroup,userDateOfBirth;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    /*
           IBM Watson Conversation Credentials
     */
    private static final String WATSON_CONVERSATION_USERNAME = "d0311543-0b7d-47d3-8563-cb5d8c2b28c5";
    private static final String WATSON_CONVERSATION_PASSWORD= "R71H3Oluaunl";
    private static final String WATSON_WORKSPACE_ID= "669cc349-1dba-4aea-8d1b-ca78431f50ed";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /*
                Connecting to lawout elements
         */
        textToSpeechResultField=(TextView) findViewById(R.id.textToSpeechResultField);
        automatedResponseResultField=(TextView) findViewById(R.id.automatedResponseResultField);
        speechToTextButton=(ImageButton) findViewById(R.id.speechToTextButton);
        textToSpeechButton=(ImageButton) findViewById(R.id.textToSpeechButton);
        clearButton=(Button) findViewById(R.id.clearButton);
        copyButton=(Button) findViewById(R.id.copyButton);
        callButton=(Button) findViewById(R.id.callButton);
        scanButton=(Button) findViewById(R.id.scanButton);
        toSpeakField=(MultiAutoCompleteTextView)findViewById(R.id.toSpeakField);

        /*
         Checking for UserDetails.txt which contains Username and other Details for Automated Response System
         */
        File file = new File(getApplicationContext().getFilesDir(),"UserDetails.dat");
        if(file.exists()){
            try{
                FileInputStream fis = openFileInput("UserDetails.dat");
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                userName = br.readLine();
                userDateOfBirth = br.readLine();
                userBloodGroup=br.readLine();
                userEmailID=br.readLine();

            }catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error Reading file!",Toast.LENGTH_SHORT).show();
            }

        }
        else{
            Toast.makeText(MainActivity.this,"File Not Found",Toast.LENGTH_SHORT).show();

            startActivity(new Intent(MainActivity.this,UserDetailsActivity.class));
        }
        /*
                Connnection to IBM  Watson Conversation Service
         */
        watsonConversationService = new ConversationService(ConversationService.VERSION_DATE_2017_02_03);
        watsonConversationService.setUsernameAndPassword(WATSON_CONVERSATION_USERNAME,WATSON_CONVERSATION_PASSWORD);

        /*
                Speech Recognition Class
         */
        speechRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Akuvo");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {
                Log.i( "onRmsChanged: ","hello" );

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        /*
        Setting Up text to speech Language Locale.getDefault() can be repalced by preffered Language .
         */
        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.getDefault());
                }
            }
        });

        /*
                Speech to Text Activation Button. Activates Google SpeechToText Feature.
         */
        speechToTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });
        /*
                Text To Speech  Convertion Button. Speaks out contents of toSpeak field.
         */
        textToSpeechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String toSpeak = toSpeakField.getText().toString();
                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
        /*
                Reset Button to clear all fields
         */
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeechResultField.setText("");
                toSpeakField.setText("");
                automatedResponseResultField.setText("");
            }
        });
        /*
                Copy Button
                copies all contents of automatedResponse field to toSpeak Field;
         */
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toSpeakField.setText(automatedResponseResultField.getText());
                automatedResponseResultField.setText("");
            }
        });
        /*
                 Call Button
                 used for remote communication.
         */
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(MainActivity.this,AudioRecoderActivity.class));;
                startActivity(new Intent(MainActivity.this,RecordedAudioListActivity.class));
            }
        });
        /*
                  Scan Button
                  used for audio scanning and matching with prerecorded sounds.
         */
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,AudioScannerActivity.class));
            }
        });

    }
    private void promptSpeechInput() {
        try {
            startActivityForResult(speechRecognizerIntent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Not Supported",
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                            String SpeechResult=result.get(0);

                            textToSpeechResultField.setText(SpeechResult);

                    try {
                        String WatsonResponse = new FindRespose().execute(SpeechResult).get();
                        automatedResponseResultField.setText(WatsonResponse);
                    }catch (Exception e) {
                    }
                }
                break;
            }

        }
    }
    /*
           Communcation with IBM Watson Conversation Service.
           Uses AsyncTask due to mainactivity can't access internet directly
     */
    private class FindRespose extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... request) {
            String ResponseStr="No Automated Respose";
            try {

                Log.i("test",request[0]);

                MessageRequest newMessage = new MessageRequest.Builder().inputText(request[0]).build();

                JSONObject ResposeJSON;

                MessageResponse response = watsonConversationService.message(WATSON_WORKSPACE_ID, newMessage).execute();
                Log.i("test",response.toString());
                ResposeJSON = new JSONObject(response.toString());
                JSONObject WatsonOutput=ResposeJSON.getJSONObject("output");
                ResponseStr=WatsonOutput.getString("text");
                // Removing [""] from JSON Object from Watson
                ResponseStr=ResponseStr.replaceAll("\"","").replaceAll("\\[", "").replaceAll("\\]","");
                // Customizing Automated response specialized for each user.
                ResponseStr = ResponseStr.replaceAll("UNAME",userName).replaceAll("UDOB",userDateOfBirth).replaceAll("UBLOOD",userBloodGroup).replaceAll("UEMAIL",userEmailID);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ResponseStr;
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

}
