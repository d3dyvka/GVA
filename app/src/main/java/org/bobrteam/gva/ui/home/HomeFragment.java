package org.bobrteam.gva.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements RecognitionListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSIONS_REQUEST_CALL_PHONE = 2;
    private Model model;
    private SpeechService speechService;
    private ImageButton microphoneButton;
    private boolean isRecording = false;
    private TextToSpeech textToSpeech; // Добавлено для синтезатора речи

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int permissionCheck = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            initModel();
        }
        initTextToSpeech(); // Инициализация синтезатора речи
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported");
                }
            } else {
                Log.e("TTS", "Initialization failed");
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        return root;
    }

    private void initModel() {
        StorageService.unpack(requireContext(), "vosk-model-small-ru-0.22", "model", (model) -> {
            this.model = model;
            showToast("Model loaded successfully");
        }, (exception) -> setErrorState("Failed to unpack the model" + exception.getMessage()));
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
            if (recognizedText.equals("набери номер один")) {
                String phone = loadPhone("один");
                playDialSound(R.raw.calling, phone);
                //speak("Набираю номер один: " + phone); // Озвучка
            } else if (recognizedText.equals("набери номер два")) {
                String phone = loadPhone("два");
                playDialSound(R.raw.call, phone);
                //speak("Набираю номер два: " + phone); // Озвучка
            } else if (recognizedText.equals("набери номер три")) {
                String phone = loadPhone("РЖД-Медицина Клиническая больница");
                playDialSound(R.raw.ringing, phone);
                //speak("Набираю номер три: " + phone); // Озвучка
            } else if (recognizedText.equals("набери номер четыре")) {
                String phone = loadPhone("Новосибирская областная больница");
                playDialSound(R.raw.call, phone);
                //speak("Набираю номер четыре: " + phone); // Озвучка
            } else if (recognizedText.equals("набери номер пять")) {
                String phone = loadPhone("Городская клиническая больница №11");
                playDialSound(R.raw.ringing, phone);
                //speak("Набираю номер пять: " + phone); // Озвучка
            } else if (recognizedText.equals("набери номер шесть")) {
                String phone = loadPhone("Городская клиническая поликлиника №13");
                playDialSound(R.raw.calling, phone);
                //speak("Набираю номер шесть: " + phone); // Озвучка
            } else if (recognizedText.equals("набери номер для записи")) {
                String phone = loadPhone("Единый номер для записи");
                playDialSound(R.raw.ringing, phone);
                //speak("Набираю номер для записи: " + phone); // Озвучка
            } else if (recognizedText.equals("концерты на завтра")) {
                // Получение и вывод концертов на завтра в фоновом потоке
                new FetchConcertsTask().execute();
            } else {
                MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.cwtr);
                mediaPlayer.start();
                //speak("Неизвестная команда"); // Озвучка
            }
        } catch (JSONException | IOException e) {
            setErrorState("Error processing result: " + e.getMessage());
        }
    }

    @Override
    public void onFinalResult(String hypothesis) {
        // Handle final result if needed
    }

    @Override
    public void onError(Exception e) {
        setErrorState("Recognition error: " + e.getMessage());
    }

    @Override
    public void onTimeout() {
        // Handle timeout if needed
    }

    private void setErrorState(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void vibrate() {
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

    private void playDialSound(int soundResourceId, String phoneNumber) {
        MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), soundResourceId);
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> dialPhoneNumber(phoneNumber));
            mediaPlayer.start();
        } else {
            Log.e("MediaPlayer", "Failed to initialize MediaPlayer");
        }
    }

    private static String loadJSON(Context context) {
        String json;
        try (InputStream is = context.getAssets().open("numbers.json")) {
            int size = is.available();
            byte[] buffer = new byte[size];
            int bytesRead = is.read(buffer);
            if (bytesRead == -1) {
                return null;
            }
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
        return json;
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.shutdown(); // Освобождение ресурсов синтезатора речи
        }
    }

    // Асинхронная задача для получения концертов
    private class FetchConcertsTask extends AsyncTask<Void, Void, List<Parse.Concert>> {
        @Override
        protected List<Parse.Concert> doInBackground(Void... voids) {
            Parse parse = new Parse();
            try {
                return parse.getConcerts(); // Парсим концерты
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(List<Parse.Concert> concerts) {
            if (concerts.isEmpty()) {
                showToast("Концертов на завтра не найдено");
                speak("Концертов на завтра не найдено"); // Озвучка
            } else {
                StringBuilder concertInfo = new StringBuilder("Концерты на завтра:\n");
                for (Parse.Concert concert : concerts) {
                    concertInfo.append(concert.toString()).append("\n");
                }
                showToast(concertInfo.toString()); // Вывод информации о концертах
                speak(concertInfo.toString()); // Озвучка информации о концертах
            }
        }
    }
}
