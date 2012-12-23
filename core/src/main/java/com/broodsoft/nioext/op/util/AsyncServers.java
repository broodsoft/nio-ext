package com.broodsoft.nioext.op.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

import com.broodsoft.nioext.op.AsyncOp;
import com.broodsoft.nioext.op.server.AsyncAccept;
import com.google.common.base.Function;
import com.google.common.base.Supplier;

public final class AsyncServers {
	public static AsynchronousServerSocketChannel openServerChannelUnchecked(
		AsynchronousChannelGroup asyncChannelGroup
	) throws IllegalStateException {
		try {
			return AsynchronousServerSocketChannel.open(asyncChannelGroup);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to open a server channel", e);
		}
	}

	public static void bindUnchecked(AsynchronousServerSocketChannel channel, int port)
	throws IllegalStateException {
		try {
			channel.bind(new InetSocketAddress(InetAddress.getLocalHost(), port));
		} catch (IOException e) {
			throw new IllegalStateException("Failed to bind a server channel", e);
		}
	}

	public static void bindUnchecked(AsynchronousServerSocketChannel channel, int port, int maxPendingConnections)
	throws IllegalStateException {
		try {
			channel.bind(new InetSocketAddress(InetAddress.getLocalHost(), port), maxPendingConnections);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to bind a server channel", e);
		}
	}

	public static AsyncOp<Void> accept(
		AsynchronousServerSocketChannel channel,
		Supplier<Function<AsynchronousSocketChannel, Void>> clientHandlerFactory
	) {
		return new AsyncAccept(channel, clientHandlerFactory);
	}
}
