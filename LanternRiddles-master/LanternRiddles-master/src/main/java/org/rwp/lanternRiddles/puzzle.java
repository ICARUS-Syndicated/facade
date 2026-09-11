package org.rwp.lanternRiddles;

import com.sun.jdi.connect.Connector;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import java.beans.PropertyDescriptor;

public class puzzle {
    public String question;
    public String[] options;
    public byte answer;
    public String ID;
    
    public boolean selected;
    public Pride pride;

    public puzzle(String question, String[] option, byte answer, String ID,boolean selected, Pride pride) {
        this.question = question;
        this.options = option;
        this.answer = answer;
        this.ID = ID;
        this.selected = selected;
        this.pride = pride;
    }
}

