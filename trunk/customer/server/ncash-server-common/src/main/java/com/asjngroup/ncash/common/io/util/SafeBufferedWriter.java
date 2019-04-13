package com.asjngroup.ncash.common.io.util;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public class SafeBufferedWriter extends BufferedWriter {
	private Object safeLock = new Object();

	public SafeBufferedWriter(Writer paramWriter) {
		super(paramWriter);
	}

	public void write(int paramInt) throws IOException {
		synchronized (this.safeLock) {
			super.write(paramInt);
		}
	}
}
