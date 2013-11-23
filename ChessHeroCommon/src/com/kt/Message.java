package com.kt;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Toshko
 * Date: 11/12/13
 * Time: 1:07 AM
 * To change this template use File | Settings | File Templates.
 */

abstract public class Message
{
    public static final short ACTION_REGISTER = 1;
    public static final short ACTION_LOGIN = 2;
    public static final short ACTION_MOVE = 3;

    public static boolean isActionValid(int action)
    {
        return (action == ACTION_REGISTER || action == ACTION_LOGIN || action == ACTION_MOVE);
    }

    public static Message fromData(byte data[]) throws ChessHeroException
    {
        ByteBuffer buf = ByteBuffer.allocate(data.length);
        buf.put(data);
        buf.rewind();

        try
        {
            // Read action number
            short action = buf.getShort();
            SLog.write("Message action: " + action);

            switch (action)
            {
                case ACTION_REGISTER:
                case ACTION_LOGIN:
                    Credentials credentials = readCredentials(buf);
                    return new AuthMessage(action, credentials);

                case ACTION_MOVE:

                    break;

                default:
                    throw new ChessHeroException(Result.INVALID_ACTION);
            }

            return null;
        }
        catch (BufferUnderflowException e)
        {
            throw new ChessHeroException(Result.INVALID_MESSAGE);
        }
        catch (ChessHeroException e)
        {
            throw e;
        }
    }

    private static Credentials readCredentials(ByteBuffer buf) throws ChessHeroException
    {
        try
        {
            short nameLen = buf.getShort();
            if (nameLen < Credentials.MIN_NAME_LENGTH || nameLen > Credentials.MAX_NAME_LENGTH)
            {
                throw new ChessHeroException(Result.INVALID_NAME);
            }

            // Read name bytes
            byte nameData[] = new byte[nameLen];
            buf.get(nameData, 0, nameLen);

            short passLen = buf.getShort();
            if (passLen < Credentials.MIN_PASS_LENGTH || passLen > Credentials.MAX_PASS_LENGTH)
            {
                throw new ChessHeroException(Result.INVALID_PASS);
            }

            // Read pass bytes
            byte passData[] = new byte[passLen];
            buf.get(passData, 0, passLen);

            return new Credentials(new String(nameData), new String(passData));
        }
        catch (BufferUnderflowException e)
        {
            throw new ChessHeroException(Result.INVALID_MESSAGE);
        }
    }

    abstract public byte[] toData();
}