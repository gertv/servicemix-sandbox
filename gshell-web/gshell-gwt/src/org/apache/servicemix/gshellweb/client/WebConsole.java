/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.gshellweb.client;

import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WebConsole implements EntryPoint {

    private static Console console;

    public void onModuleLoad() {
        installKeyHook();
    }

    public static native void installKeyHook() /*-{
        function keyHandler(e)
        {
            var keyCode = $wnd.document.all ? $wnd.event.keyCode : e.which;
            if( keyCode == 126 ) {
                @org.apache.servicemix.gshellweb.client.WebConsole::toggleConsole()();
                return false;
            }
            return true;
        }
        $wnd.document.onkeypress = keyHandler;
    }-*/;

    public static void toggleConsole() {
        if (console == null) {
            console = new Console();
            console.setOutputHTML("Welcome to the ServiceMix Web Console\n\n");
            console.setPromptHTML("<b>servicemix&gt;</b> ");
            console.show();
        } else {
            console.toggleShowing();
        }
    }


}
