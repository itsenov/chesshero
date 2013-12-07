/*** ToDo:
*
* The newly connected user sends an unique token
* 1. Get the the uid and the gid from the DB by the given token
* 2. Assign the uid and the gid to the current player
* 3. Check if the uid already exists, if it exists, reject the player and close the connection 
* 4. Check the in game status of the player with that gid - get isPaired();
* 	If they are already paried, reject the current player and close the connection
* 	If the gid already exists one time, pair the two players
*	If it doesn't exist, wait for another player with the same gid
*
* ***/

package com.chessherochatsrv;

import java.net.*;
import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.*;

public class ClientHandler extends Thread {
    private static String DB_URL = "jdbc:mysql://localhost:3306/";
    private static String DB_NAME = "srv_db";
    private static String DB_USER = "root";
    private static String DB_PASS = "";
    
	protected Socket socket;
	protected Vector threadPool;

    private Connection conn = null;

    private boolean keepAlive = false;
    private boolean isOpen = false;
    
	public ClientHandler(Socket socket, Vector threadPool, String token) {
		this.socket = socket;
		this.threadPool = threadPool;
		threadPool.add(this);
	}

    private void connectToDB() throws SQLException
    {
        if (isOpen)
        {
            return;
        }

        String connector = "com.mysql.jdbc.Driver";

        try
        {
            Class.forName(connector).newInstance();
            conn = DriverManager.getConnection(DB_URL + DB_NAME, DB_USER, DB_PASS);
            isOpen = true;
        }
        catch (IllegalAccessException e)
        {
            throw new SQLException("Connect raised illegal access exception");
        }
        catch (InstantiationException e)
        {
            throw new SQLException("Connect raised instantiation exception");
        }
        catch (ClassNotFoundException e)
        {
            throw new SQLException("Connect raised class not found exception");
        }
    }
    
    private void disconnectFromDB()
    {
        if (null == conn)
        {
            return;
        }

        try
        {
            conn.close();
            isOpen = false;
        }
        catch (SQLException ignore)
        {
        }
        finally
        {
            conn = null;
        }
    }
    
	public void run() {
		
		try {
			connectToDB();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		String outString;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			PrintWriter out = new PrintWriter(new OutputStreamWriter(
					socket.getOutputStream()));

			out.println("Hello!\n Enter 'q' to exit.");
			out.flush();

			while (true) {
				String inString = in.readLine();
				System.out.println("Client said: " + inString);

				if (inString == null) {
					break;
				} else {
					outString = "hey";
					out.println(outString);
					out.flush();

					if (inString.trim().equals("q")) {
						break;
					}
				}
			}
			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			threadPool.remove(this);
		}
	}
}