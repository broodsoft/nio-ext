package com.broodsoft.nioext.op.util;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.broodsoft.nioext.op.AsyncOp;
import com.broodsoft.nioext.op.AsyncOpLink;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

public final class AsyncOpLinks {
	public static <T> AsyncOpLink start(AsyncOp<T> op, SettableFuture<T> resultFuture,
		ListeningExecutorService executor
	) {
		return new Leaf<T>(op, resultFuture, executor);
	}

	public static <T> AsyncOpLink start(AsyncOp<T> op, SettableFuture<T> resultFuture) {
		return new Leaf<T>(op, resultFuture, MoreExecutors.sameThreadExecutor());
	}

	public static <T> AsyncOpLink start(AsyncOp<T> op, ListeningExecutorService executor) {
		return new Leaf<T>(op, SettableFuture.<T>create(), executor);
	}

	public static <T> AsyncOpLink start(AsyncOp<T> op) {
		return start(op, MoreExecutors.sameThreadExecutor());
	}



	private static abstract class BaseLink implements AsyncOpLink {
		final ListeningExecutorService executor;

		BaseLink(ListeningExecutorService executor) {
			this.executor = executor;
		}

		public AsyncOpLink whenDone(AsyncOpLink link, ListeningExecutorService executor) {
			return new Serial(this, link, executor);
		}

		public AsyncOpLink whenDone(AsyncOpLink link) {
			return new Serial(this, link, MoreExecutors.sameThreadExecutor());
		}

 		public AsyncOpLink then(AsyncOpLink link, ListeningExecutorService executor) {
 			return new Parallel(this, link, executor);
		}

 		public AsyncOpLink then(AsyncOpLink link) {
 			return new Parallel(this, link, MoreExecutors.sameThreadExecutor());
 		}

		public AsyncOpLink await(long duration, TimeUnit unit, ListeningExecutorService executor) {
			return new Await(this, duration, unit, executor);
		}
	}

	private static final class Leaf<T> extends BaseLink {
		final AsyncOp<T> op;
		final SettableFuture<T> resultFuture;

		Leaf(AsyncOp<T> op, SettableFuture<T> resultFuture, ListeningExecutorService executor) {
			super(executor);

			this.op = op;
			this.resultFuture = resultFuture;
		}

		public ListenableFuture<Void> execute() {
			final ListenableFuture<T> executionFuture = op.execute(resultFuture);
			final SettableFuture<Void> completionFuture = SettableFuture.create();
			Futures.addCallback(executionFuture, new FutureCallback<T>() {
				public void onSuccess(T result){ completionFuture.set(null); }
				public void onFailure(Throwable throwable){ completionFuture.setException(throwable); }
			}, executor);
			return completionFuture;
		}
	}

	private static final class Serial extends BaseLink {
		final AsyncOpLink first;
		final AsyncOpLink second;

		Serial(AsyncOpLink first, AsyncOpLink second, ListeningExecutorService executor) {
			super(executor);

			this.first = first;
			this.second = second;
		}

		public ListenableFuture<Void> execute() {
			return Futures.transform(first.execute(), new AsyncFunction<Void, Void>() {
				public ListenableFuture<Void> apply(Void result) throws Exception {
					return second.execute();
				}
			}, executor);
		}
	}

	private static final class Parallel extends BaseLink {
		final AsyncOpLink op1;
		final AsyncOpLink op2;

		Parallel(AsyncOpLink op1, AsyncOpLink op2, ListeningExecutorService executor) {
			super(executor);

			this.op1 = op1;
			this.op2 = op2;
		}

		@SuppressWarnings("unchecked")
		public ListenableFuture<Void> execute() {
			final SettableFuture<Void> bothFinished = SettableFuture.create();
			Futures.addCallback(
				Futures.allAsList(op1.execute(), op2.execute()),
				new FutureCallback<List<Void>>() {
					public void onSuccess(List<Void> results) {
						bothFinished.set(null);
					}
					public void onFailure(Throwable throwable) {
						bothFinished.setException(throwable);
					}
				},
				executor
			);
			return bothFinished;
		}
	}

	private static final class Await extends BaseLink {
		final AsyncOpLink first;
		final long duration;
		final TimeUnit unit;

		Await(AsyncOpLink first, long duration, TimeUnit unit, ListeningExecutorService executor) {
			super(executor);

			this.first = first;
			this.duration = duration;
			this.unit = unit;
		}

		public ListenableFuture<Void> execute() {
			final ListenableFuture<?> opFuture = first.execute();
			final SettableFuture<Void> resultFuture = SettableFuture.create();
			executor.submit(new Runnable() {
				public void run() {
					try {
						opFuture.get(duration, unit);
						resultFuture.set(null);
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						opFuture.cancel(true);
						resultFuture.setException(e);
					}
				}
			});
			return resultFuture;
		}
	}
}
