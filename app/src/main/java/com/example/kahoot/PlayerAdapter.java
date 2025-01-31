package com.example.kahoot;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerViewHolder> {

        private List<Player> playersList;

        // Constructor
        public PlayerAdapter(List<Player> playersList) {
            this.playersList = playersList;
        }

        @Override
        public PlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player, parent, false);
            return new PlayerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PlayerViewHolder holder, int position) {
            Player player = playersList.get(position);
            // Muestra el nombre y el emoji en el TextView
            holder.emojiTextView.setText(player.getImage_emoji());  // Asigna el emoji
            holder.usernameTextView.setText(player.getUsername());

            String colorHex = player.getImage_color();  // Obtén el valor hexadecimal del color
            if (colorHex != null && !colorHex.isEmpty()) {
                // Convertir el color hexadecimal a Color
                int color = Color.parseColor(colorHex);

                Drawable background = holder.emojiTextView.getBackground();
                if (background != null) {
                    background.setTint(Color.parseColor(colorHex));
                    holder.emojiTextView.setBackground(background);
                }
            }
        }

        @Override
        public int getItemCount() {
            return playersList.size();
        }
    }

