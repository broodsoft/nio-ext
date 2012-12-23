nio-ext
=======

Java 7 NIO.2 extensions

Requirements:
 * Java 7

The example below illustrates connecting to a server, sending a request, and receiving a response. An explicit timeout of one second is specified on each asynchronous operation.
```
	import static com.limebrokerage.async.op.util.AsyncOpLinks.start;
	import static com.limebrokerage.async.op.util.AsyncClients.*;

	...

	final String request = ...
	final ListeningExecutorService executor = ...
	final AsynchronousSocketChannel client = …
	final SocketAddress address = ...
	final SettableFuture<String> responseFuture = SettableFuture.create();

	final ListenableFuture<Void> executionFuture =
		start(connect(client, address)).
		await(1, TimeUnit.SECONDS, executor).
		whenDone(
			start(write(client, request)).
			await(1, TimeUnit.SECONDS, executor)).
		whenDone(
			start(read(client, responseProcessor), responseFuture).
			await(1, TimeUnit.SECONDS, executor)).
		execute();

	Futures.addCallback(executionFuture, new FutureCallback<Void>() {
		public void onSuccess(Void nullValue) {
			disconnect(client);
			process(responseFuture);
		}
		public void onFailure(Throwable throwable) {
			LOGGER.warn("Error while sending request: "+request, throwable);
			disconnect(client);
		}
	}, executor);
```

Alternatively, a single timeout can be specified for all operations (that is, all three operations - connecting, writing and reading - must complete within three seconds):
```
	start(connect(client, address)).
	whenDone(start(write(client, request))).
	whenDone(start(read(client, responseProcessor), responseFuture)).
	await(3, TimeUnit.SECONDS, executor).
	execute();
```