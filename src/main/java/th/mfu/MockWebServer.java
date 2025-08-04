package th.mfu;

import java.io.*;
import java.net.*;

public class MockWebServer implements Runnable {

    private int port;
    private volatile boolean running = true;

    public MockWebServer(int port) {
        this.port = port;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Mock Web Server running on port " + port + "...");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    try (
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
                    ) {
                        // Read and print the HTTP request (just the first line for simplicity)
                        String requestLine = in.readLine();
                        System.out.println("Received on port " + port + ": " + requestLine);

                        // Send a simple HTTP response
                        String response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n"
                                + "<html><body>Hello, Web! on Port " + port + "</body></html>";
                        out.println(response);
                    }

                    clientSocket.close();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error handling client: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server on port " + port + ": " + e.getMessage());
        }

        System.out.println("Server on port " + port + " stopped.");
    }

    public static void main(String[] args) {
        MockWebServer server8080 = new MockWebServer(8080);
        MockWebServer server8081 = new MockWebServer(8081);

        Thread server1 = new Thread(server8080);
        Thread server2 = new Thread(server8081);

        server1.start();
        server2.start();

        System.out.println("Press Enter to stop the server...");
        try {
            System.in.read();

            server8080.stop();
            server8081.stop();

            server1.join();
            server2.join();

            System.out.println("Mock web servers stopped.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
