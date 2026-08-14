package com.resilichain.simulator.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Raw TCP server that lets a presenter (or a script) drive the simulator live: START/STOP the
 * tick loop, ask for STATUS, or trigger a DISRUPT event, without redeploying anything.
 * One thread per connection; command parsing/handling is delegated so this class stays
 * socket-plumbing only.
 */
public final class ControlServer implements Runnable {

    private final int port;
    private final CommandHandler handler;

    public ControlServer(int port, CommandHandler handler) {
        this.port = port;
        this.handler = handler;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[TCP] Control server listening on port " + port);
            while (!serverSocket.isClosed()) {
                Socket client = serverSocket.accept();
                Thread.ofVirtual().start(() -> handleClient(client));
            }
        } catch (IOException e) {
            System.err.println("[TCP] Control server error: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket) {
        try (socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {

            writer.println("ResiliChain Twin simulator control channel. Type HELP for commands.");
            String line;
            while ((line = reader.readLine()) != null) {
                Command command = CommandParser.parse(line);
                writer.println(handler.handle(command));
                if (command instanceof Command.Quit) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("[TCP] Client handling error: " + e.getMessage());
        }
    }
}
