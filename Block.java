package BlockChain;
import java.security.MessageDigest;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.List;
import java.util.Scanner;


public class Block implements Serializable {
	
	private String previousHash;
	private String hash;
	//instance variables
	private long timeStamp;
	private int nonce;
	private String data;
	//port number
	private int port = 7000;
	
	public Block() {
		this.data = "GenesisBlock";
		this.timeStamp = new Date().getTime();
		this.nonce = 0;
		this.previousHash = "0";
		this.hash = "0";
	}
	
	public Block(String blockInfo) {
		this.data = blockInfo;
		this.timeStamp = new Date().getTime();
		this.nonce = 0;
		this.previousHash = "0";
		this.hash = "0";
		
	}
	
	public String calculateBlockHash() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		
			//combine all the instance variables in the Block into one large String
			String instanceVarData = "" + this.timeStamp + this.nonce + this.data + this.previousHash;
			//make a new message digest object that implements the hash function we will be using
			MessageDigest myDigest = MessageDigest.getInstance("SHA-256");
			//take the data and hash it
			byte[] hashBytes = myDigest.digest(instanceVarData.getBytes("UTF-8"));
			//convert the hash into a string
			StringBuffer buffer = new StringBuffer();
			for (byte b: hashBytes) {
			      buffer.append(String.format("%02x", b));
			}
			String theHash = buffer.toString();
			
			return theHash;
		
	}
	
	public void mineBlock(int difficulty) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String target = new String(new char[difficulty]).replace('\0', '0');
		while (!calculateBlockHash().substring(0, difficulty).equals(target)) {
			nonce++;
		}
		hash = calculateBlockHash();
	}
	
	//getHash
	public String getHash() {
		return this.hash;
	}
	
	//getPreviousHash
	public String getPreviousHash() {
		return this.previousHash;
	}
	
	
	public String toString() {
		return "Block: " + this.data + " " + this.timeStamp + " " + this.nonce;
	}

	public void setPreviousHash(String hash2) {
		// TODO Auto-generated method stub
		this.previousHash = hash2;
	}
	
	

}
