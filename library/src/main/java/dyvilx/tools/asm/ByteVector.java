/***
 * ASM: a very small and fast Java bytecode manipulation framework Copyright (c) 2000-2011 INRIA, France Telecom All
 * rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution. 3. Neither the name of the copyright holders nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dyvilx.tools.asm;

/**
 * A dynamically extensible vector of bytes. This class is roughly equivalent to
 * a DataOutputStream on top of a ByteArrayOutputStream, but is more efficient.
 *
 * @author Eric Bruneton
 */
public class ByteVector
{
	
	/**
	 * The content of this vector.
	 */
	byte[] data;
	
	/**
	 * Actual number of bytes in this vector.
	 */
	int length;
	
	/**
	 * Constructs a new {@link ByteVector ByteVector} with a default initial
	 * size.
	 */
	public ByteVector()
	{
		this.data = new byte[64];
	}
	
	/**
	 * Constructs a new {@link ByteVector ByteVector} with the given initial
	 * size.
	 *
	 * @param initialSize
	 *            the initial size of the byte vector to be constructed.
	 */
	public ByteVector(final int initialSize)
	{
		this.data = new byte[initialSize];
	}
	
	/**
	 * Puts a byte into this byte vector. The byte vector is automatically
	 * enlarged if necessary.
	 *
	 * @param b
	 *            a byte.
	 * @return this byte vector.
	 */
	public ByteVector putByte(final int b)
	{
		int length = this.length;
		if (length + 1 > this.data.length)
		{
			this.enlarge(1);
		}
		this.data[length++] = (byte) b;
		this.length = length;
		return this;
	}
	
	/**
	 * Puts two bytes into this byte vector. The byte vector is automatically
	 * enlarged if necessary.
	 *
	 * @param b1
	 *            a byte.
	 * @param b2
	 *            another byte.
	 * @return this byte vector.
	 */
	ByteVector put11(final int b1, final int b2)
	{
		int length = this.length;
		if (length + 2 > this.data.length)
		{
			this.enlarge(2);
		}
		byte[] data = this.data;
		data[length++] = (byte) b1;
		data[length++] = (byte) b2;
		this.length = length;
		return this;
	}
	
	/**
	 * Puts a short into this byte vector. The byte vector is automatically
	 * enlarged if necessary.
	 *
	 * @param s
	 *            a short.
	 * @return this byte vector.
	 */
	public ByteVector putShort(final int s)
	{
		int length = this.length;
		if (length + 2 > this.data.length)
		{
			this.enlarge(2);
		}
		byte[] data = this.data;
		data[length++] = (byte) (s >>> 8);
		data[length++] = (byte) s;
		this.length = length;
		return this;
	}
	
	/**
	 * Puts a byte and a short into this byte vector. The byte vector is
	 * automatically enlarged if necessary.
	 *
	 * @param b
	 *            a byte.
	 * @param s
	 *            a short.
	 * @return this byte vector.
	 */
	ByteVector put12(final int b, final int s)
	{
		int length = this.length;
		if (length + 3 > this.data.length)
		{
			this.enlarge(3);
		}
		byte[] data = this.data;
		data[length++] = (byte) b;
		data[length++] = (byte) (s >>> 8);
		data[length++] = (byte) s;
		this.length = length;
		return this;
	}
	
	/**
	 * Puts an int into this byte vector. The byte vector is automatically
	 * enlarged if necessary.
	 *
	 * @param i
	 *            an int.
	 * @return this byte vector.
	 */
	public ByteVector putInt(final int i)
	{
		int length = this.length;
		if (length + 4 > this.data.length)
		{
			this.enlarge(4);
		}
		byte[] data = this.data;
		data[length++] = (byte) (i >>> 24);
		data[length++] = (byte) (i >>> 16);
		data[length++] = (byte) (i >>> 8);
		data[length++] = (byte) i;
		this.length = length;
		return this;
	}
	
	/**
	 * Puts a long into this byte vector. The byte vector is automatically
	 * enlarged if necessary.
	 *
	 * @param l
	 *            a long.
	 * @return this byte vector.
	 */
	public ByteVector putLong(final long l)
	{
		int length = this.length;
		if (length + 8 > this.data.length)
		{
			this.enlarge(8);
		}
		byte[] data = this.data;
		int i = (int) (l >>> 32);
		data[length++] = (byte) (i >>> 24);
		data[length++] = (byte) (i >>> 16);
		data[length++] = (byte) (i >>> 8);
		data[length++] = (byte) i;
		i = (int) l;
		data[length++] = (byte) (i >>> 24);
		data[length++] = (byte) (i >>> 16);
		data[length++] = (byte) (i >>> 8);
		data[length++] = (byte) i;
		this.length = length;
		return this;
	}
	
	/**
	 * Puts an UTF8 string into this byte vector. The byte vector is
	 * automatically enlarged if necessary.
	 *
	 * @param s
	 *            a String whose UTF8 encoded length must be less than 65536.
	 * @return this byte vector.
	 */
	public ByteVector putUTF8(final String s)
	{
		int charLength = s.length();
		if (charLength > 65535)
		{
			throw new IllegalArgumentException();
		}
		int len = this.length;
		if (len + 2 + charLength > this.data.length)
		{
			this.enlarge(2 + charLength);
		}
		byte[] data = this.data;
		// optimistic algorithm: instead of computing the byte length and then
		// serializing the string (which requires two loops), we assume the byte
		// length is equal to char length (which is the most frequent case), and
		// we start serializing the string right away. During the serialization,
		// if we find that this assumption is wrong, we continue with the
		// general method.
		data[len++] = (byte) (charLength >>> 8);
		data[len++] = (byte) charLength;
		for (int i = 0; i < charLength; ++i)
		{
			char c = s.charAt(i);
			if (c >= '\001' && c <= '\177')
			{
				data[len++] = (byte) c;
			}
			else
			{
				this.length = len;
				return this.encodeUTF8(s, i, 65535);
			}
		}
		this.length = len;
		return this;
	}
	
	/**
	 * Puts an UTF8 string into this byte vector. The byte vector is
	 * automatically enlarged if necessary. The string length is encoded in two
	 * bytes before the encoded characters, if there is space for that (i.e. if
	 * this.length - i - 2 >= 0).
	 *
	 * @param s
	 *            the String to encode.
	 * @param i
	 *            the index of the first character to encode. The previous
	 *            characters are supposed to have already been encoded, using
	 *            only one byte per character.
	 * @param maxByteLength
	 *            the maximum byte length of the encoded string, including the
	 *            already encoded characters.
	 * @return this byte vector.
	 */
	ByteVector encodeUTF8(final String s, int i, int maxByteLength)
	{
		int charLength = s.length();
		int byteLength = i;
		char c;
		for (int j = i; j < charLength; ++j)
		{
			c = s.charAt(j);
			if (c >= '\001' && c <= '\177')
			{
				byteLength++;
			}
			else if (c > '\u07FF')
			{
				byteLength += 3;
			}
			else
			{
				byteLength += 2;
			}
		}
		if (byteLength > maxByteLength)
		{
			throw new IllegalArgumentException();
		}
		int start = this.length - i - 2;
		if (start >= 0)
		{
			this.data[start] = (byte) (byteLength >>> 8);
			this.data[start + 1] = (byte) byteLength;
		}
		if (this.length + byteLength - i > this.data.length)
		{
			this.enlarge(byteLength - i);
		}
		int len = this.length;
		for (int j = i; j < charLength; ++j)
		{
			c = s.charAt(j);
			if (c >= '\001' && c <= '\177')
			{
				this.data[len++] = (byte) c;
			}
			else if (c > '\u07FF')
			{
				this.data[len++] = (byte) (0xE0 | c >> 12 & 0xF);
				this.data[len++] = (byte) (0x80 | c >> 6 & 0x3F);
				this.data[len++] = (byte) (0x80 | c & 0x3F);
			}
			else
			{
				this.data[len++] = (byte) (0xC0 | c >> 6 & 0x1F);
				this.data[len++] = (byte) (0x80 | c & 0x3F);
			}
		}
		this.length = len;
		return this;
	}
	
	/**
	 * Puts an array of bytes into this byte vector. The byte vector is
	 * automatically enlarged if necessary.
	 *
	 * @param b
	 *            an array of bytes. May be <tt>null</tt> to put <tt>len</tt>
	 *            null bytes into this byte vector.
	 * @param off
	 *            index of the fist byte of b that must be copied.
	 * @param len
	 *            number of bytes of b that must be copied.
	 * @return this byte vector.
	 */
	public ByteVector putByteArray(final byte[] b, final int off, final int len)
	{
		if (this.length + len > this.data.length)
		{
			this.enlarge(len);
		}
		if (b != null)
		{
			System.arraycopy(b, off, this.data, this.length, len);
		}
		this.length += len;
		return this;
	}
	
	/**
	 * Enlarge this byte vector so that it can receive n more bytes.
	 *
	 * @param size
	 *            number of additional bytes that this byte vector should be
	 *            able to receive.
	 */
	private void enlarge(final int size)
	{
		int length1 = 2 * this.data.length;
		int length2 = this.length + size;
		byte[] newData = new byte[length1 > length2 ? length1 : length2];
		System.arraycopy(this.data, 0, newData, 0, this.length);
		this.data = newData;
	}
}
