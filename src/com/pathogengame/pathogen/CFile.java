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
    
    static String StripExtension(String s1)
    {
    	int dot = s1.lastIndexOf('.');
    	String s2;

    	if (dot != -1)
    		s2 = s1.substring(0, dot);
    	else
    		s2 = s1;
    	
    	return s2;
    }
    
    /*
	static int ReadInt(InputStream iS, int offset)
	{
		byte[] bucket = ReadBytes(iS, offset, 4);
		
		//System.out.println("bucket.length = " + bucket.length);
		//java.nio.ByteBuffer.wrap(bytes).getInt();
		return java.nio.ByteBuffer.wrap(bucket).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
	}*/
	
	static int ReadInt(byte bucket[], int offset)
	{
		//byte[] subbucket = SubBucket(bucket, offset, 4);
		//System.out.println("bucket.length = " + bucket.length);
		//java.nio.ByteBuffer.wrap(bytes).getInt();
		//return java.nio.ByteBuffer.wrap(subbucket).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();

        int firstByte = (0x000000FF & ((int)bucket[offset+3]));
        int secondByte = (0x000000FF & ((int)bucket[offset+2]));
        int thirdByte = (0x000000FF & ((int)bucket[offset+1]));
        int fourthByte = (0x000000FF & ((int)bucket[offset+0]));
        return ((int) (firstByte << 24
                | secondByte << 16
                | thirdByte << 8
                | fourthByte));
	}
	
	/*
	static float ReadFloat(InputStream iS, int offset)
	{
		byte[] bucket = ReadBytes(iS, offset, 4);
		return java.nio.ByteBuffer.wrap(bucket).order(java.nio.ByteOrder.LITTLE_ENDIAN).getFloat();
	}*/

	static float ReadFloat(byte bucket[], int offset)
	{
		//byte[] subbucket = SubBucket(bucket, offset, 4);
		//return java.nio.ByteBuffer.wrap(subbucket).order(java.nio.ByteOrder.LITTLE_ENDIAN).getFloat();

        int firstByte = (0x000000FF & ((int)bucket[offset+3]));
        int secondByte = (0x000000FF & ((int)bucket[offset+2]));
        int thirdByte = (0x000000FF & ((int)bucket[offset+1]));
        int fourthByte = (0x000000FF & ((int)bucket[offset+0]));
        return ((float) (firstByte << 24
                | secondByte << 16
                | thirdByte << 8
                | fourthByte));
	}
	
	/*
	static short ReadShort(InputStream iS, int offset)
	{
		byte[] bucket = ReadBytes(iS, offset, 2);
		return java.nio.ByteBuffer.wrap(bucket).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort();
	}*/
	
	static short ReadShort(byte bucket[], int offset)
	{
		//byte[] subbucket = SubBucket(bucket, offset, 2);
		//return java.nio.ByteBuffer.wrap(subbucket).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort();
        int firstByte = (0x000000FF & ((int)bucket[offset+1]));
        int secondByte = (0x000000FF & ((int)bucket[offset+0]));
        return (short) (firstByte << 8 | secondByte);
	}
	
	// http://darksleep.com/player/JavaAndUnsignedTypes.html
	/*
	static char ReadUShort(InputStream iS, int offset)
	{
		byte[] bucket = ReadBytes(iS, offset, 2);
        int firstByte = (0x000000FF & ((int)bucket[1]));
        int secondByte = (0x000000FF & ((int)bucket[0]));
        return (char) (firstByte << 8 | secondByte);
	}*/
	
	static char ReadUShort(byte bucket[], int offset)
	{
		//byte[] subbucket = SubBucket(bucket, offset, 2);
        int firstByte = (0x000000FF & ((int)bucket[offset+1]));
        int secondByte = (0x000000FF & ((int)bucket[offset+0]));
        return (char) (firstByte << 8 | secondByte);
	}
	
	/*
	static short ReadUByte(InputStream iS, int offset)
	{
		byte[] bucket = ReadBytes(iS, offset, 1);
		int firstByte = (0x000000FF & ((int)bucket[0]));
		return (short)firstByte;
	}*/
	
	static short ReadUByte(byte bucket[], int offset)
	{
		//byte[] subbucket = SubBucket(bucket, offset, 1);
		int firstByte = (0x000000FF & ((int)bucket[offset+0]));
		return (short)firstByte;
	}
	
	/*
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
	}*/

	static long ReadUInt(byte bucket[], int offset)
	{
		//byte[] subbucket = SubBucket(bucket, offset, 4);
        int firstByte = (0x000000FF & ((int)bucket[offset+3]));
        int secondByte = (0x000000FF & ((int)bucket[offset+2]));
        int thirdByte = (0x000000FF & ((int)bucket[offset+1]));
        int fourthByte = (0x000000FF & ((int)bucket[offset+0]));
        return ((long) (firstByte << 24
                | secondByte << 16
                | thirdByte << 8
                | fourthByte))
               & 0xFFFFFFFFL;
	}
	
	static byte[] SubBucket(byte bucket[], int offset, int readlen)
	{
		byte[] subbucket = new byte[readlen];

		for(int i=0; i<readlen; i++)
			subbucket[i] = bucket[offset+i];

		return subbucket;
	}
	
	/*
	static byte[] ReadBytes(InputStream iS, int offset, int readlen)
	{
        byte bucket[] = new byte[offset+readlen];
        
        try 
        {
			//int bytesRead = 
        	iS.read(bucket, offset, readlen);
		} 
        catch (IOException e) 
        {
			//e.printStackTrace();
        	System.out.println("Failed to read");
		}
        
        byte subbucket[] = new byte[readlen];
        
        for(int i=0; i<readlen; i++)
        	subbucket[i] = bucket[offset+i];
        
        return subbucket;
	}*/
	
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
            //e.printStackTrace();
        	System.out.println("Failed to open file " + filepath);
            iS = null;
        }
        
        return iS;
	}
	
	static byte[] ReadWhole(InputStream iS)
	{
		byte[] buffer = new byte[25600];
		byte[] totalbuf = new byte[1];
		
		int rsz;
		int totalsz = 0;
		int i;
		
		byte[] newbuf;
		
		try 
        {
			do
			{
				rsz = iS.read(buffer, 0, buffer.length);
				
				if(rsz > 0)
				{
					newbuf = new byte[totalsz + rsz];
					
					for(i=0; i<totalsz; i++)
						newbuf[i] = totalbuf[i];
					
					for(i=totalsz; i<totalsz+rsz; i++)
						newbuf[i] = buffer[i-totalsz];
					
					totalsz += rsz;
					totalbuf = newbuf;
				}
			}while (rsz > 0);
        }
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		buffer = null;
		newbuf = null;
		//System.gc();
		return totalbuf;
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
