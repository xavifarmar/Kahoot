package com.example.kahoot;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class CorrectAnswerActivity extends AppCompatActivity {

    private TextView correctAnswerTextView, timerTextView;
    private int correctAnswerColor;
    private int timeLeft = 5;  // 5 segundos para mostrar la respuesta correcta
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_correct_answer);

        // Obtener los datos del Intent
        String correctAnswer = getIntent().getStringExtra("correctAnswer");
        correctAnswerColor = getIntent().getIntExtra("correctAnswerColor", Color.RED);

        // Inicializar los TextViews
        correctAnswerTextView = findViewById(R.id.correctAnswerTextView);
        timerTextView = findViewById(R.id.timerTextView);

        // Mostrar la respuesta correcta
        correctAnswerTextView.setText(correctAnswer);
        correctAnswerTextView.setTextColor(Color.DKGRAY);
        correctAnswerTextView.setBackgroundColor(correctAnswerColor); // Establecer el color de fondo

        // Iniciar el temporizador para los 5 segundos
        startCountdownTimer();
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
                finish();  // Finalizar la actividad y volver a la actividad principal
            }
        };
        countDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();  // Cancelar el temporizador si la actividad es destruida
        }
    }
}


