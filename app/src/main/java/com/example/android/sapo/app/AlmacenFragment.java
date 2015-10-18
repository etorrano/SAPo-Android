package com.example.android.sapo.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sapo.app.adapters.TiendaAdapter;
import com.example.android.sapo.app.datatypes.DataTienda;

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

public class AlmacenFragment extends Fragment {

    private TiendaAdapter tiendasAdapter;

    public AlmacenFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        List<DataTienda> list = new ArrayList<DataTienda>();

        tiendasAdapter =
                new TiendaAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_tiendas, // The name of the layout ID.
                        (ArrayList<DataTienda>) list);

        updateAlmacenes();

        View rootView = inflater.inflate(R.layout.fragment_tiendas, container, false);
        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_tiendas);
        listView.setAdapter(tiendasAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getActivity(), CategoriasActivity.class)
                        .putExtra("almacenID", tiendasAdapter.getItem(i).getId())
                        .putExtra("almacenNombre", tiendasAdapter.getItem(i).getNombre());

                startActivity(intent);
            }
        });

        return rootView;
    }


    private void updateAlmacenes() {
        FetchTiendasTask tiendasTask = new FetchTiendasTask(getActivity(), tiendasAdapter);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        tiendasTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateAlmacenes();
    }

}