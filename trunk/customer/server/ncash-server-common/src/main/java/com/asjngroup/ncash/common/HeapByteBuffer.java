package com.asjngroup.ncash.common;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class HeapByteBuffer extends NCashByteBuffer
{
	protected byte[] hb;
	protected int offset;

	public HeapByteBuffer( int cap )
	{
		super( -1L, 0L, cap, cap );

		this.hb = new byte[cap];
		this.offset = 0;
	}

	public HeapByteBuffer( byte[] buf, int off, int len )
	{
		super( -1L, off, off + len, buf.length );
		this.hb = buf;
		this.offset = 0;
	}

	public HeapByteBuffer( byte[] buf )
	{
		this( buf, 0, buf.length );
	}

	public HeapByteBuffer( byte[] buf, int mark, int pos, int lim, int cap, int off )
	{
		super( mark, pos, lim, cap );
		this.hb = buf;
		this.offset = off;
	}

	public NCashByteBuffer slice()
	{
		return new HeapByteBuffer( this.hb, -1, 0, ( int ) remaining(), ( int ) remaining(), ( int ) position() + this.offset );
	}

	public NCashByteBuffer duplicate()
	{
		return new HeapByteBuffer( this.hb, ( int ) markValue(), ( int ) position(), ( int ) limit(), ( int ) capacity(), this.offset );
	}

	public NCashByteBuffer asReadOnlyBuffer()
	{
		throw new UnsupportedOperationException();
	}

	protected int ix( int i )
	{
		return ( i + this.offset );
	}

	public byte get()
	{
		return this.hb[ix( ( int ) nextGetIndex() )];
	}

	public byte get( long i )
	{
		return this.hb[ix( ( int ) checkIndex( i ) )];
	}

	public NCashByteBuffer get( byte[] dst, int offSet, int length )
	{
		checkBounds( offSet, length, dst.length );
		if ( length > remaining() )
			throw new BufferUnderflowException();
		System.arraycopy( this.hb, ix( ( int ) position() ), dst, offSet, length );
		position( position() + length );
		return this;
	}

	public boolean isReadOnly()
	{
		return false;
	}

	public NCashByteBuffer put( byte x )
	{
		this.hb[ix( ( int ) nextPutIndex() )] = x;
		return this;
	}

	public NCashByteBuffer put( long i, byte x )
	{
		this.hb[ix( ( int ) checkIndex( i ) )] = x;
		return this;
	}

	public NCashByteBuffer put( byte[] src, int offSet, int length )
	{
		checkBounds( offSet, length, src.length );
		if ( length > remaining() )
			throw new BufferOverflowException();
		System.arraycopy( src, offSet, this.hb, ix( ( int ) position() ), length );
		position( position() + length );
		return this;
	}

	public NCashByteBuffer put( NCashByteBuffer src )
	{
		if ( src instanceof HeapByteBuffer )
		{
			if ( src == this )
				throw new IllegalArgumentException();
			HeapByteBuffer sb = ( HeapByteBuffer ) src;
			int n = ( int ) sb.remaining();
			if ( n > remaining() )
				throw new BufferOverflowException();
			System.arraycopy( sb.hb, sb.ix( ( int ) sb.position() ), this.hb, ix( ( int ) position() ), n );

			sb.position( sb.position() + n );
			position( position() + n );
		}
		else
		{
			super.put( src );
		}
		return this;
	}

	public NCashByteBuffer compact()
	{
		System.arraycopy( this.hb, ix( ( int ) position() ), this.hb, ix( 0 ), ( int ) remaining() );
		position( remaining() );
		limit( capacity() );
		return this;
	}

	public ByteBuffer toByteBuffer( int length )
	{
		return ByteBuffer.wrap( this.hb, ( int ) position(), length );
	}
}