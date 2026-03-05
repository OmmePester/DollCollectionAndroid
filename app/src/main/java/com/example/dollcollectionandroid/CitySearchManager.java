package com.example.dollcollectionandroid;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CitySearchManager {
    // client is the engine driving the request to the web
    private final OkHttpClient client = new OkHttpClient();

    // interface to send results back to the Activity UI thread
    public interface CityCallback {
        void onResult(List<String> cities, List<Double> lats, List<Double> lons);
        void onError(String error);
    }

    public void searchCity(String query, CityCallback callback) {
        // Nominatim (OpenStreetMap) API URL.
        // We add 'format=json' to get data we can parse, and 'limit=5' to keep it fast

        // Changed featuretype to class=place to ensure major cities like Moscow are caught.
        // Added 'q' encoding to handle spaces in city names correctly.
        String url = "https://nominatim.openstreetmap.org/search?q=" + query
                + "&format=json&addressdetails=1&class=place&limit=30";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "DollCollectionApp_v1_Personal_Study_Project")    // requires a User-Agent header
                .build();

        // enqueue runs the search in the "background" so the app doesn't freeze
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Unexpected code " + response);
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    JSONArray jsonArray = new JSONArray(jsonData);
                    List<String> names = new ArrayList<>();
                    List<Double> lats = new ArrayList<>();
                    List<Double> lons = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        // Extracting the "display_name", "lat", and "lon" from the web data
                        // Nominatim sends lat/lon as Strings. We MUST use Double.parseDouble [cite: 2026-03-03]
                        names.add(obj.getString("display_name"));
                        lats.add(Double.parseDouble(obj.getString("lat")));
                        lons.add(Double.parseDouble(obj.getString("lon")));
                    }
                    callback.onResult(names, lats, lons);
                } catch (Exception e) {
                    callback.onError("Parsing error: " + e.getMessage());
                }
            }
        });
    }
}