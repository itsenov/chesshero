package com.kt.utils;

import com.kt.Config;

import java.util.logging.*;

/**
 * Created with IntelliJ IDEA.
 * User: Toshko
 * Date: 11/7/13
 * Time: 12:04 AM
 * To change this template use File | Settings | File Templates.
 */

public class SLog
{
    private static Logger logger = Logger.getLogger("SLog");
    static {
        logger.setLevel((Config.DEBUG ? Level.ALL : Level.OFF));

        Handler handlers[] = logger.getParent().getHandlers();
        for (Handler handler : handlers)
        {
            handler.setFormatter(new _SLogFormatter());
        }
    }

    public static void write(String str)
    {
        logger.info(str);
    }

    public static void write(Object obj)
    {
        logger.info(obj.toString());
    }

    public static void write(boolean b)
    {
        logger.info("" + b);
    }

    public static void write(int i)
    {
        logger.info("" + i);
    }

    public static void write(float f)
    {
        logger.info("" + f);
    }

    public static void write(double d)
    {
        logger.info("" + d);
    }

    public static void write(long l)
    {
        logger.info("" + l);
    }

    public static void write(char c)
    {
        logger.info("" + c);
    }

    public static void write(char c[])
    {
        String str = new String(c);
        logger.info(str);
    }

    // Private formatter to remove the default timestamp before the log message
    private static class _SLogFormatter extends SimpleFormatter
    {
        @Override
        public String format(LogRecord record)
        {
            return record.getMessage() + "\r\n";
        }
    }
}
