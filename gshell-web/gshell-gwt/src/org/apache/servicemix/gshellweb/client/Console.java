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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.core.client.GWT;

public class Console extends PopupPanel {

    private HTML output;
    private HTML prompt;
    private TextBox input;

    public interface CommandListener {
        public void onCommand(Console source, String command);
    }

    CommandListener commandListener;
    List<String> commandHistory = new ArrayList<String>();
    int historyIndex=0;
    private boolean showing;
    private ShellServiceAsync shell;
    private State state;
    private String sessionId;

    private enum State {
        PromptLogin,
        PromptPassword,
        PromptCommand
    };
    
    public Console() {

        addStyleName("console");
        addStyleName("console-pro");
        setAnimationEnabled(true);

        FlowPanel panel = new FlowPanel();
        add(panel);
        
        // Setup the output area.
        output = new HTML();
        output.setStyleName("output");
        panel.add(output);

        // Setup the prompt Area..
        prompt = new HTML();
        prompt.setStyleName("prompt");
        input = new TextBox();
        input.setStyleName("input");
        Grid promptTable = new Grid(1, 2);
        promptTable.setWidget(0, 0, prompt);
        promptTable.getCellFormatter().setStyleName(0, 0, "prompt");
        promptTable.setWidget(0, 1, input);
        promptTable.getCellFormatter().setStyleName(0, 1, "input");
        panel.add(promptTable);
        
        input.addKeyboardListener(new KeyboardListenerAdapter() {
            public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                switch (keyCode) {
                case KEY_TAB:
                    onTab();
                    input.cancelKey();
                    break;
                case KEY_UP:
                    onUpHistory();
                    input.cancelKey();
                    break;
                case KEY_DOWN:
                    onDownHistroy();
                    input.cancelKey();
                    break;
                case KEY_ENTER:
                    onEnter();
                    input.cancelKey();
                    break;
                }
            }

        });
        
        getElement().getStyle().setProperty("margin","20px");
        getElement().getStyle().setProperty("minWidth", (Window.getClientWidth()-100)+"px");
        getElement().getStyle().setProperty("minHeight", (Window.getClientHeight()-100)+"px");
        
        Window.addWindowResizeListener(new WindowResizeListener() {
            public void onWindowResized(int width, int height) {
                getElement().getStyle().setProperty("minWidth", (Window.getClientWidth()-100)+"px");
                getElement().getStyle().setProperty("minHeight", (Window.getClientHeight()-100)+"px");
                input.getElement().scrollIntoView();
            }
        });

    }

    private void createShell() {
        println("Creating shell service...");
        shell = GWT.create(ShellService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) shell;
        String moduleRelativeURL = GWT.getModuleBaseURL() + "shell";
        endpoint.setServiceEntryPoint(moduleRelativeURL);
        println("Logging in...");
        try {
            shell.login("smx", "smx", new AsyncCallback<String>() {
                public void onFailure(Throwable throwable) {
                    println("Login failed: " + throwable);
                }

                public void onSuccess(String o) {
                    println("Login succeeded: " + o);
                    sessionId = o;
                    state = State.PromptCommand;
                    pollNextLineOut();
                    pollNextLineErr();
                }
            });
        } catch (Throwable t) {
            println("Error " + t);
        }
    }

    private void pollNextLineOut() {
        shell.getNextLineOut(sessionId, new AsyncCallback<String>() {
            public void onFailure(Throwable throwable) {
                println("getNextLineOut error: " + throwable);
            }

            public void onSuccess(String s) {
                if (s != null) {
                    println("out: " + s);
                }
                pollNextLineOut();
            }
        });
    }

    private void pollNextLineErr() {
        shell.getNextLineErr(sessionId, new AsyncCallback<String>() {
            public void onFailure(Throwable throwable) {
                println("pollNextLineErr error: " + throwable);
            }

            public void onSuccess(String s) {
                if (s != null) {
                    println("err: " + s);
                }
                pollNextLineErr();
            }
        });
    }

    protected void onTab() {
        // TODO Auto-generated method stub
        
    }

    protected void onEnter() {
        if (state != State.PromptCommand) {
            println("No session");
            return;
        }

        String command = input.getText();
        input.setText("");
        commandHistory.add(command);
        
        println(prompt.getHTML()+command);

        shell.setNextLineIn(sessionId, command, new AsyncCallback() {
            public void onFailure(Throwable throwable) {
                println("Unable to send command: " + throwable);
            }
            public void onSuccess(Object o) {
            }
        });
        /*
        if( commandListener!=null ) {
            commandListener.onCommand(this, command);
        } else {
            println("Internal Error.. no command listener configured.");
        }
        */
        
    }

    private void println(String line) {
        output.setHTML(output.getHTML()+line+"\n");
        input.getElement().scrollIntoView();
    }

    protected void onDownHistroy() {
        // TODO Auto-generated method stub
    }

    protected void onUpHistory() {
    }


    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            input.setFocus(true);
        }
    }

    public CommandListener getCommandListener() {
        return commandListener;
    }

    public void setCommandListener(CommandListener commandListener) {
        this.commandListener = commandListener;
    }

    public void setOutputHTML(String text) {
        output.setHTML(text);
    }

    public void setPromptHTML(String text) {
        prompt.setHTML(text);    
    }
    
    public void toggleShowing() {
        if (showing) {
            hide();
        } else {
            show();
        }        
    }
    
    @Override
    public void show() {
        super.show();
        input.getElement().scrollIntoView();
        input.setFocus(true);
        showing=true;
        if (shell == null) {
            createShell();
        }
    }
    
    @Override
    public void hide() {
        super.hide();
        showing=false;
    }

    public boolean isShowing() {
        return showing;
    }
}
