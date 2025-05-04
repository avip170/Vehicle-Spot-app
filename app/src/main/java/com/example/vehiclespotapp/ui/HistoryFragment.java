package com.example.vehiclespotapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehiclespotapp.R;
import com.example.vehiclespotapp.data.ParkingLocation;
import com.example.vehiclespotapp.viewmodel.ParkingViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {
    private ParkingViewModel parkingViewModel;
    private HistoryAdapter historyAdapter;
    private RecyclerView recyclerView;
    private TextView emptyHistoryText;
    private Button clearAllButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        
        recyclerView = view.findViewById(R.id.historyRecyclerView);
        emptyHistoryText = view.findViewById(R.id.emptyHistoryText);
        clearAllButton = view.findViewById(R.id.clearAllButton);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        historyAdapter = new HistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(historyAdapter);

        clearAllButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Clear All History")
                    .setMessage("Are you sure you want to delete all parking history?")
                    .setPositiveButton("Clear All", (dialog, which) -> {
                        parkingViewModel.deleteAll();
                        Toast.makeText(requireContext(), "All history cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        parkingViewModel = new ViewModelProvider(requireActivity()).get(ParkingViewModel.class);
        parkingViewModel.getAllLocations().observe(getViewLifecycleOwner(), locations -> {
            if (locations != null) {
                historyAdapter.updateLocations(new ArrayList<>(locations));
                updateEmptyView(locations);
                // Show/hide clear all button based on whether there are items
                clearAllButton.setVisibility(locations.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void updateEmptyView(List<ParkingLocation> locations) {
        if (locations == null || locations.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyHistoryText.setVisibility(View.VISIBLE);
            clearAllButton.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyHistoryText.setVisibility(View.GONE);
            clearAllButton.setVisibility(View.VISIBLE);
        }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
        private List<ParkingLocation> locations;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

        public HistoryAdapter(List<ParkingLocation> locations) {
            this.locations = locations;
        }

        public void updateLocations(List<ParkingLocation> newLocations) {
            this.locations = newLocations;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_parking_history, parent, false);
            return new HistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
            ParkingLocation location = locations.get(position);
            holder.bind(location);
        }

        @Override
        public int getItemCount() {
            return locations.size();
        }

        class HistoryViewHolder extends RecyclerView.ViewHolder {
            private final TextView dateTimeText;
            private final TextView locationText;
            private final Button navigateButton;
            private final ImageButton deleteButton;
            private final SimpleDateFormat dateFormat;

            public HistoryViewHolder(@NonNull View itemView) {
                super(itemView);
                dateTimeText = itemView.findViewById(R.id.dateTimeText);
                locationText = itemView.findViewById(R.id.locationText);
                navigateButton = itemView.findViewById(R.id.navigateButton);
                deleteButton = itemView.findViewById(R.id.btnDelete);
                dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            }

            public void bind(ParkingLocation location) {
                dateTimeText.setText(dateFormat.format(location.getTimestamp()));
                String locationString = location.getAddress();
                if (locationString == null || locationString.isEmpty()) {
                    locationString = String.format(Locale.getDefault(), 
                        "Lat: %.6f, Long: %.6f", 
                        location.getLatitude(), 
                        location.getLongitude());
                }
                locationText.setText(locationString);

                navigateButton.setOnClickListener(v -> {
                    Uri gmmIntentUri = Uri.parse(String.format(Locale.US,
                        "google.navigation:q=%f,%f", 
                        location.getLatitude(), 
                        location.getLongitude()));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    
                    if (mapIntent.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                        itemView.getContext().startActivity(mapIntent);
                    } else {
                        Toast.makeText(itemView.getContext(), 
                            "Google Maps app is not installed", 
                            Toast.LENGTH_SHORT).show();
                    }
                });

                deleteButton.setOnClickListener(v -> {
                    new MaterialAlertDialogBuilder(itemView.getContext())
                            .setTitle(R.string.delete_location_title)
                            .setMessage(R.string.delete_location_message)
                            .setPositiveButton(R.string.delete, (dialog, which) -> {
                                // Delete from the database first
                                parkingViewModel.delete(location);
                                
                                // Show success message
                                Toast.makeText(itemView.getContext(), 
                                    R.string.location_deleted, 
                                    Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                });
            }
        }
    }
} 