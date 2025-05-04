package com.example.vehiclespotapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LocationActivity extends AppCompatActivity {
    private AutoCompleteTextView countrySpinner;
    private AutoCompleteTextView stateSpinner;
    private AutoCompleteTextView citySpinner;
    private TextView selectedLocation;
    private String selectedCountry = "";
    private String selectedState = "";
    private String selectedCity = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // Initialize views
        countrySpinner = findViewById(R.id.countrySpinner);
        stateSpinner = findViewById(R.id.stateSpinner);
        citySpinner = findViewById(R.id.citySpinner);
        selectedLocation = findViewById(R.id.selectedLocation);

        // Set up country spinner
        String[] countries = LocationData.getCountries();
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                countries
        );
        countrySpinner.setAdapter(countryAdapter);

        // Set up state spinner
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{}
        );
        stateSpinner.setAdapter(stateAdapter);

        // Set up city spinner
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{}
        );
        citySpinner.setAdapter(cityAdapter);

        // Country selection listener
        countrySpinner.setOnItemClickListener((parent, view, position, id) -> {
            selectedCountry = countries[position];
            selectedState = "";
            selectedCity = "";
            
            // Update states
            String[] states = LocationData.getStates(selectedCountry);
            stateAdapter.clear();
            stateAdapter.addAll(states);
            stateAdapter.notifyDataSetChanged();
            
            // Clear city selection
            cityAdapter.clear();
            cityAdapter.notifyDataSetChanged();
            
            updateSelectedLocation();
        });

        // State selection listener
        stateSpinner.setOnItemClickListener((parent, view, position, id) -> {
            selectedState = stateAdapter.getItem(position);
            selectedCity = "";
            
            // Update cities
            String[] cities = LocationData.getCities(selectedCountry, selectedState);
            cityAdapter.clear();
            cityAdapter.addAll(cities);
            cityAdapter.notifyDataSetChanged();
            
            updateSelectedLocation();
        });

        // City selection listener
        citySpinner.setOnItemClickListener((parent, view, position, id) -> {
            selectedCity = cityAdapter.getItem(position);
            updateSelectedLocation();
        });
    }

    private void updateSelectedLocation() {
        if (!selectedCountry.isEmpty() && !selectedState.isEmpty() && !selectedCity.isEmpty()) {
            String location = String.format("Selected Location: %s, %s, %s", 
                selectedCity, selectedState, selectedCountry);
            selectedLocation.setText(location);
            Toast.makeText(this, location, Toast.LENGTH_SHORT).show();
        }
    }
} 