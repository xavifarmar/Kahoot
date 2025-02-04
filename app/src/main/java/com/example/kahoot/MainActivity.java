package com.example.kahoot;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView playersRecyclerView;
    private TextView gameCodeTextView, playersTitle;
    private Button generateCodeBtn, startButton;
    private FirebaseDatabase database;
    private DatabaseReference gameRef;
    private List<Player> playersList;
    private PlayerAdapter playerAdapter;
    private int playersCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //Inicializar firebase
        database = FirebaseDatabase.getInstance();
        gameRef = database.getReference("Games");
        gameCodeTextView = findViewById(R.id.gameCodeTextView);
        generateCodeBtn = findViewById(R.id.generateCodeBtn);
        playersTitle = findViewById(R.id.playersTitle);
        startButton = findViewById(R.id.startButton);

        playersRecyclerView = findViewById(R.id.playersRecyclerView);
        playersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        playersList = new ArrayList<>();
        playerAdapter = new PlayerAdapter(playersList);
        playersRecyclerView.setAdapter(playerAdapter);

        generateCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 generate_code();

            }
        });

        startButton.setOnClickListener(v -> {
            startGame();
        });


    }


    public void generate_code() {
        Random random = new Random();
        int gameCode = 100000 + random.nextInt(900000);  // Genera un código de 6 dígitos
        gameCodeTextView.setText(String.valueOf(gameCode));  // Muestra el código en la UI

        // Crear un nuevo objeto Game con el código generado y el estado "waiting"
        Game newGame = new Game(gameCode, "waiting");


        // Verificar si ya existen códigos en la base de datos
        gameRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Si existen códigos, desactivar el último
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Game lastGame = snapshot.getValue(Game.class);
                        if (lastGame != null) {
                            String lastCodeKey = snapshot.getKey();  // Obtener la clave del último código
                            gameRef.child(lastCodeKey).child("status").setValue("inactive");  // Cambiar el estado a "inactive"
                        }
                    }
                }

                // Agregar el nuevo código de juego a Firebase
                gameRef.child("game_" + gameCode).setValue(newGame)
                        .addOnSuccessListener(aVoid -> {
                            gameRef.child("game_" + gameCode).child("currentQuestion").setValue(0);
                            gameRef.child("game_" + gameCode).child("questionId").setValue("default");

                            System.out.println("Se ha enviado el código perfectamente");
                            listenForPlayers(gameCode);
                        })
                        .addOnFailureListener(e -> {
                            // Manejo de errores
                            System.out.println("No se ha podido enviar el código");
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejo de errores en caso de que la lectura sea cancelada
            }
        });
    }
    public void listenForPlayers(int gameCode) {
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("Games").child("game_" + gameCode).child("players");

        gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                playersList.clear();  // Limpiar la lista antes de agregar los nuevos jugadores
                playersTitle.setVisibility(View.VISIBLE);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Player player = snapshot.getValue(Player.class);
                    if (player != null) {
                        playersList.add(player);  // Agregar el jugador a la lista
                    }
                }

                playersCount = playersList.size();  // Actualizar el contador de jugadores

                // Actualizar la base de datos con el nuevo contador de jugadores
                gameRef.getParent().child("playersCount").setValue(playersCount)
                        .addOnSuccessListener(aVoid -> {
                            System.out.println("Contador de jugadores actualizado correctamente.");
                        })
                        .addOnFailureListener(e -> {
                            System.out.println("Error al actualizar el contador de jugadores: " + e.getMessage());
                        });

                // Si hay jugadores, habilitar el botón de Start
                if (!playersList.isEmpty()) {
                    startButton.setVisibility(View.VISIBLE);
                }

                playerAdapter.notifyDataSetChanged();  // Notificar que los datos han cambiado
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error al leer los jugadores: " + databaseError.getMessage());
            }
        });
    }


    private void startGame() {
        // Obtener el código del juego (puedes obtener el código desde la UI, o guardarlo previamente en algún lugar)
        String gameCode = gameCodeTextView.getText().toString();

        // Actualizar el estado del juego en Firebase a "playing"
        gameRef.child("game_" + gameCode).child("status").setValue("playing")
                .addOnSuccessListener(aVoid -> {
                    // Cargar preguntas desde Firebase antes de iniciar el juego
                    loadQuestions(gameCode);

                })
                .addOnFailureListener(e -> {
                    // Si hubo un error al actualizar el estado, puedes manejarlo aquí
                    System.out.println("Error al actualizar el estado del juego: " + e.getMessage());
                });
    }
    private void loadQuestions(String gameCode) {
        DatabaseReference questionsRef = FirebaseDatabase.getInstance().getReference("Questions").child("default");

        questionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Verifica si existen datos
                if (dataSnapshot.exists()) {
                    ArrayList<Question> questionsList = new ArrayList<>();

                    // Imprimir los datos recibidos para depuración
                    System.out.println("Datos recibidos desde Firebase:");
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Question question = snapshot.getValue(Question.class);
                        System.out.println("Pregunta: " + snapshot.getValue());  // Agregar log aquí

                        if (question != null) {
                            // Agregar el `questionId` (en este caso, "default")
                            question.setQuestionId("default");
                            questionsList.add(question);
                        }
                    }

                    if (!questionsList.isEmpty()) {
                        // Si las preguntas están disponibles, continuar con la carga del juego
                        Intent intent = new Intent(MainActivity.this, GameActivity.class);
                        intent.putExtra("gameCode", Integer.parseInt(gameCode));  // Pasar el código del juego
                        intent.putParcelableArrayListExtra("questionsList", questionsList);  // Pasar la lista de preguntas

                        // También pasamos el questionId y currentQuestion
                        intent.putExtra("questionId", "default");  // Aquí puedes poner un questionId dinámico si es necesario
                        intent.putExtra("currentQuestion", 0);  // El índice de la pregunta inicial es 0 (primera pregunta)
                        intent.putExtra("playersCount", playersCount);
                        startActivity(intent);

                        // Aquí actualizamos Firebase para que el índice de la pregunta sea 0 (comienza con la primera pregunta)
                        gameRef.child("game_" + gameCode).child("currentQuestion").setValue(0);
                        gameRef.child("game_" + gameCode).child("questionId").setValue("default");
                    } else {
                        System.out.println("No se encontraron preguntas.");
                    }
                } else {
                    System.out.println("No hay datos en la ruta de preguntas.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Error al obtener preguntas: " + databaseError.getMessage());
            }
        });
    }



}
