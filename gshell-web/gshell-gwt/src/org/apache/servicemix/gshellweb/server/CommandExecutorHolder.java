package org.apache.servicemix.gshellweb.server;

import org.apache.geronimo.gshell.command.CommandExecutor;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Aug 27, 2008
 * Time: 12:23:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandExecutorHolder {

    private static CommandExecutor commandExecutor;

    public static CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public void setCommandExecutor(CommandExecutor commandExecutor) {
        CommandExecutorHolder.commandExecutor = commandExecutor;
    }
}
