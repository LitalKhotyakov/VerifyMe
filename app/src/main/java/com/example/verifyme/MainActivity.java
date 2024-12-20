package com.example.verifyme;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private ImageView star1, star2, star3, star4, star5;
    private EditText passwordInput;
    private Button loginButton;
    private BroadcastReceiver wifiReceiver;
    private BroadcastReceiver bluetoothReceiver;


    private boolean isBatteryConditionMet = false;
    private boolean isInternetConditionMet = false;
    private boolean isLightConditionMet = false;
    private boolean isBluetoothConditionMet = false;
    private boolean isTemperatureConditionMet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        // Check conditions
        checkBatteryCondition();
        checkInternetCondition();
        checkLightCondition();
        checkBluetoothCondition();
        checkTemperatureCondition();

        // Setup listeners
        setupWifiListener();
        setupBluetoothListener();
        setupLightSensorListener();

        // Enable login button if all conditions are met
        loginButton.setOnClickListener(v -> {
            if (isAllConditionsMet()) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                // Navigate to SuccessActivity
                Intent intent = new Intent(MainActivity.this, SuccessActivity.class);
                startActivity(intent);
                finish(); // Optional: Close the current activity
            } else {
                Toast.makeText(this, "Conditions not met.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkBatteryCondition() {
        BatteryManager batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        // Check if password matches battery level
        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            String input = passwordInput.getText().toString();
            if (input.equals(String.valueOf(batteryLevel))) {
                isBatteryConditionMet = true;
                star1.setImageResource(R.drawable.ic_star_yellow);
                enableLoginIfConditionsMet();
            }
            return false;
        });
    }

    private void checkInternetCondition() {
        // Request permissions if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
            return;
        }

        // Check Wi-Fi connection
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI && activeNetwork.isConnected()) {
            isInternetConditionMet = true;
            star2.setImageResource(R.drawable.ic_star_yellow);
        } else {
            isInternetConditionMet = false;
            star2.setImageResource(R.drawable.ic_star_grey);
        }

        enableLoginIfConditionsMet(); // Check if all conditions are met
    }


    private void checkLightCondition() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor != null) {
            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float lightLevel = event.values[0];
                    if (lightLevel > 50) { // Example threshold for light
                        isLightConditionMet = true;
                        star3.setImageResource(R.drawable.ic_star_yellow);
                        enableLoginIfConditionsMet();
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            }, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void checkBluetoothCondition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
            return;
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled() && !bluetoothAdapter.getBondedDevices().isEmpty()) {
            isBluetoothConditionMet = true;
            star4.setImageResource(R.drawable.ic_star_yellow);
            enableLoginIfConditionsMet();
        }
    }

    private void checkTemperatureCondition() {
        new Thread(() -> {
            try {
                // Fetch weather data from the API
                URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=32.115164572295626&longitude=34.818230212475335&current_weather=true");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() == 200) {
                    // Read the API response
                    Scanner scanner = new Scanner(connection.getInputStream());
                    StringBuilder jsonResponse = new StringBuilder();
                    while (scanner.hasNext()) {
                        jsonResponse.append(scanner.nextLine());
                    }
                    scanner.close();

                    // Parse JSON response
                    JSONObject weatherData = new JSONObject(jsonResponse.toString());
                    double temperature = weatherData.getJSONObject("current_weather").getDouble("temperature");

                    // Log the temperature for debugging
                    Log.d("TemperatureCheck", "Current temperature: " + temperature);

                    // Update the UI based on the temperature
                    runOnUiThread(() -> {
                        if (temperature > 10.0 && temperature < 30.0) { // Example threshold for temperature
                            isTemperatureConditionMet = true;
                            star5.setImageResource(R.drawable.ic_star_yellow);
                        } else {
                            isTemperatureConditionMet = false;
                            star5.setImageResource(R.drawable.ic_star_grey);
                        }
                        enableLoginIfConditionsMet();
                    });
                } else {
                    Log.e("TemperatureCheck", "Error: Response code " + connection.getResponseCode());
                }
            } catch (Exception e) {
                Log.e("TemperatureCheck", "Error fetching temperature data", e);
            }
        }).start();
    }



    private boolean isAllConditionsMet() {
        return isBatteryConditionMet && isInternetConditionMet && isLightConditionMet && isBluetoothConditionMet && isTemperatureConditionMet;
    }

    private void enableLoginIfConditionsMet() {
        if (isAllConditionsMet()) {
            loginButton.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) { // ACCESS_NETWORK_STATE
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkInternetCondition();
            } else {
                Toast.makeText(this, "Permission for network state denied.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 2) { // BLUETOOTH_CONNECT
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkBluetoothCondition();
            } else {
                Toast.makeText(this, "Permission for Bluetooth denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupWifiListener() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    isInternetConditionMet = true;
                    star2.setImageResource(R.drawable.ic_star_yellow);
                } else {
                    isInternetConditionMet = false;
                    star2.setImageResource(R.drawable.ic_star_grey);
                }
                enableLoginIfConditionsMet();
            }
        };
        registerReceiver(wifiReceiver, filter);
    }

    private void setupBluetoothListener() {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    isBluetoothConditionMet = true;
                    star4.setImageResource(R.drawable.ic_star_yellow);
                } else {
                    isBluetoothConditionMet = false;
                    star4.setImageResource(R.drawable.ic_star_grey);
                }
                enableLoginIfConditionsMet();
            }
        };
        registerReceiver(bluetoothReceiver, filter);
    }


    private void setupLightSensorListener() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor != null) {
            SensorEventListener lightEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float lightValue = event.values[0];
                    if (lightValue > 100) { // Example threshold for "enough light"
                        isLightConditionMet = true;
                        star3.setImageResource(R.drawable.ic_star_yellow);
                    } else {
                        isLightConditionMet = false;
                        star3.setImageResource(R.drawable.ic_star_grey);
                    }
                    enableLoginIfConditionsMet();
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // Not used
                }
            };
            sensorManager.registerListener(lightEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (wifiReceiver != null) {
            unregisterReceiver(wifiReceiver);
        }
        if (bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver);
        }
    }


}
