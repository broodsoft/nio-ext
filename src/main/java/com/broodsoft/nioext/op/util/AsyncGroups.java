package com.broodsoft.nioext.op.util;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class AsyncGroups {
	public static boolean awaitTerminationUnchecked(AsynchronousChannelGroup group, long time, TimeUnit unit)
	throws IllegalStateException {
		try {
			return group.awaitTermination(time, unit);
		} catch (InterruptedException e) {
			throw new IllegalStateException("Error while waiting for termination", e);
		}
	}

	public static boolean awaitTerminationUnchecked(AsynchronousChannelGroup group)
	throws IllegalStateException {
		return awaitTerminationUnchecked(group, Long.MAX_VALUE, TimeUnit.DAYS);
	}

	public static AsynchronousChannelGroup withCachedThreadPoolUnchecked(ExecutorService executor, int initialSize)
	throws IllegalStateException {
		try {
			return AsynchronousChannelGroup.withCachedThreadPool(executor, initialSize);
		} catch(IOException e) {
			throw new IllegalStateException(
				"Failed to create an instance of an async channel group with a cached thread pool", e);
		}
	}
}
