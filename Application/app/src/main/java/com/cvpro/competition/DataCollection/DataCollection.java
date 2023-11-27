package com.cvpro.competition.DataCollection;

import static com.cvpro.competition.MainActivity.usbService;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.cvpro.competition.MainActivity;
import com.cvpro.competition.R;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
public class DataCollection extends AppCompatActivity {
    public static int SERVER_PORT;
    public static String SERVER_IP;
    private ClientThread clientThread;
    private Thread thread;
    private LinearLayout msgList;
    private Handler handler;
    private int clientTextColor;
    private EditText etIP, etPort;
    public static String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);
        clientTextColor = ContextCompat.getColor(this, R.color.black);
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        etIP = findViewById(R.id.etIP);
        etPort = findViewById(R.id.etPort);
    }

    public TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            // handle empty message if needed
        }
        TextView tv = new TextView(this);
        tv.setTextColor(color);
        tv.setText(message);
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }

    public void showMessage(final String message, final int color) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgList.addView(textView(message, color));
            }
        });
    }

    public void onClick(View view) {
        try {
            if (view.getId() == R.id.connect_server) {
                SERVER_IP = etIP.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(etPort.getText().toString().trim());
                msgList.removeAllViews();
                showMessage("Connecting...", clientTextColor);
                clientThread = new ClientThread();
                thread = new Thread(clientThread);
                thread.start();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(this, "Please Enter IP address and Port Number", Toast.LENGTH_SHORT).show();
        }
    }

    class ClientThread implements Runnable {
        private BufferedReader input;
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                Socket socket = new Socket(serverAddr, SERVER_PORT);
                openMainActivity2();
                showMessage("Connected to Server...", clientTextColor);
                while (!Thread.currentThread().isInterrupted()) {
                    if (socket.isConnected()) {
                        if (usbService != null) {
                            handleSocketInput(socket);
                        }
                    }
                }
                // Close the BufferedReader and Socket outside the loop
                input.close();
                socket.close();
                Thread.currentThread().interrupt();
                showMessage("Server Disconnected..........!", Color.RED);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        private void handleSocketInput(Socket socket) throws IOException {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
            int message = Integer.parseInt(String.valueOf(input.read()));
            char value_char = (char) message;
            msg = String.valueOf(value_char);
            Log.i("Python Values", msg);

            handleCommand();
            if (msg.equals("x")) {
                cleanUpAndExit(socket);
            } else if (!Objects.equals(msg, "o")) {
                // showMessage("Server : "+ msg, Color.BLUE);
            }
        }

        private void handleCommand() {
             usbService.write(msg.getBytes(StandardCharsets.UTF_8));
        }

        private void cleanUpAndExit(Socket socket) throws IOException {
            input.close();
            socket.close();
            Thread.currentThread().interrupt();
            showMessage("Server Disconnected..........!", Color.RED);

            // Close all Activities
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
        }
    }
    private void openMainActivity2() {
        Intent intent = new Intent(this, CameraActivity_Data.class);
        startActivity(intent);
    }
}
