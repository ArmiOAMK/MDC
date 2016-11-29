package com.example.armi.sunrisesunset;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;


//Yleiskäyttöinen JSON parser, millä käsitellään kaikki kolme JSON datahakua.
public class JSONParser {
    public static List<String> parseFeed(String content, int parseIndex){

        try {

            JSONObject object = new JSONObject(content);

            if (parseIndex == 1) {

                JSONArray ar = object.getJSONArray("results");
                JSONObject obj = ar.getJSONObject(0);
                JSONObject geo = obj.getJSONObject("geometry");
                JSONObject loc = geo.getJSONObject("location");
                String lat = loc.getString("lat");
                String lng = loc.getString("lng");
                List<String> jsonList = new ArrayList<>();

                jsonList.add(lat);
                jsonList.add(lng);

                return jsonList;

            } else if (parseIndex == 2){

                JSONObject obj = object.getJSONObject("results");
                String sunrise = obj.getString("sunrise");
                String sunset = obj.getString("sunset");
                List<String> jsonList = new ArrayList<>();


                jsonList.add(sunrise);
                jsonList.add(sunset);

                return jsonList;

            } else if (parseIndex == 3){
                String obj = object.getString("rawOffset");
                List<String> jsonList = new ArrayList<>();

                jsonList.add(obj);

                return jsonList;
            } else {
                return null;
            }


        } catch (JSONException e){
            e.printStackTrace();

            return null;
        }

    }
}
