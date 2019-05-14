package com.example.smartbeds;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;

public class BedsActivity extends AppCompatActivity {

    private final Context context = this;

    private Socket mSocket;
    private Socket inSocket;
    private Manager manager;

    private JSONObject data;

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mSocket.close();
        inSocket.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beds);

        Session session = Session.getInstance();
        if(session.getToken()==null) {
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
        }

        String urlParameters = "token="+session.getToken();
        JSONObject resultado = APIUtil.petitionAPI("/api/beds", urlParameters);
        int status = APIUtil.getStatusFromJSON(resultado);

        List<String> bed_names = new ArrayList<String>();
        try {
            JSONArray beds_info = (JSONArray) resultado.get("beds");
            for (int i = 0; i < beds_info.length(); i++) {
                JSONObject bed_info = (JSONObject) beds_info.get(i);
                bed_names.add( (String) bed_info.get("bed_name"));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        urlParameters = "token="+session.getToken()+"&bedname="+bed_names.get(0);
        resultado = APIUtil.petitionAPI("/api/bed", urlParameters);
        status = APIUtil.getStatusFromJSON(resultado);

        String namespace=null;
        try {
            namespace = (String) resultado.get("namespace");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        data = new JSONObject();
        try {
            data.put("namespace", namespace);
            data.put("bedname", bed_names.get(0));
        }catch(JSONException e){
            e.printStackTrace();
        }
        Log.d("data", data.toString());

        try {
            manager = new Manager(new URI("https://ubu.joselucross.com"));
            manager.open();
            mSocket = manager.socket("/"); //namespace genérico
            inSocket = manager.socket("/"+namespace);
            mSocket.open();
            inSocket.open();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if(mSocket.connected()){
                    Log.d("CONECTADO", "SI");
                }
                mSocket.emit("give_me_data", data);
                //mSocket.close();
            }
        });

        inSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if(inSocket.connected()){
                    Log.d("CONECTADO 2", "SI");
                }
            }
        });

        inSocket.on("package", new Emitter.Listener(){
            @Override
            public void call(Object... args) {
                JSONObject resultado = (JSONObject) args[0];
                Log.d("RESULTADO", resultado.toString());
            }
        });


        final ArrayAdapter<String> bedsAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, bed_names);

        ListView listView = (ListView) findViewById(R.id.beds_list);
        listView.setAdapter(bedsAdapter);

    }
}
