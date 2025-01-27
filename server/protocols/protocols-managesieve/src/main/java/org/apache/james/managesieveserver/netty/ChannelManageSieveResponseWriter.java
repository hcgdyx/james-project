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

package org.apache.james.managesieveserver.netty;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.james.protocols.api.CommandDetectionSession;

import io.netty.channel.Channel;
import io.netty.handler.stream.ChunkedStream;

public class ChannelManageSieveResponseWriter implements CommandDetectionSession {
    private final Channel channel;
    private String cumulation = null;
    private boolean needsCommandInjectionDetection = true;

    public ChannelManageSieveResponseWriter(Channel channel) {
        this.channel = channel;
    }

    public void write(String response) {
        if (channel.isActive()) {
            InputStream in = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
            channel.writeAndFlush(new ChunkedStream(in));
        }
    }

    @Override
    public boolean needsCommandInjectionDetection() {
        return needsCommandInjectionDetection;
    }

    @Override
    public void startDetectingCommandInjection() {
        needsCommandInjectionDetection = true;
    }

    @Override
    public void stopDetectingCommandInjection() {
        needsCommandInjectionDetection = false;
    }

    public void resetCumulation() {
        cumulation = null;
    }

    public String cumulate(String s) {
        if (cumulation == null || cumulation.equals("\r\n")) {
            cumulation = s;
        } else {
            cumulation += s;
        }
        return cumulation;
    }
}
