package org.bobrteam.gva.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.bobrteam.gva.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.StorageService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HomeFragment extends Fragment implements RecognitionListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSIONS_REQUEST_CALL_PHONE = 2;
    private Model model;
    private SpeechService speechService;
    private ImageButton microphoneButton;
    private boolean isRecording = false;

    private static String loadJSON(Context context) {
        String json;
        try (InputStream is = context.getAssets().open("numbers.json")) {
            int size = is.available();
            byte[] buffer = new byte[size];
            int bytesRead = is.read(buffer);
            if (bytesRead == -1) {
                // Обработка ошибки чтения, если нужно
                return null;
            }
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
        return json;
    }


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        microphoneButton = root.findViewById(R.id.recognize_mic);
        microphoneButton.setOnClickListener(v -> {
            try {
                toggleRecording();
                vibrate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        int permissionCheck = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            initModel();
        }

        return root;
    }

    private void initModel() {
        StorageService.unpack(requireContext(), "vosk-model-small-ru-0.22", "model",
                (model) -> {
                    this.model = model;
                    showToast("Model loaded successfully");
                },
                (exception) -> setErrorState("Failed to unpack the model" + exception.getMessage()));
    }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        isRecording = true;
        try {
            Recognizer rec = new Recognizer(model, 16000.0f);
            speechService = new SpeechService(rec, 16000.0f);
            speechService.startListening(this);
        } catch (IOException e) {
            setErrorState("Error starting recording: " + e.getMessage());
        }
        microphoneButton.setImageResource(R.drawable.mic_48px);
    }

    private void stopRecording() {
        isRecording = false;
        speechService.stop();
        speechService.shutdown();
        microphoneButton.setImageResource(R.drawable.mic_off_48px);
    }

    @Override
    public void onPartialResult(String hypothesis) {
    }

    @Override
    public void onResult(String hypothesis) {
        try {
            JSONObject json = new JSONObject(hypothesis);
            String recognizedText = json.optString("text", "");
            showToast(recognizedText);
            switch (recognizedText) {
                case "набери номер один": {
                    String phone = loadPhone("один");
                    dialPhoneNumber(phone);
                    break;
                }
                case "набери номер два": {
                    String phone = loadPhone("два");
                    dialPhoneNumber(phone);
                    break;
                }
                case "набери номер три": {
                    String phone = loadPhone("три");
                    dialPhoneNumber(phone);
                    break;
                }
                case "набери номер четыре": {
                    String phone = loadPhone("четыре");
                    dialPhoneNumber(phone);
                    break;
                }
                case "набери номер пять": {
                    String phone = loadPhone("пять");
                    dialPhoneNumber(phone);
                    break;
                }
            }
        } catch (JSONException e) {
            setErrorState("Error parsing JSON: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onFinalResult(String hypothesis) {
        // Обработка окончательных результатов
    }

    @Override
    public void onError(Exception e) {
        setErrorState("Recognition error: " + e.getMessage());
    }

    @Override
    public void onTimeout() {
        // Обработка таймаута
    }

    private void setErrorState(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void vibrate() {
        getContext();
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(50);
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE}, PERMISSIONS_REQUEST_CALL_PHONE);
        } else {
            startActivity(intent);
        }
    }

    private String loadPhone(String hospitalName) throws IOException, JSONException {
        String result = "";

        String jsonData = loadJSON(requireContext());
        assert jsonData != null;
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray hospitals = jsonObject.getJSONArray("hospitals");

        for (int i = 0; i < hospitals.length(); i++) {
            JSONObject hospital = hospitals.getJSONObject(i);
            String name = hospital.getString("name");
            if (name.equals(hospitalName)) {
                result = hospital.getString("phone_number");
                break;
            }
        }

        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
