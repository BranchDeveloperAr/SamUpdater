import org.update4j.Configuration;
import org.update4j.service.UpdateHandler;
import org.update4j.FileMetadata;

import javax.swing.*;
import java.awt.*;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class UpdateLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new UpdateLauncher().start();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error al iniciar: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }

    public void start() throws Exception {
        // Crear ventana de progreso
        JFrame frame = new JFrame("SamAnalisis - Actualizador");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 150);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel statusLabel = new JLabel("Verificando actualizaciones...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("0%");
        progressBar.setIndeterminate(true);

        panel.add(statusLabel, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);

        frame.add(panel);
        frame.setVisible(true);

        // Cargar configuración
        new Thread(() -> {
            try {
                String configUrlString = "https://github.com/BranchDeveloperAr/SamUpdater/releases/latest/download/config.xml";

                SwingUtilities.invokeLater(() -> statusLabel.setText("Conectando al servidor..."));

                URL configUrl = new URL(configUrlString);
                HttpURLConnection connection = (HttpURLConnection) configUrl.openConnection();
                connection.setRequestProperty("User-Agent", "SamAnalisis-Updater/1.0");
                connection.setInstanceFollowRedirects(true);

                Configuration config = Configuration.read(
                        new InputStreamReader(connection.getInputStream())
                );

                SwingUtilities.invokeLater(() -> statusLabel.setText("Verificando archivos..."));

                boolean needsUpdate = config.requiresUpdate();

                if (needsUpdate) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Descargando actualizaciones...");
                        progressBar.setIndeterminate(false);
                    });

                    // Versión alternativa minimalista si la anterior falla
                    UpdateHandler handler = new UpdateHandler() {
                        @Override
                        public void updateDownloadFileProgress(FileMetadata file, float progress) {
                            int percent = (int) (progress * 100);
                            SwingUtilities.invokeLater(() -> {
                                progressBar.setValue(percent);
                                progressBar.setString(percent + "%");
                            });
                        }

                        @Override
                        public void failed(Throwable t) {
                            t.printStackTrace();
                        }
                    };

                    // Realizar la actualización
                    config.update(handler);

                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Actualización completada");
                        progressBar.setValue(100);
                        progressBar.setString("100%");
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Aplicación actualizada");
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(100);
                        progressBar.setString("100%");
                    });
                }

                Thread.sleep(500);

                SwingUtilities.invokeLater(() -> statusLabel.setText("Iniciando aplicación..."));

                // Lanzar la aplicación
                config.launch();

                // Cerrar ventana de actualización
                Thread.sleep(1000);
                SwingUtilities.invokeLater(() -> frame.dispose());

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame,
                            "Error durante la actualización:\n" + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    System.exit(1);
                });
            }
        }).start();
    }
}