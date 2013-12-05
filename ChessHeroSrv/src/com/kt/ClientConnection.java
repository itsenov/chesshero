package com.kt;

import com.sun.org.apache.bcel.internal.generic.GOTO;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: Toshko
 * Date: 11/9/13
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientConnection extends Thread
{
    public final static int READ_TIMEOUT = 15 * 1000; // In milliseconds

    private boolean running = true;
    private Socket sock = null;
    private Database db = null;

    private boolean hasAuthenticated = false;
    private int userID;

    ClientConnection(Socket sock)
    {
        this.sock = sock;
        db = new Database();
    }

    public int getUserID()
    {
        return userID;
    }

    private void closeSocket()
    {
        SLog.write("Closing socket...");

        if (sock.isClosed())
        {
            SLog.write("Socket already closed");
            return;
        }

        try
        {
            sock.close();
            SLog.write("Socket closed");
        }
        catch (IOException ignore)
        {
        }
    }

    public void run()
    {
        listen();

        closeSocket();

        if (db.getKeepAlive())
        {
            db.setKeepAlive(false);
        }
    }

    private void listen()
    {
        try
        {
            // Set timeout for the first message, if someone connected, he must say something
            sock.setSoTimeout(READ_TIMEOUT);

            while (running)
            {
                // Read header
                byte header[] = readBytesWithLength(2);
                short bodyLen = Utils.shortFromBytes(header, 0);

                SLog.write("Header read, body length is: " + bodyLen);

                if (0 == bodyLen)
                {   // An error has occurred during header reading or header is invalid, end the task
                    throw new ChessHeroException(Result.INVALID_MESSAGE);
                }

                // Set timeout for the body
                sock.setSoTimeout(READ_TIMEOUT);

                SLog.write("Reading body");

                // Read body
                byte body[] = readBytesWithLength(bodyLen);

                SLog.write("Body read");

                handleMessage(Message.fromData(body));

                // Remove timeout when listening for header
                sock.setSoTimeout(0);
            }
        }
        catch (SocketTimeoutException e)
        {
            SLog.write("Read timed out");
        }
        catch (EOFException e)
        {
            SLog.write("Client closed connection: " + e);
        }
        catch (IOException e)
        {
            SLog.write("Error reading: " + e);
        }
        catch (ChessHeroException e)
        {
            int code = e.getCode();
            SLog.write("Chess hero exception: " + code);
            writeMessage(new ResultMessage(code));
        }
        catch (Exception e)
        {
            SLog.write("Surprise exception: " + e);
        }
    }

    private byte[] readBytesWithLength(int len) throws IOException, EOFException
    {
        InputStream stream = sock.getInputStream();
        int bytesRead = 0;
        byte data[] = new byte[len];

        do
        {   // The docs are ambiguous as to whether this will definitely try to read len or can return less than len
            // so just in case iterating until len is read or shit happens
            bytesRead = stream.read(data, 0, len);
            if (-1 == bytesRead)
            {
                throw new EOFException();
            }
        }
        while (bytesRead != len);

        return data;
    }

    private void writeMessage(Message msg)
    {
        try
        {
            byte body[] = msg.toData();
            byte header[] = Utils.bytesFromShort((short)body.length);

            SLog.write("Writing message: " + msg + "...");

            sock.getOutputStream().write(header);
            sock.getOutputStream().write(body);

            SLog.write("Message written");
        }
        catch (IOException e)
        {
            SLog.write("Exception raised while writing to socket: " + e);
            running = false;
        }
    }

    private void handleMessage(Message msg) throws ChessHeroException
    {
        SLog.write("Received message: " + msg);

        short type = msg.getType();

        if (!hasAuthenticated && type != Message.TYPE_LOGIN && type != Message.TYPE_REGISTER)
        {
            SLog.write("Client attempting unauthorized action");
            throw new ChessHeroException(Result.AUTH_REQUIRED);
        }

        switch(type)
        {
            case Message.TYPE_REGISTER:
                handleRegister((AuthMessage)msg);
                break;

            case Message.TYPE_LOGIN:
                handleLogin((AuthMessage)msg);
                break;

            case Message.TYPE_CREATE_GAME:
                handleCreateGame((CreateGameMessage)msg);
                break;
        }
    }

    private void handleRegister(AuthMessage msg) throws ChessHeroException
    {
        try
        {
            Credentials credentials = msg.getCredentials();
            String name = credentials.getName();

            if (!Credentials.isNameValid(name))
            {
                writeMessage(new ResultMessage(Result.INVALID_NAME));
                return;
            }

            if (Credentials.isBadUser(name))
            {
                writeMessage(new ResultMessage(Result.BAD_USER));
                return;
            }

            if (!Credentials.isPassValid(credentials.getPass()))
            {
                writeMessage(new ResultMessage(Result.INVALID_PASS));
                return;
            }

            db.setKeepAlive(true);

            if (db.userExists(name))
            {
                writeMessage(new ResultMessage(Result.USER_EXISTS));
                return;
            }

            AuthPair auth = credentials.getAuthPair();

            db.insertUser(name, auth.getHash(), auth.getSalt());

            userID = db.getUserID(name);

            if (-1 == userID)
            {   // Could not fetch the user id
                throw new ChessHeroException(Result.INTERNAL_ERROR);
            }

            hasAuthenticated = true;
            writeMessage(new ResultMessage(Result.OK));
        }
        catch (SQLException e)
        {
            SLog.write("Registration failed: " + e);
            throw new ChessHeroException(Result.INTERNAL_ERROR);
        }
        catch (NoSuchAlgorithmException e)
        {
            SLog.write("Registration failed: " + e);
            throw new ChessHeroException(Result.INTERNAL_ERROR);
        }
        finally
        {
            db.setKeepAlive(false);
        }
    }

    private void handleLogin(AuthMessage msg) throws ChessHeroException
    {
        try
        {
            db.setKeepAlive(true);

            Credentials credentials = msg.getCredentials();

            AuthPair auth = db.getAuthPair(credentials.getName());

            if (null == auth || !auth.matches(credentials.getPass()))
            {
                writeMessage(new ResultMessage(Result.INVALID_CREDENTIALS));
                return;
            }

            userID = db.getUserID(credentials.getName());

            if (-1 == userID)
            {
                throw new ChessHeroException(Result.INTERNAL_ERROR);
            }

            hasAuthenticated = true;
            writeMessage(new ResultMessage(Result.OK));
        }
        catch (SQLException e)
        {
            SLog.write("Logging failed: " + e);
            throw new ChessHeroException(Result.INTERNAL_ERROR);
        }
        catch (NoSuchAlgorithmException e)
        {
            SLog.write("Logging failed: " + e);
            throw new ChessHeroException(Result.INTERNAL_ERROR);
        }
        finally
        {
            db.setKeepAlive(false);
        }
    }

    private void handleCreateGame(CreateGameMessage msg) throws ChessHeroException
    {
        try
        {
            String gameName = msg.getName();
            int gameID = db.insertGame(gameName, userID, Game.STATE_PENDING);

            if (-1 == gameID)
            {
                throw new ChessHeroException(Result.INTERNAL_ERROR);
            }

            Game game = new Game(gameID, gameName, this);
            Game.addGame(game);
            // TODO: figure out what this should return, too tired to do it now
            writeMessage(new ResultMessage(Result.OK));
        }
        catch (SQLException e)
        {
            throw new ChessHeroException(Result.INTERNAL_ERROR);
        }
        finally
        {
            db.setKeepAlive(false);
        }
    }
}
