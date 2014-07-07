package com.smartinfo.mysmartgps;

/**
 * Created by CristianPC on 3/07/14.
 */
public class Configuracion_Globales {

    public static String imei = "";
    public static String host_name = "";
    public static int tienpo_actualizacion = 10000;
    public static int distancia_actualizacion = 50;
    public static String url_script = "/movil/insertar_coordenadas.php";


    public static void set_valores(String imeiii, String host_name, int tiempo, int distancia) {
        Configuracion_Globales.imei = imeiii;
        Configuracion_Globales.host_name = host_name;
        Configuracion_Globales.tienpo_actualizacion = tiempo;
        Configuracion_Globales.distancia_actualizacion = distancia;
    }


}
