package com.example.kahoot;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameActivity extends AppCompatActivity {
    private TextView questionTextView, answer1TextView,
            answer2TextView, answer3TextView, answer4TextView,
            answersText;
    private TextView timerTextView;
    private FirebaseDatabase database;
    private DatabaseReference gameRef;
    private int gameCode; // Código del juego
    private CountDownTimer countDownTimer;
    private int playersCount;

    private ArrayList<Question> questionsList;
    private int currentQuestionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        // Obtener el gameCode del Intent
        Intent intent = getIntent();
        gameCode = intent.getIntExtra("gameCode", -1); // Valor predeterminado -1 en caso de que no se pase
        questionsList = intent.getParcelableArrayListExtra("questionsList"); // Recuperando la lista de preguntas
        playersCount = intent.getIntExtra("playersCount", 0);
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
        answersText = findViewById(R.id.answersText);

        // Inicializar la base de datos de Firebase
        database = FirebaseDatabase.getInstance();
        gameRef = database.getReference("Games").child(String.valueOf("game_" + gameCode));

        // Mostrar la primera pregunta recibida por el Intent
        if (questionsList != null && !questionsList.isEmpty()) {
            showQuestion(questionsList.get(0));  // Mostrar la primera pregunta
            updateCurrentQuestionIndex();  // Actualizar el índice de la pregunta actual en Firebase
        }

        // Obtener el cuestionario de Firebase
        fetchQuestionary();

        // Iniciar el temporizador de cuenta regresiva de 30 segundos
        startCountdownTimer();

        // Escuchar las respuestas de los jugadores
        listenForAnswers(playersCount);
    }

    private void listenForAnswers(int playersCount) {
        gameRef.child("playersAnswered").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Obtener el valor de playersAnswered (número de jugadores que han respondido)
                    Integer playersAnsweredCount = dataSnapshot.getValue(Integer.class);

                    // Si el valor es null, inicializamos playersAnsweredCount en 0
                    if (playersAnsweredCount == null) {
                        playersAnsweredCount = 0;
                    }

                    Log.d("GameActivity", "Jugadores que han respondido: " + playersAnsweredCount);
                    answersText.setText("Respuestas: " + playersAnsweredCount);
                    // Comparar el número de respuestas con el número de jugadores
                    if (playersAnsweredCount == playersCount) {

                        if(countDownTimer != null){
                            countDownTimer.cancel();// Si todos los jugadores han respondido, pasamos a la siguiente pregunta
                        }

                        showCorrectAnswer();
                    }
                } else {
                    // Si el nodo playersAnswered no existe aún, lo inicializamos en 0
                    gameRef.child("playersAnswered").setValue(0);
                    Log.d("GameActivity", "Inicializando playersAnswered en 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GameActivity", "Error al escuchar las respuestas: " + error.getMessage());
            }
        });
    }




    private void moveToNextQuestion() {
        currentQuestionIndex++;  // Mover al siguiente índice de pregunta

        // Reiniciar el contador de respuestas cuando se pasa a la siguiente pregunta
        gameRef.child("playersAnswered").setValue(0); // Reinicia el contador de respuestas

        // Actualizamos el índice de la pregunta en Firebase
        gameRef.child("currentQuestion").setValue(currentQuestionIndex);

        if (currentQuestionIndex < questionsList.size()) {
            // Si hay más preguntas, mostrar la siguiente
            showQuestion(questionsList.get(currentQuestionIndex));
        } else {
            // Si no hay más preguntas, finalizar el juego o mostrar los resultados
            Log.d("GameActivity", "El juego ha terminado.");
        }
    }



    private void updateCurrentQuestionIndex() {
        // Actualizamos el índice de la pregunta en Firebase
        gameRef.child("currentQuestion").setValue(currentQuestionIndex);
    }

    private void fetchQuestionary() {
        gameRef.child("Questions/default").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("GameActivity", "Datos recibidos: " + dataSnapshot.getValue());

                    // Recorrer las preguntas y agregarlas a la lista
                    for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                        Map<String, Object> questionData = (Map<String, Object>) questionSnapshot.getValue();
                        // Aquí deberías agregar las preguntas recibidas de Firebase a la lista de preguntas si es necesario
                    }

                    // Verificar si las preguntas fueron añadidas correctamente
                    Log.d("GameActivity", "Número de preguntas: " + questionsList.size());

                    if (!questionsList.isEmpty()) {
                        showQuestion(questionsList.get(currentQuestionIndex));  // Mostrar la primera pregunta de la lista
                        startCountdownTimer();  // Iniciar el temporizador
                    } else {
                        Log.e("GameActivity", "No se encontraron preguntas.");
                    }
                } else {
                    Log.e("GameActivity", "No hay preguntas en la base de datos.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GameActivity", "Error al obtener las preguntas: " + error.getMessage());
            }
        });
    }

    private void startCountdownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Cancelar el temporizador anterior si existe
        }

        countDownTimer = new CountDownTimer(30000, 1000) { // 30 segundos, cuenta atrás cada 1 segundo
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                timerTextView.setText("0");

                // Cuando termina el tiempo, mostrar la respuesta correcta
                showCorrectAnswer();  // Llamar a la función que maneja el cambio de pregunta
            }
        }.start();
    }


    // Método modificado para mostrar la pregunta recibida desde el Intent
    private void showQuestion(Question question) {
        if (question != null) {
            // Mostrar la primera pregunta en la interfaz de usuario
            questionTextView.setText(question.getQuestion());
            List<String> answers = question.getAnswers();
            if (answers != null && answers.size() >= 4) {
                answer1TextView.setText(answers.get(0));
                answer2TextView.setText(answers.get(1));
                answer3TextView.setText(answers.get(2));
                answer4TextView.setText(answers.get(3));
            } else {
                Log.e("GameActivity", "Las respuestas no tienen el formato esperado.");
            }
        } else {
            Log.e("GameActivity", "No se recibió una pregunta válida.");
        }
    }
    private void showCorrectAnswer() {
        // Obtener la respuesta correcta de la pregunta actual
        Question currentQuestion = questionsList.get(currentQuestionIndex);
        String correctAnswer = currentQuestion.getAnswers().get(currentQuestion.getCorrectAnswer());

        // Identificar el TextView que contiene la respuesta correcta y recoger el color de fondo
        TextView correctAnswerTextView = null;
        if (answer1TextView.getText().toString().equals(correctAnswer)) {
            correctAnswerTextView = answer1TextView;
        } else if (answer2TextView.getText().toString().equals(correctAnswer)) {
            correctAnswerTextView = answer2TextView;
        } else if (answer3TextView.getText().toString().equals(correctAnswer)) {
            correctAnswerTextView = answer3TextView;
        } else if (answer4TextView.getText().toString().equals(correctAnswer)) {
            correctAnswerTextView = answer4TextView;
        }

        // Obtener el color de fondo de la respuesta correcta
        int correctAnswerColor = correctAnswerTextView.getSolidColor();

        // Crear un Intent para la actividad de la respuesta correcta y pasar la respuesta y el color
        Intent intent = new Intent(GameActivity.this, CorrectAnswerActivity.class);
        intent.putExtra("correctAnswer", correctAnswer);
        intent.putExtra("correctAnswerColor", correctAnswerColor);
        intent.putExtra("gameCode", gameCode);
        Log.d("Game activity", "GAME CODE: " + gameCode);
        startActivity(intent);

        // Pasar a la siguiente pregunta después de 5 segundos
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                moveToNextQuestion();  // Mover a la siguiente pregunta
                startCountdownTimer();  // Reiniciar el temporizador para la siguiente pregunta
            }
        }, 5000);  // Esperar 5 segundos antes de mostrar la siguiente pregunta
    }


    private void onFinish() {
        // Cuando el temporizador termine, mostrar la respuesta correcta
        showCorrectAnswer();
    }

}
