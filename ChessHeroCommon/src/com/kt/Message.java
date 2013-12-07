package com.kt;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Toshko
 * Date: 11/12/13
 * Time: 1:07 AM
 * To change this template use File | Settings | File Templates.
 */

abstract public class Message
{
    public static final byte TYPE_REGISTER = 1;
    public static final byte TYPE_LOGIN = 2;
    public static final byte TYPE_RESULT = 3;
    public static final byte TYPE_MAP = 4;
    public static final byte TYPE_CREATE_GAME = 5;
    public static final byte TYPE_CANCEL_GAME = 6;

    public static final byte FLAG_PUSH = 1 << 0;
    public static final byte FLAG_INNERMSG = 1 << 1;

    protected byte type;
    protected byte flags = 0;
    public Message innerMessage = null;

    protected Message(byte type, byte flags)
    {
        this.type = type;
        this.flags = flags;
    }

    public byte getType()
    {
        return type;
    }

    public byte getFlags()
    {
        return flags;
    }

    public static Message fromData(byte data[]) throws ChessHeroException
    {
        SLog.write("Parsing message");

        ByteBuffer buf = ByteBuffer.allocate(data.length);
        buf.put(data);
        buf.rewind();

        return parse(buf);
    }

    private static Message parse(ByteBuffer buf) throws ChessHeroException
    {
        try
        {
            // Read type and flags
            byte type = buf.get();
            byte flags = buf.get();
            SLog.write("Message type: " + type);

            Message msg = null;

            switch (type)
            {
                case TYPE_REGISTER:
                case TYPE_LOGIN:
                    Credentials credentials = readCredentials(buf);
                    msg = new AuthMessage(type, flags, credentials);

                    break;

                case TYPE_RESULT:
                    int result = buf.getInt();
                    msg = new ResultMessage(result, flags);

                    break;

                case TYPE_MAP:
                    HashMap<String, Object> map = readMap(buf);
                    msg = new MapMessage(map, flags);

                    break;

                case TYPE_CREATE_GAME:
                    short nameLen = buf.getShort();
                    byte nameData[] = new byte[nameLen];
                    buf.get(nameData, 0, nameLen);
                    msg = new CreateGameMessage(new String(nameData), flags);

                    break;

                default:
                    throw new ChessHeroException(Result.INVALID_TYPE);
            }

            if ((flags & FLAG_INNERMSG) != 0)
            {
                msg.innerMessage = parse(buf);
            }

            return msg;
        }
        catch (BufferUnderflowException e)
        {
            SLog.write(e);
            throw new ChessHeroException(Result.INVALID_MESSAGE);
        }
    }

    private static Credentials readCredentials(ByteBuffer buf) throws ChessHeroException
    {
        SLog.write("Parsing credentials");

        try
        {
            short nameLen = buf.getShort();

            // Read name bytes
            byte nameData[] = new byte[nameLen];
            buf.get(nameData, 0, nameLen);

            short passLen = buf.getShort();

            // Read pass bytes
            byte passData[] = new byte[passLen];
            buf.get(passData, 0, passLen);

            return new Credentials(new String(nameData), new String(passData));
        }
        catch (BufferUnderflowException e)
        {
            SLog.write(e);
            throw new ChessHeroException(Result.INVALID_MESSAGE);
        }
    }

    private static HashMap<String, Object> readMap(ByteBuffer buf) throws ChessHeroException
    {
        SLog.write("Parsing map");

        try
        {
            HashMap<String, Object> map = new HashMap<String, Object>();
            byte type;

            while (true)
            {
                // Read key type
                type = buf.get();

                if (MapMessage.MAP_END == type)
                {
                    break;
                }

                if (type != MapMessage.VAL_TYPE_STR)
                {   // Key type must be string
                    throw new ChessHeroException(Result.INVALID_MESSAGE);
                }

                short keyLen = (short)buf.get();
                byte keyData[] = new byte[keyLen];
                buf.get(keyData, 0, keyLen);

                String key = new String(keyData);

                // Read value type
                type = buf.get();

                if (MapMessage.VAL_TYPE_INT == type)
                {
                    int val = buf.getInt();
                    map.put(key, val);
                }
                else if (MapMessage.VAL_TYPE_STR == type)
                {
                    short valLen = (short)buf.get();
                    byte valData[] = new byte[valLen];
                    buf.get(valData, 0, valLen);

                    map.put(key, new String(valData));
                }
            }

            return map;
        }
        catch (BufferUnderflowException e)
        {
            SLog.write(e);
            throw new ChessHeroException(Result.INVALID_MESSAGE);
        }
    }

    public byte[] toData()
    {
        int len = 0;
        ArrayList<byte[]> messages = new ArrayList<byte[]>();
        Message msg = this;

        do
        {
            byte data[] = msg.serialized();
            len += data.length;
            messages.add(data);

            if (0 == (msg.flags & FLAG_INNERMSG) || null == msg.innerMessage)
            {
                break;
            }

            msg = msg.innerMessage;
        }
        while (true);

        if (1 == messages.size())
        {   // No need to create a copy here
            return messages.get(0);
        }

        byte all[] = new byte[len];
        int index = 0;

        for (byte chunk[] : messages)
        {
            Utils.bytesPutBytes(all, chunk, index);
            index += chunk.length;
        }

        return all;
    }

    protected abstract byte[] serialized();

    @Override
    public String toString()
    {
        return "<Message :: type: " + type + ", flags: " + flags + ">";
    }
}
