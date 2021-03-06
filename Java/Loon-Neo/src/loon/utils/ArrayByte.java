/**
 * Copyright 2008 - 2009
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loon
 * @author cping
 * @email：javachenpeng@yahoo.com
 * @version 0.1
 */
package loon.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;

import loon.utils.MathUtils;

public class ArrayByte {

	public static final int BIG_ENDIAN = 0;

	public static final int LITTLE_ENDIAN = 1;

	private byte[] data;

	private int position;

	private int byteOrder;

	public ArrayByte() {
		this(1024 * 10);
	}

	public ArrayByte(int length) {
		this(new byte[length]);
	}

	public ArrayByte(String base64) {
		if (!Base64Coder.isBase64(base64)) {
			throw new RuntimeException("it is not base64 :" + base64);
		}
		this.data = Base64Coder.decodeBase64(base64.toCharArray());
		reset();
	}

	public ArrayByte(byte[] data) {
		this.data = data;
		reset();
	}
	
	public void reset() {
		setOrder(BIG_ENDIAN);
	}

	public void setOrder(int type) {
		position = 0;
		byteOrder = type;
	}

	public byte[] getData() {
		return data;
	}

	public byte get(int idx) {
		return data[idx];
	}

	public byte get() {
		return data[position++];
	}

	public int getByteOrder() {
		return byteOrder;
	}

	public void setByteOrder(int byteOrder) {
		this.byteOrder = byteOrder;
	}

	public byte[] readByteArray(int readLength) throws Exception {
		byte[] readBytes = new byte[readLength];
		read(readBytes);
		return readBytes;
	}

	public int length() {
		return data.length;
	}

	public void setLength(int length) {
		if (length != data.length) {
			byte[] oldData = data;
			data = new byte[length];
			System.arraycopy(oldData, 0, data, 0,
					MathUtils.min(oldData.length, length));
			if (position > length) {
				position = length;
			}
		}
	}

	public int position() {
		return position;
	}

	public void setPosition(int position) throws IndexOutOfBoundsException {
		if (position < 0 || position > data.length) {
			throw new IndexOutOfBoundsException();
		}
		this.position = position;
	}

	public void truncate() {
		setLength(position);
	}

	public int available() {
		return length() - position();
	}

	private void checkAvailable(int length) throws IndexOutOfBoundsException {
		if (available() < length) {
			throw new IndexOutOfBoundsException();
		}
	}

	public int read() throws IndexOutOfBoundsException {
		checkAvailable(1);
		return data[position++] & 0xff;
	}

	public byte readByte() throws IndexOutOfBoundsException {
		checkAvailable(1);
		return data[position++];
	}

	public int read(byte[] buffer) throws IndexOutOfBoundsException {
		return read(buffer, 0, buffer.length);
	}

	public int read(byte[] buffer, int offset, int length)
			throws IndexOutOfBoundsException {
		if (length == 0) {
			return 0;
		}
		checkAvailable(length);
		System.arraycopy(data, position, buffer, offset, length);
		position += length;
		return length;
	}

	public long skip(long n) throws IOException {
		long remaining = n;
		int nr;
		if (n <= 0) {
			return 0;
		}
		int size = (int) MathUtils.min(2048, remaining);
		byte[] skipBuffer = new byte[size];
		while (remaining > 0) {
			nr = read(skipBuffer, 0, (int) MathUtils.min(size, remaining));
			if (nr < 0) {
				break;
			}
			remaining -= nr;
		}
		return n - remaining;
	}

	public void read(OutputStream out) throws IOException {
		out.write(data, position, data.length - position);
		position = data.length;
	}

	public boolean readBoolean() throws IndexOutOfBoundsException {
		return (readByte() != 0);
	}

	public short readShort() throws IndexOutOfBoundsException {
		checkAvailable(2);
		if (byteOrder == LITTLE_ENDIAN) {
			return (short) ((data[position++] & 0xff) | ((data[position++] & 0xff) << 8));
		} else {
			return (short) (((data[position++] & 0xff) << 8) | (data[position++] & 0xff));
		}
	}

	public int readInt() throws IndexOutOfBoundsException {
		checkAvailable(4);
		if (byteOrder == LITTLE_ENDIAN) {
			return (data[position++] & 0xff) | ((data[position++] & 0xff) << 8)
					| ((data[position++] & 0xff) << 16)
					| ((data[position++] & 0xff) << 24);
		} else {
			return ((data[position++] & 0xff) << 24)
					| ((data[position++] & 0xff) << 16)
					| ((data[position++] & 0xff) << 8)
					| (data[position++] & 0xff);
		}
	}

	public double readDouble() throws IndexOutOfBoundsException {
		return NumberUtils.longBitsToDouble(readLong());
	}

	public long readLong() throws IndexOutOfBoundsException {
		checkAvailable(8);
		if (byteOrder == LITTLE_ENDIAN) {
			return (readInt() & 0xffffffffL)
					| ((readInt() & 0xffffffffL) << 32L);
		} else {
			return ((readInt() & 0xffffffffL) << 32L)
					| (readInt() & 0xffffffffL);
		}
	}

	public float readFloat() throws IndexOutOfBoundsException {
		return NumberUtils.intBitsToFloat(readInt());
	}

	public String readUTF() throws IndexOutOfBoundsException,
			UTFDataFormatException {
		checkAvailable(2);
		int utfLength = readShort() & 0xffff;
		checkAvailable(utfLength);

		int goalPosition = position() + utfLength;

		StringBuffer string = new StringBuffer(utfLength);
		while (position() < goalPosition) {
			int a = readByte() & 0xff;
			if ((a & 0x80) == 0) {
				string.append((char) a);
			} else {
				int b = readByte() & 0xff;
				if ((b & 0xc0) != 0x80) {
					throw new UTFDataFormatException();
				}

				if ((a & 0xe0) == 0xc0) {
					char ch = (char) (((a & 0x1f) << 6) | (b & 0x3f));
					string.append(ch);
				} else if ((a & 0xf0) == 0xe0) {
					int c = readByte() & 0xff;
					if ((c & 0xc0) != 0x80) {
						throw new UTFDataFormatException();
					}
					char ch = (char) (((a & 0x0f) << 12) | ((b & 0x3f) << 6) | (c & 0x3f));
					string.append(ch);
				} else {
					throw new UTFDataFormatException();
				}
			}
		}
		return string.toString();
	}

	private void ensureCapacity(int dataSize) {
		if (position + dataSize > data.length) {
			setLength(position + dataSize);
		}
	}

	public void writeByte(byte v) {
		ensureCapacity(1);
		data[position++] = v;
	}

	public void writeByte(int v) {
		ensureCapacity(1);
		data[position++] = (byte) v;
	}

	public void write(byte[] buffer) {
		write(buffer, 0, buffer.length);
	}

	public void write(byte[] buffer, int offset, int length) {
		if (length == 0) {
			return;
		}
		ensureCapacity(length);
		System.arraycopy(buffer, offset, data, position, length);
		position += length;
	}

	public void writeBoolean(boolean v) {
		writeByte(v ? -1 : 0);
	}

	public void writeShort(int v) {
		ensureCapacity(2);
		if (byteOrder == LITTLE_ENDIAN) {
			data[position++] = (byte) (v & 0xff);
			data[position++] = (byte) ((v >> 8) & 0xff);
		} else {
			data[position++] = (byte) ((v >> 8) & 0xff);
			data[position++] = (byte) (v & 0xff);
		}
	}

	public void writeInt(int v) {
		ensureCapacity(4);
		if (byteOrder == LITTLE_ENDIAN) {
			data[position++] = (byte) (v & 0xff);
			data[position++] = (byte) ((v >> 8) & 0xff);
			data[position++] = (byte) ((v >> 16) & 0xff);
			data[position++] = (byte) (v >>> 24);
		} else {
			data[position++] = (byte) (v >>> 24);
			data[position++] = (byte) ((v >> 16) & 0xff);
			data[position++] = (byte) ((v >> 8) & 0xff);
			data[position++] = (byte) (v & 0xff);
		}
	}

	public void writeLong(long v) {
		ensureCapacity(8);
		if (byteOrder == LITTLE_ENDIAN) {
			writeInt((int) (v & 0xffffffffL));
			writeInt((int) (v >>> 32));
		} else {
			writeInt((int) (v >>> 32));
			writeInt((int) (v & 0xffffffffL));
		}
	}

	public void writeFloat(float v) {
		writeInt(NumberUtils.floatToIntBits(v));
	}

	public void writeUTF(String s) throws UTFDataFormatException {

		int utfLength = 0;
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch > 0 && ch < 0x80) {
				utfLength++;
			} else if (ch == 0 || (ch >= 0x80 && ch < 0x800)) {
				utfLength += 2;
			} else {
				utfLength += 3;
			}
		}

		if (utfLength > 65535) {
			throw new UTFDataFormatException();
		}

		ensureCapacity(2 + utfLength);
		writeShort(utfLength);

		for (int i = 0; i < s.length(); i++) {
			int ch = s.charAt(i);
			if (ch > 0 && ch < 0x80) {
				writeByte(ch);
			} else if (ch == 0 || (ch >= 0x80 && ch < 0x800)) {
				writeByte(0xc0 | (0x1f & (ch >> 6)));
				writeByte(0x80 | (0x3f & ch));
			} else {
				writeByte(0xe0 | (0x0f & (ch >> 12)));
				writeByte(0x80 | (0x3f & (ch >> 6)));
				writeByte(0x80 | (0x3f & ch));
			}
		}
	}

	public int limit() {
		return data == null ? 0 : data.length;
	}

	@Override
	public String toString() {
		return new String(Base64Coder.encode(data));
	}

	public void close() {
		data = null;
	}

}
