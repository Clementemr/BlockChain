package BlockChain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.net.Inet4Address;
import java.net.ServerSocket;



public class BCNode {
	
	private ArrayList<Block> blockchain;
	private int difficulty = 5;
	private ArrayList<BCNode> nodes;
	private ConnectionHandler connectionHandler;
	private ArrayList<ObjectOutputStream> oos;
	private ArrayList<ObjectInputStream> ois;
	private ServerSocket ss;
	private ArrayList<Socket> sockets = new ArrayList<Socket>();
	private int myPort;
	
	public BCNode(int myPort, List<Integer> remotePorts) throws NoSuchAlgorithmException {
		this.myPort=myPort;
		this.blockchain = new ArrayList<Block>();
		// Create the genesis block
		Block b = new Block();
		try {
			b.mineBlock(difficulty);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.blockchain.add(b);
		
		try {
			 this.ss = new ServerSocket(myPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//connect to other nodes
		this.nodes = new ArrayList<BCNode>();
		this.sockets = new ArrayList<Socket>();
		this.oos = new ArrayList<ObjectOutputStream>();
		this.ois = new ArrayList<ObjectInputStream>();
		int i = 0;
		while (remotePorts.size() > 0) {
			int remotePort = remotePorts.remove(0);
			try {
				Socket s = new Socket("localhost", remotePort);
				this.sockets.add(s);
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				this.oos.add(oos);
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
				this.ois.add(ois);
				oos.writeObject("give me your blockchain");
				oos.flush();
				ArrayList<Block> remoteChain = (ArrayList<Block>) ois.readObject();
				this.setBlockchain(remoteChain);
//				System.out.println("Connected to node on port: " + remotePort + " with blockchain: " + remoteChain);
				
				//make readhandler as a thread
				ReadHandler rh = new ReadHandler(this, s, oos, ois);
				Thread t = new Thread(rh);
				t.start();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			i++;
		}
		
		//set up connectionhandler as a thread
		this.connectionHandler = new ConnectionHandler(this, this.ss);
		Thread t = new Thread(connectionHandler);
		t.start();

		
	}


	private void setBlockchain(ArrayList<Block> remoteChain) {
		// TODO Auto-generated method stub
		this.blockchain = remoteChain;
		
	}


	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Scanner keyScan = new Scanner(System.in);
        
        // Grab my port number on which to start this node
        synchronized (System.out) {
        System.out.print("Enter port to start (on current IP): ");
        }
        int myPort = keyScan.nextInt();
        
        // Need to get what other Nodes to connect to
        synchronized (System.out) {
        System.out.print("Enter remote ports (current IP is assumed): ");
        }
        keyScan.nextLine(); // skip the NL at the end of the previous scan int
        String line = keyScan.nextLine();
        List<Integer> remotePorts = new ArrayList<Integer>();
        if (line != "") {
            String[] splitLine = line.split(" ");
            for (int i=0; i<splitLine.length; i++) {
                remotePorts.add(Integer.parseInt(splitLine[i]));
            }
        }
        // Create the Node
        BCNode n = new BCNode(myPort, remotePorts);
        
        String ip = "";
        try {
             ip = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
        synchronized (System.out) {
        System.out.println("Node started on port " + myPort);
        }
        
        // Node command line interface
        while(true) {
        	synchronized (System.out) {
            System.out.println("\nNODE on port: " + myPort);
            System.out.println("1. Display Node's blockchain");
            System.out.println("2. Create/mine new Block");
            System.out.println("3. Kill Node");
            System.out.print("Enter option: ");
        	}
            int in = keyScan.nextInt();
            
            if (in == 1) {
            	synchronized (System.out) {
                System.out.println(n);
            	}
                
            } else if (in == 2) {
                // Grab the information to put in the block
            	synchronized (System.out) {
                System.out.print("Enter information for new Block: ");
            	}
                String blockInfo = keyScan.next();
                Block b = new Block(blockInfo);
                n.addBlock(b);
                
            } else if (in == 3) {
                // Take down the whole virtual machine (and all the threads)
                //   for this Node.  If we just let main end, it would leave
                //   up the Threads the node created.
                keyScan.close();
                System.exit(0);
            }
        }
    }


	
	
	

	void addBlock(Block b) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		// TODO Auto-generated method stub
		//chain the block to the previous block
		b.setPreviousHash(this.blockchain.get(this.blockchain.size()-1).getHash());
		//mine the block
		try {
			b.mineBlock(difficulty);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(b);
		//validate the block
		if (validateBlock(blockchain, b,blockchain.size()) == 1) {
			this.blockchain.add(b);
			synchronized (System.out) {
			System.out.println("Block added to the blockchain");
			}
		}
		else {
			synchronized (System.out) {
			System.out.println("Block is not valid");
			}
		}
		if (validateChain(blockchain) == 1) {
			synchronized (System.out) {
			System.out.println("Blockchain is valid");
			}
		} else {
			synchronized (System.out) {
			System.out.println("Blockchain is not valid");
			}
		}
		
		updateRemoteChains(b);
		
		
	}
	

	private void updateRemoteChains(Block b) {
//	    synchronized (System.out) {
//	        System.out.println("Broadcasting blockchain update to peers...");
//	    }

	    // Send update to outbound connections
	    for (ObjectOutputStream oos : this.oos) {
	        try {
	            oos.writeObject("here is my blockchain");
	            oos.writeObject(b);
	            oos.flush();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    // Send update to inbound connections (connections stored in ConnectionHandler)
	    for (ObjectOutputStream oos : this.connectionHandler.getConnectionOutputs()) {
	        try {
	            oos.writeObject("here is my blockchain");
	            oos.writeObject(b);
	            oos.flush();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}



	public int validateBlock(ArrayList<Block> blockChain, Block b, int index) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		int N = this.difficulty;
		if(b.getPreviousHash()=="0") {
            return 1;
		}
		String prefixZeros = new String(new char[N]).replace('\0', '0');
		String theHash = b.calculateBlockHash();
		if(theHash.substring(0, N).equals(prefixZeros)) {
				if (b.getHash().equals(b.calculateBlockHash())) {
					
//					System.out.println(b.getPreviousHash());
//					System.out.println(blockChain.get(index-1).getHash());
					
					if(b.getPreviousHash().equals(blockChain.get(index-1).getHash())) {
						return 1;
					} else {
						synchronized (System.out) {
                        System.out.println("Error: Previous hash is invalid.");
						}
                        return 0;
					}
                }else {
                	synchronized (System.out) {
                    System.out.println("Error: Block hash is invalid.");
                                        	}
                    return 0;
                }
            }else {
            	synchronized (System.out) {
                System.out.println("Error: Block hash does not meet difficulty requirements.");
                                    	}
                return 0;
            }
		}

	
	public int validateChain(ArrayList<Block> chain) throws NoSuchAlgorithmException, UnsupportedEncodingException {
	    if (chain.isEmpty()) {
	    	synchronized (System.out) {
	        System.out.println("Error: Blockchain is empty, cannot validate.");
	                        	}
	        return 0;
	    }

	    if (!chain.get(0).getPreviousHash().equals("0")) {
	    	synchronized (System.out) {
	        System.out.println("Error: Genesis block is invalid.");
	                    	}
	        return 0;
	    }

	    for (int i = 1; i < chain.size(); i++) {
	        if (validateBlock(chain, chain.get(i),i) == 0) {
	        	synchronized (System.out) {
	            System.out.println("Error: Blockchain validation failed at block index " + i);
	                            	}
	            return 0;
	        }
	    }

	    return 1; // Blockchain is valid
	}




	public synchronized String toString() {
		String s = "";
		for (Block b : this.blockchain) {
			s += b.toString() + "\n";
		}
		return s;
	}

	public synchronized void setBlock(Block remoteBlock) throws NoSuchAlgorithmException, UnsupportedEncodingException {
	    synchronized (System.out) {
	        System.out.println("\nReceived blockchain update");
	    }

	    this.blockchain.add(remoteBlock);
//	    System.out.println("Blockchain updated.");
	    validateChain(this.blockchain);
	}




	public ArrayList<ObjectOutputStream> getOOS() {
		// TODO Auto-generated method stub
		return this.oos;
	}

	public ArrayList<ObjectInputStream> getOIS() {
		// TODO Auto-generated method stub
		return this.ois;
	}
	
	public ServerSocket getSS() {
		// TODO Auto-generated method stub
		return this.ss;
	}
	
	public ArrayList<Block> getBlockchain() {
	    return blockchain;
	}

	public ArrayList<Socket> getConnections() {
	    return connectionHandler.getConnections();
	}

	public int getPort() {
		// TODO Auto-generated method stub
		return myPort;
	}
	

	public void terminateConnection(ObjectOutputStream oos, ObjectInputStream ois) {
    synchronized (this) {
        try {
            oos.close();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.oos.remove(oos);
        this.ois.remove(ois);
        System.out.println("Removed a disconnected node.");
    }
}

	
	
	

}
