package com.dauntlessdev.celebrityguess;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    Button button0;
    Button button1;
    Button button2;
    Button button3;
    ImageView imageView;
    int correctAnswerButton;
    ArrayList<String> nameList;
    ArrayList<String> picList;
    ArrayList<String> choiceList;
    public class CodeDownloader extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1 ){
                    char current = (char) data;
                    result = result + current;
                    data = reader.read();
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Failed";
        }
    }
    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream in = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    public void checkAnswer(View view){
        String msg = "";
        if (Integer.parseInt(view.getTag().toString().trim()) == correctAnswerButton)  msg = "Correct!";
        else  msg = "Wrong!";

        generateQuestion();

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    public void generateQuestion(){

        Random random = new Random();
        int correctAnswer = random.nextInt(picList.size());

        ImageDownloader picDownloader = new ImageDownloader();
        Bitmap bitmap = null;
        try {
            bitmap = picDownloader.execute(picList.get(correctAnswer)).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
        choiceList.clear();
        correctAnswerButton = random.nextInt(4);
        for (int i = 0; i < 4; i++){
            if ( i == correctAnswerButton) choiceList.add(nameList.get(correctAnswer));
            int otherChoice = random.nextInt(picList.size());
            while(choiceList.contains(nameList.get(otherChoice)) || otherChoice == correctAnswer){
                otherChoice = random.nextInt(picList.size());
            }
            choiceList.add(nameList.get(otherChoice));
        }

        button0.setText(choiceList.get(0));
        button1.setText(choiceList.get(1));
        button2.setText(choiceList.get(2));
        button3.setText(choiceList.get(3));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button0 = findViewById(R.id.button1);
        button1 = findViewById(R.id.button2);
        button2 = findViewById(R.id.button3);
        button3 = findViewById(R.id.button4);
        imageView = findViewById(R.id.imageView);

        picList = new ArrayList<>();
        nameList = new ArrayList<>();
        choiceList = new ArrayList<>();

        CodeDownloader  codeDownloader = new CodeDownloader();
        String allCode = null;
        try {
            allCode = codeDownloader.execute("http://www.posh24.se/kandisar/").get();
            String[] splitCode = allCode.split("<div class=\"listedArticles\">");
            Pattern namePattern = Pattern.compile("alt=\"(.*?)\"");
            Matcher nameMatcher = namePattern.matcher(splitCode[0]);
            while(nameMatcher.find()){
                nameList.add(nameMatcher.group(1));
                //Log.i("Name: " ,nameMatcher.group(1));
            }
            Pattern picPattern = Pattern.compile("img src=\"(.*?)\"");
            Matcher picMatcher = picPattern.matcher(splitCode[0]);
            while(picMatcher.find()){
                picList.add(picMatcher.group(1));
                //Log.i("Pic: " ,picMatcher.group(1));
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        generateQuestion();
    }
}
