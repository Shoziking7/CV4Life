package com.example.newgemini;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.android.material.textfield.TextInputEditText;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class viviActivity extends AppCompatActivity {

    private static final String TAG = "viviActivity";
    private TextInputEditText queryEditText;
    private Button sendQueryButton;
    private ProgressBar progressBar;
    private LinearLayout chatBodyContainer;
    private ChatFutures chatModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vivi);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        chatModel = getChatModel();
        queryEditText = findViewById(R.id.queryEditText);
        sendQueryButton = findViewById(R.id.sendPromptButton);
        progressBar = findViewById(R.id.sendPromptProgressBar);
        chatBodyContainer = findViewById(R.id.chatResponseLayout);

        sendQueryButton.setOnClickListener(v -> {
            String query = queryEditText.getText().toString();
            if(query.isEmpty()){
                Toast.makeText(this, "Please enter a query", Toast.LENGTH_SHORT).show();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            queryEditText.setText("");
            populateChatBody("You", query, getDate());
            Log.d(TAG, "Sending query: " + query);

            GeminiPro.getResponse(chatModel, query, new ResponseCallBack() {
                @Override
                public void onResponse(String response) {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "Received response: " + response);
                    populateChatBody("VIVI", response, getDate());
                }

                @Override
                public void onError(Throwable throwable) {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error in getResponse", throwable);
                    Toast.makeText(viviActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    populateChatBody("VIVI", "Sorry I'm having trouble understanding that. Please try again.", getDate());
                }
            });
        });
    }

    private ChatFutures getChatModel() {
        GeminiPro model = new GeminiPro();
        GenerativeModelFutures modelFutures = model.getModel();
        return modelFutures.startChat();
    }

    public void populateChatBody(String userName, String message, String date) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_message_block, null);

        TextView userAgentName = view.findViewById(R.id.userAgentNameTextfield);
        TextView userAgentMessage = view.findViewById(R.id.userAgentMessageTextView);
        TextView dateTextView = view.findViewById(R.id.dateTextView);

        userAgentName.setText(userName);
        userAgentMessage.setText(message);
        dateTextView.setText(date);

        chatBodyContainer.addView(view);

        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private String getDate() {
        Instant instant = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm").withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }
}
