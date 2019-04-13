package com.asjngroup.ncash.common;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;

import com.asjngroup.ncash.common.util.StringUtil;

public final class BinaryHelper
{
	private static final String defaultCharset = Charset.defaultCharset().displayName();
	public static long[] power2;
	public static BigInteger[] power2BigInt;
	public static String[] byteToHex;
	private static char[] nibbleToHex =
	{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	public static final char[] BcdDigits;
	public static final char[] BcdSymbols;
	public static final char[] HexSymbols;

	public static char nibbleToHexChar( int nibble )
	{
		return nibbleToHex[nibble];
	}

	public static final String getString( byte[] bytes, int offset, int length ) throws UnsupportedEncodingException
	{
		return new String( bytes, offset, length, defaultCharset );
	}

	public static final int getUnsignedInt( byte byteValue )
	{
		return ( byteValue & 0xFF );
	}

	public static final int getUnsignedInt( byte[] bytes, int offset, int length )
	{
		int result = 0;
		for ( int i = 0; i < length; ++i )
			result |= getUnsignedInt( bytes[( offset + i )] ) << 8 * ( length - i - 1 );
		return result;
	}

	public static final int getUnsignedIntReverse( byte[] bytes, int offset, int length )
	{
		int result = 0;
		for ( int i = 0; i < length; ++i )
			result |= getUnsignedInt( bytes[( offset + i )] ) << 8 * i;
		return result;
	}

	public static String byteArrayToHexString( byte[] bytes )
	{
		return getBCD( bytes, 0, bytes.length * 2, HexSymbols, false );
	}

	public static final String getBCD( byte[] bytes, int offset, int length, char[] decodeChars, boolean skipFirst )
	{
		char[] result = new char[length];
		int decodedCount = 0;
		int resultLength = 0;

		int index = offset;
		int startIndex = index;

		while ( decodedCount != length )
		{
			int bcdValue = getUnsignedInt( bytes[index] );

			if ( ( !( skipFirst ) ) || ( index != startIndex ) )
			{
				int highIdx = ( bcdValue & 0xF0 ) >> 4;
				if ( decodeChars[highIdx] != 0 )
					result[( resultLength++ )] = decodeChars[highIdx];
				++decodedCount;
			}

			if ( decodedCount == length )
				break;
			int lowIdx = bcdValue & 0xF;
			if ( decodeChars[lowIdx] != 0 )
				result[( resultLength++ )] = decodeChars[lowIdx];
			++decodedCount;

			++index;
		}
		return new String( result, 0, resultLength );
	}

	public static final String getBCD( NCashByteBuffer buffer, int offset, int length, char[] decodeChars, boolean skipFirst )
	{
		char[] result = new char[length];
		int decodedCount = 0;
		int resultLength = 0;

		buffer.position( buffer.position() + offset );
		boolean firstByte = true;

		while ( decodedCount != length )
		{
			int bcdValue = getUnsignedInt( buffer.get() );

			if ( ( !( skipFirst ) ) || ( !( firstByte ) ) )
			{
				int highIdx = ( bcdValue & 0xF0 ) >> 4;
				if ( decodeChars[highIdx] != 0 )
					result[( resultLength++ )] = decodeChars[highIdx];
				++decodedCount;
			}

			if ( decodedCount == length )
				break;
			int lowIdx = bcdValue & 0xF;
			if ( decodeChars[lowIdx] != 0 )
				result[( resultLength++ )] = decodeChars[lowIdx];
			++decodedCount;

			firstByte = false;
		}
		return new String( result, 0, resultLength );
	}

	public static NCashByteBuffer duplicateByteBufferToTerminator( NCashByteBuffer buffer, byte[] terminator )
	{
		long originalPosition = buffer.position();

		boolean foundTerminator = false;
		int terminatorIndex = 0;

		while ( buffer.remaining() > 0L )
		{
			byte b = buffer.get();

			if ( b == terminator[terminatorIndex] )
				++terminatorIndex;
			else
			{
				terminatorIndex = 0;
			}

			if ( terminatorIndex == terminator.length )
			{
				foundTerminator = true;
				break;
			}

		}

		if ( !( foundTerminator ) )
		{
			buffer.position( originalPosition );
			return null;
		}

		NCashByteBuffer newBuffer = ( NCashByteBuffer ) buffer.duplicate();
		newBuffer.limit( newBuffer.position() - terminator.length );
		newBuffer.position( originalPosition );

		return newBuffer;
	}

	public static NCashByteBuffer duplicateByteBufferLength( NCashByteBuffer buffer, long length )
	{
		return duplicateByteBufferLength( buffer, length, true );
	}

	public static NCashByteBuffer duplicateByteBufferLength( NCashByteBuffer buffer, long length, boolean consume )
	{
		Buffer newBuffer = buffer.duplicate();

		newBuffer.limit( newBuffer.position() + length );

		if ( consume )
		{
			buffer.position( buffer.position() + length );
		}
		return ( NCashByteBuffer ) newBuffer;
	}

	public static int createByteMask( int fromBit, int toBit )
	{
		if ( toBit > fromBit )
		{
			int temp = toBit;
			toBit = fromBit;
			fromBit = temp;
		}

		int mask = 0;

		for ( int i = fromBit; i >= 0; --i )
		{
			mask <<= 1;

			if ( toBit <= i )
			{
				++mask;
			}
		}
		return mask;
	}

	public static NCashByteBuffer byteCreateCopyBitOffset( NCashByteBuffer buffer, int offset, int bitOffset, int length )
	{
		long originalPosition = buffer.position();
		NCashByteBuffer dest;
		if ( ( bitOffset == 7 ) && ( buffer.isReadOnly() ) )
		{
			buffer.position( buffer.position() + offset );

			dest = duplicateByteBufferLength( buffer, length, false );
		}
		else
		{
			dest = new HeapByteBuffer( length );

			byteCopyBitOffset( buffer, offset, bitOffset, length, dest );
		}

		buffer.position( originalPosition );

		return dest;
	}

	public static void byteCopyBitOffset( NCashByteBuffer buffer, int offset, int bitOffset, int length, NCashByteBuffer dest )
	{
		if ( length <= 0 )
		{
			throw new IllegalArgumentException( "Length must be > 0" );
		}
		long originalPosition = buffer.position();
		buffer.position( buffer.position() + offset );

		if ( bitOffset == 7 )
		{
			long limit = buffer.limit();
			buffer.limit( buffer.position() + length );

			dest.put( buffer );

			buffer.limit( limit );

			dest.flip();
		}
		else
		{
			byte lastByte = buffer.get();

			int highMask = createByteMask( 7, bitOffset + 1 );
			int lowMask = createByteMask( 0, bitOffset );
			int shiftLastPart = 7 - bitOffset;
			int shiftNextPart = bitOffset + 1;

			for ( int i = 0; i < length; ++i )
			{
				int newByte = ( lastByte & lowMask ) << shiftLastPart;

				lastByte = buffer.get();

				newByte |= ( lastByte & highMask ) >> shiftNextPart;

				dest.put( ( byte ) newByte );
			}

			dest.flip();
		}

		buffer.position( originalPosition );
	}

	public static String byteBufferToHex( NCashByteBuffer buffer, int length, boolean consume )
	{
		long position = buffer.position();

		StringBuilder sb = new StringBuilder();

		for ( long i = position; i < position + length; i += 1L )
		{
			sb.append( byteToHex[( buffer.get() & 0xFF )] );
		}

		if ( !( consume ) )
		{
			buffer.position( position );
		}

		return sb.toString();
	}

	public static String byteBufferToHex( NCashByteBuffer buffer, int length )
	{
		return byteBufferToHex( buffer, length, true );
	}

	public static String byteToBitmask( byte b )
	{
		String str = "";
		for ( int i = 7; i >= 0; --i )
			str = new StringBuilder().append( str ).append( ( ( b & power2[i] ) > 0L ) ? "1" : "0" ).toString();
		return str;
	}

	public static String bigIntegerToBitMask( BigInteger value )
	{
		int bitLength = value.bitLength();

		StringBuilder sb = new StringBuilder( bitLength );
		for ( int i = bitLength - 1; i >= 0; --i )
		{
			if ( value.testBit( i ) )
			{
				sb.append( '1' );
			}
			else
			{
				sb.append( '0' );
			}
		}

		return sb.toString();
	}

	public static String longToBitmask( long value )
	{
		return bigIntegerToBitMask( BigInteger.valueOf( value ) );
	}

	public static byte bitmaskToByte( String s )
	{
		if ( s.length() != 8 )
		{
			throw new IllegalArgumentException( "Bitmask must be 8 characters" );
		}
		byte b = 0;
		for ( int i = 0; i < 8; ++i )
			if ( s.charAt( i ) == '1' )
				b = ( byte ) ( int ) ( b | power2[( 7 - i )] );
		return b;
	}

	public static String hexStringFromByteBuffer( NCashByteBuffer buffer, int offset, int startBit, int bitCount )
	{
		if ( ( startBit < 0 ) || ( startBit > 7 ) )
		{
			throw new IllegalArgumentException( "Start bit must be between 0 and 7" );
		}
		if ( !( buffer.hasRemaining() ) )
		{
			return "";
		}

		int bitsProcessed = 0;

		long originalPosition = buffer.position();
		buffer.position( originalPosition + offset );

		char[] result = new char[( bitCount + 3 ) / 4];
		int resultOffset = 0;

		int bitsForNibble = bitCount % 4;
		if ( bitsForNibble == 0 )
		{
			bitsForNibble = 4;
		}
		byte currentByte = buffer.get();

		while ( bitsProcessed != bitCount )
		{
			int currentNibble = 0;

			for ( int i = 0; ( i < bitsForNibble ) && ( bitsProcessed != bitCount ); ++i )
			{
				if ( startBit < 0 )
				{
					currentByte = buffer.get();

					startBit = 7;
					++offset;
				}

				currentNibble <<= 1;

				if ( isBitSet( currentByte, startBit ) )
				{
					++currentNibble;
				}
				--startBit;

				++bitsProcessed;
			}

			bitsForNibble = 4;

			result[( resultOffset++ )] = nibbleToHex[currentNibble];
		}

		buffer.position( originalPosition );

		return new String( result );
	}

	public static boolean isBitSet( byte b, int bitOffset )
	{
		return ( ( b & 0xFF & power2[bitOffset] ) != 0L );
	}

	public static BigInteger bigIntegerFromByteBuffer( NCashByteBuffer buffer, int offset, int startBit, int bitCount )
	{
		if ( bitCount < 64 )
		{
			return BigInteger.valueOf( longFromByteBuffer( buffer, offset, startBit, bitCount ) );
		}

		int readBytes = ( bitCount + 7 ) / 8;

		buffer.position( buffer.position() + offset );

		byte[] bytes = new byte[readBytes];
		buffer.get( bytes );

		buffer.position( buffer.position() - readBytes - offset );

		if ( startBit != 7 )
		{
			bytes[0] = ( byte ) ( int ) ( bytes[0] & power2[( startBit + 1 )] - 1L );
		}

		BigInteger bi = new BigInteger( 1, bytes );

		int extraBits = 8 - ( 7 - startBit + bitCount & 0x7 ) & 0x7;

		if ( extraBits > 0 )
		{
			bi = bi.shiftRight( extraBits );
		}

		return bi;
	}

	public static long longFromByteBuffer( NCashByteBuffer buffer, int offset, int startBit, int bitCount )
	{
		if ( ( startBit < 0 ) || ( startBit > 7 ) )
		{
			throw new IllegalArgumentException( "Start bit must be between 0 and 7" );
		}

		int bitsNeeded = 0;
		int bitsProcessed = 0;
		int oldBitsProcessed = 0;
		int bit = 0;
		long value = 0L;
		long currentByte = 0L;
		bit = startBit;

		long originalPosition = buffer.position();
		buffer.position( originalPosition + offset );

		while ( bitsProcessed != bitCount )
		{
			bitsNeeded = bitCount - bitsProcessed;

			bitsProcessed += 8;
			currentByte = getUnsignedInt( buffer.get() );

			if ( bit != 7 )
			{
				currentByte &= power2[( bit + 1 )] - 1L;
				bitsProcessed -= 7 - bit;
			}

			if ( bitsNeeded < bit + 1 )
			{
				currentByte &= 255L - ( power2[( bit - bitsNeeded + 1 )] - 1L );
				bitsProcessed -= bit - bitsNeeded + 1;
			}

			currentByte = shiftRight( currentByte, bit - ( bitCount - 1 - oldBitsProcessed ) );
			value |= currentByte;
			oldBitsProcessed = bitsProcessed;
			bit = 7;
		}

		buffer.position( originalPosition );

		return value;
	}

	public static int getPadNibbleCount( NCashByteBuffer buffer, int offset, int padChar )
	{
		long length = buffer.remaining();

		long originalPosition = buffer.position();
		buffer.position( originalPosition + offset );

		int count = 0;
		for ( int i = 0; i < length; ++i )
		{
			byte b = buffer.get();

			if ( ( b & 0xF0 ) >> 4 != padChar )
				break;
			++count;

			if ( ( b & 0xF ) != padChar )
				break;
			++count;
		}

		buffer.position( originalPosition );

		return count;
	}

	public static int getTermNibbleCount( NCashByteBuffer buffer, int termChar, int offset, boolean skipFirstNibble )
	{
		long length = buffer.remaining();

		long originalPosition = buffer.position();
		buffer.position( originalPosition + offset );

		int count = 0;
		for ( int i = offset; i < length; ++i )
		{
			byte b = buffer.get();

			if ( ( i != offset ) || ( !( skipFirstNibble ) ) )
			{
				if ( ( b & 0xF0 ) >> 4 == termChar )
					break;
				++count;
			}

			if ( ( b & 0xF ) == termChar )
				break;
			++count;
		}

		buffer.position( originalPosition );

		return ( int ) ( length * 2L - ( offset * 2 + count ) - ( ( skipFirstNibble ) ? 1 : 0 ) );
	}

	public static int shiftLeft( int value, int shiftCount )
	{
		return ( ( shiftCount > 0 ) ? value << shiftCount : value >> shiftCount * -1 );
	}

	public static int shiftRight( int value, int shiftCount )
	{
		return ( ( shiftCount > 0 ) ? value >> shiftCount : value << shiftCount * -1 );
	}

	public static long shiftLeft( long value, int shiftCount )
	{
		return ( ( shiftCount > 0 ) ? value << shiftCount : value >> shiftCount * -1 );
	}

	public static long shiftRight( long value, int shiftCount )
	{
		return ( ( shiftCount > 0 ) ? value >> shiftCount : value << shiftCount * -1 );
	}

	public static String normaliseHexValue( String hexValue )
	{
		if ( ( hexValue.startsWith( "0x" ) ) || ( hexValue.startsWith( "0X" ) ) )
		{
			return hexValue.substring( 2 );
		}

		return hexValue;
	}

	public static byte[] hexToByteArray( String hexValue )
	{
		hexValue = normaliseHexValue( hexValue );

		if ( hexValue.length() % 2 != 0 )
		{
			hexValue = new StringBuilder().append( "0" ).append( hexValue ).toString();
		}

		int byteLength = hexValue.length() / 2;
		byte[] bytes = new byte[byteLength];
		for ( int i = 0; i < byteLength; ++i )
			bytes[i] = hexToByte( hexValue.substring( 2 * i, 2 + 2 * i ) );
		return bytes;
	}

	public static byte hexToByte( String hexValue )
	{
		if ( hexValue.length() != 2 )
		{
			throw new IllegalArgumentException( StringUtil.create( "Invalid hex value specified '%1' - must be a single hex byte (2 characters)", new Object[]
			{ hexValue } ) );
		}

		return ( byte ) Integer.parseInt( hexValue, 16 );
	}

	public static void applyByteMask( byte[] bytes, byte[] byteMask )
	{
		for ( int i = 0; i < byteMask.length; ++i )
		{
			if ( i >= bytes.length )
				return;
			bytes[i] = ( byte ) ( bytes[i] & byteMask[i] );
		}
	}

	public static void swapNibblesInPlace( byte[] bytes, int offset, int length )
	{
		for ( int i = offset; ( i < offset + length ) && ( i < bytes.length ); ++i )
		{
			byte b = ( byte ) ( ( bytes[i] & 0xF ) << 4 );

			b = ( byte ) ( b | ( bytes[i] & 0xF0 ) >> 4 );

			bytes[i] = b;
		}
	}

	public static void swapBytePairsInPlace( byte[] bytes, int offset, int length )
	{
		for ( int i = offset; i < offset + length - 1; i += 2 )
		{
			byte b = bytes[i];
			bytes[i] = bytes[( i + 1 )];
			bytes[( i + 1 )] = b;
		}
	}

	public static void reverseBytesInPlace( byte[] bytes, int offset, int length )
	{
		if ( length == 0 )
			return;
		int i = 0;
		while ( i <= ( length - 1 ) / 2 )
		{
			byte lowByte = bytes[( offset + i )];
			byte highByte = bytes[( offset + length - 1 - i )];
			bytes[( offset + i )] = highByte;
			bytes[( offset + length - 1 - i )] = lowByte;
			++i;
		}
	}

	public static String decodeBCDString( String str, char[] decodeMap )
	{
		char[] chars = str.toCharArray();

		for ( int i = 0; i < chars.length; ++i )
		{
			if ( ( chars[i] >= 'A' ) && ( chars[i] <= 'F' ) )
				chars[i] = decodeMap[( chars[i] - 'A' + 10 )];
			else if ( ( chars[i] >= '0' ) && ( chars[i] <= '9' ) )
				chars[i] = decodeMap[( chars[i] - '0' )];
			else
			{
				chars[i] = '!';
			}
		}
		return new String( chars );
	}

	static
	{
		power2 = new long[64];
		for ( int i = 0; i < power2.length; ++i )
		{
			power2[i] = ( 1 << i );
		}
		power2BigInt = new BigInteger[256];
		for ( int i = 0; i < power2BigInt.length; ++i )
		{
			power2BigInt[i] = BigInteger.ONE.shiftLeft( i );
		}
		byteToHex = new String[256];
		for ( int i = 0; i < byteToHex.length; ++i )
		{
			int highNibble = i >>> 4;
			int lowNibble = i & 0xF;

			byteToHex[i] = new StringBuilder().append( "" ).append( nibbleToHexChar( highNibble ) ).append( nibbleToHexChar( lowNibble ) ).toString();
		}

		BcdDigits = new char[]
		{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ', ' ', ' ', ' ', ' ', ' ' };

		BcdSymbols = new char[]
		{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ', '*', '#', ' ', ' ', ' ' };

		HexSymbols = new char[]
		{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	}
}	