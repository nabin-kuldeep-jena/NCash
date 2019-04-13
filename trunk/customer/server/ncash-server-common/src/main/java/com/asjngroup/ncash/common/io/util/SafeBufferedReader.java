package com.asjngroup.ncash.common.io.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class SafeBufferedReader extends BufferedReader
{
	private Object safeLockObj = new Object();

	public SafeBufferedReader( Reader paramReader, int paramInt )
	{
		super( paramReader, paramInt );
	}

	public SafeBufferedReader( Reader paramReader )
	{
		super( paramReader );
	}

	public String readLine() throws IOException
	{
		synchronized (this.safeLockObj)
		{
			return super.readLine();
		}
	}

	public int read() throws IOException
	{
		synchronized (this.safeLockObj)
		{
			return super.read();
		}
	}

	public int read( char[] paramArrayOfChar, int paramInt1, int paramInt2 ) throws IOException
	{
		synchronized (this.safeLockObj)
		{
			return super.read( paramArrayOfChar, paramInt1, paramInt2 );
		}
	}

	public long skip( long paramLong ) throws IOException
	{
		synchronized (this.safeLockObj)
		{
			return super.skip( paramLong );
		}
	}

	public boolean ready() throws IOException
	{
		synchronized (this.safeLockObj)
		{
			return super.ready();
		}
	}

	public void mark( int paramInt ) throws IOException
	{
		synchronized (this.safeLockObj)
		{
			super.mark( paramInt );
		}
	}

	public void reset() throws IOException
	{
		synchronized (this.safeLockObj)
		{
			super.reset();
		}
	}
}
