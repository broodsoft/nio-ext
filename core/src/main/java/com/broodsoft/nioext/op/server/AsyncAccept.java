package com.broodsoft.nioext.op.server;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broodsoft.nioext.op.AsyncOpBase;
import com.broodsoft.nioext.op.client.AsyncConnect;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public final class AsyncAccept extends AsyncOpBase<Void> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncConnect.class);

	private final AsynchronousServerSocketChannel channel;
	private final Supplier<Function<AsynchronousSocketChannel, Void>> clientHandlerFactory;

	public AsyncAccept(
		AsynchronousServerSocketChannel channel,
		Supplier<Function<AsynchronousSocketChannel, Void>> clientHandlerFactory
	) {
		this.channel = channel;
		this.clientHandlerFactory = clientHandlerFactory;
	}

	public ListenableFuture<Void> execute(SettableFuture<Void> resultFuture) {
		channel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
			public void completed(AsynchronousSocketChannel client, Void nullValue) {
				clientHandlerFactory.get().apply(client);
				channel.accept(null, this);
			}

			public void failed(Throwable throwable, Void nullValue) {
				LOGGER.warn("Failed to accept a connection", throwable);
				channel.accept(null, this);//ignore errors, continue
			}
		});
		resultFuture.set(null);
		return resultFuture;
	}
}
