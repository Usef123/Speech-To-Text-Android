package org.tnt.speech.speechtotext;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
public class SpeechToTextActivity extends AppCompatActivity {
    private EditText mQuickNoteEdit;
    private TextView recordAudioTitle;
    private ScrollView quicknoteScrollView;
    private ImageButton buttonRecordAudio;
    public Context mContext;
    private boolean mStartRecognizing = false;
    private SpeechRecognizer speechRecognizer = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#bd2130")));
            getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#bd2130")));
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_speech_main);
        this.mContext = this;

        buttonRecordAudio = findViewById(R.id.buttonRecordAudio);
        buttonRecordAudio.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mStartRecognizing == false) {
                    startListeningRecognition();
                }
                else {
                    stopStopRecognition();
                }
            }
        });

        quicknoteScrollView = findViewById(R.id.quicknoteScrollView);
        quicknoteScrollView.setVerticalScrollBarEnabled(false);
        recordAudioTitle = findViewById(R.id.recordAudioTitle);
        mQuickNoteEdit = findViewById(R.id.tv_quicknotecontent);

        mQuickNoteEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    stopStopRecognition();
                }
                else {}
                return false;
            }
        });
        startListeningRecognition();
    }

    class SpeechListener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params)	{
        }
        public void onBeginningOfSpeech(){
        }
        public void onRmsChanged(float rmsdB){
        }
        public void onBufferReceived(byte[] buffer)	{
        }
        public void onEndOfSpeech()	{
        }
        public void onError(int error)	{
            String strError = getErrorText(error);
            if(strError.isEmpty()){
                stopStopRecognition();
                Toast.makeText(SpeechToTextActivity.this, "Connection problem or Google speech service is temporarily pause", Toast
                        .LENGTH_SHORT).show();
            }
            else {
                startListeningRecognition();
            }
        }
        public void onResults(Bundle results) {

            ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if(matches != null ) {
                if (matches.size() > 0) {
                    String textContent = matches.get(0);
                    String strContent = mQuickNoteEdit.getText().toString();
                    mQuickNoteEdit.setText(strContent + "\n" + textContent);
                }
            }
            stopStopRecognition();
        }
        public void onPartialResults(Bundle partialResults)
        {
        }
        public void onEvent(int eventType, Bundle params)
        {
        }
    }

    private void startListeningRecognition(){
        if(mQuickNoteEdit.isShown()){
            setHideKeyboard(mQuickNoteEdit);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int   hasContactPermission = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO);
            if(hasContactPermission != PackageManager.PERMISSION_GRANTED ) {
               requestAllPermission();
            }else {
                listenRecognizer();
            }
        }
        else{
            listenRecognizer();
        }
    }

    private void listenRecognizer(){
        mStartRecognizing = true;
        buttonRecordAudio.setImageResource(R.mipmap.mn_recordstarting);
        recordAudioTitle.setText("Listening...");
        Intent  recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        if(recognizerIntent != null){
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi");
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "vi");
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "vi");
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            if(speechRecognizer == null){
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
                speechRecognizer.setRecognitionListener(new SpeechListener());
            }
            speechRecognizer.startListening(recognizerIntent);
        }
        else{}

    }
    private void stopStopRecognition(){
        mStartRecognizing = false;
        buttonRecordAudio.setImageResource(R.mipmap.mn_recordstart);
        recordAudioTitle.setText("Touch to speak");
        killSpeech();
    }

    private  void killSpeech(){
        if(speechRecognizer != null){
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    private void setHideKeyboard(View view){
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }




    @Override
    protected void onResume() {
        super.onResume();

    }
    @Override
    protected void onPause() {
        super.onPause();
        if(mQuickNoteEdit.isShown()){
            setHideKeyboard(mQuickNoteEdit);
        }
        mStartRecognizing = false;
        stopStopRecognition();

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mStartRecognizing = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode){
            case 102:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   startListeningRecognition();
                }
                else {
                    ActivityCompat.shouldShowRequestPermissionRationale(SpeechToTextActivity.this, Manifest.permission.RECORD_AUDIO);
                }
            }

            break;
        }
    }

    private void requestAllPermission() {
        int hasContactPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if(hasContactPermission != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 102);
        }
        else {
            startListeningRecognition();
        }
    }
    public static String getErrorText(int errorCode) {
        String message = "";
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
            case SpeechRecognizer.ERROR_CLIENT:
            case SpeechRecognizer.ERROR_NETWORK:
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
            case SpeechRecognizer.ERROR_SERVER:
                message = "";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
            case SpeechRecognizer.ERROR_NO_MATCH:
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
            default:
                message = "error";
                break;
        }
        return message;
    }
}
