package BlockChain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class ReadHandler implements Runnable {
	
	private BCNode bcNode;
	private ObjectOutputStream objectOutputStream;
	private ObjectInputStream objectInputStream;
	private ArrayList<Block> blockchain;
	private int difficulty;
	

	public ReadHandler(BCNode bcNode, Socket socket, ObjectOutputStream objectOutputStream,
			ObjectInputStream objectInputStream) {
		// TODO Auto-generated constructor stub
		this.bcNode = bcNode;
		this.objectOutputStream = objectOutputStream;
		this.objectInputStream = objectInputStream;
//		synchronized (System.out) {
//            System.out.println("\nReadHandler " + bcNode.getPort() + " Connected to " + socket.getInetAddress() + "\n");
//		}
		}

	@Override
	public void run() {
	    try {
	        while (true) {
	            String message = (String) this.objectInputStream.readObject();
	            
	            if (message.equals("give me your blockchain")) {
	                this.objectOutputStream.writeObject(this.bcNode.getBlockchain());
	                this.objectOutputStream.flush();
	            } 
	            else if (message.equals("here is my blockchain")) {
	                Block receivedBlock = (Block) this.objectInputStream.readObject();
	                this.bcNode.setBlock(receivedBlock);
	            }
	        }
	    } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
	        System.out.println("A node has disconnected. Removing from active connections.");
	        this.bcNode.terminateConnection(this.objectOutputStream, this.objectInputStream);
	    }
	}


	

	

}
