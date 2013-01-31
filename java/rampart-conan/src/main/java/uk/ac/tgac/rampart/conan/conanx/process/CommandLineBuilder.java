/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package uk.ac.tgac.rampart.conan.conanx.process;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 07/01/13
 * Time: 14:07
 */
public class CommandLineBuilder {

    private List<String> preCommands;
    private List<String> postCommands;
    private String executable;

    public CommandLineBuilder(String executable) {
        this.preCommands = new ArrayList<String>();
        this.postCommands = new ArrayList<String>();
        this.executable = executable;
    }

    public String getPreCommand() {
        return StringUtils.join(preCommands, "; ");
    }

    public List<String> getPreCommands() {
        return preCommands;
    }

    public void setPreCommands(List<String> preCommands) {
        this.preCommands = preCommands;
    }

    public void addPreCommand(String preCommand) {
        if (this.preCommands == null) {
            this.preCommands = new ArrayList<String>();
        }
        this.preCommands.add(preCommand);
    }

    public String getPostCommand() {
        return StringUtils.join(postCommands, "; ");
    }

    public List<String> getPostCommands() {
        return postCommands;
    }

    public void addPostCommand(String postCommand) {
        if (this.postCommands == null) {
            this.postCommands = new ArrayList<String>();
        }
        this.postCommands.add(postCommand);
    }

    public String getFullCommand(ProcessArgs args) {
        return getFullCommand(args, false);
    }

    public String getFullCommand(ProcessArgs args, boolean build) {
        return getFullCommand(args, build, "--", " ");
    }

    /**
     * Creates a command line to execute from the supplied process args.  The are various means of constructing the command
     * line to suit various use cases.  See the parameter listings for more details.  The command line will be wrapped by
     * any pre and post commands already stored in this object.
     * @param args The process arguments from which to build a command line to execute.
     * @param build If true, the paramPrefix and keyValSep args are used to construct each arg in the following way:
     *              [paramPrefix][paramName][keyValSep][argValue]. If false, it is assumed the the arg values contains all
     *              the complete argument string.
     * @param paramPrefix The prefix to apply before each argument.  e.g. "--" is used for long posix style arguments.
     * @param keyValSep The string that separates the param name from the arg value. e.g. " " is used for posix style arguments.
     * @return A complete command to execute, including any pre or post commands.
     */
    public String getFullCommand(ProcessArgs args, boolean build, String paramPrefix, String keyValSep) {

        List<String> commands = new ArrayList<String>();

        if (this.preCommands != null && !this.preCommands.isEmpty()) {
            commands.add(this.getPreCommand());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(this.executable);
        sb.append(" ");
        for(Map.Entry<ConanParameter, String> param : args.getArgMap().entrySet()) {
            if (build) {
                sb.append(paramPrefix);
                sb.append(param.getKey());
            }
            if (!param.getKey().isBoolean()) {
                sb.append(keyValSep);
                sb.append(param.getValue());
            }
            sb.append(" ");
        }

        commands.add(sb.toString().trim());

        if (this.postCommands != null && !this.postCommands.isEmpty()) {
            commands.add(this.getPostCommand());
        }

        String command = StringUtils.join(commands, "; ");

        return command;
    }
}
