package com.broodsoft.nioext.demo;

import static com.broodsoft.nioext.op.ForwardingOp.async;
import static com.broodsoft.nioext.op.util.AsyncOpLinks.start;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broodsoft.nioext.op.AsyncOp;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

public class NioExtDemo {
	private static final Logger LOGGER = LoggerFactory.getLogger(NioExtDemo.class);

	static final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		LOGGER.debug("starting");

		try {
			timeoutDemo().get();
			LOGGER.error("Timeout didn't occur as expected!");
		} catch(ExecutionException e) {
			LOGGER.debug("As expected, the task timed-out:");
			e.printStackTrace();
			LOGGER.debug("continuing...");
		}

		final ListenableFuture<Void> demo1Future = demo1();
//		demo1Future.get();
		final ListenableFuture<Void> demo2Future = demo2();
//		demo2Future.get();
		final ListenableFuture<Void> demo3Future = demo3();
//		demo3Future.get();
		final ListenableFuture<Void> demo4Future = demo4();
//		demo4Future.get();
		
		Futures.addCallback(
			Futures.allAsList(demo1Future, demo2Future, demo3Future, demo4Future),
			new FutureCallback<Object>() {
				public void onSuccess(Object nullValue) {
					executor.shutdownNow();
				}

				public void onFailure(Throwable throwable) {
					throwable.printStackTrace();
					executor.shutdownNow();
				}
			}
		);

		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		LOGGER.debug("finished");
	}

	private static ListenableFuture<Void> demo1() throws Exception {
		final long started = System.currentTimeMillis();

		SettableFuture<Void> first = SettableFuture.<Void>create();
		Futures.addCallback(first, new FutureCallback<Void>() {
				public void onSuccess(Void nullValue) {
					final long firstFinished = System.currentTimeMillis();;
					LOGGER.debug("first in "+(firstFinished-started)+" ms");
				}

				public void onFailure(Throwable t) {
					t.printStackTrace();
				}
		});

		SettableFuture<Void> second = SettableFuture.<Void>create();
		Futures.addCallback(second, new FutureCallback<Void>() {
			public void onSuccess(Void nullValue) {
				final long secondFinished = System.currentTimeMillis();;
				LOGGER.debug("second in "+(secondFinished-started)+" ms");
			}

			public void onFailure(Throwable t) {
				t.printStackTrace();
			}
		});

		ListenableFuture<Void> asyncChainResult =
			start(act("task #1", 3000), first).
			then(start(act("task #2", 1000), second)).
			execute();

		Futures.addCallback(asyncChainResult, new FutureCallback<Void>() {
				public void onSuccess(Void nullValue) {
					final long allFinished = System.currentTimeMillis();;
					LOGGER.debug("all in "+(allFinished-started)+" ms");
				}

				public void onFailure(Throwable t) {
					t.printStackTrace();
				}
		});

		return asyncChainResult;
	}

	private static ListenableFuture<Void> demo2() {
		return
			start(act("1.a", 2000)).
			whenDone(
				start(act("2", 1000))
			).
			then(start(act("1.b", 1000))).
			execute();
	}
	
	private static ListenableFuture<Void> demo3() {
		return
			start(act("1", 3000)).
			whenDone(
				start(act("2.a", 2000)).
				then(start(act("2.b", 1000)))
			).
			execute();
	}
	
	private static ListenableFuture<Void> demo4() {
		return
			start(act("0", 1000)).
			whenDone(
				start(act("1.a", 2000)).
				then(start(act("1.b", 1000))).
				await(2500, TimeUnit.MILLISECONDS, executor).
				then(start(act("1.c", 3000)))
			).
			execute();
	}

	private static ListenableFuture<Void> timeoutDemo() {
		return
			start(act("will timeout and fail", 1500)).
			await(1, TimeUnit.SECONDS, executor).
			execute();
	}

	private static AsyncOp<Void> act(final String name, final long ms) {
		return async(new Callable<Void>() {
			public Void call() throws Exception {
				LOGGER.debug(name+"...");
				final long started = System.currentTimeMillis();
				Thread.sleep(ms);
				final long finished = System.currentTimeMillis();
				LOGGER.debug("..."+name+" done in "+(finished-started)+"ms");
				return null;
			}
		}, executor);
	}
}
