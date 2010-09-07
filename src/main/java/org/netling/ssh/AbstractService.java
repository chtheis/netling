/*
 * Copyright 2010 Shikhar Bhushan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netling.ssh;

import org.netling.ssh.common.DisconnectReason;
import org.netling.ssh.common.Message;
import org.netling.ssh.common.SSHException;
import org.netling.ssh.common.SSHPacket;
import org.netling.ssh.transport.Transport;
import org.netling.ssh.transport.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An abstract class for {@link Service} that implements common or default functionality. */
public abstract class AbstractService
        implements Service {

    /** Logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Assigned name of this service */
    protected final String name;
    /** Transport layer */
    protected final Transport trans;
    /** Timeout for blocking operations */
    protected int timeout;

    public AbstractService(String name, Transport trans) {
        this.name = name;
        this.trans = trans;
        timeout = trans.getTimeout();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void handle(Message msg, SSHPacket buf)
            throws SSHException {
        trans.sendUnimplemented();
    }

    @Override
    public void notifyError(SSHException error) {
        log.debug("Was notified of {}", error.toString());
    }

    @Override
    public void notifyUnimplemented(long seqNum)
            throws SSHException {
        throw new SSHException(DisconnectReason.PROTOCOL_ERROR, "Unexpected: SSH_MSG_UNIMPLEMENTED");
    }

    @Override
    public void notifyDisconnect()
            throws SSHException {
        log.debug("Was notified of disconnect");
    }

    @Override
    public void request()
            throws TransportException {
        final Service active = trans.getService();
        if (!equals(active))
            if (name.equals(active.getName()))
                trans.setService(this);
            else
                trans.reqService(this);
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}
