package ejemplopruebas.example.com.piotter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class ItemsTwitterAdapter extends ArrayAdapter {

    Context context;
    int plantillaItemLista;
    ArrayList<String> listaImagenes;
    ArrayList<String> listaNombres;
    ArrayList<String> listaContenidos;

    public ItemsTwitterAdapter(Context _context, int _plantillaItemLista, ArrayList<String> _listaImagenes, ArrayList<String> _listaNombres, ArrayList<String> _listaContenidos) {
        super(_context, _plantillaItemLista);
        this.context = _context;
        this.plantillaItemLista = _plantillaItemLista;
        this.listaImagenes = _listaImagenes;
        this.listaNombres = _listaNombres;
        this.listaContenidos = _listaContenidos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.context);

        View view = inflater.inflate(plantillaItemLista, null);

        TextView nombreUsuario = view.findViewById(R.id.nombre);
        TextView contenidoMensaje = view.findViewById(R.id.contenido);
        ImageView fotoUsuario = view.findViewById(R.id.imagen);

        TwitterTimeLineActivity twitterTimeLineActivity = new TwitterTimeLineActivity();

        nombreUsuario.setText(twitterTimeLineActivity.timeLineNames.get(position));
        contenidoMensaje.setText(twitterTimeLineActivity.timeLineContent.get(position));

        try {
            InputStream is = new URL(twitterTimeLineActivity.timeLineURLImages.get(position)).openStream();
            Bitmap logo = BitmapFactory.decodeStream(is);
            fotoUsuario.setImageBitmap(logo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return view;
    }
}
