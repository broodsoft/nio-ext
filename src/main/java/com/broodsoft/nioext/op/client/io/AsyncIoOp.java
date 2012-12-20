package com.broodsoft.nioext.op.client.io;

import java.nio.channels.AsynchronousSocketChannel;

import com.broodsoft.nioext.op.client.AsyncClientOpBase;

abstract class AsyncIoOp<T> extends AsyncClientOpBase<T> {
	protected final IoProcessor<T> processor;

	AsyncIoOp(AsynchronousSocketChannel channel, IoProcessor<T> processor) {
		super(channel);

		this.processor = processor;
	}
}
