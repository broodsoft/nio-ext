package com.broodsoft.nioext.op.client.io;

import java.nio.ByteBuffer;

public interface IoProcessor<T> {
	ByteBuffer buffer();
	boolean process(Integer byteCount, ByteBuffer buffer);
	T result();
}
