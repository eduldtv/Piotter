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
    Bitmap logo;

    public ItemsTwitterAdapter(Context _context, int _plantillaItemLista, ArrayList<String> _listaImagenes, ArrayList<String> _listaNombres, ArrayList<String> _listaContenidos) {
        super(_context, _plantillaItemLista);
        this.context = _context;
        this.plantillaItemLista = _plantillaItemLista;
        this.listaImagenes = _listaImagenes;
        this.listaNombres = _listaNombres;
        this.listaContenidos = _listaContenidos;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.context);

        View view = inflater.inflate(plantillaItemLista, null);

        TextView nombreUsuario = view.findViewById(R.id.nombre);
        TextView contenidoMensaje = view.findViewById(R.id.contenido);
        final ImageView fotoUsuario = view.findViewById(R.id.imagen);


        // hacemos un objeto de TwitterTimeLineActivity para poder acceder a los arrays
        final TwitterTimeLineActivity twitterTimeLineActivity = new TwitterTimeLineActivity();

        // accedemos a cada posicion del array y la metemos en sus respectivos textViews e imageView
        nombreUsuario.setText(listaNombres.get(position));
        contenidoMensaje.setText(listaContenidos.get(position));

        // la carga de la imagen la hacemos en un hilo

        new Thread() {
            public void run() {


                try {
                    InputStream is = new URL(listaImagenes.get(position)).openStream();
                    logo = BitmapFactory.decodeStream(is);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                // cuando vamos a cargar la imagen lo hacemos con un runOnUiThread
                twitterTimeLineActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        fotoUsuario.setImageBitmap(logo);

                    }
                });
            }
        }.start();


        return view;
    }

    // Explicación del porqué del Override de esta funcion getCount:
    // Un ArrayListAdapter siempre se suele crear con un objeto, ejm: ArrayListAdapter<MiObjeto>
    // haciéndolo así todos sus métodos funcionan como deberían, sin embargo, no lo hemos creado
    // de esa manera, y el método que usa el Adapter getCount() siempre devuelve 0 y nunca llama
    // a getView(), por eso Overrideamos getCount()
    @Override
    public int getCount() {
        // se podría usar cualquier otro array, pues son los 3 de la misma longitud
        return listaImagenes.size();
    }
}
