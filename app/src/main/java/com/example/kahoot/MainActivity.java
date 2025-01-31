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

        Map<String, Object> playersMap = new HashMap<>();
        playersMap.put("player0", "admin");

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
                            // Mensaje cuando se agrega correctamente el código
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
                    if (playersList != null) {
                        playersList.add(player);
                    }
                }
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
                    // Si la actualización fue exitosa, inicia la nueva actividad
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    // Si hubo un error al actualizar el estado, puedes manejarlo aquí
                    System.out.println("Error al actualizar el estado del juego: " + e.getMessage());
                });
    }

}
