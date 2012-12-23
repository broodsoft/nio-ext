package com.broodsoft.nioext.demo;

import org.apache.commons.pool.ObjectPool;

public final class Pools {
	public static <T> T borrowObjectUnchecked(ObjectPool<T> pool)
	throws IllegalStateException {
		try {
			return pool.borrowObject();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static <T> void returnObjectUnchecked(ObjectPool<T> pool, T obj)
	throws IllegalStateException {
		try {
			pool.returnObject(obj);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static <T> void closeUnchecked(ObjectPool<T> pool)
	throws IllegalStateException {
		try {
			pool.close();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to close the pool", e);
		}
	}
}
