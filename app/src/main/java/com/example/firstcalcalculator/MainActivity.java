package com.example.firstcalcalculator;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.mariuszgromada.math.mxparser.*;

import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    TextView text_num, text_num2;       //обявление переменных для хранения объектов типа ТЕКСТ
    Expression e = new Expression();    //объявление и создание объекта е для решения математических задач

    boolean delAnswer = false;          //переменная определяет УДАЛЯТЬ ответ при вводе цифр, но НЕ УДАЛЯТЬ при вводе символов
    boolean setDot = true;              //разрешает запись точки

    static final String STATE_ANSWER = "scoreAnswer";       //ключи для сохранения значений
    static final String STATE_DANO = "scoreDano";

    String filename = "calculation_history.txt";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        text_num = findViewById(R.id.text_num);
        text_num2 = findViewById(R.id.text_num2);
        

    }

    //метод для сохранения значений при повороте экрана
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state.
        savedInstanceState.putString(STATE_ANSWER, text_num.getText().toString());
        savedInstanceState.putString(STATE_DANO, text_num2.getText().toString());

        // Always call the superclass so it can save the view hierarchy state.
        super.onSaveInstanceState(savedInstanceState);
    }
    //метод для восстановления значений, не требует проверки на null, в отличии от восстановлния в onCreate
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy.
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance.
        text_num.setText(savedInstanceState.getString(STATE_ANSWER));
        text_num2.setText(savedInstanceState.getString(STATE_DANO));
    }

    //метод работает с кнопками, которые МОЖНО нажать с пустым полем ВВОДА
    public void click_symb_start_but(View view) {
        String text = text_num.getText().toString();                //читаем ТЕКСТ и записываем в переменную
        String text1 = ((Button) view).getText().toString();        //получаем данные в тип View, переводим переменную view к типу Button, используем метод для получения текста в кнопке, переводим к типу Стринг

        if (text1.charAt(0) >= 'a' && text1.charAt(0)<='z'){       //если первый символ это буква добавляем открытую скобку
            text1+="(";
        }
        if ((text1.charAt(0) >= '0' && text1.charAt(0)<='9' || text1.charAt(0) == '.') && delAnswer) text = "";     //если введена цифра или точка и был дан ответ, то стираем ответ
        if (text1.charAt(0) == '-')setDot = true;
        if (text1.charAt(0) == '.'  && setDot) setDot = false;
        else if (text1.charAt(0) == '.') return;
        if (!text.isEmpty()) {
            if (text.substring((text.length() - 1)).equals("-") && text1.charAt(0) == '-') text1 = "";
            if (text.length() == 1 && text.charAt(0) == '0' && text1.charAt(0) != '.') text = "";
            if (text.substring((text.length() - 1)).equals("а") || text.substring((text.length() - 1)).equals("о")) text = "";
        }

        delAnswer = false;
        text+= text1;
        text_num.setText(text);
    }

    // метод вызывается нажатием на кнопку РАВНО
    public void clickRavno(View view) {
        String text = text_num.getText().toString();

        if (!text.isEmpty()) {      //если есть ТЕКСТ, то запускаем программу
            delAnswer = true;
            setDot = true;

            int kolvoSkobok = 0;    //проверка на открытые скобки, если не закрыты закрываем
            for (int i = 0; i < text.length();i++){
                if (text.charAt(i) == '(') kolvoSkobok++;
                else if (text.charAt(i) == ')') kolvoSkobok--;
            }
            while (kolvoSkobok>0) {
                text+=")";
                kolvoSkobok--;
            }

            e.setExpressionString(text);            // используем метод для РЕШЕНИЕ задачи
            text = String.valueOf(e.calculate());   //переводим ответ в Стринг и присваиваем значение


            if (text.substring((text.length() - 2)).equals(".0")) text = text.substring(0,text.length()-2); //убирает .0 в конце целого числа
            if (text == "NaN") text = "Ошибка";
            if (text == "Infinity") text = "Бесконечно";
            text_num.setText(text);
            text_num2.setText(String.valueOf(e.getExpressionString()));     //вывод задачи

            // Сохранение истории
            saveHistoryToFile(text_num2.getText().toString(), text_num.getText().toString());
        }
    }

    // метод вызывается нажатием на кнопку УДАЛИТЬ
    public void clickDel(View view) {
        String text = text_num.getText().toString();
        if (!text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            text_num.setText(text);
            delAnswer = false;
            setDot = true;
        }
    }

    // метод вызывается нажатием на кнопку ОЧИСТИТЬ
    public void clickC(View view) {
        text_num.setText("");
        text_num2.setText("");
        setDot = true;
    }

    //метод работает с кнопками, которые НЕЛЬЗЯ нажать с пустым полем ВВОДА
    public void click_symb_not_start_but (View view) {
        String text = text_num.getText().toString();
        String text1 = ((Button) view).getText().toString();
        if (!text.isEmpty()) {
            if (text.charAt(text.length() - 1) >= '0' && text.charAt(text.length() - 1) <= '9') { // если последний символ - это цифра
                text += text1;
            }
            else if (text.charAt(text.length() - 1) != text1.charAt(0) && (text.charAt(text.length() - 1) == '/' || text.charAt(text.length() - 1) == '*'
            || text.charAt(text.length() - 1) == '+'  )) {
                text = text.substring(0, text.length() - 1);
                text += text1;
            }
            text_num.setText(text);
            delAnswer = false;
            setDot = true;
        }
    }

    //метод для сохранения данных в фйал
    public void saveHistoryToFile(String equation, String result) {

        String content = equation + " = " + result + "\n"+ "\n";

        try {
            FileOutputStream fos = openFileOutput(filename, Context.MODE_APPEND);
            fos.write(content.getBytes());
            fos.close();
            Toast.makeText(this, "История сохранена", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при сохранении истории", Toast.LENGTH_SHORT).show();
        }
    }


    private void showDialogWithFileContent() {
        String content = loadValuesFromFile();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Содержимое файла")
                .setMessage(content)
                .setPositiveButton("OK", null)
                .show();
    }

    private String loadValuesFromFile() {
        FileInputStream fis = null;
        StringBuilder content = new StringBuilder();
        try {
            fis = openFileInput(filename);
            byte[] data = new byte[fis.available()];
            fis.read(data);
            content.append(new String(data));
        } catch (IOException e) {
            e.printStackTrace();
            content.append("Ошибка чтения файла: ").append(e.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content.toString();
    }

    public void viewHistory(View view) {
        showDialogWithFileContent();
    }
}
