package com.asjngroup.ncash.common;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public abstract class NCashByteBuffer extends Buffer implements Comparable<NCashByteBuffer>
{
	boolean isReadOnly;

	public NCashByteBuffer( long mark, long pos, long lim, long cap )
	{
		super( mark, pos, lim, cap );
	}

	public abstract Buffer slice();

	public abstract Buffer duplicate();

	public abstract byte get();

	public abstract Buffer put( byte paramByte );

	public abstract byte get( long paramLong );

	public abstract Buffer put( long paramLong, byte paramByte );

	public NCashByteBuffer get( byte[] dst, int offset, int length )
	{
		checkBounds( offset, length, dst.length );
		if ( length > remaining() )
			throw new BufferUnderflowException();
		int end = offset + length;
		for ( int i = offset; i < end; ++i )
			dst[i] = get();
		return this;
	}

	public NCashByteBuffer get( byte[] dst )
	{
		return get( dst, 0, dst.length );
	}

	public NCashByteBuffer put( NCashByteBuffer src )
	{
		if ( src == this )
			throw new IllegalArgumentException();
		long n = src.remaining();
		if ( n > remaining() )
			throw new BufferOverflowException();
		for ( long i = 0L; i < n; i += 1L )
			put( src.get() );
		return this;
	}

	public NCashByteBuffer put( byte[] src, int offset, int length )
	{
		checkBounds( offset, length, src.length );
		if ( length > remaining() )
			throw new BufferOverflowException();
		int end = offset + length;
		for ( int i = offset; i < end; ++i )
			put( src[i] );
		return this;
	}

	public final NCashByteBuffer put( byte[] src )
	{
		return put( src, 0, src.length );
	}

	public abstract NCashByteBuffer compact();

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( super.getClass().getName() );
		sb.append( "[pos=" );
		sb.append( position() );
		sb.append( " lim=" );
		sb.append( limit() );
		sb.append( " cap=" );
		sb.append( capacity() );
		sb.append( "]" );
		return sb.toString();
	}

	public int hashCode()
	{
		int h = 1;
		long p = position();
		for ( long i = limit() - 1L; i >= p; i -= 1L )
			h = 31 * h + get( i );
		return h;
	}

	public boolean equals( Object ob )
	{
		if ( !( ob instanceof NCashByteBuffer ) )
			return false;
		NCashByteBuffer that = ( NCashByteBuffer ) ob;
		if ( remaining() != that.remaining() )
			return false;
		long p = position();
		long i = limit() - 1L;
		for ( long j = that.limit() - 1L; i >= p; j -= 1L )
		{
			byte v1 = get( i );
			byte v2 = that.get( j );
			if ( v1 != v2 )
				if ( ( v1 == v1 ) || ( v2 == v2 ) )
				{
					return false;
				}
			i -= 1L;
		}

		return true;
	}

	public int compareTo( NCashByteBuffer that )
	{
		long n = position() + Math.min( remaining(), that.remaining() );
		long i = position();
		for ( long j = that.position(); i < n; j += 1L )
		{
			byte v1 = get( i );
			byte v2 = that.get( j );
			if ( v1 != v2 )
			{
				if ( ( v1 == v1 ) || ( v2 == v2 ) )
				{
					if ( v1 < v2 )
						return -1;
					return 1;
				}
			}
			i += 1L;
		}

		if ( remaining() > that.remaining() )
		{
			return 1;
		}
		if ( remaining() < that.remaining() )
		{
			return -1;
		}
		return 0;
	}

	public abstract ByteBuffer toByteBuffer( int paramInt );
}