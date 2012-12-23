package com.broodsoft.nioext.op.client.io;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public final class AsyncWrite<T> extends AsyncIoOp<T> {
	public AsyncWrite(AsynchronousSocketChannel channel, IoProcessor<T> processor) {
		super(channel, processor);
	}

	public ListenableFuture<T> execute(final SettableFuture<T> resultFuture) {
		ByteBuffer firstBuffer = processor.buffer();
		channel.write(firstBuffer, firstBuffer, new CompletionHandler<Integer, ByteBuffer>() {
			public void completed(Integer bytesWritten, ByteBuffer buffer) {
				buffer.clear();
				if(processor.process(bytesWritten, buffer)) {
					ByteBuffer nextBuffer = processor.buffer();
					channel.write(nextBuffer, nextBuffer, this);
				} else {
					resultFuture.set(processor.result());
				}
			}

			public void failed(Throwable throwable, ByteBuffer buffer) {
				resultFuture.setException(throwable);
			}
		});
		return resultFuture;
	}
}
