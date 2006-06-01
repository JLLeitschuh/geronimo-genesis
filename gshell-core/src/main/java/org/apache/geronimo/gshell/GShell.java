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

package org.apache.geronimo.gshell;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.time.StopWatch;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.command.CommandExecutor;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandManager;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.command.VariablesMap;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.commandline.CommandLineBuilder;
import org.apache.geronimo.gshell.commandline.CommandLine;
import org.apache.geronimo.gshell.util.Arguments;

/**
 * ???
 *
 * @version $Id$
 */
public class GShell
    implements CommandExecutor
{
    private static final Log log = LogFactory.getLog(GShell.class);

    private final IO io;

    private final CommandManager commandManager;

    private final CommandLineBuilder commandLineBuilder;

    private final Variables vars = new VariablesMap();

    public GShell(final IO io) throws CommandException {
        if (io == null) {
            throw new IllegalArgumentException("IO is null");
        }

        this.io = io;

        //
        // HACK: DI components...  Maybe need to setup the top-level container here
        //

        this.commandManager = new CommandManager();
        this.commandLineBuilder = new CommandLineBuilder(this);
    }
    
    public GShell() throws CommandException {
        this(new IO());
    }

    public Variables getVariables() {
        return vars;
    }

    public IO getIO() {
        return io;
    }

    public int execute(final String commandLine) throws Exception {
        assert commandLine != null;

        log.info("Executing (String): " + commandLine);

        CommandLine cl = commandLineBuilder.create(commandLine);
        cl.execute();

        //
        // TODO: Fix API to allow CL to pass back data
        //

        return 0;
    }

    //
    // CommandExecutor
    //

    public int execute(final String commandName, final String[] args) throws Exception {
        assert commandName != null;
        assert args != null;

        boolean debug = log.isDebugEnabled();

        log.info("Executing (" + commandName + "): " + Arguments.asString(args));

        //
        // TODO: Insert CommandContainer bits here
        //

        Command cmd = commandManager.getCommand(commandName);

        //
        // TODO: DI all bits if we can, then free up "context" to replace "category" as a term
        //

        final Variables parent = getVariables();

        cmd.init(new CommandContext() {
            final Variables vars = new VariablesMap(parent);

            public IO getIO() {
                return io;
            }

            public Variables getVariables() {
                return vars;
            }
        });

        // Setup command timings
        StopWatch watch = null;
        if (debug) {
            watch = new StopWatch();
            watch.start();
        }

        int status;

        try {
            status = cmd.execute(args);

            if (debug) {
                log.debug("Command completed in " + watch);
            }
        }
        finally {
            cmd.destroy();
        }

        return status;
    }

    public int execute(final String... args) throws Exception {
        assert args != null;
        assert args.length > 1;

        log.info("Executing (String[]): " + Arguments.asString(args));

        return execute(args[0], Arguments.shift(args));
    }
}
