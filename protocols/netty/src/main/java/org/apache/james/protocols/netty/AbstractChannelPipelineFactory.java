/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.protocols.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * Abstract base class for {@link ChannelInitializer} implementations
 */
@ChannelHandler.Sharable
public abstract class AbstractChannelPipelineFactory<C extends SocketChannel> extends ChannelInitializer<C> {
    public static final int MAX_LINE_LENGTH = 8192;

    protected final ConnectionLimitUpstreamHandler connectionLimitHandler;
    protected final ConnectionPerIpLimitUpstreamHandler connectionPerIpLimitHandler;
    private final int timeout;
    private final ChannelHandlerFactory frameHandlerFactory;
    private final EventExecutorGroup eventExecutorGroup;

    public AbstractChannelPipelineFactory(ChannelHandlerFactory frameHandlerFactory, EventExecutorGroup eventExecutorGroup) {
        this(0, 0, 0, frameHandlerFactory, eventExecutorGroup);
    }

    public AbstractChannelPipelineFactory(int timeout, int maxConnections, int maxConnectsPerIp,
                                          ChannelHandlerFactory frameHandlerFactory, EventExecutorGroup eventExecutorGroup) {
        this.connectionLimitHandler = new ConnectionLimitUpstreamHandler(maxConnections);
        this.connectionPerIpLimitHandler = new ConnectionPerIpLimitUpstreamHandler(maxConnectsPerIp);
        this.timeout = timeout;
        this.frameHandlerFactory = frameHandlerFactory;
        this.eventExecutorGroup = eventExecutorGroup;
    }
    
    
    @Override
    protected void initChannel(C channel) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast(HandlerConstants.CONNECTION_LIMIT_HANDLER, connectionLimitHandler);

        pipeline.addLast(HandlerConstants.CONNECTION_PER_IP_LIMIT_HANDLER, connectionPerIpLimitHandler);

        
        // Add the text line decoder which limit the max line length, don't strip the delimiter and use CRLF as delimiter
        pipeline.addLast(eventExecutorGroup, HandlerConstants.FRAMER, frameHandlerFactory.create(pipeline));
       
        // Add the ChunkedWriteHandler to be able to write ChunkInput
        pipeline.addLast(HandlerConstants.CHUNK_HANDLER, new ChunkedWriteHandler());
        pipeline.addLast(HandlerConstants.TIMEOUT_HANDLER, new TimeoutHandler(timeout));

        pipeline.addLast(eventExecutorGroup, HandlerConstants.CORE_HANDLER, createHandler());
    }

    
    /**
     * Create the core {@link ChannelInboundHandlerAdapter} to use
     *
     * @return coreHandler
     */
    protected abstract ChannelInboundHandlerAdapter createHandler();

}
