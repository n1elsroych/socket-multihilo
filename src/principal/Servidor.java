package principal;

import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private static final int PORT = 8888;

    private static Map<String, PrintWriter> clientes = new HashMap<>(); // Almacena los clientes conectados y sus flujos de salida

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado en el puerto " + PORT);

        try {
            while (true) {
                new ManejadorCliente(serverSocket.accept()).start(); // Inicia un nuevo hilo por cada cliente que se conecte
            }
        } finally {
            serverSocket.close();
        }
    }

    /**
     * Maneja la conexión de un cliente
     */
    private static class ManejadorCliente extends Thread {
        private String nombreCliente;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Solicita el nombre del cliente y lo agrega a la lista de clientes conectados
                while (true) {
                    out.println("INGRESE SU NOMBRE:");
                    nombreCliente = in.readLine().trim();
                    if (nombreCliente == null || nombreCliente.isEmpty()) {
                        continue;
                    }
                    synchronized (clientes) {
                        if (!clientes.containsKey(nombreCliente)) {
                            clientes.put(nombreCliente, out);
                            break;
                        }
                    }
                }

                out.println("NOMBRE_ACEPTADO");
                out.println("BIENVENIDO " + nombreCliente + "!");
                enviarMensaje("El usuario " + nombreCliente + " se ha conectado.");

                // Escucha los mensajes de entrada del cliente y los reenvía a todos los demás clientes
                while (true) {
                    String input = in.readLine();
                    if (input == null || input.equals("/salir")) {
                        break;
                    }
                    enviarMensaje(nombreCliente + ": " + input);
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (nombreCliente != null) {
                    clientes.remove(nombreCliente);
                    enviarMensaje("El usuario " + nombreCliente + " se ha desconectado.");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }

        /**
         * Envía un mensaje a todos los clientes conectados
         */
        private void enviarMensaje(String mensaje) {
            synchronized (clientes) {
                for (PrintWriter writer : clientes.values()) {
                    writer.println(mensaje);
                }
            }
        }
    }
}

