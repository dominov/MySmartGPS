package com.smartinfo.mysmartgps;


import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import android.util.Log;
import android.util.Xml;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;

import android.telephony.TelephonyManager;

import com.smartinfo.mysmartgps.Httppostaux;
import com.smartinfo.mysmartgps.R;

public class SmartGPS extends Activity implements Runnable {

    private String host_name = "";
    private String imei = "";
    private String tiempo_envio = "";
    private String distancia_envio = "";

    private OutputStreamWriter fout = null;
    private XmlSerializer ser = Xml.newSerializer();
    private EditText url = null;
    private TextView label_url = null;
    private Button coonfButton = null;
    private Httppostaux post;
    private Timer timer = null;
    private boolean provider = false;
    private Thread thread = null;
    private TextView outlat;
    private TextView outlong;

    private LocationManager mLocationManager;
    private Location mLocation;
    private MyLocationListener mLocationListener;
    private Location currentLocation = null;
    private String urlserver = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_principal);
        boolean exit = valconfig("conf_instal");

        coonfButton = (Button) findViewById(R.id.btn_config);
        label_url = (TextView) findViewById(R.id.label_url);
        url = (EditText) findViewById(R.id.text_url);
        post = new Httppostaux();
        timer = new Timer();
        outlat = (TextView) findViewById(R.id.outlat);
        outlong = (TextView) findViewById(R.id.outlong);

        if (!exit) {
            outlat.setVisibility(View.INVISIBLE);
            outlong.setVisibility(View.INVISIBLE);

            //---Bloque de configuracion si no esta configurado
            coonfButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    getHostName();
                    startService();
                }
            });
            //---Fin Bloque configuracion
        } else {
            hiddenconfig_label();
            outlat.setVisibility(View.VISIBLE);
            outlong.setVisibility(View.VISIBLE);
            getconf("conf_instal");
            activar_hilo();
        }
    }

    void getHostName() {

        tiempo_envio = "10000";
        distancia_envio = "10";
        host_name = url.getText().toString();
        if (host_name.length() == 0)
            Toast.makeText(this, "Debe ingresar un DomineName", Toast.LENGTH_SHORT).show();
        else {
            TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            imei = TelephonyMgr.getDeviceId();
            boolean res = setconfig(host_name, imei, tiempo_envio, distancia_envio);
            if (res) {
                hiddenconfig_label();
                activar_hilo();
                outlat.setVisibility(View.VISIBLE);
                outlong.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setxml(String latit, String longit) {
        try {
            fout = new OutputStreamWriter(openFileOutput("signal_gps.xml", Context.MODE_PRIVATE));
            //Asignamos el resultado del serializer al fichero
            ser.setOutput(fout);

            //Construimos el XML
            ser.startTag("", "pointgps");

            ser.startTag("", "latitud");
            ser.text(latit);//ser.text(String.valueOf(latitud));
            ser.endTag("", "latitud");

            ser.startTag("", "longitud");
            ser.text(longit);//ser.text(String.valueOf(longitud));
            ser.endTag("", "longitud");

            ser.endTag("", "pointgps");

            ser.endDocument();

            fout.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            setNotification("No se pudo crear el archivo");
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            setNotification("IllegalArgumentException");
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            setNotification("IllegalStateException");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            setNotification("IOException");
        }
    }

    public boolean setconfig(String domine, String imei, String tiempo_envio, String distancia_envio) {
        try {
            fout = new OutputStreamWriter(openFileOutput("conf_instal.xml", Context.MODE_PRIVATE));
            //Asignamos el resultado del serializer al fichero
            ser.setOutput(fout);

            //Construimos el XML
            ser.startTag("", "config");

            ser.startTag("", "domine");
            ser.text(domine);//ser.text(String.valueOf(latitud));
            ser.endTag("", "domine");

            ser.startTag("", "imai");
            ser.text(imei);//ser.text(String.valueOf(longitud));
            ser.endTag("", "imai");

            ser.startTag("", "tiempo_envio");
            ser.text(tiempo_envio);//ser.text(String.valueOf(longitud));
            ser.endTag("", "tiempo_envio");

            ser.startTag("", "distancia_envio");
            ser.text(distancia_envio);//ser.text(String.valueOf(longitud));
            ser.endTag("", "distancia_envio");

            ser.endTag("", "config");

            ser.endDocument();

            fout.close();
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "No se puedo Configurar el aplicativo error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, "No se puedo Configurar el aplicativo error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, "No se puedo Configurar el aplicativo error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, "No se puedo Configurar el aplicativo error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public boolean valconfig(String name) {
        try {
            FileInputStream fil = openFileInput(name + ".xml");

            if (fil != null)
                return true;
            else
                return false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            return false;
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            return false;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block

            return false;
        }
    }


    public void getconf(String name) {
        try {
            FileInputStream fil = openFileInput(name + ".xml");

            if (fil != null) {
                //DOM (Por ejemplo)
                DocumentBuilderFactory factory =
                        DocumentBuilderFactory.newInstance();

                DocumentBuilder builder = factory.newDocumentBuilder();
                Document dom = builder.parse(fil);


                // Obtenemos la etiqueta raiz
                Element elementRaiz = dom.getDocumentElement();
                // Iteramos sobre sus hijos
                NodeList hijos = elementRaiz.getChildNodes();
                Node nodo = hijos.item(0);

                host_name = nodo.getTextContent().toString();
                nodo = hijos.item(1);
                imei = nodo.getTextContent().toString();
                tiempo_envio = nodo.getTextContent().toString();
                distancia_envio = nodo.getTextContent().toString();
            } else {
                host_name = "";
                imei = "";
                tiempo_envio = "";
                distancia_envio = "";
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private boolean validarHostName(String hostname) {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_smart_gps, menu);
        return true;
    }

    public void hiddenconfig_label() {
        Toast.makeText(this, "Ubicando datos", Toast.LENGTH_SHORT).show();
        label_url.setVisibility(View.INVISIBLE);
        url.setVisibility(View.INVISIBLE);
        coonfButton.setVisibility(View.INVISIBLE);
    }

    public void setNotification(String mesaje) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notManager = (NotificationManager) getSystemService(ns);
        int icono = android.R.drawable.stat_sys_warning;
        CharSequence textoEstado = "SmartGps!";
        long hora = System.currentTimeMillis();

        Notification notif = new Notification(icono, textoEstado, hora);
        Context contexto = getApplicationContext();
        CharSequence titulo = "SmartGps";
        CharSequence descripcion = mesaje;

        Intent notIntent = new Intent(contexto, SmartGPS.class);

        PendingIntent contIntent = PendingIntent.getActivity(contexto, 0, notIntent, 0);

        notif.setLatestEventInfo(contexto, titulo, descripcion, contIntent);
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        notManager.notify(12, notif);
    }

    public boolean enviar_datos(String longitud, String latitud, String id_usuario, String Urlserver, String tiempo_envio, String distancia_envio) {
        int logstatus = -1;

        ArrayList<NameValuePair> postparameterssend2 = new ArrayList<NameValuePair>();
        postparameterssend2.add(new BasicNameValuePair("lat", latitud));
        postparameterssend2.add(new BasicNameValuePair("long", longitud));
        postparameterssend2.add(new BasicNameValuePair("id_usuario", id_usuario));
        JSONArray jata = null;

        try {

            jata = post.getserverdata(postparameterssend2, Urlserver, tiempo_envio, distancia_envio);
        } catch (Exception e) {
            Log.e("HTTPPOST", "server " + e.toString() + " " + postparameterssend2.toString() + " " + Urlserver);
        }
        if (jata != null && jata.length() > 0) {
            JSONObject jdata_object;
            try {

                jdata_object = jata.getJSONObject(0);
                logstatus = jdata_object.getInt("logstatus");
                Log.e("loginstatus", "logstatus " + logstatus);
            } catch (JSONException e) {
                e.printStackTrace();

            }

            if (logstatus == 0) {
                Log.e("loginstatus", "invalido");
                return false;

            } else {
                Log.e("loginstatus", "valido");
                return true;
            }

        } else {
            Log.e("JSON", "ERROR");
            return false;
        }
    }

    public void activar_hilo() {
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                //writeSignalGPS();
                validar_provider();
            }
        }, 0, 180000);
    }

    private void validar_provider() {
        if (!provider) {
            writeSignalGPS();
        }
    }

    private void writeSignalGPS() {


        handler.sendEmptyMessage(0);

        thread = new Thread(this);
        thread.start();

    }

    public String showURL() {

        String urlserver = "http://" + host_name + "/movil/insertar_coordenadas.php";
        return urlserver;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //boolean res=false;
            //pd.dismiss();
            //mLocationManager.removeUpdates(mLocationListener);
            if (currentLocation != null) {
                double latitud = currentLocation.getLatitude();
                double longitud = currentLocation.getLongitude();
                String lat = "Latitude: " + latitud;
                String lon = "Longitude: " + longitud;

                String urlserver = "http://" + host_name + "/movil/insertar_coordenadas.php";

                outlat.setText(lat);
                outlong.setText(lon);

                //res = enviar_datos(String.valueOf(longitud),String.valueOf(latitud),imei,urlserver);
                    /*if(!res){
                        setNotification(lat+" "+lon);
    				//	setxml(String.valueOf(latitud),String.valueOf(longitud));	
    				}*/

                //thread.stop();
                //thread.destroy();
            }
        }
    };


    @Override
    public void run() {

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {//hay gps
            provider = true;
            Looper.prepare();

            mLocationListener = new MyLocationListener();

            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 180000, 50, mLocationListener);
            Looper.loop();
            Looper.myLooper().quit();


        } else if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = true;

            Looper.prepare();

            mLocationListener = new MyLocationListener();

            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 180000, 50, mLocationListener);
            Looper.loop();
            Looper.myLooper().quit();

        } else {// no hay gps
            provider = false;
            //setxml("0","0");
            setNotification("No se a podido establecer ubicacion configure uno de los dos protocolos");
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            //enviar_datos(String.valueOf(0),String.valueOf(0),"6117",urlserver);
        }
    }

    private void setCurrentLocation(Location loc) {
        currentLocation = loc;
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                /*Toast.makeText(getBaseContext(), 
                    getResources().getString(R.string.gps_signal_found), 
                    Toast.LENGTH_LONG).show();*/
                setCurrentLocation(loc);
                handler.sendEmptyMessage(0);
            }

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    private void startService() {
        Intent svc = new Intent(this, SmartService.class);
        startService(svc);
    }
}
