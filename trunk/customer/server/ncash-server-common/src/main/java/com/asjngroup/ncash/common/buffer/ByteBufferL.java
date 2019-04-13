package com.asjngroup.ncash.common.buffer;

import java.nio.ByteBuffer;
import java.nio.BufferUnderflowException;
import java.nio.BufferOverflowException;
import java.nio.ReadOnlyBufferException;

public abstract class ByteBufferL extends BufferL implements Comparable< ByteBufferL >
{

    // These fields are declared here rather than in Heap-X-Buffer in order to
    // reduce the number of virtual method invocations needed to access these
    // values, which is especially costly when coding small buffers.
    //
    boolean isReadOnly; // Valid only for heap buffers

    // Creates a new buffer with the given mark, position, limit, capacity,
    // backing array, and array offset
    //
    public ByteBufferL( long mark, long pos, long lim, long cap )
    {
        super( mark, pos, lim, cap );
    }

    /**
     * Creates a new byte buffer whose content is a shared subsequence of
     * this buffer's content.
     *
     * <p> The content of the new buffer will start at this buffer's current
     * position.  Changes to this buffer's content will be visible in the new
     * buffer, and vice versa; the two buffers' position, limit, and mark
     * values will be independent.
     *
     * <p> The new buffer's position will be zero, its capacity and its limit
     * will be the number of bytes remaining in this buffer, and its mark
     * will be undefined.  The new buffer will be direct if, and only if, this
     * buffer is direct, and it will be read-only if, and only if, this buffer
     * is read-only.  </p>
     *
     * @return  The new byte buffer
     */
    public abstract ByteBufferL slice();

    /**
     * Creates a new byte buffer that shares this buffer's content.
     *
     * <p> The content of the new buffer will be that of this buffer.  Changes
     * to this buffer's content will be visible in the new buffer, and vice
     * versa; the two buffers' position, limit, and mark values will be
     * independent.
     *
     * <p> The new buffer's capacity, limit, position, and mark values will be
     * identical to those of this buffer.  The new buffer will be direct if,
     * and only if, this buffer is direct, and it will be read-only if, and
     * only if, this buffer is read-only.  </p>
     *
     * @return  The new byte buffer
     */
    public abstract ByteBufferL duplicate();

    // -- Singleton get/put methods --

    /**
     * Relative <i>get</i> method.  Reads the byte at this buffer's
     * current position, and then increments the position. </p>
     *
     * @return  The byte at the buffer's current position
     *
     * @throws  BufferUnderflowException
     *          If the buffer's current position is not smaller than its limit
     */
    public abstract byte get();

    /**
     * Relative <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> Writes the given byte into this buffer at the current
     * position, and then increments the position. </p>
     *
     * @param  b
     *         The byte to be written
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If this buffer's current position is not smaller than its limit
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public abstract ByteBufferL put( byte b );

    /**
     * Absolute <i>get</i> method.  Reads the byte at the given
     * index. </p>
     *
     * @param  index
     *         The index from which the byte will be read
     *
     * @return  The byte at the given index
     *
     * @throws  IndexOutOfBoundsException
     *          If <tt>index</tt> is negative
     *          or not smaller than the buffer's limit
     */
    public abstract byte get( long index );

    /**
     * Absolute <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> Writes the given byte into this buffer at the given
     * index. </p>
     *
     * @param  index
     *         The index at which the byte will be written
     *
     * @param  b
     *         The byte value to be written
     *
     * @return  This buffer
     *
     * @throws  IndexOutOfBoundsException
     *          If <tt>index</tt> is negative
     *          or not smaller than the buffer's limit
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public abstract ByteBufferL put( long index, byte b );

    // -- Bulk get operations --

    /**
     * Relative bulk <i>get</i> method.
     *
     * <p> This method transfers bytes from this buffer into the given
     * destination array.  If there are fewer bytes remaining in the
     * buffer than are required to satisfy the request, that is, if
     * <tt>length</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>, then no
     * bytes are transferred and a {@link BufferUnderflowException} is
     * thrown.
     *
     * <p> Otherwise, this method copies <tt>length</tt> bytes from this
     * buffer into the given array, starting at the current position of this
     * buffer and at the given offset in the array.  The position of this
     * buffer is then incremented by <tt>length</tt>.
     *
     * <p> In other words, an invocation of this method of the form
     * <tt>src.get(dst,&nbsp;off,&nbsp;len)</tt> has exactly the same effect as
     * the loop
     *
     * <pre>
     *     for (int i = off; i < off + len; i++)
     *         dst[i] = src.get(); </pre>
     *
     * except that it first checks that there are sufficient bytes in
     * this buffer and it is potentially much more efficient. </p>
     *
     * @param  dst
     *         The array into which bytes are to be written
     *
     * @param  offset
     *         The offset within the array of the first byte to be
     *         written; must be non-negative and no larger than
     *         <tt>dst.length</tt>
     *
     * @param  length
     *         The maximum number of bytes to be written to the given
     *         array; must be non-negative and no larger than
     *         <tt>dst.length - offset</tt>
     *
     * @return  This buffer
     *
     * @throws  BufferUnderflowException
     *          If there are fewer than <tt>length</tt> bytes
     *          remaining in this buffer
     *
     * @throws  IndexOutOfBoundsException
     *          If the preconditions on the <tt>offset</tt> and <tt>length</tt>
     *          parameters do not hold
     */
    public ByteBufferL get( byte[] dst, int offset, int length )
    {
        checkBounds( offset, length, dst.length );
        if ( length > remaining() )
            throw new BufferUnderflowException();
        int end = offset + length;
        for ( int i = offset; i < end; i++ )
            dst[ i ] = get();
        return this;
    }

    /**
     * Relative bulk <i>get</i> method.
     *
     * <p> This method transfers bytes from this buffer into the given
     * destination array.  An invocation of this method of the form
     * <tt>src.get(a)</tt> behaves in exactly the same way as the invocation
     *
     * <pre>
     *     src.get(a, 0, a.length) </pre>
     *
     * @return  This buffer
     *
     * @throws  BufferUnderflowException
     *          If there are fewer than <tt>length</tt> bytes
     *          remaining in this buffer
     */
    public ByteBufferL get( byte[] dst )
    {
        return get( dst, 0, dst.length );
    }

    // -- Bulk put operations --

    /**
     * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> This method transfers the bytes remaining in the given source
     * buffer into this buffer.  If there are more bytes remaining in the
     * source buffer than in this buffer, that is, if
     * <tt>src.remaining()</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>,
     * then no bytes are transferred and a {@link
     * BufferOverflowException} is thrown.
     *
     * <p> Otherwise, this method copies
     * <i>n</i>&nbsp;=&nbsp;<tt>src.remaining()</tt> bytes from the given
     * buffer into this buffer, starting at each buffer's current position.
     * The positions of both buffers are then incremented by <i>n</i>.
     *
     * <p> In other words, an invocation of this method of the form
     * <tt>dst.put(src)</tt> has exactly the same effect as the loop
     *
     * <pre>
     *     while (src.hasRemaining())
     *         dst.put(src.get()); </pre>
     *
     * except that it first checks that there is sufficient space in this
     * buffer and it is potentially much more efficient. </p>
     *
     * @param  src
     *         The source buffer from which bytes are to be read;
     *         must not be this buffer
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *          for the remaining bytes in the source buffer
     *
     * @throws  IllegalArgumentException
     *          If the source buffer is this buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public ByteBufferL put( ByteBufferL src )
    {
        if ( src == this )
            throw new IllegalArgumentException();
        long n = src.remaining();
        if ( n > remaining() )
            throw new BufferOverflowException();
        for ( long i = 0; i < n; i++ )
            put( src.get() );
        return this;
    }

    /**
     * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> This method transfers bytes into this buffer from the given
     * source array.  If there are more bytes to be copied from the array
     * than remain in this buffer, that is, if
     * <tt>length</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>, then no
     * bytes are transferred and a {@link BufferOverflowException} is
     * thrown.
     *
     * <p> Otherwise, this method copies <tt>length</tt> bytes from the
     * given array into this buffer, starting at the given offset in the array
     * and at the current position of this buffer.  The position of this buffer
     * is then incremented by <tt>length</tt>.
     *
     * <p> In other words, an invocation of this method of the form
     * <tt>dst.put(src,&nbsp;off,&nbsp;len)</tt> has exactly the same effect as
     * the loop
     *
     * <pre>
     *     for (int i = off; i < off + len; i++)
     *         dst.put(a[i]); </pre>
     *
     * except that it first checks that there is sufficient space in this
     * buffer and it is potentially much more efficient. </p>
     *
     * @param  src
     *         The array from which bytes are to be read
     *
     * @param  offset
     *         The offset within the array of the first byte to be read;
     *         must be non-negative and no larger than <tt>array.length</tt>
     *
     * @param  length
     *         The number of bytes to be read from the given array;
     *         must be non-negative and no larger than
     *         <tt>array.length - offset</tt>
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *
     * @throws  IndexOutOfBoundsException
     *          If the preconditions on the <tt>offset</tt> and <tt>length</tt>
     *          parameters do not hold
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public ByteBufferL put( byte[] src, int offset, int length )
    {
        checkBounds( offset, length, src.length );
        if ( length > remaining() )
            throw new BufferOverflowException();
        int end = offset + length;
        for ( int i = offset; i < end; i++ )
            this.put( src[ i ] );
        return this;
    }

    /**
     * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> This method transfers the entire content of the given source
     * byte array into this buffer.  An invocation of this method of the
     * form <tt>dst.put(a)</tt> behaves in exactly the same way as the
     * invocation
     *
     * <pre>
     *     dst.put(a, 0, a.length) </pre>
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public final ByteBufferL put( byte[] src )
    {
        return put( src, 0, src.length );
    }

    /**
     * Compacts this buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> The bytes between the buffer's current position and its limit,
     * if any, are copied to the beginning of the buffer.  That is, the
     * byte at index <i>p</i>&nbsp;=&nbsp;<tt>position()</tt> is copied
     * to index zero, the byte at index <i>p</i>&nbsp;+&nbsp;1 is copied
     * to index one, and so forth until the byte at index
     * <tt>limit()</tt>&nbsp;-&nbsp;1 is copied to index
     * <i>n</i>&nbsp;=&nbsp;<tt>limit()</tt>&nbsp;-&nbsp;<tt>1</tt>&nbsp;-&nbsp;<i>p</i>.
     * The buffer's position is then set to <i>n+1</i> and its limit is set to
     * its capacity.  The mark, if defined, is discarded.
     *
     * <p> The buffer's position is set to the number of bytes copied,
     * rather than to zero, so that an invocation of this method can be
     * followed immediately by an invocation of another relative <i>put</i>
     * method. </p>
     *

     *
     * <p> Invoke this method after writing data from a buffer in case the
     * write was incomplete.  The following loop, for example, copies bytes
     * from one channel to another via the buffer <tt>buf</tt>:
     *
     * <blockquote><pre>
     * buf.clear();          // Prepare buffer for use
     * for (;;) {
     *     if (in.read(buf) < 0 && !buf.hasRemaining())
     *         break;        // No more bytes to transfer
     *     buf.flip();
     *     out.write(buf);
     *     buf.compact();    // In case of partial write
     * }</pre></blockquote>
     *

     *
     * @return  This buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public abstract ByteBufferL compact();

    /**
     * Returns a string summarizing the state of this buffer.  </p>
     *
     * @return  A summary string
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( getClass().getName() );
        sb.append( "[pos=" );
        sb.append( position() );
        sb.append( " lim=" );
        sb.append( limit() );
        sb.append( " cap=" );
        sb.append( capacity() );
        sb.append( "]" );
        return sb.toString();
    }

    /**
     * Returns the current hash code of this buffer.
     *
     * <p> The hash code of a byte buffer depends only upon its remaining
     * elements; that is, upon the elements from <tt>position()</tt> up to, and
     * including, the element at <tt>limit()</tt>&nbsp;-&nbsp;<tt>1</tt>.
     *
     * <p> Because buffer hash codes are content-dependent, it is inadvisable
     * to use buffers as keys in hash maps or similar data structures unless it
     * is known that their contents will not change.  </p>
     *
     * @return  The current hash code of this buffer
     */
    public int hashCode()
    {
        int h = 1;
        long p = position();
        for ( long i = limit() - 1; i >= p; i-- )
            h = 31 * h + (int)get( i );
        return h;
    }

    /**
     * Tells whether or not this buffer is equal to another object.
     *
     * <p> Two byte buffers are equal if, and only if,
     *
     * <p><ol>
     *
     *   <li><p> They have the same element type,  </p></li>
     *
     *   <li><p> They have the same number of remaining elements, and
     *   </p></li>
     *
     *   <li><p> The two sequences of remaining elements, considered
     *   independently of their starting positions, are pointwise equal.
     *   </p></li>
     *
     * </ol>
     *
     * <p> A byte buffer is not equal to any other type of object.  </p>
     *
     * @param  ob  The object to which this buffer is to be compared
     *
     * @return  <tt>true</tt> if, and only if, this buffer is equal to the
     *           given object
     */
    public boolean equals( Object ob )
    {
        if ( !( ob instanceof ByteBufferL ) )
            return false;
        ByteBufferL that = (ByteBufferL)ob;
        if ( this.remaining() != that.remaining() )
            return false;
        long p = this.position();
        for ( long i = this.limit() - 1, j = that.limit() - 1; i >= p; i--, j-- )
        {
            byte v1 = this.get( i );
            byte v2 = that.get( j );
            if ( v1 != v2 )
            {
                if ( ( v1 != v1 ) && ( v2 != v2 ) ) // For float and double
                    continue;
                return false;
            }
        }
        return true;
    }

    /**
     * Compares this buffer to another.
     *
     * <p> Two byte buffers are compared by comparing their sequences of
     * remaining elements lexicographically, without regard to the starting
     * position of each sequence within its corresponding buffer.
     *
     * <p> A byte buffer is not comparable to any other type of object.
     *
     * @return  A negative integer, zero, or a positive integer as this buffer
     *		is less than, equal to, or greater than the given buffer
     */
    public int compareTo( ByteBufferL that )
    {
        long n = this.position() + Math.min( this.remaining(), that.remaining() );
        for ( long i = this.position(), j = that.position(); i < n; i++, j++ )
        {
            byte v1 = this.get( i );
            byte v2 = that.get( j );
            if ( v1 == v2 )
                continue;
            if ( ( v1 != v1 ) && ( v2 != v2 ) ) // For float and double
                continue;
            if ( v1 < v2 )
                return -1;
            return +1;
        }
        if ( this.remaining() > that.remaining() )
            return 1;

        if ( this.remaining() < that.remaining() )
            return -1;

        return 0;
    }

    // -- Other char stuff --

    // -- Other byte stuff: Access to binary data --

    //    boolean bigEndian					// package-private
    //	= true;
    //    boolean nativeByteOrder				// package-private
    //	= (Bits.byteOrder() == ByteOrder.BIG_ENDIAN);
    //
    //    /**
    //     * Retrieves this buffer's byte order.
    //     *
    //     * <p> The byte order is used when reading or writing multibyte values, and
    //     * when creating buffers that are views of this byte buffer.  The order of
    //     * a newly-created byte buffer is always {@link ByteOrder#BIG_ENDIAN
    //     * BIG_ENDIAN}.  </p>
    //     *
    //     * @return  This buffer's byte order
    //     */
    //    public final ByteOrder order() {
    //	return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    //    }
    //
    //    /**
    //     * Modifies this buffer's byte order.  </p>
    //     *
    //     * @param  bo
    //     *         The new byte order,
    //     *         either {@link ByteOrder#BIG_ENDIAN BIG_ENDIAN}
    //     *         or {@link ByteOrder#LITTLE_ENDIAN LITTLE_ENDIAN}
    //     *
    //     * @return  This buffer
    //     */
    //    public final LByteBuffer order(ByteOrder bo) {
    //	bigEndian = (bo == ByteOrder.BIG_ENDIAN);
    //	nativeByteOrder =
    //	    (bigEndian == (Bits.byteOrder() == ByteOrder.BIG_ENDIAN));
    //	return this;
    //    }
    //
    //    // Unchecked accessors, for use by ByteBufferAs-X-Buffer classes
    //    //
    //    abstract byte _get(int i);				// package-private
    //    abstract void _put(int i, byte b);			// package-private
    //
    //
    //    /**
    //     * Relative <i>get</i> method for reading a char value.
    //     *
    //     * <p> Reads the next two bytes at this buffer's current position,
    //     * composing them into a char value according to the current byte order,
    //     * and then increments the position by two.  </p>
    //     *
    //     * @return  The char value at the buffer's current position
    //     *
    //     * @throws  BufferUnderflowException
    //     *          If there are fewer than two bytes
    //     *          remaining in this buffer
    //     */
    //    public abstract char getChar();
    //
    //    /**
    //     * Relative <i>put</i> method for writing a char
    //     * value&nbsp;&nbsp;<i>(optional operation)</i>.
    //     *
    //     * <p> Writes two bytes containing the given char value, in the
    //     * current byte order, into this buffer at the current position, and then
    //     * increments the position by two.  </p>
    //     *
    //     * @param  value
    //     *         The char value to be written
    //     *
    //     * @return  This buffer
    //     *
    //     * @throws  BufferOverflowException
    //     *          If there are fewer than two bytes
    //     *          remaining in this buffer
    //     *
    //     * @throws  ReadOnlyBufferException
    //     *          If this buffer is read-only
    //     */
    //    public abstract LByteBuffer putChar(char value);
    //
    //    /**
    //     * Absolute <i>get</i> method for reading a char value.
    //     *
    //     * <p> Reads two bytes at the given index, composing them into a
    //     * char value according to the current byte order.  </p>
    //     *
    //     * @param  index
    //     *         The index from which the bytes will be read
    //     *
    //     * @return  The char value at the given index
    //     *
    //     * @throws  IndexOutOfBoundsException
    //     *          If <tt>index</tt> is negative
    //     *          or not smaller than the buffer's limit,
    //     *          minus one
    //     */
    //    public abstract char getChar(int index);
    //
    //    /**
    //     * Absolute <i>put</i> method for writing a char
    //     * value&nbsp;&nbsp;<i>(optional operation)</i>.
    //     *
    //     * <p> Writes two bytes containing the given char value, in the
    //     * current byte order, into this buffer at the given index.  </p>
    //     *
    //     * @param  index
    //     *         The index at which the bytes will be written
    //     *
    //     * @param  value
    //     *         The char value to be written
    //     *
    //     * @return  This buffer
    //     *
    //     * @throws  IndexOutOfBoundsException
    //     *          If <tt>index</tt> is negative
    //     *          or not smaller than the buffer's limit,
    //     *          minus one
    //     *
    //     * @throws  ReadOnlyBufferException
    //     *          If this buffer is read-only
    //     */
    //    public abstract LByteBuffer putChar(int index, char value);
    //
    //    /**
    //     * Creates a view of this byte buffer as a char buffer.
    //     *
    //     * <p> The content of the new buffer will start at this buffer's current
    //     * position.  Changes to this buffer's content will be visible in the new
    //     * buffer, and vice versa; the two buffers' position, limit, and mark
    //     * values will be independent.
    //     *
    //     * <p> The new buffer's position will be zero, its capacity and its limit
    //     * will be the number of bytes remaining in this buffer divided by
    //     * two, and its mark will be undefined.  The new buffer will be direct
    //     * if, and only if, this buffer is direct, and it will be read-only if, and
    //     * only if, this buffer is read-only.  </p>
    //     *
    //     * @return  A new char buffer
    //     */
    //    public abstract CharBuffer asCharBuffer();
    //
    //
    //    /**
    //     * Relative <i>get</i> method for reading a short value.
    //     *
    //     * <p> Reads the next two bytes at this buffer's current position,
    //     * composing them into a short value according to the current byte order,
    //     * and then increments the position by two.  </p>
    //     *
    //     * @return  The short value at the buffer's current position
    //     *
    //     * @throws  BufferUnderflowException
    //     *          If there are fewer than two bytes
    //     *          remaining in this buffer
    //     */
    //    public abstract short getShort();
    //
    //    /**
    //     * Relative <i>put</i> method for writing a short
    //     * value&nbsp;&nbsp;<i>(optional operation)</i>.
    //     *
    //     * <p> Writes two bytes containing the given short value, in the
    //     * current byte order, into this buffer at the current position, and then
    //     * increments the position by two.  </p>
    //     *
    //     * @param  value
    //     *         The short value to be written
    //     *
    //     * @return  This buffer
    //     *
    //     * @throws  BufferOverflowException
    //     *          If there are fewer than two bytes
    //     *          remaining in this buffer
    //     *
    //     * @throws  ReadOnlyBufferException
    //     *          If this buffer is read-only
    //     */
    //    public abstract LByteBuffer putShort(short value);
    //
    //    /**
    //     * Absolute <i>get</i> method for reading a short value.
    //     *
    //     * <p> Reads two bytes at the given index, composing them into a
    //     * short value according to the current byte order.  </p>
    //     *
    //     * @param  index
    //     *         The index from which the bytes will be read
    //     *
    //     * @return  The short value at the given index
    //     *
    //     * @throws  IndexOutOfBoundsException
    //     *          If <tt>index</tt> is negative
    //     *          or not smaller than the buffer's limit,
    //     *          minus one
    //     */
    //    public abstract short getShort(int index);
    //
    //    /**
    //     * Absolute <i>put</i> method for writing a short
    //     * value&nbsp;&nbsp;<i>(optional operation)</i>.
    //     *
    //     * <p> Writes two bytes containing the given short value, in the
    //     * current byte order, into this buffer at the given index.  </p>
    //     *
    //     * @param  index
    //     *         The index at which the bytes will be written
    //     *
    //     * @param  value
    //     *         The short value to be written
    //     *
    //     * @return  This buffer
    //     *
    //     * @throws  IndexOutOfBoundsException
    //     *          If <tt>index</tt> is negative
    //     *          or not smaller than the buffer's limit,
    //     *          minus one
    //     *
    //     * @throws  ReadOnlyBufferException
    //     *          If this buffer is read-only
    //     */
    //    public abstract LByteBuffer putShort(int index, short value);
    //
    //    /**
    //     * Creates a view of this byte buffer as a short buffer.
    //     *
    //     * <p> The content of the new buffer will start at this buffer's current
    //     * position.  Changes to this buffer's content will be visible in the new
    //     * buffer, and vice versa; the two buffers' position, limit, and mark
    //     * values will be independent.
    //     *
    //     * <p> The new buffer's position will be zero, its capacity and its limit
    //     * will be the number of bytes remaining in this buffer divided by
    //     * two, and its mark will be undefined.  The new buffer will be direct
    //     * if, and only if, this buffer is direct, and it will be read-only if, and
    //     * only if, this buffer is read-only.  </p>
    //     *
    //     * @return  A new short buffer
    //     */
    //    public abstract ShortBuffer asShortBuffer();
    //
    //
    //    /**
    //     * Relative <i>get</i> method for reading an int value.
    //     *
    //     * <p> Reads the next four bytes at this buffer's current position,
    //     * composing them into an int value according to the current byte order,
    //     * and then increments the position by four.  </p>
    //     *
    //     * @return  The int value at the buffer's current position
    //     *
    //     * @throws  BufferUnderflowException
    //     *          If there are fewer than four bytes
    //     *          remaining in this buffer
    //     */
    //    public abstract int getInt();
    //
    //    /**
    //     * Relative <i>put</i> method for writing an int
    //     * value&nbsp;&nbsp;<i>(optional operation)</i>.
    //     *
    //     * <p> Writes four bytes containing the given int value, in the
    //     * current byte order, into this buffer at the current position, and then
    //     * increments the position by four.  </p>
    //     *
    //     * @param  value
    //     *         The int value to be written
    //     *
    //     * @return  This buffer
    //     *
    //     * @throws  BufferOverflowException
    //     *          If there are fewer than four bytes
    //     *          remaining in this buffer
    //     *
    //     * @throws  ReadOnlyBufferException
    //     *          If this buffer is read-only
    //     */
    //    public abstract LByteBuffer putInt(int value);
    //
    //    /**
    //     * Absolute <i>get</i> method for reading an int value.
    //     *
    //     * <p> Reads four bytes at the given index, composing them into a
    //     * int value according to the current byte order.  </p>
    //     *
    //     * @param  index
    //     *         The index from which the bytes will be read
    //     *
    //     * @return  The int value at the given index
    //     *
    //     * @throws  IndexOutOfBoundsException
    //     *          If <tt>index</tt> is negative
    //     *          or not smaller than the buffer's limit,
    //     *          minus three
    //     */
    //    public abstract int getInt(int index);
    //
    //    /**
    //     * Absolute <i>put</i> method for writing an int
    //     * value&nbsp;&nbsp;<i>(optional operation)</i>.
    //     *
    //     * <p> Writes four bytes containing the given int value, in the
    //     * current byte order, into this buffer at the given index.  </p>
    //     *
    //     * @param  index
    //     *         The index at which the bytes will be written
    //     *
    //     * @param  value
    //     *         The int value to be written
    //     *
    //     * @return  This buffer
    //     *
    //     * @throws  IndexOutOfBoundsException
    //     *          If <tt>index</tt> is negative
    //     *          or not smaller than the buffer's limit,
    //     *          minus three
    //     *
    //     * @throws  ReadOnlyBufferException
    //     *          If this buffer is read-only
    //     */
    //    public abstract LByteBuffer putInt(int index, int value);
    //
    //    /**
    //     * Creates a view of this byte buffer as an int buffer.
    //     *
    //     * <p> The content of the new buffer will start at this buffer's current
    //     * position.  Changes to this buffer's content will be visible in the new
    //     * buffer, and vice versa; the two buffers' position, limit, and mark
    //     * values will be independent.
    //     *
    //     * <p> The new buffer's position will be zero, its capacity and its limit
    //     * will be the number of bytes remaining in this buffer divided by
    //     * four, and its mark will be undefined.  The new buffer will be direct
    //     * if, and only if, this buffer is direct, and it will be read-only if, and
    //     * only if, this buffer is read-only.  </p>
    //     *
    //     * @return  A new int buffer
    //     */
    //    public abstract IntBuffer asIntBuffer();
    //
    //
    //    /**
    //     * Relative <i>get</i> method for reading a long value.
    //     *
    //     * <p> Reads the next eight bytes at this buffer's current position,
    //     * composing them into a long value according to the current byte order,
    //     * and then increments the position by eight.  </p>
    //     *
    //     * @return  The long value at the buffer's current position
    //     *
    //     * @throws  BufferUnderflowException
    //     *          If there are fewer than eight bytes
    //     *          remaining in this buffer
    //     */
    //    public abstract long getLong();
    //
    //    /**
    //     * Relative <i>put</i> method for writing a long
    //     * value&nbsp;&nbsp;<i>(optional operation)</i>.
    //     *
    //     * <p> Writes eight bytes containing the given long value, in the
    //     * current byte order, into this buffer at the current position, and then
    //     * increments the position by eight.  </p>
    //     *
    //     * @param  value
    //     *         The long value to be written
    //     *
    //     * @return  This buffer
    //     *
    //     * @throws  BufferOverflowException
    //     *          If there are fewer than eight bytes
    //     *          remaining in this buffer
    //     *
    //     * @throws  ReadOnlyBufferException
    //     *          If this buffer is read-only
    //     */
    //    public abstract LByteBuffer putLong(long value);
    //
    //    /**
    //     * Absolute <i>get</i> method for reading a long value.
    //     *
    //     * <p> Reads eight bytes at the given index, composing them into a
    //     * long value according to the current byte order.  </p>
    //     *
    //     * @param  index
    //     *         The index from which the bytes will be read
    //     *
    //     * @return  The long value at the given index
    //     *
    //     * @throws  IndexOutOfBoundsException
    //     *          If <tt>index</tt> is negative
    //     *          or not smaller than the buffer's limit,
    //     *          minus seven
    //     */
    //    public abstract long getLong(int index);
    //
    //    /**
    //     * Absolute <i>put</i> method for writing a long
    //     * value&nbsp;&nbsp;<i>(optional operation)</i>.
    //     *
    //     * <p> Writes eight bytes containing the given long value, in the
    //     * current byte order, into this buffer at the given index.  </p>
    //     *
    //     * @param  index
    //     *         The index at which the bytes will be written
    //     *
    //     * @param  value
    //     *         The long value to be written
    //     *
    //     * @return  This buffer
    //     *
    //     * @throws  IndexOutOfBoundsException
    //     *          If <tt>index</tt> is negative
    //     *          or not smaller than the buffer's limit,
    //     *          minus seven
    //     *
    //     * @throws  ReadOnlyBufferException
    //     *          If this buffer is read-only
    //     */
    //    public abstract LByteBuffer putLong(int index, long value);
    //
    //    /**
    //     * Creates a view of this byte buffer as a long buffer.
    //     *
    //     * <p> The content of the new buffer will start at this buffer's current
    //     * position.  Changes to this buffer's content will be visible in the new
    //     * buffer, and vice versa; the two buffers' position, limit, and mark
    //     * values will be independent.
    //     *
    //     * <p> The new buffer's position will be zero, its capacity and its limit
    //     * will be the number of bytes remaining in this buffer divided by
    //     * eight, and its mark will be undefined.  The new buffer will be direct
    //     * if, and only if, this buffer is direct, and it will be read-only if, and
    //     * only if, this buffer is read-only.  </p>
    //     *
    //     * @return  A new long buffer
    //     */
    //    public abstract LongBuffer asLongBuffer();
    //
    //
    //    /**
    //     * Relative <i>get</i> method for reading a float value.
    //     *
    //     * <p> Reads the next four bytes at this buffer's current position,
    //     * composing them into a float value according to the current byte order,
    //     * and then increments the position by four.  </p>
    //     *
    //     * @return  The float value at the buffer's current position
    //     *
    //     * @throws  BufferUnderflowException
    //     *          If there are fewer than four bytes
    //     *          remaining in this buffer
    //     */
    //    public abstract float getFloat();
    //
    //    /**
    //     * Relative <i>put</i> method for writing a float
    //     * value&nbsp;&nbsp;<i>(optional operation)</i>.
    //     *
    //     * <p> Writes four bytes containing the given float value, in the
    //     * current byte order, into this buffer at the current position, and then
    //     * increments the position by four.  </p>
    //     *
    //     * @param  value
    //     *         The float value to be written
    //     *
    //     * @return  This buffer
    //     *
    //     * @throws  BufferOverflowException
    //     *          If there are fewer than four bytes
    //     *          remaining in this buffer
    //     *
    //     * @throws  ReadOnlyBufferException
    //     *          If this buffer is read-only
    //     */
    //    public abstract LByteBuffer putFloat(float value);
    //
    //    /**
    //     * Absolute <i>get</i> method for reading a float value.
    //     *
    //     * <p> Reads four bytes at the given index, composing them into a
    //     * float value according to the current byte order.  </p>
    //     *
    //     * @param  index
    //     *         The index from which the bytes will be read
    //     *
    //     * @return  The float value at the given index
    //     *
    //     * @throws  IndexOutOfBoundsException
    //     *          If <tt>index</tt> is negative
    //     *          or not smaller than the buffer's limit,
    //     *          minus three
    //     */
    //    public abstract float getFloat(int index);
    //
    //    /**
    //     * Absolute <i>put</i> method for writing a float
    //     * value&nbsp;&nbsp;<i>(optional operation)</i>.
    //     *
    //     * <p> Writes four bytes containing the given float value, in the
    //     * current byte order, into this buffer at the given index.  </p>
    //     *
    //     * @param  index
    //     *         The index at which the bytes will be written
    //     *
    //     * @param  value
    //     *         The float value to be written
    //     *
    //     * @return  This buffer
    //     *
    //     * @throws  IndexOutOfBoundsException
    //     *          If <tt>index</tt> is negative
    //     *          or not smaller than the buffer's limit,
    //     *          minus three
    //     *
    //     * @throws  ReadOnlyBufferException
    //     *          If this buffer is read-only
    //     */
    //    public abstract LByteBuffer putFloat(int index, float value);
    //
    //    /**
    //     * Creates a view of this byte buffer as a float buffer.
    //     *
    //     * <p> The content of the new buffer will start at this buffer's current
    //     * position.  Changes to this buffer's content will be visible in the new
    //     * buffer, and vice versa; the two buffers' position, limit, and mark
    //     * values will be independent.
    //     *
    //     * <p> The new buffer's position will be zero, its capacity and its limit
    //     * will be the number of bytes remaining in this buffer divided by
    //     * four, and its mark will be undefined.  The new buffer will be direct
    //     * if, and only if, this buffer is direct, and it will be read-only if, and
    //     * only if, this buffer is read-only.  </p>
    //     *
    //     * @return  A new float buffer
    //     */
    //    public abstract FloatBuffer asFloatBuffer();
    //
    //
    //    /**
    //     * Relative <i>get</i> method for reading a double value.
    //     *
    //     * <p> Reads the next eight bytes at this buffer's current position,
    //     * composing them into a double value according to the current byte order,
    //     * and then increments the position by eight.  </p>
    //     *
    //     * @return  The double value at the buffer's current position
    //     *
    //     * @throws  BufferUnderflowException
    //     *          If there are fewer than eight bytes
    //     *          remaining in this buffer
    //     */
    //    public abstract double getDouble();
    //
    //    /**
    //     * Relative <i>put</i> method for writing a double
    //     * value&nbsp;&nbsp;<i>(optional operation)</i>.
    //     *
    //     * <p> Writes eight bytes containing the given double value, in the
    //     * current byte order, into this buffer at the current position, and then
    //     * increments the position by eight.  </p>
    //     *
    //     * @param  value
    //     *         The double value to be written
    //     *
    //     * @return  This buffer
    //     *
    //     * @throws  BufferOverflowException
    //     *          If there are fewer than eight bytes
    //     *          remaining in this buffer
    //     *
    //     * @throws  ReadOnlyBufferException
    //     *          If this buffer is read-only
    //     */
    //    public abstract LByteBuffer putDouble(double value);
    //
    //    /**
    //     * Absolute <i>get</i> method for reading a double value.
    //     *
    //     * <p> Reads eight bytes at the given index, composing them into a
    //     * double value according to the current byte order.  </p>
    //     *
    //     * @param  index
    //     *         The index from which the bytes will be read
    //     *
    //     * @return  The double value at the given index
    //     *
    //     * @throws  IndexOutOfBoundsException
    //     *          If <tt>index</tt> is negative
    //     *          or not smaller than the buffer's limit,
    //     *          minus seven
    //     */
    //    public abstract double getDouble(int index);
    //
    //    /**
    //     * Absolute <i>put</i> method for writing a double
    //     * value&nbsp;&nbsp;<i>(optional operation)</i>.
    //     *
    //     * <p> Writes eight bytes containing the given double value, in the
    //     * current byte order, into this buffer at the given index.  </p>
    //     *
    //     * @param  index
    //     *         The index at which the bytes will be written
    //     *
    //     * @param  value
    //     *         The double value to be written
    //     *
    //     * @return  This buffer
    //     *
    //     * @throws  IndexOutOfBoundsException
    //     *          If <tt>index</tt> is negative
    //     *          or not smaller than the buffer's limit,
    //     *          minus seven
    //     *
    //     * @throws  ReadOnlyBufferException
    //     *          If this buffer is read-only
    //     */
    //    public abstract LByteBuffer putDouble(int index, double value);
    //
    //    /**
    //     * Creates a view of this byte buffer as a double buffer.
    //     *
    //     * <p> The content of the new buffer will start at this buffer's current
    //     * position.  Changes to this buffer's content will be visible in the new
    //     * buffer, and vice versa; the two buffers' position, limit, and mark
    //     * values will be independent.
    //     *
    //     * <p> The new buffer's position will be zero, its capacity and its limit
    //     * will be the number of bytes remaining in this buffer divided by
    //     * eight, and its mark will be undefined.  The new buffer will be direct
    //     * if, and only if, this buffer is direct, and it will be read-only if, and
    //     * only if, this buffer is read-only.  </p>
    //     *
    //     * @return  A new double buffer
    //     */
    //    public abstract DoubleBuffer asDoubleBuffer();

    public abstract ByteBuffer toByteBuffer( int length );

    // Public methods used in XML Parsing 
    public boolean hasElements( String recordTypeName, int depthLevel )
    {
        throw new UnsupportedOperationException( "Method should be overridden" );
    }

    public long getOffset( String elementName )
    {
        throw new UnsupportedOperationException( "Method should be overridden" );
    }

    public int getLength( String elementName )
    {
        throw new UnsupportedOperationException( "Method should be overridden" );
    }

    public String getFieldValue( String key )
    {
        throw new UnsupportedOperationException( "Method should be overridden" );
    }

    public void moveToNextElement()
    {
        throw new UnsupportedOperationException( "Method should be overridden" );
    }
    
    public void setParserType(boolean isAttrib)
    {
    	throw new UnsupportedOperationException( "Method should be overridden" );
    }
    
    public boolean getIsElementPresent()
    {
    	throw new UnsupportedOperationException( "Method should be overridden" );
    }
    
    public void setRecordChanged(boolean changed)
    {
    	throw new UnsupportedOperationException( "Method should be overridden" );
    }
    
    public boolean isCountNotValid(String elementName)
    {
    	throw new UnsupportedOperationException( "Method should be overridden" );
    }
}
