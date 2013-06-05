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

import java.awt.AWTKeyStroke;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

/**
 * <p>
 * To traverse between text fields it is common to use TAB and SHIFT+TAB
 * to move forward and backward, respectively. The default behavior for
 * JTextArea is to use CTRL+TAB and CTRL+SHIFT+TAB to move forward and
 * backward, respectively. The behavior is very annoying if JTextAreas
 * are mixed into a form with other components.
 * 
 * <p>
 * To fix the problem this class changes the behavior of JTextAreas
 * to conform with other components. After applying this class to a JTextArea
 * you can move forward and backward between all components using TAB and
 * SHIFT+TAB. To insert an actual tab (\t) use CTRL+TAB.
 *
 * <p>
 * Adapted from: http://www.javalobby.org/java/forums/t20457.html
 */
public class TextAreaFocusTraversalModifier
{
    /**
     * Switches the forward/backward traversal behavior for a JTextArea from
     * CTRL+TAB/CTRL+SHIFT+TAB to TAB/SHIFT+TAB. Tabs can be inserted to the
     * text area by using CTRL+TAB.
     * 
     * @param textArea  
     *          the JTextArea to modify the traversal behavior of
     */
    public static void invertFocusTraversalBehaviour(final JTextArea textArea)
    {
        // Get the current forward and backward traversal keys:
        Set<AWTKeyStroke> forwardKeys  = 
            textArea.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set<AWTKeyStroke> backwardKeys = 
            textArea.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
 
        // Check that we want to modify current focus traversal keystrokes:
        if (forwardKeys.size() != 1 || backwardKeys.size() != 1)
        {
            return;
        }
        
        final AWTKeyStroke fks = forwardKeys.iterator().next();
        final AWTKeyStroke bks = backwardKeys.iterator().next();
        final int fkm = fks.getModifiers();
        final int bkm = bks.getModifiers();
        final int ctrlMask      = KeyEvent.CTRL_MASK + KeyEvent.CTRL_DOWN_MASK;
        final int ctrlShiftMask = KeyEvent.SHIFT_MASK + KeyEvent.SHIFT_DOWN_MASK + ctrlMask;
        
        // Check that the current forward traversal keystroke is CTRL+TAB:
        if ( fks.getKeyCode() != KeyEvent.VK_TAB || 
             (fkm & ctrlMask) == 0 || 
             (fkm & ctrlMask) != fkm)
        {
            return;
        }
        
        // Check that the current backward traversal keystroke is CTRL+SHIFT+TAB:
        if ( bks.getKeyCode() != KeyEvent.VK_TAB || 
             (bkm & ctrlShiftMask) == 0 || 
             (bkm & ctrlShiftMask) != bkm)
        {
            return;
        }
        
        // Bind the new forward traversal keys:
        Set<AWTKeyStroke> newForwardKeys = new HashSet<AWTKeyStroke>(1);
        newForwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
        textArea.setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            Collections.unmodifiableSet(newForwardKeys)
        );
        
        // Bind the new backward traversal keys:
        Set<AWTKeyStroke> newBackwardKeys = new HashSet<AWTKeyStroke>(1);
        newBackwardKeys.add(AWTKeyStroke.getAWTKeyStroke(
            KeyEvent.VK_TAB,
            KeyEvent.SHIFT_MASK + KeyEvent.SHIFT_DOWN_MASK));
        textArea.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            Collections.unmodifiableSet(newBackwardKeys)
        );
        
        // Allow inserting tabs through CTRL+TAB:
        Action insertTabAction = new AbstractAction(){
            public void actionPerformed(ActionEvent e)
            {
                textArea.insert("\t", textArea.getCaretPosition());
            }
            
            private static final long serialVersionUID = 1L;
        };
        
        InputMap focusInputMap = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        KeyStroke ctrlTab = KeyStroke.getKeyStroke(
                KeyEvent.VK_TAB, 
                KeyEvent.CTRL_MASK + KeyEvent.CTRL_DOWN_MASK);
        focusInputMap.put(ctrlTab, insertTabAction);
    }
}
