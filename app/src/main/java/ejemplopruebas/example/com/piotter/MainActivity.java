package ejemplopruebas.example.com.piotter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends Activity {
    // Constants
    /**
     * Register your here app https://dev.twitter.com/apps/new and get your
     * consumer key and secret
     */
    static String TWITTER_CONSUMER_KEY = "nCrImfm4UdCASrDxu3biOwEF5";
    static String TWITTER_CONSUMER_SECRET = "sRGpxhA0X7CFoxntMbS25zYA3JzpcrmWr3593wNtbjrRlIOuGr";

    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

    static final String TWITTER_CALLBACK_URL = "https://www.fempa.es";

    // Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

    // Login button
    Button btnLoginTwitter;
    // Update status button
    Button btnUpdateStatus;
    // Logout button
    Button btnLogoutTwitter;
    // Boton ver ultimos tweets
    Button botonTimeLine;
    // EditText for update
    EditText txtUpdate;
    // lbl update
    TextView lblUpdate;
    TextView lblUserName;
    ImageView imagenUsuario;
    String URLImagenUsuario;


    // Progress dialog
    ProgressDialog pDialog;

    // Twitter
    private static Twitter twitter;
    private static RequestToken requestToken;

    // Shared Preferences
    private static SharedPreferences mSharedPreferences;

    // Internet Connection detector
    private ConnectionDetector cd;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // All UI elements
        btnLoginTwitter = (Button) findViewById(R.id.btnLoginTwitter);
        btnUpdateStatus = (Button) findViewById(R.id.btnUpdateStatus);
        btnLogoutTwitter = (Button) findViewById(R.id.btnLogoutTwitter);
        txtUpdate = (EditText) findViewById(R.id.txtUpdateStatus);
        lblUpdate = (TextView) findViewById(R.id.lblUpdate);
        lblUserName = (TextView) findViewById(R.id.lblUserName);
        imagenUsuario = (ImageView) findViewById(R.id.imageViewUsuario);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // if añadido por problemas de permisos de acceso a internet
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        cd = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Check if twitter keys are set
        if (TWITTER_CONSUMER_KEY.trim().length() == 0 || TWITTER_CONSUMER_SECRET.trim().length() == 0) {
            // Internet Connection is not present
            alert.showAlertDialog(MainActivity.this, "Twitter oAuth tokens", "Please set your twitter oauth tokens first!", false);
            // stop executing code by return
            return;
        }


        // Shared Preferences
        mSharedPreferences = getApplicationContext().getSharedPreferences(
                "MyPref", 0);


/**
 * Twitter login button click event will call loginToTwitter() function
 * */
        btnLoginTwitter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Call login twitter function
                loginToTwitter();

                /*
                    Download the logo from online and set it as
                    ImageView image programmatically.
                */

            }
        });

        /**
         * Button click event to Update Status, will call updateTwitterStatus()
         * function
         * */
        btnUpdateStatus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Call update status function
                // Get the status from EditText
                String status = txtUpdate.getText().toString();

                // Check for blank text
                if (status.trim().length() > 0) {
                    // update status
                    new updateTwitterStatus().execute(status);
                } else {
                    // EditText is empty
                    Toast.makeText(getApplicationContext(),
                            "Por favor escribe un mensaje", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });


        /** Boton botonTimeLine
        */

        // se crearán los tres ArrayList de imagenes, nombres y contenidos, los rellenaremos
        // y se los pasaremos por EXTRAS  a TwitterTimeLineActivity

        botonTimeLine = (Button) findViewById(R.id.btnVerTimeLine);
        botonTimeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // para calcular el tiempo que se tarda haciéndolo con un hilo
                long tiempoInicial = System.currentTimeMillis();

                // ArrayLists de imangenes, nombres y contenidos
                final ArrayList<String> timeLineURLImages = new ArrayList<String>();
                final ArrayList<String> timeLineNames = new ArrayList<String>();
                final ArrayList<String> timeLineContent = new ArrayList<String>();

                // creamos el hilo que los rellenará
                new Thread() {
                    public void run() {
                        // al parecer, no es necesario runOnUiThread() para pasar a otra activity
                        // con un hilo a secas es posible

                        // runOnUiThread(new Runnable() {

                        //@Override
                        //public void run() {

                        try {
                            List<Status> statuses = twitter.getHomeTimeline();
                            for (Status status : statuses) {
                                timeLineURLImages.add(status.getUser().get400x400ProfileImageURL());
                                Log.d("fotosT", status.getUser().get400x400ProfileImageURL());

                                timeLineNames.add(status.getUser().getName());
                                Log.d("nombresT", status.getUser().getName());

                                timeLineContent.add(status.getText());
                                Log.d("textosT", status.getText());

                            }
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }

                        // intent a TwitterTimeLineActivity
                        // pasandole como EXTRAS los ArrayLists creados

                        Intent intent = new Intent(MainActivity.this, TwitterTimeLineActivity.class);

                        intent.putExtra("listaImagenes", timeLineURLImages);
                        intent.putExtra("listaNombres", timeLineNames);
                        intent.putExtra("listaContenidos", timeLineContent);

                        startActivity(intent);

                        // }
                        // });
                    }
                }.start();

                // para calcular el tiempo que se tarda haciéndolo con un hilo
                Log.d("tiempoBotonTimeLine", (System.currentTimeMillis() - tiempoInicial) + "");

            }
        });


        /**
         * Button click event for logout from twitter
         * */
        btnLogoutTwitter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Call logout twitter function
                logoutFromTwitter();
            }
        });


        /** This if conditions is tested once is
         * redirected from twitter page. Parse the uri to get oAuth
         * Verifier
         * */
        if (!isTwitterLoggedInAlready()) {
            Uri uri = getIntent().getData();
            // vuelve a llamar al onCreate y si contiene una URL que empieza con twitter_callback_url
            if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
                // oAuth verifier
                String verifier = uri
                        .getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

                try {
                    // Get the access token
                    AccessToken accessToken = twitter.getOAuthAccessToken(
                            requestToken, verifier);

                    // Shared Preferences
                    Editor e = mSharedPreferences.edit();

                    // After getting access token, access token secret
                    // store them in application preferences7

                    e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
                    e.putString(PREF_KEY_OAUTH_SECRET,
                            accessToken.getTokenSecret());
                    // Store login status - true
                    e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
                    e.commit(); // save changes

                    Log.e("Twitter OAuth Token", "> " + accessToken.getToken());

                    // Getting user details from twitter
                    // For now i am getting his name only
                    long userID = accessToken.getUserId();
                    User user = twitter.showUser(userID);

                    cargarInterfazUsuarioLogeado(user);

                } catch (Exception e) {
                    // Check log for login errors
                    Log.e("Twitter Login Error", "> " + e.getMessage());
                }
            }
        } else {
            // si entra aquí es que el usuario está logeado antes, y aquí conseguimos su clave
            // y ponemos la interfaz como si estuviera ya logeado aunque cierre y abra la aplicacion
            mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");

            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
            Configuration configuration = builder.build();

            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();
            //builder.setOAuthAccessTokenSecret()


            AccessToken accessToken = new AccessToken(mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, ""), mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, ""));

            twitter.setOAuthAccessToken(accessToken);
            try {

                User nuevoUsuario = twitter.showUser(accessToken.getUserId());

                cargarInterfazUsuarioLogeado(nuevoUsuario);

            } catch (TwitterException e) {
                e.printStackTrace();
            }

        }


    } // termina el onCreate


    // funcion cargar interfaz usuario ya logeado

    public void cargarInterfazUsuarioLogeado(User user) {


        btnLoginTwitter = (Button) findViewById(R.id.btnLoginTwitter);
        btnUpdateStatus = (Button) findViewById(R.id.btnUpdateStatus);
        btnLogoutTwitter = (Button) findViewById(R.id.btnLogoutTwitter);
        txtUpdate = (EditText) findViewById(R.id.txtUpdateStatus);
        lblUpdate = (TextView) findViewById(R.id.lblUpdate);
        lblUserName = (TextView) findViewById(R.id.lblUserName);
        imagenUsuario = (ImageView) findViewById(R.id.imageViewUsuario);


        // Hide login button
        btnLoginTwitter.setVisibility(View.GONE);

        // Show Update Twitter
        lblUpdate.setVisibility(View.VISIBLE);
        txtUpdate.setVisibility(View.VISIBLE);
        btnUpdateStatus.setVisibility(View.VISIBLE);
        btnLogoutTwitter.setVisibility(View.VISIBLE);

        botonTimeLine.setVisibility(View.VISIBLE);
        imagenUsuario.setVisibility(View.VISIBLE);

        // Getting user details from twitter
        // For now i am getting his name only
        //long userID = accessToken.getUserId();
        //User user = twitter.showUser(userID);

        String username = user.getName();

        // Displaying in xml ui
        lblUserName.setText(Html.fromHtml("<b>Bienvenido/a " + username + "</b>"));

        // Guardar imagen usuario en un string
        URLImagenUsuario = user.get400x400ProfileImageURL();

        // llamamos a asynctask para descargar y mostrar la imagen del perfil de twitter
        new DownLoadImageTask(imagenUsuario).execute(URLImagenUsuario);

        long tiempoInicial = System.currentTimeMillis();

    }


    /**
     * Function to login twitter
     */
    private void loginToTwitter() {
        // Check if already logged in
        if (!isTwitterLoggedInAlready()) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
            Configuration configuration = builder.build();

            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            try {
                requestToken = twitter
                        .getOAuthRequestToken(TWITTER_CALLBACK_URL);
                this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse(requestToken.getAuthenticationURL())));
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {
            // user already logged into twitter
            Toast.makeText(getApplicationContext(),
                    "Ya está autenticado en Twitter", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Check user already logged in your application using twitter Login flag is
     * fetched from Shared Preferences
     */
    private boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }


    /**
     * Function to update status
     */
    class updateTwitterStatus extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Updating to twitter...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Places JSON
         */

        protected String doInBackground(String... args) {
            Log.d("Tweet Text", "> " + args[0]);
            String status = args[0];
            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
                builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

                // Access Token
                String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
                // Access Token Secret
                String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                // Update status
                twitter4j.Status response = twitter.updateStatus(status);

                Log.d("Status", "> " + response.getText());
            } catch (TwitterException e) {
                // Error in updating status
                Log.d("Twitter Update Error", e.getMessage());
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog and show
         * the data in UI Always use runOnUiThread(new Runnable()) to update UI
         * from background thread, otherwise you will get error
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            // no hace falta poner runonUIThread, ya que ese método se ejecuta en el hilo principal
            //runOnUiThread(new Runnable() {
                //@Override
                //public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Mensaje enviado satisfactoriamente", Toast.LENGTH_SHORT)
                            .show();
                    // Clearing EditText field
                    txtUpdate.setText("");
               // }
            //});
        }

    }

    /**
     * Function to logout from twitter
     * It will just clear the application shared preferences
     */
    private void logoutFromTwitter() {
        // Clear the shared preferences
        Editor e = mSharedPreferences.edit();
        e.remove(PREF_KEY_OAUTH_TOKEN);
        e.remove(PREF_KEY_OAUTH_SECRET);
        e.remove(PREF_KEY_TWITTER_LOGIN);
        e.commit();

        // After this take the appropriate action
        // I am showing the hiding/showing buttons again
        // You might not needed this code
        btnLogoutTwitter.setVisibility(View.GONE);
        btnUpdateStatus.setVisibility(View.GONE);
        txtUpdate.setVisibility(View.GONE);
        lblUpdate.setVisibility(View.GONE);
        lblUserName.setText("");
        lblUserName.setVisibility(View.GONE);

        btnLoginTwitter.setVisibility(View.VISIBLE);
        imagenUsuario.setVisibility(View.GONE);
        botonTimeLine.setVisibility(View.GONE);
    }

    // Asynctask para actualizar la imagen del usuario
/*
        AsyncTask enables proper and easy use of the UI thread. This class
        allows to perform background operations and publish results on the UI
        thread without having to manipulate threads and/or handlers.
     */

    /*
        final AsyncTask<Params, Progress, Result>
            execute(Params... params)
                Executes the task with the specified parameters.
     */
    private class DownLoadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownLoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        /*
            doInBackground(Params... params)
                Override this method to perform a computation on a background thread.
         */
        protected Bitmap doInBackground(String... urls) {
            String urlOfImage = urls[0];
            Bitmap logo = null;
            try {
                InputStream is = new URL(urlOfImage).openStream();
                /*
                    decodeStream(InputStream is)
                        Decode an input stream into a bitmap.
                 */
                logo = BitmapFactory.decodeStream(is);
            } catch (Exception e) { // Catch the download exception
                e.printStackTrace();
            }
            return logo;
        }

        /*
            onPostExecute(Result result)
                Runs on the UI thread after doInBackground(Params...).
         */
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}


