package com.broodsoft.nioext.demo.clientserver;

import static com.broodsoft.nioext.op.util.AsyncClients.connect;
import static com.broodsoft.nioext.op.util.AsyncClients.write;
import static com.broodsoft.nioext.op.util.AsyncClients.read;
import static com.broodsoft.nioext.op.util.AsyncClients.openClientChannelUnchecked;
import static com.broodsoft.nioext.op.util.AsyncGroups.withCachedThreadPoolUnchecked;
import static com.broodsoft.nioext.op.util.AsyncOpLinks.start;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broodsoft.nioext.op.client.io.IoProcessor;
import com.google.common.base.Charsets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

public class Client {
	private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

	public static void main(String[] args) throws Exception {
		final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
		final AsynchronousChannelGroup channelGroup = withCachedThreadPoolUnchecked(Executors.newCachedThreadPool(), 5);
		final AsynchronousSocketChannel client = openClientChannelUnchecked(channelGroup);

		final SocketAddress serverAddress = new InetSocketAddress(InetAddress.getLocalHost(), Config.SERVER_PORT);

		final IoProcessor<String> responseProcessor = new IoProcessor<String>() {
			final StringBuilder responseBuilder = new StringBuilder();

			public ByteBuffer buffer() {
				return ByteBuffer.allocate(32);
			}

			public boolean process(Integer bytesRead, ByteBuffer buffer) {
				responseBuilder.append(Charsets.US_ASCII.decode(buffer));
				return false;
			}

			public String result() {
				return responseBuilder.toString();
			}
		};

		final SettableFuture<String> responseFuture = SettableFuture.create();
		Futures.addCallback(responseFuture, new FutureCallback<String>() {
			public void onSuccess(String response) {
				LOGGER.debug("Received response: "+response);
				finishup();
			}

			public void onFailure(Throwable throwable) {
				LOGGER.error("Error while reading response", throwable);
				finishup();
			}

			public void finishup() {
				try {
					client.close();
				} catch(IOException e) {
					LOGGER.error("Error closing connection to server", e);
				}
				executor.shutdown();
				channelGroup.shutdown();
			}
		});

		final String request = "hello!";
		LOGGER.debug("Sending request: "+request);
		start(connect(client, serverAddress)).
		whenDone(start(write(client, Charsets.US_ASCII.encode(request)))).
		whenDone(start(read(client, responseProcessor), responseFuture)).
		await(2, TimeUnit.SECONDS, executor).
		execute();

		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		LOGGER.debug("Client finished");
	}
}
