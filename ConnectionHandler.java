package BlockChain;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

//public class ConnectionHandler implements Runnable {
//    
//    private BCNode bcNode;
//    private ServerSocket serverSocket;
//    private ArrayList<Socket> connections = new ArrayList<>(); // Store connected nodes
//
//    public ConnectionHandler(BCNode bcNode, ServerSocket serverSocket) {
//        this.bcNode = bcNode;
//        this.serverSocket = serverSocket;
//    }
//
//    @Override
//    public void run() {
//        while (true) {
//            try {
////				synchronized (System.out) {
////					System.out.println("\nConnectionHandler " + bcNode.getPort() + " Waiting for connection...");
////				}
//                Socket socket = serverSocket.accept();
//                connections.add(socket); // Store new connection
//                
//                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
//                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
//                
////                synchronized (System.out) {
////                	System.out.println("ConnectionHandler " + bcNode.getPort() + " Connected to " + socket.getInetAddress() + "oos and ois created");
////                }
//
//                new Thread(new ReadHandler(bcNode, socket, oos, ois)).start();
//                
//                
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public ArrayList<Socket> getConnections() {
//        return connections;
//    }
//}


public class ConnectionHandler implements Runnable {
    
    private BCNode bcNode;
    private ServerSocket serverSocket;
    private ArrayList<Socket> connections = new ArrayList<>(); 
    private ArrayList<ObjectOutputStream> connectionOutputs = new ArrayList<>(); // Store output streams

    public ConnectionHandler(BCNode bcNode, ServerSocket serverSocket) {
        this.bcNode = bcNode;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                connections.add(socket);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                connectionOutputs.add(oos); // Store the output stream

                // Start a new read handler
                ReadHandler rh = new ReadHandler(bcNode, socket, oos, ois);
				Thread t = new Thread(rh);
				t.start();

            } catch (IOException e) {
                System.out.println("Error accepting new connection, but network will continue.");
            }
        }
    }


    public ArrayList<ObjectOutputStream> getConnectionOutputs() {
        return connectionOutputs;
    }
    
	public ArrayList<Socket> getConnections() {
		return connections;
	}

	public void removeConnection(ObjectOutputStream oos) {
		// TODO Auto-generated method stub
		connectionOutputs.remove(oos);
		
	}
}

