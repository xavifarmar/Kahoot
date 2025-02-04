package com.example.kahoot;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.os.Handler;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class CorrectAnswerActivity extends AppCompatActivity {

    private TextView correctAnswerTextView, timerTextView;
    private int correctAnswerColor;
    private int timeLeft = 5;  // 5 segundos para mostrar la respuesta correcta
    private CountDownTimer countDownTimer;
    private int gameCode;  // Código del juego

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_correct_answer);

        // Obtener los datos del Intent
        String correctAnswer = getIntent().getStringExtra("correctAnswer");
        correctAnswerColor = getIntent().getIntExtra("correctAnswerColor", Color.RED);
        this.gameCode = getIntent().getIntExtra("gameCode", 0);  // Asegúrate de pasar este dato desde la actividad anterior

        // Inicializar los TextViews
        correctAnswerTextView = findViewById(R.id.correctAnswerTextView);
        timerTextView = findViewById(R.id.timerTextView);

        // Mostrar la respuesta correcta
        correctAnswerTextView.setText(correctAnswer);
        correctAnswerTextView.setTextColor(Color.DKGRAY);
        correctAnswerTextView.setBackgroundColor(correctAnswerColor); // Establecer el color de fondo

        // Cambiar el estado a "scoreScreen" en Firebase
        changeGameStatusToScoreScreen(gameCode);

        // Iniciar el temporizador para los 5 segundos
        startCountdownTimer();
    }

    private void changeGameStatusToScoreScreen(int gameCode) {
        // Obtener una referencia a Firebase para actualizar el estado del juego
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("Games").child("game_" + gameCode);

        // Cambiar el estado a "scoreScreen"
        gameRef.child("status").setValue("scoreScreen")
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Estado del juego cambiado a 'scoreScreen'");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error al cambiar el estado del juego: " + e.getMessage());
                });
    }

    private void startCountdownTimer() {
        countDownTimer = new CountDownTimer(5000, 1000) {  // 5 segundos, cuenta atrás cada 1 segundo
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000);
                timerTextView.setText(String.valueOf(timeLeft));  // Actualizar el temporizador
            }

            @Override
            public void onFinish() {
                // Pasar a la siguiente pregunta o finalizar el juego
                changeGameStatusToPlaying();  // Cambiar el estado a "playing" antes de pasar a la siguiente pregunta
                finish();  // Finalizar la actividad y volver a la actividad principal
            }
        };
        countDownTimer.start();
    }

    private void changeGameStatusToPlaying() {
        // Obtener una referencia a Firebase para actualizar el estado del juego
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("Games").child("game_" + gameCode);

        // Cambiar el estado a "playing"
        gameRef.child("status").setValue("playing")
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Estado del juego cambiado a 'playing'");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error al cambiar el estado del juego: " + e.getMessage());
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();  // Cancelar el temporizador si la actividad es destruida
        }
    }
}
