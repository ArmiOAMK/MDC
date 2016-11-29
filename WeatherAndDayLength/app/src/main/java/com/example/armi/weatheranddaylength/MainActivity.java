package com.example.armi.weatheranddaylength;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    public double longitude;
    public double latitude;

    TextView sunriseOutput;
    TextView sunsetOutput;
    EditText input;

    //Tehtävälista, mikä tulee suorittaa oikeassa järjestyksessä
    List<MyTask> tasks;

    //JSON parse tulokset tallennetaan näihin listoihin
    List<String> locationResultList;
    List<String> sunriseResultList;
    List<String> timezoneResultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sunriseOutput = (TextView) findViewById(R.id.textView);
        sunsetOutput = (TextView) findViewById(R.id.textView2);

        input = (EditText) findViewById(R.id.editText);

        tasks = new ArrayList<>();
    }

    //Nappia painamalla haetaan ensimmäisenä osoitteelle koordinaatit, mitä tarvitaan myöhemmissä tehtävissä
    public void onClick(View view){
        String inputString = String.valueOf(input.getText()).replaceAll("\\s+","+");
        requestData("https://maps.googleapis.com/maps/api/geocode/json?&address=" + inputString, 1);
        sunriseOutput.setText("");
        sunsetOutput.setText("");
    }

    //Luodaan uusi yleiskäyttöinen WebFetch olio, mikä suorietaan taskina
    private void requestData(String uri, int resultIndex) {
        WebFetch p = new WebFetch();
        p.setMethod("POST");
        p.setUri(uri);
        p.setResultIndex(resultIndex);

        MyTask task = new MyTask();
        task.execute(p);
    }

    //Päivitetään UI oikeilla aikatiedoilla (käytetään hyväksi timeChange() metodia)
    protected void updateDisplay() {
        if (sunriseResultList != null){
            sunriseOutput.append("Aurinko nousee klo " + timeChange(sunriseResultList.get(0), Integer.parseInt(timezoneResultList.get(0))));
            sunsetOutput.append("Aurinko laskee klo " + timeChange(sunriseResultList.get(1), Integer.parseInt(timezoneResultList.get(0))));
        }
    }

    //Muutetaan saatu aika oikeaan muotoon ja paikallisaikaan aikavyöhykkeen offsetin avulla
    private String timeChange(String date, int seconds){
        java.text.SimpleDateFormat original = new java.text.SimpleDateFormat("hh:mm:ss a");
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("HH:mm:ss");

        Date testDate = null;

        try {
            testDate = original.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(testDate);
            calendar.add(Calendar.SECOND, seconds);
            testDate = calendar.getTime();
        } catch(Exception e){
            e.printStackTrace();
        }

        String newFormat = formatter.format(testDate);
        return newFormat;
    }


    //Suoritetaan haut taustalla AsyncTaskia hyväksikäyttäen
    private class MyTask extends AsyncTask<WebFetch, String, String> {

        @Override
        protected void onPreExecute() {
            tasks.add(this);
        }

        @Override
        protected String doInBackground(WebFetch... params) {
            String content = HTTPManager.getData(params[0]);
            return content;
        }

        @Override
        protected void onPostExecute(String result) {

            int taskIndex = tasks.indexOf(this);

            if (taskIndex == 0){
                locationResultList = JSONParser.parseFeed(result, 1);
                latitude = Double.parseDouble(locationResultList.get(0));
                longitude = Double.parseDouble(locationResultList.get(1));
                requestData("http://api.sunrise-sunset.org/json?lat=" + latitude + "&lng=" + longitude, 2);
            } else if (taskIndex == 1){
                sunriseResultList = JSONParser.parseFeed(result, 2);
                requestData("https://maps.googleapis.com/maps/api/timezone/json?location=" + latitude + "," + longitude + "&timestamp=1130161200&key=AIzaSyDKO6b57DrL9G-rtjDZAe4bOdLgo4mz_ns", 3);
            } else if (taskIndex == 2){
                timezoneResultList = JSONParser.parseFeed(result, 3);
                updateDisplay();
                tasks.clear();
            }

        }

    }
}
