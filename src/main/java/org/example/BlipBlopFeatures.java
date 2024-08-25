package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class BlipBlopFeatures {

    private List<String> features;

    public BlipBlopFeatures(String jsonFilePath) {
        try {
            Gson gson = new Gson();

            Type featureListType = new TypeToken<List<String>>() {}.getType();
            features = gson.fromJson(new FileReader(jsonFilePath), featureListType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getFeatures() {
        return features;
    }
}
