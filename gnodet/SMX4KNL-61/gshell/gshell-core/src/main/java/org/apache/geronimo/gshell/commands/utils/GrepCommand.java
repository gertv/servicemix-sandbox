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
package org.apache.geronimo.gshell.commands.utils;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.support.OsgiCommandSupport;
import org.codehaus.plexus.util.StringUtils;

@CommandComponent(id="utils:grep", description="Print lines matching a pattern")
public class GrepCommand extends OsgiCommandSupport {

    @Argument(required=true, description="Regular expression")
    private String regex;

    @Option(name="-n", aliases = { "--line-number" }, description="Prefix each line of output with the line number within its input file.")
    private boolean lineNumber;

    @Option(name = "-v", aliases = { "--invert-match" }, description = "Invert the sense of matching, to select non-matching lines.")
    private boolean invertMatch;

    @Option(name = "-w", aliases = { "--word-regexp" }, description = "Select only those lines containing matches that form whole " +
                                                                      "words.  The test is that the matching substring must either be " +
                                                                      "at  the beginning of the line, or preceded by a non-word constituent " +
                                                                      "character.  Similarly, it must be either at the end of " +
                                                                      "the line or followed by a non-word constituent character.  " +
                                                                      "Word-constituent characters are letters, digits, and the underscore.")
    private boolean wordRegexp;

    @Option(name = "-x", aliases = { "--line-regexp" }, description = "Select only those matches that exactly match the whole line.")
    private boolean lineRegexp;

    protected Object doExecute() throws Exception {
        String regexp = regex;
        if (wordRegexp) {
            regexp = "\\b" + regexp + "\\b";
        }
        if (lineRegexp) {
            regexp = "^" + regexp + "$";
        } else {
            regexp = ".*" + regexp + ".*";
        }
        Pattern p = Pattern.compile(regexp);
        try {
            int lineno = 1;
            String line;
            while ((line = readLine(io.in)) != null) {
                if (p.matcher(line).matches() ^ invertMatch) {
                    if (lineNumber) {
                        String gutter = StringUtils.leftPad(String.valueOf(lineno), 6);
                        io.out.print(gutter);
                        io.out.print("  ");
                    }
                    io.out.println(line);
                    lineno++;
                }
            }
        } catch (IOException e) {
        }
        return null;
    }

    private String readLine(Reader in) throws IOException {
        StringBuffer buf = new StringBuffer();
        while (true) {
            int i = in.read();
            if (i == -1 && buf.length() == 0) {
                throw new IOException("break");
            }
            if (i == -1 || i == '\n' || i == '\r') {
                return buf.toString();
            }
            buf.append((char) i);
        }
    }

}
