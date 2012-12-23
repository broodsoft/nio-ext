package com.broodsoft.nioext.demo;

import java.nio.ByteBuffer;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

public final class ByteBufferPoolFactory {
	public static ObjectPool<ByteBuffer> create(final int bufferSize, final int poolSize) {
		return new GenericObjectPool<>(new BasePoolableObjectFactory<ByteBuffer>() {
			public ByteBuffer makeObject() throws Exception {
				return ByteBuffer.allocate(bufferSize);
			}
			public void passivateObject(ByteBuffer buffer) throws Exception {
				buffer.clear();
			}
		}, poolSize);
	}
}
