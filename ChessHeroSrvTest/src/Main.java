import com.kt.*;
import com.kt.api.Action;
import com.kt.chesco.CHESCOReader;
import com.kt.chesco.CHESCOWriter;
import com.kt.utils.SLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Toshko on 12/7/13.
 */
public class Main
{
    static Socket sock;

    private static CHESCOReader reader;
    private static CHESCOWriter writer;

    private static final boolean interactive = true;

    public static void main(String []args)
    {
        try
        {
            sock = new Socket(InetAddress.getLocalHost(), 4848);

            reader = new CHESCOReader(sock.getInputStream());
            writer = new CHESCOWriter(sock.getOutputStream());

            if (interactive)
            {
                startInteractive();
            }
            else
            {
                startStatic();
            }
        }
        catch (Throwable e)
        {
            System.out.println("Exception: " + e);
        }
    }

    public static void startInteractive() throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true)
        {
            SLog.write("enter command: ");
            String input = br.readLine();

            if (null == input)
            {
                SLog.write("End of file stream");
                break;
            }

            input = input.toLowerCase();
            String args[] = input.split(" ");

            if (args[0].equals("exit"))
            {
                break;
            }
            else if (args[0].equals("login"))
            {
                login(args[1], args[2]);
            }
            else if (args[0].equals("register"))
            {
                reg(args[1], args[2]);
            }
            else if (args[0].equals("creategame"))
            {
                createGame(args[1]);
            }
            else if (args[0].equals("cancelgame"))
            {
                cancelGame(Integer.parseInt(args[1]));
            }
            else
            {
                SLog.write("Unrecognized command");
            }
        }
    }

    public static void startStatic() throws IOException
    {

    }

    public static void login(String name, String pass) throws IOException
    {
        HashMap req = new HashMap();
        req.put("action", Action.LOGIN);
        req.put("username", name);
        req.put("password", pass);
        writer.write(req);

        SLog.write(reader.read());
    }

    public static void reg(String name, String pass) throws IOException
    {
        HashMap req = new HashMap();
        req.put("action", Action.REGISTER);
        req.put("username", name);
        req.put("password", pass);
        writer.write(req);

        SLog.write(reader.read());
    }

    public static void createGame(String name) throws IOException
    {
        HashMap req = new HashMap();
        req.put("action", Action.CREATE_GAME);
        req.put("gamename", name);
        writer.write(req);

        SLog.write(reader.read());
    }

    public static void cancelGame(int gameid) throws IOException
    {
        HashMap req = new HashMap();
        req.put("action", Action.CANCEL_GAME);
        req.put("gameid", gameid);
        writer.write(req);

        SLog.write(reader.read());
    }
}
