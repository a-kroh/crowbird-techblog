/*
 * Copyright 2009 Andrew Kroh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.krohinc.ui.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import com.krohinc.ui.util.TextAreaFocusTraversalModifier;

/**
 * <p>
 * This is an example to demonstrate the usage of
 * the TextAreaTraversalModifier.
 * 
 * @author Andrew Kroh
 */
public class TextAreaTraversalExample extends JPanel
{
    public TextAreaTraversalExample()
    {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Default TextArea", buildDefaultBehaviorPanel());
        tabbedPane.addTab("Modified TextArea", buildModifiedBehaviorPanel());

        tabbedPane.setPreferredSize(new Dimension(400, 200));
        add(tabbedPane);
        add(new JLabel("From http://blog.crowbird.com"), BorderLayout.PAGE_END);
    }
    
    private JComponent buildDefaultBehaviorPanel()
    {
        JTextArea textArea = new JTextArea();
        
        JPanel panel = new JPanel(new GridLayout(6, 1, 5, 5));
        panel.add(new JLabel("TextField:"));
        panel.add(new JTextField());
        panel.add(new JLabel("Default Behavior:"));
        panel.add(textArea);
        panel.add(new JLabel("TextField:"));
        panel.add(new JTextField());
        
        return panel;
    }
    
    private JComponent buildModifiedBehaviorPanel()
    {
        JTextArea textArea = new JTextArea();
        TextAreaFocusTraversalModifier.invertFocusTraversalBehaviour(textArea);
        
        JPanel panel = new JPanel(new GridLayout(6, 1, 5, 5));
        panel.add(new JLabel("TextField:"));
        panel.add(new JTextField());
        panel.add(new JLabel("Modified Behavior:    (To insert a tab use CTRL+TAB.)"));
        panel.add(textArea);
        panel.add(new JLabel("TextField:"));
        panel.add(new JTextField());
        
        return panel;
    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from
     * the event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TextAreaFocusTraversalModifier Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Add content to the window.
        frame.add(new TextAreaTraversalExample(), BorderLayout.CENTER);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 1L;
    
    public static void main(String[] args)
    {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() 
            {
                // Add a border to the text area (for macs):
                UIDefaults uiDefaults = UIManager.getDefaults();
                uiDefaults.put("TextArea.border", uiDefaults.get("TextField.border"));
                createAndShowGUI();
            }
        });  
    }
}
