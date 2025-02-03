package com.example.kahoot;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Question implements Parcelable {
    private String question;
    private List<String> answers;
    private int correctAnswer;
    private String questionId;  // Nuevo campo para el ID de la pregunta

    public Question() {
        // Constructor vacío necesario para Firebase
    }

    public Question(String question, List<String> answers, int correctAnswer, String questionId) {
        this.question = question;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
        this.questionId = questionId;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(question);
        dest.writeStringList(answers);
        dest.writeInt(correctAnswer);
        dest.writeString(questionId);  // Guardar el questionId en el Parcel
    }

    public static final Parcelable.Creator<Question> CREATOR = new Parcelable.Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    protected Question(Parcel in) {
        question = in.readString();
        answers = in.createStringArrayList();
        correctAnswer = in.readInt();
        questionId = in.readString();  // Recuperar el questionId desde el Parcel
    }
}
