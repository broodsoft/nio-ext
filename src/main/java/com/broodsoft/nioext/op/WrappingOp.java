package com.broodsoft.nioext.op;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public class WrappingOp<T> extends AsyncOpBase<T> {
	private final ListenableFuture<T> delegate;

	private WrappingOp(ListenableFuture<T> future) {
		this.delegate = future;
	}

	public ListenableFuture<T> execute() {
		return delegate;
	}

	public ListenableFuture<T> execute(final SettableFuture<T> resultFuture) {
		Futures.addCallback(delegate, new FutureCallback<T>() {
			public void onSuccess(T result) {
				resultFuture.set(result);
			}
			public void onFailure(Throwable throwable) {
				resultFuture.setException(throwable);
			}
		});
		return resultFuture;
	}

	public static <T> AsyncOp<T> wrap(ListenableFuture<T> future) {
		return new WrappingOp<>(future);
	}
}
