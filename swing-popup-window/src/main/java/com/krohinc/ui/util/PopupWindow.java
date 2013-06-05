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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * <p>
 * This PopupWindow behaves similar to a JPopupMenu but
 * allows components within the popup to gain focus.
 * 
 * <p>
 * @author Andrew Kroh
 */
public class PopupWindow extends JPanel
{
    private Frame frame;
    private JWindow window;
    private JComponent invoker;
    private Point location;
    
    // Listeners:
    private final GlobalMouseEventListener globalMouseEventListener;
    private final PopupWindowFocusListener popupWindowFocusListener;
    private final PopupAncestorListener ancestorListener;
    
    public PopupWindow()
    {
        globalMouseEventListener = new GlobalMouseEventListener();
        popupWindowFocusListener = new PopupWindowFocusListener();
        ancestorListener = new PopupAncestorListener();
    }
    
    /* Causes this Window to be sized to fit the preferred size
     * and layouts of its subcomponents.  If the window and/or its owner
     * are not yet displayable, both are made displayable before
     * calculating the preferred size.  The Window will be validated
     * after the preferredSize is calculated.
     */
    public void pack()
    {
        if (window != null)
        {
            window.pack();
        }
    }
    
    @Override
    public void setVisible(boolean visible)
    {
        // No-op:
        if (visible == isVisible())
        {
            return;
        }
        
        if (visible)
        {   
            if (window != null)
            {
                uninstallListeners();
                window.setVisible(false);
                window.dispose();
            }
            
            window = createWindow();
            installListeners();
            window.setVisible(true);
        }
        else
        {
            if (window != null)
            {
                uninstallListeners();
                window.setVisible(false);
                window.dispose();
                window = null;
            }
        }
    }
    
    @Override
    public boolean isVisible()
    {
        return window != null && window.isVisible();
    }
    
    @Override
    public void setLocation(int x, int y)
    {
        if (location == null)
        {
            location = new Point(x, y);
        }
        else
        {
            location.x = x;
            location.y = y;
        }
        
        if (window != null)
        {
            window.setLocation(location);
        }
    }
    
    @Override
    public void setLocation(Point p)
    {
        if (location != p)
        {
            location = p;
            
            if (p != null && window != null)
            {
                window.setLocation(location);
            }
        }
    }
    
    /**
     * Displays the popup menu at the position x,y in the coordinate space of
     * the component invoker.
     * 
     * @param invoker
     *            the component in whose space the popup menu is to appear
     * @param x
     *            the x coordinate in invoker's coordinate space at which the
     *            popup menu is to be displayed
     * @param y
     *            the y coordinate in invoker's coordinate space at which the
     *            popup menu is to be displayed
     */
    public void show(JComponent invoker, int x, int y)
    {
        // Store the invoker for special event handling:
        this.invoker = invoker;

        Frame newFrame = getFrame(invoker);
        // If the frame has changed then hide the current
        // popup window:
        if (newFrame != frame)
        {
            // Use the invoker's frame so that events
            // are propagated properly:
            if (newFrame != null)
            {
                this.frame = newFrame;
                if (window != null)
                {
                    setVisible(false);
                }
            }
        }
        
        if (invoker != null)
        {
            Point invokerOrigin = invoker.getLocationOnScreen();
            setLocation(invokerOrigin.x + x, invokerOrigin.y + y);
        } else
        {
            setLocation(x, y);
        }
        
        setVisible(true);      
    }
    
    private class GlobalMouseEventListener implements AWTEventListener
    {
        public void eventDispatched(AWTEvent event)
        {
            if (event instanceof MouseEvent)
            {
                MouseEvent mouseEvent = (MouseEvent)event;
                
                if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED)
                {
                    Component c = mouseEvent.getComponent();
                    
                    if (c == null || c == invoker) {
                        return;
                    }
                    
                    Component component = SwingUtilities.getDeepestComponentAt(c, mouseEvent.getX(), mouseEvent.getY());
                    
                    if (!isAncestorOf(component, window))
                    {
                        setVisible(false);
                    }
                }
            }
        }
        
        public boolean isAncestorOf(Component component, Object ancestor) 
        {
            if (component == null) 
            {
                return false;
            }
    
            for (Component p = component; p != null; p = p.getParent()) 
            {
                if (p == ancestor) {
                    return true;
                }
            }
            return false;
        } 
    }

    private class PopupWindowFocusListener implements WindowFocusListener
    {
        public void windowGainedFocus(WindowEvent e)
        {
            // Ignore
        }

        public void windowLostFocus(WindowEvent e)
        {
            setVisible(false);
        }
    }
    
    private class PopupAncestorListener implements AncestorListener
    {
        public void ancestorAdded(AncestorEvent event)
        {
            setVisible(false);
        }

        public void ancestorMoved(AncestorEvent event)
        {
            // Removes the popup when the invoker component moves:
            setVisible(false);
        }

        public void ancestorRemoved(AncestorEvent event)
        {
            setVisible(false);
        }
    }
    
    private static Frame getFrame(Component c) 
    {
        Component w = c;

        while(!(w instanceof Frame) && (w!=null)) {
            w = w.getParent();
        }
        return (Frame)w;
    }
    
    private JWindow createWindow()
    {
        JWindow window;
        
        if (frame != null)
        {
            window = new JWindow(frame);
        }
        else
        {
            window = new JWindow();
            System.out.println("PopupWindow will not be focusable.");
        }
        
        if (location != null)
        {
            window.setLocation(location);
        }
        
        window.setAlwaysOnTop(true);
        window.getContentPane().add(this);
        window.pack();
        
        return window;
    }
    
    private void installListeners()
    {
        Toolkit.getDefaultToolkit().addAWTEventListener(globalMouseEventListener, 
                                                        AWTEvent.MOUSE_EVENT_MASK);

        if (window != null)
        {
            window.addWindowFocusListener(popupWindowFocusListener);
        }
        
        if (invoker != null)
        {
            invoker.addAncestorListener(ancestorListener);
        }
    }
    
    private void uninstallListeners()
    {
        Toolkit.getDefaultToolkit().removeAWTEventListener(globalMouseEventListener);
        
        if (window != null)
        {
            window.removeWindowFocusListener(popupWindowFocusListener);
        }
        
        if (invoker != null)
        {
            invoker.removeAncestorListener(ancestorListener);
        }
    }
    
    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 1L;
}
