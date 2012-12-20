package com.broodsoft.nioext.op.util;

import java.io.IOException;
import java.nio.channels.AsynchronousChannel;

public final class AsyncChannels {
	public final static void closeUnchecked(AsynchronousChannel channel)
	throws IllegalArgumentException {
		try {
			channel.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to close a channel", e);
		}
	}
}
