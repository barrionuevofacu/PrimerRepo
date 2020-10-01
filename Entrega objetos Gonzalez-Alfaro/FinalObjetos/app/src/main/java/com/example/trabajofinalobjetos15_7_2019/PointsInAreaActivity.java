package com.example.trabajofinalobjetos15_7_2019;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.trabajofinalobjetos15_7_2019.DTOs.LocationDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PointsInAreaActivity extends AppCompatActivity {

    private Api_Interface api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_in_area);

        LocationDTO location = new LocationDTO(getIntent().getStringExtra("coordinates"));
        String token = getIntent().getStringExtra("token");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getString(R.string.base_Url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(Api_Interface.class);
        Call<List<LocationDTO>> call = api.getLocationsInArea(location, token);
        call.enqueue(new Callback<List<LocationDTO>>() {
            @Override
            public void onResponse(Call<List<LocationDTO>> call, Response<List<LocationDTO>> response) {
                if (!response.isSuccessful())
                    return;

                List<LocationDTO> locations = response.body();
                ArrayList<String> stringList = new ArrayList<>();
                if (locations.size() > 0){
                    for (LocationDTO l : locations)
                        stringList.add(l.getData());
                }
                else
                    stringList = null;
                //LLAMO AL ACTIVITY DEL MAPA
                Intent intent = new Intent(PointsInAreaActivity.this, MapActivity.class);
                intent.putExtra("imgs", stringList);
                intent.putExtra("Token", getIntent().getStringExtra("token"));
                intent.putExtra("vieneDePointsInArea",true);
                startActivityForResult(intent, 2);
                //initRecyclerView(stringList);
            }

            @Override
            public void onFailure(Call<List<LocationDTO>> call, Throwable t) {
                Toast toast = Toast.makeText(getApplicationContext(), "Falla de servicio", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }

    public void initRecyclerView(ArrayList<String> stringList) {
        RecyclerView recyclerView = findViewById(R.id.recycler);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(stringList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
