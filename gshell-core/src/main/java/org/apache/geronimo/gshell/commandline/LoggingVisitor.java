/*
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.gshell.commandline;

import org.apache.geronimo.gshell.commandline.parser.CommandLineParserVisitor;
import org.apache.geronimo.gshell.commandline.parser.SimpleNode;
import org.apache.geronimo.gshell.commandline.parser.ASTCommandLine;
import org.apache.geronimo.gshell.commandline.parser.ASTExpression;
import org.apache.geronimo.gshell.commandline.parser.ASTQuotedString;
import org.apache.geronimo.gshell.commandline.parser.ASTOpaqueString;
import org.apache.geronimo.gshell.commandline.parser.ASTPlainString;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Visitor whichs logs nodes in the tree.
 *
 * @version $Id$
 */
public class LoggingVisitor
    implements CommandLineParserVisitor
{
    private final Log log;

    private int indent = 0;

    public LoggingVisitor() {
        this(LogFactory.getLog(LoggingVisitor.class));
    }

    public LoggingVisitor(final Log log) {
        if (log == null) {
            throw new IllegalArgumentException("Log is null");
        }

        this.log = log;
    }

    private Object log(final Class type, final SimpleNode node, Object data) {
        if (!log.isDebugEnabled()) {
            return data;
        }

        StringBuffer buff = new StringBuffer();

        for (int i=0; i<indent; i++) {
            buff.append(" ");
        }

        buff.append(node).append(" (").append(type.getName()).append(")");
        if (data != null) {
            buff.append("; Data: ").append(data);
        }

        //
        // TODO: Expose DEBUG/INFO switch?
        //

        log.debug(buff);

        indent++;
        data = node.childrenAccept(this, data);
        indent--;

        return data;
    }

    public Object visit(final SimpleNode node, Object data) {
        return log(SimpleNode.class, node, data);
    }

    public Object visit(final ASTCommandLine node, Object data) {
        return log(ASTCommandLine.class, node, data);
    }

    public Object visit(final ASTExpression node, Object data) {
        return log(ASTExpression.class, node, data);
    }

    public Object visit(final ASTQuotedString node, Object data) {
        return log(ASTQuotedString.class, node, data);
    }

    public Object visit(final ASTOpaqueString node, Object data) {
        return log(ASTOpaqueString.class, node, data);
    }

    public Object visit(final ASTPlainString node, Object data) {
        return log(ASTPlainString.class, node, data);
    }
}
