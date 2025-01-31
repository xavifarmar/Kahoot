package com.example.kahoot;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class GameActivity extends AppCompatActivity {
    private TextView questionTextView, answer1TextView,
            answer2TextView, answer3TextView, answer4TextView;
    private TextView timerTextView;
    private FirebaseDatabase database;
    private DatabaseReference gameRef;
    private int gameCode; // Código del juego

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        // Obtener el gameCode del Intent
        gameCode = getIntent().getIntExtra("gameCode", -1); // Valor predeterminado -1 en caso de que no se pase

        // Verificar que el gameCode no es -1
        if (gameCode == -1) {
            Log.e("GameActivity", "No se pudo obtener el gameCode");
            return; // Terminar la ejecución si no se recibe el gameCode
        }

        // Inicializar los TextViews
        timerTextView = findViewById(R.id.timerTextView);
        questionTextView = findViewById(R.id.questionTextView);
        answer1TextView = findViewById(R.id.answer1);
        answer2TextView = findViewById(R.id.answer2);
        answer3TextView = findViewById(R.id.answer3);
        answer4TextView = findViewById(R.id.answer4);

        // Inicializar la base de datos de Firebase
        database = FirebaseDatabase.getInstance();
        gameRef = database.getReference("Games");

        // Obtener el cuestionario de Firebase
        fetchQuestionary();

        // Iniciar el temporizador de cuenta regresiva de 30 segundos
        startCountdownTimer();
    }

    private void fetchQuestionary() {
        // Obtener el cuestionario del juego en Firebase
        gameRef.child("game_" + gameCode).child("Questions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Obtener los datos del cuestionario
                    Object questionaryObj = dataSnapshot.getValue();

                    if (questionaryObj instanceof Map) {
                        // Si es un Map, el código original sigue siendo válido
                        Map<String, Object> questionary = (Map<String, Object>) questionaryObj;

                        // Obtener la primera pregunta
                        Map<String, Object> question1 = (Map<String, Object>) questionary.get("question_1");

                        if (question1 != null) {
                            // Recuperar la pregunta y las respuestas
                            String question = (String) question1.get("question");
                            String answer1 = (String) question1.get("options/a");
                            String answer2 = (String) question1.get("options/b");
                            String answer3 = (String) question1.get("options/c");
                            String answer4 = (String) question1.get("options/d");

                            // Actualizar los TextViews con la pregunta y respuestas
                            questionTextView.setText(question);
                            answer1TextView.setText(answer1);
                            answer2TextView.setText(answer2);
                            answer3TextView.setText(answer3);
                            answer4TextView.setText(answer4);
                        } else {
                            Log.e("GameActivity", "La primera pregunta no está disponible.");
                        }
                    } else if (questionaryObj instanceof List) {
                        // Si es una List, recorrer las preguntas
                        List<Map<String, Object>> questionaryList = (List<Map<String, Object>>) questionaryObj;

                        // Asegurarse de que haya al menos una pregunta
                        if (!questionaryList.isEmpty()) {
                            Map<String, Object> question1 = questionaryList.get(0);

                            // Recuperar la pregunta y las respuestas
                            String question = (String) question1.get("question");
                            String answer1 = (String) question1.get("options/a");
                            String answer2 = (String) question1.get("options/b");
                            String answer3 = (String) question1.get("options/c");
                            String answer4 = (String) question1.get("options/d");

                            // Actualizar los TextViews con la pregunta y respuestas
                            questionTextView.setText(question);
                            answer1TextView.setText(answer1);
                            answer2TextView.setText(answer2);
                            answer3TextView.setText(answer3);
                            answer4TextView.setText(answer4);
                        } else {
                            Log.e("GameActivity", "No se encontraron preguntas en la lista.");
                        }
                    } else {
                        Log.e("GameActivity", "La estructura de datos no es ni un Map ni una List.");
                    }
                } else {
                    Log.e("GameActivity", "No se encontraron preguntas para este juego.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejo de errores
                Log.e("GameActivity", "Error al obtener las preguntas: " + error.getMessage());
            }
        });
    }


    private void startCountdownTimer() {
        // Crear un CountDownTimer que cuenta 30 segundos
        new CountDownTimer(30000, 1000) { // 30,000 ms = 30 segundos, 1,000 ms = 1 segundo

            @Override
            public void onTick(long millisUntilFinished) {
                // Actualizar el texto del TextView con el tiempo restante
                long secondsRemaining = millisUntilFinished / 1000;  // Convertir a segundos
                timerTextView.setText(String.valueOf(secondsRemaining));
            }

            @Override
            public void onFinish() {
                // Cuando el tiempo termine, poner el texto en "0"
                timerTextView.setText("0");
            }
        }.start(); // Iniciar el temporizador
    }
}
