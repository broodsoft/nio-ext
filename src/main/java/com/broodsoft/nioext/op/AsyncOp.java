package com.broodsoft.nioext.op;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public interface AsyncOp<T> {
	ListenableFuture<T> execute(SettableFuture<T> resultFuture);
	ListenableFuture<T> execute();
}
