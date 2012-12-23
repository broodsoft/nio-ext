package com.broodsoft.nioext.op.client;

import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public final class AsyncConnect extends AsyncClientOpBase<Void> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncConnect.class);

	private final SocketAddress address;

	public AsyncConnect(AsynchronousSocketChannel channel, SocketAddress address) {
		super(channel);

		this.address = address;
	}

	public ListenableFuture<Void> execute(final SettableFuture<Void> resultFuture) {
		LOGGER.debug("Connecting");
		channel.connect(address, null, new CompletionHandler<Void, Void>() {
			public void completed(Void nullValue1, Void nullValue2) {
				LOGGER.debug("Connected successfully");
				resultFuture.set(null);
			}
			public void failed(Throwable throwable, Void attachment) {
				LOGGER.warn("Failed to connect", throwable);
				resultFuture.setException(throwable);
			}
		});
		return resultFuture;
	}
}
