package principal;

import java.io.*;
import java.net.*;

public class Cliente {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 8888;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(SERVER_ADDRESS, PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        String nombreCliente = null;
        while (true) {
            String input = in.readLine();
            if (input.startsWith("INGRESE SU NOMBRE:")) {
                System.out.println(input);
                nombreCliente = stdIn.readLine().trim();
                out.println(nombreCliente);
            } else if (input.startsWith("NOMBRE_ACEPTADO")) {
                System.out.println("Nombre aceptado. ¡Bienvenido " + nombreCliente + "!");
                break;
            }
        }

        // Crea un hilo para leer los mensajes del servidor
        Thread thread = new Thread(() -> {
            try {
                String input;
                while ((input = in.readLine()) != null) {
                    System.out.println(input);
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        });
        thread.start();

        // Escucha los mensajes de entrada del usuario y los envía al servidor
        String userInput;
        while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
        }

        // Cierra los flujos de entrada y salida y el socket
        out.close();
        in.close();
        socket.close();
    }
}

