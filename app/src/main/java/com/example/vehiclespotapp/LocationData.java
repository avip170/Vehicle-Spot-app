package com.example.vehiclespotapp;

import java.util.HashMap;
import java.util.Map;

public class LocationData {
    private static final Map<String, Map<String, String[]>> locationData = new HashMap<>();

    static {
        // India data
        Map<String, String[]> indiaStates = new HashMap<>();
        indiaStates.put("Gujarat", new String[]{"Ahmedabad", "Surat"});
        indiaStates.put("Maharashtra", new String[]{"Mumbai", "Pune"});
        locationData.put("India", indiaStates);

        // USA data
        Map<String, String[]> usaStates = new HashMap<>();
        usaStates.put("California", new String[]{"Los Angeles", "San Francisco"});
        usaStates.put("Texas", new String[]{"Houston", "Dallas"});
        locationData.put("USA", usaStates);
    }

    public static String[] getCountries() {
        return locationData.keySet().toArray(new String[0]);
    }

    public static String[] getStates(String country) {
        if (locationData.containsKey(country)) {
            return locationData.get(country).keySet().toArray(new String[0]);
        }
        return new String[0];
    }

    public static String[] getCities(String country, String state) {
        if (locationData.containsKey(country) && locationData.get(country).containsKey(state)) {
            return locationData.get(country).get(state);
        }
        return new String[0];
    }
} 