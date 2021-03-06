package com.example.android.sapo.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.sapo.app.adapters.CategoriaAdapter;
import com.example.android.sapo.app.datatypes.DataCategoria;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alejandro on 12-Oct-15.
 */
public class CategoriasFragment extends Fragment {

    private final String LOG_TAG = CategoriasFragment.class.getSimpleName();
    private CategoriaAdapter categoriasAdapter;
    private String almacenID;

    public CategoriasFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        List<DataCategoria> list = new ArrayList<DataCategoria>();

        categoriasAdapter =
                new CategoriaAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_categorias, // The name of the layout ID.
                        (ArrayList<DataCategoria>) list);

        View rootView = inflater.inflate(R.layout.fragment_categorias, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_categorias);


        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra("almacenID")) {
                almacenID = intent.getStringExtra("almacenID");
            }
            if (intent.hasExtra("almacenNombre")){
                Activity activity = getActivity();
                activity.setTitle(intent.getStringExtra("almacenNombre"));
            }
        }

        FetchCategoriasTask fetchCategoriasTask = new FetchCategoriasTask();
        if (almacenID != null){
            listView.setAdapter(categoriasAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Context context = getActivity();
                    Intent intent = new Intent(getActivity(), ProductosActivity.class)
                            .putExtra("categoriaID", categoriasAdapter.getItem(i).getIdCategoria())
                            .putExtra("categoriaNombre", categoriasAdapter.getItem(i).getNombre())
                            .putExtra("almacenID", almacenID);
                    startActivity(intent);
                }
            });
            fetchCategoriasTask.execute(almacenID);
        }

        return rootView;
    }

    public class FetchCategoriasTask extends AsyncTask<String, Void, DataCategoria[]> {

        private final String LOG_TAG = FetchCategoriasTask.class.getSimpleName();

        private DataCategoria[] getCategorias(String JsonStr) throws JSONException {
            JSONObject oJson = new JSONObject(JsonStr);
            JSONArray categorias = oJson.getJSONArray("categorias");
            DataCategoria[] resultStrs = new DataCategoria[categorias.length()];

            for(int i = 0; i < categorias.length(); i++) {
                JSONObject categoria = categorias.getJSONObject(i);
                resultStrs[i] = new DataCategoria();
                resultStrs[i].setNombre(categoria.getString("nombre"));
                resultStrs[i].setIdCategoria(categoria.getInt("id"));
            }
            return resultStrs;
        }

        @Override
        protected DataCategoria[] doInBackground(String... strings) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String JsonStr = null;

            try {
                final String SAPO_BASE_URL = "https://sapo.azure-api.net/sapo/almacenes";
                final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
                final String OCP_APIM_SUBSCRIPTION_VALUE = "9f86432ae415401db0383f63ce64c4fe";
                final String ALMACENID_VALUE = strings[0].toString();

                Uri builtUri = Uri.parse(SAPO_BASE_URL).buildUpon()
                        .appendPath(ALMACENID_VALUE)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Crea la conexión a Azure con la OCP_APIM_SUBSCRIPTION_KEY.
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty(OCP_APIM_SUBSCRIPTION_KEY, OCP_APIM_SUBSCRIPTION_VALUE);
                urlConnection.connect();

                // Lee el input stream
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Si no leyó nada, termina.
                    return null;
                }

                JsonStr = buffer.toString();
                Log.v(LOG_TAG, "JSON: " + JsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    //Cierra la conexión
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                //Parsea el JSON.
                return getCategorias(JsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(DataCategoria[] result) {
            if (result != null) {
                categoriasAdapter.clear();
                for(DataCategoria r : result) {
                    categoriasAdapter.add(r);
                }
            }
        }
    }
}
