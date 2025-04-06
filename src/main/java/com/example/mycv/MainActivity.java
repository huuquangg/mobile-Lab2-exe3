package com.example.mycv;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextInputEditText editText;
    Button predictButton;

    private final String API_KEY = "7DvaSgA7kekAfbvgqr9sI0yV3NnM";
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Allow network on main thread (not recommended for production)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        editText = findViewById(R.id.textInputEditText);
        predictButton = findViewById(R.id.button);

        predictButton.setOnClickListener(view -> {
            String inputText = editText.getText() != null ? editText.getText().toString() : "";
            if (!inputText.isEmpty()) {
                String response = callGeminiAPI(inputText +". Câu này có mang cảm súc như thế nào? Trả về câu trả lời ới chỉ 3 đáp án: Tích cực, Tiêu cực, Bình thường");
                String sentiment = extractSentiment(response);
                Toast.makeText(this, sentiment, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Please enter input text!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String callGeminiAPI(String prompt) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            JSONObject content = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject textObj = new JSONObject();
            textObj.put("text", prompt);
            partsArray.put(textObj);
            JSONArray contentsArray = new JSONArray();
            JSONObject partsWrapper = new JSONObject();
            partsWrapper.put("parts", partsArray);
            contentsArray.put(partsWrapper);
            content.put("contents", contentsArray);

            OutputStream os = new BufferedOutputStream(conn.getOutputStream());
            os.write(content.toString().getBytes());
            os.flush();
            os.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            conn.disconnect();

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi gọi API";
        }
    }

    private String extractSentiment(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray candidates = root.getJSONArray("candidates");
            JSONObject first = candidates.getJSONObject(0);
            JSONObject content = first.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            String text = parts.getJSONObject(0).getString("text").toLowerCase();

            if (text.contains("tích cực")) return "Tích cực";
            if (text.contains("tiêu cực")) return "Tiêu cực";
            if (text.contains("bình thường")) return "Bình thường";

            return text;
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi phân tích kết quả";
        }
    }
}
