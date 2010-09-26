/*
 * Copyright 2010 netling project <http://netling.org>
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
package org.netling.ssh.connection.channel.forwarded;

import org.netling.ssh.common.Buffer;
import org.netling.ssh.common.SSHPacket;
import org.netling.ssh.connection.Connection;
import org.netling.ssh.connection.ConnectionException;
import org.netling.ssh.connection.channel.OpenFailException;
import org.netling.ssh.transport.TransportException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** Handles remote port forwarding. */
public class RemotePortForwarder
        extends AbstractForwardedChannelOpener {

    /**
     * Represents a particular forwarding. From RFC 4254, s. 7.1
     * <p/>
     * <pre>
     *    The 'address to bind' and 'port number to bind' specify the IP
     *    address (or domain name) and port on which connections for forwarding
     *    are to be accepted.  Some strings used for 'address to bind' have
     *    special-case semantics.
     *    o  &quot;&quot; means that connections are to be accepted on all protocol
     *       families supported by the SSH implementation.
     *    o  &quot;0.0.0.0&quot; means to listen on all IPv4 addresses.
     *    o  &quot;::&quot; means to listen on all IPv6 addresses.
     *    o  &quot;localhost&quot; means to listen on all protocol families supported by
     *       the SSH implementation on loopback addresses only ([RFC3330] and
     *       [RFC3513]).
     *    o  &quot;127.0.0.1&quot; and &quot;::1&quot; indicate listening on the loopback
     *       interfaces for IPv4 and IPv6, respectively.
     * </pre>
     */
    public static final class Forward {

        private final String address;
        private int port;

        /**
         * Creates this forward with address as {@code ""} and specified {@code port}.
         *
         * @param port
         */
        public Forward(int port) {
            this("", port);
        }

        /**
         * Creates this forward with specified {@code address} and port as {@code 0}.
         *
         * @param address
         */
        public Forward(String address) {
            this(address, 0);
        }

        /**
         * Creates this forward with specified {@code address} and {@code port} number.
         *
         * @param address address to bind
         * @param port    port number
         */
        public Forward(String address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass())
                return false;
            Forward other = (Forward) obj;
            return address.equals(other.address) && port == other.port;
        }

        /** @return the address represented by this forward. */
        public String getAddress() {
            return address;
        }

        /** @return the port represented by this forward. */
        public int getPort() {
            return port;
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public String toString() {
            return address + ":" + port;
        }

    }

    /** A {@code forwarded-tcpip} channel. */
    public static class ForwardedTCPIPChannel
            extends AbstractForwardedChannel {

        public static final String TYPE = "forwarded-tcpip";

        private final Forward fwd;

        public ForwardedTCPIPChannel(Connection conn, int recipient, int remoteWinSize, int remoteMaxPacketSize,
                                     Forward fwd, String origIP, int origPort) {
            super(conn, TYPE, recipient, remoteWinSize, remoteMaxPacketSize, origIP, origPort);
            this.fwd = fwd;
        }

        /** @return the forwarding from which this channel originates. */
        public Forward getParentForward() {
            return fwd;
        }

    }

    protected static final String PF_REQ = "tcpip-forward";
    protected static final String PF_CANCEL = "cancel-tcpip-forward";

    protected final Map<Forward, ConnectListener> listeners = new ConcurrentHashMap<Forward, ConnectListener>();

    public RemotePortForwarder(Connection conn) {
        super(ForwardedTCPIPChannel.TYPE, conn);
    }

    /**
     * Request forwarding from the remote host on the specified {@link Forward}. Forwarded connections will be handled
     * by supplied {@code listener}.
     * <p/>
     * If {@code forward} specifies as 0, the returned forward will have the correct port number as informed by remote
     * host.
     *
     * @param forward  the {@link Forward} to put in place on remote host
     * @param listener the listener which will next forwarded connection
     *
     * @return the {@link Forward} which was put into place on the remote host
     *
     * @throws ConnectionException if there is an error requesting the forwarding
     * @throws TransportException
     */
    public Forward bind(Forward forward, ConnectListener listener)
            throws ConnectionException, TransportException {
        SSHPacket reply = req(PF_REQ, forward);
        if (forward.port == 0)
            forward.port = reply.readInt();
        log.info("Remote end listening on {}", forward);
        listeners.put(forward, listener);
        return forward;
    }

    /**
     * Request cancellation of some forwarding.
     *
     * @param forward the forward which is being cancelled
     *
     * @throws ConnectionException if there is an error with the cancellation request
     * @throws TransportException
     */
    public void cancel(Forward forward)
            throws ConnectionException, TransportException {
        try {
            req(PF_CANCEL, forward);
        } finally {
            listeners.remove(forward);
        }
    }

    protected SSHPacket req(String reqName, Forward forward)
            throws ConnectionException, TransportException {
        final byte[] specifics = new Buffer.PlainBuffer().putString(forward.address).putInt(forward.port)
                .getCompactData();
        return conn.sendGlobalRequest(reqName, true, specifics).get(conn.getTimeout(), TimeUnit.SECONDS);
    }

    /** @return the active forwards. */
    public Set<Forward> getActiveForwards() {
        return listeners.keySet();
    }

    /**
     * Internal API. Creates a {@link ForwardedTCPIPChannel} from the {@code CHANNEL_OPEN} request and calls associated
     * {@code ConnectListener} for that forward in a separate thread.
     */
    @Override
    public void handleOpen(SSHPacket buf)
            throws ConnectionException, TransportException {
        final ForwardedTCPIPChannel chan = new ForwardedTCPIPChannel(conn, buf.readInt(), buf.readInt(), buf.readInt(),
                                                                     new Forward(buf.readString(), buf.readInt()),
                                                                     buf.readString(), buf.readInt());
        if (listeners.containsKey(chan.getParentForward()))
            callListener(listeners.get(chan.getParentForward()), chan);
        else
            chan.reject(OpenFailException.Reason.ADMINISTRATIVELY_PROHIBITED, "Forwarding was not requested on `"
                                                                              + chan.getParentForward() + "`");
    }

}
