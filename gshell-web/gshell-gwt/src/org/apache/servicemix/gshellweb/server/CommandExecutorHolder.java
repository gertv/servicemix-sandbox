package org.apache.servicemix.gshellweb.server;

import org.apache.geronimo.gshell.commandline.CommandLineExecutor;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Aug 27, 2008
 * Time: 12:23:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandExecutorHolder {

    private static CommandLineExecutor commandExecutor;

    public static CommandLineExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public void setCommandExecutor(CommandLineExecutor commandExecutor) {
        CommandExecutorHolder.commandExecutor = commandExecutor;
    }
}
