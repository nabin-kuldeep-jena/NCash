package com.asjngroup.ncash.common.buffer;

import java.nio.InvalidMarkException;
import java.nio.BufferUnderflowException;
import java.nio.BufferOverflowException;

public abstract class BufferL
{
    // Invariants: mark <= position <= limit <= capacity
    protected long mark = -1;
    protected long position = 0;
    protected long limit;
    protected long capacity;

    // Creates a new buffer with the given mark, position, limit, and capacity,
    // after checking invariants.
    //
    public BufferL( long mark, long pos, long lim, long cap )
    {
        if ( cap < 0 )
            throw new IllegalArgumentException();
        this.capacity = cap;
        limit( lim );
        position( pos );
        if ( mark > 0 )
        {
            if ( mark > pos )
                throw new IllegalArgumentException();
            this.mark = mark;
        }
    }

    /**
     * Returns this buffer's capacity. </p>
     *
     * @return  The capacity of this buffer
     */
    public final long capacity()
    {
        return capacity;
    }

    public final void capacity( long newCapacity )
    {
        capacity = newCapacity;
    }

    /**
     * Returns this buffer's position. </p>
     *
     * @return  The position of this buffer
     */
    public final long position()
    {
        return position;
    }

    /**
     * Sets this buffer's position.  If the mark is defined and larger than the
     * new position then it is discarded. </p>
     *
     * @param  newPosition
     *         The new position value; must be non-negative
     *         and no larger than the current limit
     *
     * @return  This buffer
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on <tt>newPosition</tt> do not hold
     */
    public final BufferL position( long newPosition )
    {
        if ( ( newPosition > limit ) || ( newPosition < 0 ) )
            throw new IllegalArgumentException();
        position = newPosition;
        if ( mark > position )
            mark = -1;
        return this;
    }

    /**
     * Returns this buffer's limit. </p>
     *
     * @return  The limit of this buffer
     */
    public final long limit()
    {
        return limit;
    }

    /**
     * Sets this buffer's limit.  If the position is larger than the new limit
     * then it is set to the new limit.  If the mark is defined and larger than
     * the new limit then it is discarded. </p>
     *
     * @param  newLimit
     *         The new limit value; must be non-negative
     *         and no larger than this buffer's capacity
     *
     * @return  This buffer
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on <tt>newLimit</tt> do not hold
     */
    public final BufferL limit( long newLimit )
    {
        if ( ( newLimit > capacity ) || ( newLimit < 0 ) )
            throw new IllegalArgumentException();
        limit = newLimit;
        if ( position > limit )
            position = limit;
        if ( mark > limit )
            mark = -1;
        return this;
    }

    /**
     * Sets this buffer's mark at its position. </p>
     *
     * @return  This buffer
     */
    public final BufferL mark()
    {
        mark = position;
        return this;
    }

    /**
     * Resets this buffer's position to the previously-marked position.
     *
     * <p> Invoking this method neither changes nor discards the mark's
     * value. </p>
     *
     * @return  This buffer
     *
     * @throws  InvalidMarkException
     *          If the mark has not been set
     */
    public final BufferL reset()
    {
        long m = mark;
        if ( m < 0 )
            throw new InvalidMarkException();
        position = m;
        return this;
    }

    /**
     * Clears this buffer.  The position is set to zero, the limit is set to
     * the capacity, and the mark is discarded.
     *
     * <p> Invoke this method before using a sequence of channel-read or
     * <i>put</i> operations to fill this buffer.  For example:
     *
     * <blockquote><pre>
     * buf.clear();     // Prepare buffer for reading
     * in.read(buf);    // Read data</pre></blockquote>
     *
     * <p> This method does not actually erase the data in the buffer, but it
     * is named as if it did because it will most often be used in situations
     * in which that might as well be the case. </p>
     *
     * @return  This buffer
     */
    public final BufferL clear()
    {
        position = 0;
        limit = capacity;
        mark = -1;
        return this;
    }

    /**
     * Flips this buffer.  The limit is set to the current position and then
     * the position is set to zero.  If the mark is defined then it is
     * discarded.
     *
     * <p> After a sequence of channel-read or <i>put</i> operations, invoke
     * this method to prepare for a sequence of channel-write or relative
     * <i>get</i> operations.  For example:
     *
     * <blockquote><pre>
     * buf.put(magic);    // Prepend header
     * in.read(buf);      // Read data into rest of buffer
     * buf.flip();        // Flip buffer
     * out.write(buf);    // Write header + data to channel</pre></blockquote>
     *
     * <p> This method is often used in conjunction with the {@link
     * java.nio.ByteBuffer#compact compact} method when transferring data from
     * one place to another.  </p>
     *
     * @return  This buffer
     */
    public final BufferL flip()
    {
        limit = position;
        position = 0;
        mark = -1;
        return this;
    }

    /**
     * Rewinds this buffer.  The position is set to zero and the mark is
     * discarded.
     *
     * <p> Invoke this method before a sequence of channel-write or <i>get</i>
     * operations, assuming that the limit has already been set
     * appropriately.  For example:
     *
     * <blockquote><pre>
     * out.write(buf);    // Write remaining data
     * buf.rewind();      // Rewind buffer
     * buf.get(array);    // Copy data into array</pre></blockquote>
     *
     * @return  This buffer
     */
    public final BufferL rewind()
    {
        position = 0;
        mark = -1;
        return this;
    }

    /**
     * Returns the number of elements between the current position and the
     * limit. </p>
     *
     * @return  The number of elements remaining in this buffer
     */
    public final long remaining()
    {
        return limit - position;
    }

    /**
     * Tells whether there are any elements between the current position and
     * the limit. </p>
     *
     * @return  <tt>true</tt> if, and only if, there is at least one element
     *          remaining in this buffer
     */
    public final boolean hasRemaining()
    {
        return position < limit;
    }

    /**
     * Tells whether or not this buffer is read-only. </p>
     *
     * @return  <tt>true</tt> if, and only if, this buffer is read-only
     */
    public abstract boolean isReadOnly();

    // -- Package-private methods for bounds checking, etc. --

    /**
     * Checks the current position against the limit, throwing a {@link
     * BufferUnderflowException} if it is not smaller than the limit, and then
     * increments the position. </p>
     *
     * @return  The current position value, before it is incremented
     */
    final long nextGetIndex()
    { // package-private
        if ( position >= limit )
            throw new BufferUnderflowException();
        return position++;
    }

    final long nextGetIndex( long nb )
    { // package-private
        if ( limit - position < nb )
            throw new BufferUnderflowException();
        long p = position;
        position += nb;
        return p;
    }

    /**
     * Checks the current position against the limit, throwing a {@link
     * BufferOverflowException} if it is not smaller than the limit, and then
     * increments the position. </p>
     *
     * @return  The current position value, before it is incremented
     */
    final long nextPutIndex()
    { // package-private
        if ( position >= limit )
            throw new BufferOverflowException();
        return position++;
    }

    final long nextPutIndex( long nb )
    { // package-private
        if ( limit - position < nb )
            throw new BufferOverflowException();
        long p = position;
        position += nb;
        return p;
    }

    /**
     * Checks the given index against the limit, throwing an {@link
     * IndexOutOfBoundsException} if it is not smaller than the limit
     * or is smaller than zero.
     */
    final long checkIndex( long i )
    { // package-private
        if ( ( i < 0 ) || ( i >= limit ) )
            throw new IndexOutOfBoundsException();
        return i;
    }

    final long checkIndex( long i, long nb )
    { // package-private
        if ( ( i < 0 ) || ( nb > limit - i ) )
            throw new IndexOutOfBoundsException();
        return i;
    }

    final long markValue()
    { // package-private
        return mark;
    }

    static void checkBounds( long off, long len, long size )
    { // package-private
        if ( ( off | len | ( off + len ) | ( size - ( off + len ) ) ) < 0 )
            throw new IndexOutOfBoundsException();
    }

}
