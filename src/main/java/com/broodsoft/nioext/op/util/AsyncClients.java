package com.broodsoft.nioext.op.util;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;

import com.broodsoft.nioext.op.AsyncOp;
import com.broodsoft.nioext.op.client.AsyncConnect;
import com.broodsoft.nioext.op.client.io.AsyncRead;
import com.broodsoft.nioext.op.client.io.AsyncWrite;
import com.broodsoft.nioext.op.client.io.IoProcessor;

public final class AsyncClients {
	public static AsynchronousSocketChannel openClientChannelUnchecked(AsynchronousChannelGroup asyncChannelGroup)
	throws IllegalStateException {
		try {
			return AsynchronousSocketChannel.open(asyncChannelGroup);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to open a client channel", e);
		}
	}

	public final static AsyncConnect connect(AsynchronousSocketChannel channel, SocketAddress address) {
		return new AsyncConnect(channel, address);
	}

	public final static <T> AsyncOp<T> read(AsynchronousSocketChannel channel, ByteBuffer buffer) {
		return read(channel, new ProcessOnce<T>(buffer));
	}

	public final static <T> AsyncOp<T> read(AsynchronousSocketChannel channel, IoProcessor<T> processor) {
		return new AsyncRead<T>(channel, processor);
	}

	public final static <T> AsyncOp<T> write(AsynchronousSocketChannel channel, ByteBuffer buffer) {
		return write(channel, new ProcessOnce<T>(buffer));
	}

	public final static <T> AsyncOp<T> write(AsynchronousSocketChannel channel, IoProcessor<T> processor) {
		return new AsyncWrite<T>(channel, processor);
	}



	private static final class ProcessOnce<T> implements IoProcessor<T> {
		final ByteBuffer buffer;
		ProcessOnce(ByteBuffer buffer){ this.buffer = buffer; }

		public boolean process(Integer byteCount, ByteBuffer buffer){ return false; }
		public T result(){ return null; }
		public ByteBuffer buffer(){ return buffer; }
	}
}
