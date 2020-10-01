package com.example.trabajofinalobjetos15_7_2019;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trabajofinalobjetos15_7_2019.DTOs.UserDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignInActivity extends AppCompatActivity {

    private Api_Interface api;
    Button signInButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        final TextView userName = findViewById(R.id.usernameTextview);
        final TextView password = findViewById(R.id.passwordTextview);
        final TextView email = findViewById(R.id.emailTextView);
        final TextView telefono = findViewById(R.id.telefonoTextView);
        signInButton = findViewById(R.id.DeleteButton);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(userName.getText()) || TextUtils.isEmpty(password.getText()) || TextUtils.isEmpty(email.getText()) || TextUtils.isEmpty(telefono.getText())) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Complete todos los campos por favor", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    signInButton.setEnabled(false);
                    UserDTO user = new UserDTO(userName.getText().toString(), password.getText().toString(), email.getText().toString(), telefono.getText().toString());
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(getResources().getString(R.string.base_Url))
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    api = retrofit.create(Api_Interface.class);
                    Call<UserDTO> call = api.signIn(user);
                    call.enqueue(new Callback<UserDTO>() {
                        @Override
                        public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                            signInButton.setEnabled(true);
                            if (!response.isSuccessful()) {
                                Toast toast = Toast.makeText(getApplicationContext(), "Ingrese otro Username", Toast.LENGTH_SHORT);
                                toast.show();
                                return;
                            }
                            UserDTO u = response.body();
                            String username = u.getUserName();
                            Intent i = new Intent();
                            i.putExtra("username", username);
                            SignInActivity.this.setResult(RESULT_OK, i);
                            SignInActivity.this.finish();
                        }

                        @Override
                        public void onFailure(Call<UserDTO> call, Throwable t) {
                            signInButton.setEnabled(true);
                            Toast toast = Toast.makeText(getApplicationContext(), "Falla de servicio", Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }
                    });
                }
            }
        });
    }
}
