/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

//
// NOTE: Snatched from Apache Mina
//

package org.apache.geronimo.gshell.remote.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoSessionLogger;
import org.codehaus.plexus.util.IOUtil;

/**
 * A {@link IoHandler} that adapts asynchronous MINA events to stream I/O.
 * <p/>
 * Please extend this class and implement
 * {@link #processStreamIo(IoSession,InputStream,OutputStream)} to
 * execute your stream I/O logic; <b>please note that you must forward
 * the process request to other thread or thread pool.</b>
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public abstract class StreamIoHandler
    extends IoHandlerAdapter
{
    private static final String KEY_IN = StreamIoHandler.class.getName() + ".in";

    private static final String KEY_OUT = StreamIoHandler.class.getName() + ".out";

    private int readTimeout;

    private int writeTimeout;

    protected StreamIoHandler() {
        // empty
    }

    /**
     * Implement this method to execute your stream I/O logic;
     * <b>please note that you must forward the process request to other
     * thread or thread pool.</b>
     */
    protected abstract void processStreamIo(IoSession session, InputStream in, OutputStream out);

    /**
     * Returns read timeout in seconds.
     * The default value is <tt>0</tt> (disabled).
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets read timeout in seconds.
     * The default value is <tt>0</tt> (disabled).
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Returns write timeout in seconds.
     * The default value is <tt>0</tt> (disabled).
     */
    public int getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * Sets write timeout in seconds.
     * The default value is <tt>0</tt> (disabled).
     */
    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    /**
     * Initializes streams and timeout settings.
     */
    @Override
    public void sessionOpened(IoSession session) {
        // Set timeouts
        session.getConfig().setWriteTimeout(writeTimeout);
        session.getConfig().setIdleTime(IdleStatus.READER_IDLE, readTimeout);

        // Create streams
        InputStream in = new IoSessionInputStream();
        OutputStream out = new IoSessionOutputStream(session);

        session.setAttribute(KEY_IN, in);
        session.setAttribute(KEY_OUT, out);

        processStreamIo(session, in, out);
    }

    /**
     * Closes streams
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        final InputStream in = (InputStream) session.getAttribute(KEY_IN);
        final OutputStream out = (OutputStream) session.getAttribute(KEY_OUT);

        IOUtil.close(in);
        IOUtil.close(out);
    }

    /**
     * Forwards read data to input stream.
     */
    @Override
    public void messageReceived(IoSession session, Object buf) {
        final IoSessionInputStream in = (IoSessionInputStream) session.getAttribute(KEY_IN);

        in.write((ByteBuffer) buf);
    }

    /**
     * Forwards caught exceptions to input stream.
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        final IoSessionInputStream in = (IoSessionInputStream) session.getAttribute(KEY_IN);

        IOException e = null;
        if (cause instanceof StreamIoException) {
            e = (IOException) cause.getCause();
        }
        else if (cause instanceof IOException) {
            e = (IOException) cause;
        }

        if (e != null && in != null) {
            in.throwException(e);
        }
        else {
            IoSessionLogger.warn(session, "Unexpected exception.", cause);
            session.close();
        }
    }

    /**
     * Handles read timeout.
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) {
        if (status == IdleStatus.READER_IDLE) {
            throw new StreamIoException(new SocketTimeoutException("Read timeout"));
        }
    }

    private static class StreamIoException extends RuntimeException {
        private static final long serialVersionUID = 1;

        public StreamIoException(IOException cause) {
            super(cause);
        }
    }
}
