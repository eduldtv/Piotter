package ejemplopruebas.example.com.piotter;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class creadorArrayTimeLine implements Runnable {

    String imagen = "";
    String nombre = "";
    String contenido = "";

    final ArrayList<String> timeLineURLImages = new ArrayList<String>();
    final ArrayList<String> timeLineNames = new ArrayList<String>();
    final ArrayList<String> timeLineContent = new ArrayList<String>();

    Twitter twitter;

    int numeroTarea = 0;

    public creadorArrayTimeLine(Twitter _twitter, int _numeroTarea) throws TwitterException {
        this.twitter = _twitter;
        this.numeroTarea = _numeroTarea;
    }

    List<Status> statuses = twitter.getHomeTimeline();

    @Override
    public void run() {
        for (Status status : statuses) {

            switch (numeroTarea) {
                case 1:
                    timeLineURLImages.add(status.getUser().get400x400ProfileImageURL());
                    Log.d("fotosPT", status.getUser().get400x400ProfileImageURL());
                    break;
                case 2:
                    timeLineNames.add(status.getUser().getName());
                    Log.d("nombrePT", status.getUser().getName());
                    break;
                case 3:
                    timeLineContent.add(status.getText());
                    Log.d("textoPT", status.getText());
                    break;
                default:
                    break;

            }
        }
    }
}
