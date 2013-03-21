package com.pathogengame.pathogen;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import android.content.res.AssetManager;

public class CFile 
{
    static String StripPathExtension(String s0)
    {
    	int sep0 = s0.lastIndexOf('\\');
    	int sep1 = s0.lastIndexOf('/');
    	int sep = Math.max(sep0, sep1);
    	String s1;

        if (sep != -1)
            s1 = s0.substring(sep + 1);
    	else
    		s1 = s0;

    	int dot = s1.lastIndexOf('.');
    	String s2;

    	if (dot != -1)
    		s2 = s1.substring(0, dot);
    	else
    		s2 = s1;
    	
    	return s2;
    }
    
	static int ReadInt(InputStream iS, int offset)
	{
		byte[] bucket = ReadBytes(iS, offset, 4);
		//java.nio.ByteBuffer.wrap(bytes).getInt();
		return java.nio.ByteBuffer.wrap(bucket).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
	}
	
	static float ReadFloat(InputStream iS, int offset)
	{
		byte[] bucket = ReadBytes(iS, offset, 4);
		return java.nio.ByteBuffer.wrap(bucket).order(java.nio.ByteOrder.LITTLE_ENDIAN).getFloat();
	}
	
	static short ReadShort(InputStream iS, int offset)
	{
		byte[] bucket = ReadBytes(iS, offset, 2);
		return java.nio.ByteBuffer.wrap(bucket).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort();
	}
	
	// http://darksleep.com/player/JavaAndUnsignedTypes.html
	static char ReadUShort(InputStream iS, int offset)
	{
		byte[] bucket = ReadBytes(iS, offset, 2);
        int firstByte = (0x000000FF & ((int)bucket[1]));
        int secondByte = (0x000000FF & ((int)bucket[0]));
        return (char) (firstByte << 8 | secondByte);
	}
	
	static short ReadUByte(InputStream iS, int offset)
	{
		byte[] bucket = ReadBytes(iS, offset, 1);
		int firstByte = (0x000000FF & ((int)bucket[0]));
		return (short)firstByte;
	}
	
	static long ReadUInt(InputStream iS, int offset)
	{
		byte[] bucket = ReadBytes(iS, offset, 4);
        int firstByte = (0x000000FF & ((int)bucket[3]));
        int secondByte = (0x000000FF & ((int)bucket[2]));
        int thirdByte = (0x000000FF & ((int)bucket[1]));
        int fourthByte = (0x000000FF & ((int)bucket[0]));
        return ((long) (firstByte << 24
                | secondByte << 16
                | thirdByte << 8
                | fourthByte))
               & 0xFFFFFFFFL;
	}
	
	static byte[] ReadBytes(InputStream iS, int offset, int readlen)
	{
        byte[] bucket = new byte[readlen];
        
        try 
        {
			int bytesRead = iS.read(bucket, offset, readlen);
		} 
        catch (IOException e) 
        {
			e.printStackTrace();
		}
        
        return bucket;
	}
	
	static InputStream GetInput(String filepath, MainActivity act)
	{
		InputStream iS = null;

    	AssetManager aM = act.getAssets();
    	
        try 
        {
        	iS = aM.open(filepath);
        } 
        catch (final IOException e) 
        {
            e.printStackTrace();
            iS = null;
        }
        
        return iS;
	}
	
	static String ReadText(String filepath, MainActivity act)
    {
    	InputStream iS = GetInput(filepath, act);
        
        final char[] buffer = new char[256];
        final StringBuilder out = new StringBuilder();
        try 
        {
        	final Reader in = new InputStreamReader(iS, "UTF-8");
        	try 
        	{
        		for (;;) 
        		{
        			int rsz = in.read(buffer, 0, buffer.length);
        			if (rsz < 0)
        				break;
        			out.append(buffer, 0, rsz);
        		}
          }
          finally
          {
        	  in.close();
          }
        }
        catch (UnsupportedEncodingException ex) 
        {
        }
        catch (IOException ex) 
        {
        }
        
        try 
        {
        	iS.close();
        } 
        catch (final IOException e) 
        {
            e.printStackTrace();
        }

        return out.toString();
    }
}
