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
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;

import com.krohinc.ui.util.PopupWindow;
import com.krohinc.ui.util.TextAreaFocusTraversalModifier;

/**
 * <p>
 * Example application to demonstrate the usage of
 * PopupWindow. Inspired by the SMS button on Goolge Voice.
 * 
 * <p>
 * @author akroh
 */
public class PopupWindowExample extends JPanel
{
    private final PopupWindow popupWindow;
    
    public PopupWindowExample()
    {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        popupWindow = buildPopupWindow();

        final JButton smsButton = new JButton();
        smsButton.setAction(new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Show the popup 5 pixels below the button:
                popupWindow.show(smsButton, 0, smsButton.getHeight() + 5);
            }
            private static final long serialVersionUID = 1L;
        });
        smsButton.setText("<html><b>SMS</b></html>");
        
        add(smsButton);
    }
    
    private PopupWindow buildPopupWindow()
    {
        final PopupWindow popup = new PopupWindow();
        popup.setLayout(new GridLayout(7, 1, 0, 0));
        
        Border compound = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK), 
                BorderFactory.createEmptyBorder(5, 5, 5, 5));
        popup.setBorder(compound);
        
        popup.add(new JLabel("<html>Send an SMS for free:</html>"));
        popup.add(new JLabel("<html><b>To</b></html>"));
        popup.add(new JTextField());
        popup.add(new JLabel("<html>Enter a number</html>"));
        popup.add(new JLabel("<html><b>Message</b></html>"));

        JTextArea textArea = new JTextArea();
        TextAreaFocusTraversalModifier.invertFocusTraversalBehaviour(textArea);
        popup.add(textArea);
        
        JButton sendButton = new JButton(new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                popup.setVisible(false);
            }
            private static final long serialVersionUID = 1L;
        });
        sendButton.setFocusable(false);
        sendButton.setText("Send");
        
        popup.add(sendButton);
        popup.pack();
        
        return popup;
    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from
     * the event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("PopupWindow Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Add content to the window.
        frame.add(new PopupWindowExample(), BorderLayout.CENTER);
        
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
