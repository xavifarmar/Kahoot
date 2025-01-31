package com.example.kahoot;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class PlayerViewHolder extends RecyclerView.ViewHolder {
    public TextView usernameTextView;
    public TextView emojiTextView;

    public PlayerViewHolder(View itemView) {
        super(itemView);
        usernameTextView = itemView.findViewById(R.id.usernameTextView);
        emojiTextView = itemView.findViewById(R.id.emojiTextView);

    }
}

