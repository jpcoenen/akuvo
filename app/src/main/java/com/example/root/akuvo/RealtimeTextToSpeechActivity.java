package com.example.root.akuvo;


        import android.Manifest;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.os.Bundle;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.text.TextUtils;
        import android.text.method.ScrollingMovementMethod;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.widget.TextView;

        import butterknife.BindView;
        import butterknife.ButterKnife;

public class RealtimeTextToSpeechActivity extends AppCompatActivity {


    private static final int RECORD_REQUEST_CODE = 101;
    @BindView(R.id.status)
    TextView status;
    @BindView(R.id.textMessage)
    TextView textMessage;

    TextView covertedTextField ;
    String TEXTVIEW_HISTORY;
    private SpeechAPI speechAPI;
    private VoiceRecorder mVoiceRecorder;
    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            if (speechAPI != null) {
                speechAPI.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (speechAPI != null) {
                speechAPI.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            if (speechAPI != null) {
                speechAPI.finishRecognizing();
            }
        }

    };
    private final SpeechAPI.Listener mSpeechServiceListener =
            new SpeechAPI.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if (isFinal) {
                        mVoiceRecorder.dismiss();
                    }
                    if (textMessage != null && !TextUtils.isEmpty(text)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal) {
                                    // Writing Processed text to TextView.
                                    textMessage.setText(null);
                                    covertedTextField.append(" "+text);
                                    TEXTVIEW_HISTORY=text;
                                } else {
                                    textMessage.setText(text);
                                }
                            }
                        });
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_text_to_speech);;
        ButterKnife.bind(this);
        speechAPI = new SpeechAPI(RealtimeTextToSpeechActivity.this,"ml-IN");
        covertedTextField = (TextView) findViewById(R.id.covertedTextField);
        covertedTextField.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected void onStop() {
        stopVoiceRecorder();

        // Stop Cloud Speech API
        speechAPI.removeListener(mSpeechServiceListener);
        speechAPI.destroy();
        speechAPI = null;

        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isGrantedPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecorder();
        } else {
            makeRequest(Manifest.permission.RECORD_AUDIO);
        }
        speechAPI.addListener(mSpeechServiceListener);
    }

    private int isGrantedPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission);
    }

    private void makeRequest(String permission) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, RECORD_REQUEST_CODE);
    }

    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RECORD_REQUEST_CODE) {
            if (grantResults.length == 0 && grantResults[0] == PackageManager.PERMISSION_DENIED
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                finish();
            } else {
                startVoiceRecorder();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.realtime_text_to_speech_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.undo_text_translated){
            if(TEXTVIEW_HISTORY!=null){
                covertedTextField.setText(covertedTextField.getText().toString().replace(TEXTVIEW_HISTORY,""));
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
