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

package com.krohinc.cvs;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.krohinc.cvs.CvsPassword;

/**
 * <p>
 * This is a demo app to show CVS password
 * encryption/decryption.
 * 
 * @author Andrew Kroh
 */
public class CvsPasswordUi extends JPanel
{
    private final JTextField plainTextField;
    private final JTextField cipherTextField;
    private boolean isChanging = false;
    
    public CvsPasswordUi()
    {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        plainTextField = new JTextField(20);
        plainTextField.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                setCipherText();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                setCipherText();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                setCipherText();
            }
            
            private void setCipherText()
            {
                if (!isChanging)
                {
                    isChanging = true;
                    String cipher = CvsPassword.encode(plainTextField.getText());
                    cipherTextField.setText(cipher);
                    isChanging = false;
                }
            }
        });
        
        cipherTextField = new JTextField(20);
        cipherTextField.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                setPlainText();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                setPlainText();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                setPlainText();
            }
            
            private void setPlainText()
            {
                if (!isChanging)
                {
                    isChanging = true;
                    String plain = CvsPassword.decode(cipherTextField.getText());
                    plainTextField.setText(plain);
                    isChanging = false;
                }
            }
        });
        
        add(buildCorePanel());
        add(new JLabel("From http://blog.crowbird.com"), BorderLayout.PAGE_END);
    }
    
    private JComponent buildCorePanel()
    {
        JPanel panel = new JPanel(new GridLayout(2, 4, 5, 5));
        panel.add(new JLabel("Plain Text:"));
        panel.add(plainTextField);
        panel.add(new JLabel("Cipher Text:"));
        panel.add(cipherTextField);
        
        return panel;
    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from
     * the event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("CVS Password Encrypter/Decrypter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Add content to the window.
        frame.add(new CvsPasswordUi(), BorderLayout.CENTER);
        
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
