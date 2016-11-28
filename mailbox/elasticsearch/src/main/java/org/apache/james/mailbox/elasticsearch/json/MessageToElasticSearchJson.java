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

package org.apache.james.mailbox.elasticsearch.json;

import java.time.ZoneId;
import java.util.List;

import javax.inject.Inject;
import javax.mail.Flags;

import org.apache.james.mailbox.MailboxSession.User;
import org.apache.james.mailbox.elasticsearch.IndexAttachments;
import org.apache.james.mailbox.extractor.TextExtractor;
import org.apache.james.mailbox.store.mail.model.MailboxMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Preconditions;

public class MessageToElasticSearchJson {

    private final ObjectMapper mapper;
    private final TextExtractor textExtractor;
    private final ZoneId zoneId;

    public MessageToElasticSearchJson(TextExtractor textExtractor, ZoneId zoneId) {
        this.textExtractor = textExtractor;
        this.zoneId = zoneId;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new GuavaModule());
        this.mapper.registerModule(new Jdk8Module());
    }

    @Inject
    public MessageToElasticSearchJson(TextExtractor textExtractor) {
        this(textExtractor, ZoneId.systemDefault());
    }

    public String convertToJson(MailboxMessage message, List<User> users) throws JsonProcessingException {
        Preconditions.checkNotNull(message);
        return mapper.writeValueAsString(IndexableMessage.from(message, users, textExtractor, zoneId, IndexAttachments.NO));
    }

    public String getUpdatedJsonMessagePart(Flags flags, long modSeq) throws JsonProcessingException {
        Preconditions.checkNotNull(flags);
        return mapper.writeValueAsString(new MessageUpdateJson(flags, modSeq));
    }

}
