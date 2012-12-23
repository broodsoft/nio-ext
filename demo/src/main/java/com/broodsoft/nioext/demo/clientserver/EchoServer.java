package com.broodsoft.nioext.demo.clientserver;

import static com.broodsoft.nioext.op.util.AsyncClients.*;
import static com.broodsoft.nioext.op.util.AsyncGroups.awaitTerminationUnchecked;
import static com.broodsoft.nioext.op.util.AsyncGroups.withCachedThreadPoolUnchecked;
import static com.broodsoft.nioext.op.util.AsyncOpLinks.*;
import static com.broodsoft.nioext.op.util.AsyncServers.bindUnchecked;
import static com.broodsoft.nioext.op.util.AsyncServers.openServerChannelUnchecked;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Executors;

import org.apache.commons.pool.ObjectPool;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.broodsoft.nioext.demo.ByteBufferPoolFactory;
import com.broodsoft.nioext.demo.Pools;
import com.broodsoft.nioext.op.util.AsyncChannels;
import com.broodsoft.nioext.op.util.AsyncServers;

public class EchoServer {
	private final ListeningExecutorService executor;

	private final int port;
	private final AsynchronousChannelGroup channelGroup;
	private final AsynchronousServerSocketChannel server;

	private final ObjectPool<ByteBuffer> bufferPool;

	public EchoServer(int port) {
		this.port = port;

		executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

		channelGroup = withCachedThreadPoolUnchecked(Executors.newCachedThreadPool(), 5);
		server = openServerChannelUnchecked(channelGroup);

		bufferPool = ByteBufferPoolFactory.create(256, 16);
	}

	public void startup() {
		bindUnchecked(server, port, 100);

		AsyncServers.accept(server, new Supplier<Function<AsynchronousSocketChannel,Void>>() {
			public Function<AsynchronousSocketChannel,Void> get() {
				return new Function<AsynchronousSocketChannel, Void>() {
					public Void apply(AsynchronousSocketChannel client) {
						handleAcceptedClient(client);
						return null;
					}
				};
			}
		}).execute();
		awaitTerminationUnchecked(channelGroup);
	}

	private void handleAcceptedClient(AsynchronousSocketChannel client) {
		report(client);
		echo(client);
	}

	void report(AsynchronousSocketChannel client) {
		try {
			System.out.println("New client IP: "+client.getRemoteAddress().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void echo(final AsynchronousSocketChannel client) {
		final ByteBuffer buffer = Pools.borrowObjectUnchecked(bufferPool);
		Futures.addCallback(
			start(read(client, buffer)).
			whenDone(start(write(client, buffer))).
			execute(),
			new FutureCallback<Void>() {
				public void onSuccess(Void result) {
					disconnect(client, buffer);
				}
				public void onFailure(Throwable t) {
					disconnect(client, buffer);
				}
			},
			executor
		);
	}

	private void disconnect(AsynchronousSocketChannel client, ByteBuffer buffer) {
			Pools.returnObjectUnchecked(bufferPool, buffer);
			AsyncChannels.closeUnchecked(client);
	}

	public void shutdown() {
		channelGroup.shutdown();
		Pools.closeUnchecked(bufferPool);
	}

	public static void main(String[] args) throws InterruptedException {
		new EchoServer(Config.SERVER_PORT).startup();
	}
}
