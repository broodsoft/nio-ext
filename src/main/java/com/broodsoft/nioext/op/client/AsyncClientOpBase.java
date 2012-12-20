package com.broodsoft.nioext.op.client;

import java.nio.channels.AsynchronousSocketChannel;

import com.broodsoft.nioext.op.AsyncOpBase;

public abstract class AsyncClientOpBase<T> extends AsyncOpBase<T> {
	protected final AsynchronousSocketChannel channel;

	protected AsyncClientOpBase(AsynchronousSocketChannel channel) {
		this.channel = channel;
	}
}
