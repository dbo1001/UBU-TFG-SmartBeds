package com.example.smartbeds;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BedsManagementActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private Context context = this;
    private int focusedItem=0;
    private boolean itemSelected=false;

    private BottomNavigationView bottomNavigation;

    private DrawerLayout drawer;
    private NavigationView navigation;


    @Override
    public void onBackPressed(){
        bottomNavigation = findViewById(R.id.beds_management_navigation);
        ListView listView = findViewById(R.id.beds_management_list);
        if(bottomNavigation.getVisibility()==View.VISIBLE){
            bottomNavigation.setVisibility(View.GONE);
            listView.getChildAt(focusedItem).setBackgroundColor(ContextCompat.getColor(context, R.color.background));
            itemSelected=false;
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beds_management);

        Session session = Session.getInstance();
        if(session.getToken()==null) {
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
        }

        createNavigationMenu();

        String urlParameters = "token="+session.getToken();
        JSONObject resultado = APIUtil.petitionAPI("/api/beds", urlParameters);
        int status = APIUtil.getStatusFromJSON(resultado);

        List<String> bedNames = new ArrayList<>();
        try{
            JSONArray bedsJSON = (JSONArray) resultado.get("beds");
            for(int i=0; i< bedsJSON.length(); i++){
                JSONObject bedJSON = (JSONObject) bedsJSON.get(i);
                bedNames.add((String) bedJSON.get("bed_name"));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        final ArrayAdapter<String> bedNamesAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, bedNames);
        final ListView listView = (ListView) findViewById(R.id.beds_management_list);
        listView.setAdapter(bedNamesAdapter);

        bottomNavigation = findViewById(R.id.beds_management_navigation);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                bottomNavigation.setVisibility(View.VISIBLE);
                parent.getChildAt(focusedItem).setBackgroundColor(ContextCompat.getColor(context, R.color.background));
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
                focusedItem=position;
                itemSelected=true;
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position!=focusedItem && itemSelected){
                    parent.getChildAt(focusedItem).setBackgroundColor(ContextCompat.getColor(context, R.color.background));
                    view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
                    focusedItem=position;
                }
            }
        });


        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch((String)menuItem.getTitle()){
                    case "Eliminar":
                        DialogUtil.showDialog(context, "Eliminar cama", "Esta acción no está disponible en la versión 1.0 de la aplicación.");
                        break;
                    case "Modificar":
                        DialogUtil.showDialog(context, "Modificar datos de la cama", "Esta acción no está disponible en la versión 1.0 de la aplicación.");
                        break;
                    case "Asignar usuarios":
                        Intent intent = new Intent(context, BedAsignUsersActivity.class);
                        Bundle b = new Bundle();
                        b.putString("bedName", bedNamesAdapter.getItem(focusedItem));
                        intent.putExtras(b);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }

    protected void anadirCama(View view){
        DialogUtil.showDialog(context, "Añadir cama", "Esta acción no está disponible en la versión 1.0 de la aplicación.");
    }

    private void createNavigationMenu(){
        Session session = Session.getInstance();
        drawer = findViewById(R.id.drawer_layout);
        navigation = findViewById(R.id.navigation_view);
        LinearLayout lLayout = (LinearLayout) navigation.getHeaderView(0).getRootView();
        RelativeLayout rLayout = (RelativeLayout) lLayout.getChildAt(0);

        TextView username = (TextView) rLayout.getChildAt(0);
        username.setText(session.getUsername());

        Menu menu = navigation.getMenu();


        menu.getItem(2).setEnabled(false);


        navigation.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Session session = Session.getInstance();
        Intent intent;
        switch ((String) menuItem.getTitle()){
            case "Modificar Contraseña":
                intent = new Intent(context, UserPassChangeActivity.class);
                Bundle b = new Bundle();
                b.putString("username", session.getUsername());
                intent.putExtras(b);
                startActivity(intent);
                break;
            case "Gestión de usuarios":
                intent = new Intent(context, UsersManagementActivity.class);
                startActivity(intent);
                break;
            case "Visualización de camas":
                intent = new Intent(context, BedsActivity.class);
                startActivity(intent);
                break;
            case "Cerrar sesión":
                Session.resetSession();
                intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
        return true;
    }

    protected void showMenu(View view){
        drawer.openDrawer(Gravity.LEFT);
    }
}
