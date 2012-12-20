package com.broodsoft.nioext.op;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public abstract class AsyncOpBase<T> implements AsyncOp<T> {
	public ListenableFuture<T> execute() {
		return execute(SettableFuture.<T>create());
	}
}
