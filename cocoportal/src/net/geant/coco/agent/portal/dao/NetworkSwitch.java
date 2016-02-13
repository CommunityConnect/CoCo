/**
 * 
 */
package net.geant.coco.agent.portal.dao;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author rvdp
 *
 */
public class NetworkSwitch extends NetworkElement {
    public NetworkSwitch() {
        
    }
    
    public NetworkSwitch(int id, String name, int x, int y) {
        super(id, name, NODE_TYPE.SWITCH);
        this.name = name;
        this.x = x;
        this.y = y;
    }

    private int id;
    
    // Name of the switch.
    @Size(min = 5, max = 100, message = "Name must be between 5 and 100 characters.")
    private String name;
    
    // X-coordinate where this switch is plotted on the portal.
    @NotNull
    private int x;
    
    // Y-coordinate where this switch is plotted on the portal.
    @NotNull
    private int y;
    
    private int mplsLabel;
    
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = "_";

        result.append(this.getName() + NEW_LINE);
        result.append(Integer.toString(this.getMplsLabel()) + NEW_LINE);
        
        return result.toString();
      }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMplsLabel() {
        return mplsLabel;
    }

    public void setMplsLabel(int mplsLabel) {
        this.mplsLabel = mplsLabel;
    }

}
