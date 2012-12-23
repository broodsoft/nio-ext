package com.broodsoft.nioext.op;

import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public interface AsyncOpLink {
	AsyncOpLink whenDone(AsyncOpLink link, ListeningExecutorService executor);
	AsyncOpLink whenDone(AsyncOpLink link);

	AsyncOpLink then(AsyncOpLink link, ListeningExecutorService executor);
	AsyncOpLink then(AsyncOpLink link);

	AsyncOpLink await(long duration, TimeUnit unit, ListeningExecutorService executor);

	ListenableFuture<Void> execute();
}
