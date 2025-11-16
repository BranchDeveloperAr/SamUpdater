import org.update4j.Configuration;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateLauncher {

    public static void main(String[] args) throws Exception {

        String configUrlString = "https://github.com/tomas01cuello/sam-analisis/releases/download/4.0.0/config.xml";

        System.out.println("Descargando configuración desde: " + configUrlString);

        URL configUrl = new URL(configUrlString);
        HttpURLConnection connection = (HttpURLConnection) configUrl.openConnection();

        // Agregar User-Agent para evitar bloqueo de GitHub
        connection.setRequestProperty("User-Agent", "SamAnalisis-Updater/1.0");
        connection.setInstanceFollowRedirects(true);
        connection.connect();

        int responseCode = connection.getResponseCode();
        System.out.println("Código de respuesta: " + responseCode);

        if (responseCode != 200) {
            throw new Exception("Error al descargar config.xml. Código: " + responseCode);
        }

        Configuration config = Configuration.read(
                new InputStreamReader(connection.getInputStream())
        );

        System.out.println("Configuración cargada correctamente");
        System.out.println("Verificando actualizaciones...");

        if (config.requiresUpdate()) {
            System.out.println("Descargando actualizaciones...");
            config.update();
        } else {
            System.out.println("La aplicación está actualizada");
        }

        System.out.println("Iniciando aplicación...");
        config.launch();
    }
}