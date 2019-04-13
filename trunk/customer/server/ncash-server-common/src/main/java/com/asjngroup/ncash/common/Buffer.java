package com.asjngroup.ncash.common;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.InvalidMarkException;

public abstract class Buffer
{
	protected long mark = -1L;
	protected long position = 0L;
	protected long limit;
	protected long capacity;

	public Buffer( long mark, long pos, long lim, long cap )
	{
		if ( cap < 0L )
			throw new IllegalArgumentException();
		this.capacity = cap;
		limit( lim );
		position( pos );
		if ( mark > 0L )
		{
			if ( mark > pos )
				throw new IllegalArgumentException();
			this.mark = mark;
		}
	}

	public final long capacity()
	{
		return this.capacity;
	}

	public final long position()
	{
		return this.position;
	}

	public final Buffer position( long newPosition )
	{
		if ( ( newPosition > this.limit ) || ( newPosition < 0L ) )
			throw new IllegalArgumentException();
		this.position = newPosition;
		if ( this.mark > this.position )
			this.mark = -1L;
		return this;
	}

	public final long limit()
	{
		return this.limit;
	}

	public final Buffer limit( long newLimit )
	{
		if ( ( newLimit > this.capacity ) || ( newLimit < 0L ) )
			throw new IllegalArgumentException();
		this.limit = newLimit;
		if ( this.position > this.limit )
			this.position = this.limit;
		if ( this.mark > this.limit )
			this.mark = -1L;
		return this;
	}

	public final Buffer mark()
	{
		this.mark = this.position;
		return this;
	}

	public final Buffer reset()
	{
		long m = this.mark;
		if ( m < 0L )
			throw new InvalidMarkException();
		this.position = m;
		return this;
	}

	public final Buffer clear()
	{
		this.position = 0L;
		this.limit = this.capacity;
		this.mark = -1L;
		return this;
	}

	public final Buffer flip()
	{
		this.limit = this.position;
		this.position = 0L;
		this.mark = -1L;
		return this;
	}

	public final Buffer rewind()
	{
		this.position = 0L;
		this.mark = -1L;
		return this;
	}

	public final long remaining()
	{
		return ( this.limit - this.position );
	}

	public final boolean hasRemaining()
	{
		return ( this.position < this.limit );
	}

	public abstract boolean isReadOnly();

	final long nextGetIndex()
	{
		if ( this.position >= this.limit )
			throw new BufferUnderflowException();
		return ( this.position++ );
	}

	final long nextGetIndex( long nb )
	{
		if ( this.limit - this.position < nb )
			throw new BufferUnderflowException();
		long p = this.position;
		this.position += nb;
		return p;
	}

	final long nextPutIndex()
	{
		if ( this.position >= this.limit )
			throw new BufferOverflowException();
		return ( this.position++ );
	}

	final long nextPutIndex( long nb )
	{
		if ( this.limit - this.position < nb )
			throw new BufferOverflowException();
		long p = this.position;
		this.position += nb;
		return p;
	}

	final long checkIndex( long i )
	{
		if ( ( i < 0L ) || ( i >= this.limit ) )
			throw new IndexOutOfBoundsException();
		return i;
	}

	final long checkIndex( long i, long nb )
	{
		if ( ( i < 0L ) || ( nb > this.limit - i ) )
			throw new IndexOutOfBoundsException();
		return i;
	}

	final long markValue()
	{
		return this.mark;
	}

	static void checkBounds( long off, long len, long size )
	{
		if ( ( off | len | off + len | size - ( off + len ) ) < 0L )
			throw new IndexOutOfBoundsException();
	}
}