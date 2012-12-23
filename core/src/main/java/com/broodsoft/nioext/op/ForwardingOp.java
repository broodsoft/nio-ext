package com.broodsoft.nioext.op;

import java.util.concurrent.Callable;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;

public final class ForwardingOp<T> extends AsyncOpBase<T> {
	private final Callable<T> task;
	private final ListeningExecutorService executor;

	private ForwardingOp(Callable<T> task, ListeningExecutorService executor) {
		this.task = task;
		this.executor = executor;
	}

	public ListenableFuture<T> execute(final SettableFuture<T> resultFuture) {
		Futures.addCallback(executor.submit(task), new FutureCallback<T>() {
			public void onSuccess(T result) {
				resultFuture.set(result);
			}
			public void onFailure(Throwable throwable) {
				resultFuture.setException(throwable);
			}
		});
		return resultFuture;
	}

	public static <T> AsyncOp<T> async(Callable<T> task, ListeningExecutorService executor) {
		return new ForwardingOp<T>(task, executor);
	}
}