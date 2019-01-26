package ejemplopruebas.example.com.piotter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

public class TwitterTimeLineActivity extends AppCompatActivity {

    ArrayList<String> timeLineURLImages = new ArrayList<String>();
    ArrayList<String> timeLineNames = new ArrayList<String>();
    ArrayList<String> timeLineContent = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_time_line);

        // Recibo los datos que me pasa MainActivity por EXTRAS y los meto en arrays

        Intent intent = getIntent();

        timeLineURLImages = intent.getStringArrayListExtra("listaImagenes");
        timeLineNames = intent.getStringArrayListExtra("listaNombres");
        timeLineContent = intent.getStringArrayListExtra("listaContenidos");


        // hilo con runOnUiThread para mostrar los datos en el listView
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        ItemsTwitterAdapter adapter = new ItemsTwitterAdapter(TwitterTimeLineActivity.this, R.layout.fila_personalizada, timeLineURLImages, timeLineNames, timeLineContent);
                        ListView androidListView = (ListView) findViewById(R.id.lista);
                        androidListView.setAdapter(adapter);

                    }
                });
            }
        }.start();

    }
}







