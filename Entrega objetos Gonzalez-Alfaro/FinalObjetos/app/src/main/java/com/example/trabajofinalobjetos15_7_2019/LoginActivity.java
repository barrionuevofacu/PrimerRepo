package com.example.trabajofinalobjetos15_7_2019;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trabajofinalobjetos15_7_2019.DTOs.UserDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private TextView passwordTextView;
    private Api_Interface api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userNameTextView = findViewById(R.id.tagTextView);
        passwordTextView = findViewById(R.id.passwordField);
        Button loginButton = findViewById(R.id.SaveButton);
        Button signInButton = findViewById(R.id.DeleteButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(userNameTextView.getText()) || TextUtils.isEmpty(passwordTextView.getText())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Inserte usuario y contraseña", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    UserDTO user = new UserDTO(userNameTextView.getText().toString(), passwordTextView.getText().toString(), "");
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(getResources().getString(R.string.base_Url))
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    api = retrofit.create(Api_Interface.class);

                    Call<UserDTO> call = api.login(user);
                    call.enqueue(new Callback<UserDTO>() {
                        @Override
                        public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                            if (!response.isSuccessful()) {
                                Toast toast = Toast.makeText(getApplicationContext(), "Contraseña inválida", Toast.LENGTH_SHORT);
                                toast.show();
                                return;
                            }
                            UserDTO u = response.body();
                            Intent i = new Intent(LoginActivity.this, MapActivity.class);
                            String token = u.getToken();
                            String username = u.getUserName();
                            i.putExtra("Token", token);
                            i.putExtra("username", username);
                            startActivity(i);
                        }

                        @Override
                        public void onFailure(Call<UserDTO> call, Throwable t) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Falla de servicio", Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }
                    });

                }
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SignInActivity.class);
                startActivityForResult(i, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            userNameTextView.setText(data.getStringExtra("username"));
        }
    }
}
