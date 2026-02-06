package com.example.lab5_starter;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private Button deleteCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private CityArrayAdapter cityArrayAdapter;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        addCityButton = findViewById(R.id.buttonAddCity);
        deleteCityButton = findViewById(R.id.buttonDeleteCity);
        cityListView = findViewById(R.id.listviewCities);

        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
                return;
            }

            cityArrayList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot snapshot : value) {
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");
                    cityArrayList.add(new City(name, province));
                }
            }
            cityArrayAdapter.notifyDataSetChanged();
        });

        cityListView.setOnItemClickListener((parent, view, position, id) -> {
            City city = cityArrayAdapter.getItem(position);
            CityDialogFragment dialog =
                    CityDialogFragment.newInstance(city);
            dialog.show(getSupportFragmentManager(), "City Details");
        });

        cityListView.setOnItemLongClickListener((parent, view, position, id) -> {
            City city = cityArrayAdapter.getItem(position);
            if (city != null) {
                deleteCity(city);
                Toast.makeText(this,
                        city.getName() + " deleted",
                        Toast.LENGTH_SHORT).show();
            }
            return true; // consume long press
        });

        addCityButton.setOnClickListener(v -> {
            new CityDialogFragment()
                    .show(getSupportFragmentManager(), "Add City");
        });

        deleteCityButton.setOnClickListener(v -> {
            Toast.makeText(
                    MainActivity.this,
                    "Long tap a city to delete it",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    @Override
    public void addCity(City city) {
        citiesRef.document(city.getName()).set(city);
    }

    @Override
    public void updateCity(City city, String title, String year) {
        if (city == null) return;

        if (!city.getName().equals(title)) {
            citiesRef.document(city.getName()).delete();
        }

        city.setName(title);
        city.setProvince(year);
        citiesRef.document(title).set(city);
    }

    private void deleteCity(City city) {
        citiesRef.document(city.getName()).delete();
    }
}




