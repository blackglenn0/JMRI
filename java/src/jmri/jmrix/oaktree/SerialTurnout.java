package jmri.jmrix.oaktree;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractTurnout for oak tree serial layouts
 *
 * This object doesn't listen to the Oak Tree serial communications. This is
 * because it should be the only object that is sending messages for this
 * turnout; more than one Turnout object pointing to a single device is not
 * allowed.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006
  */
public class SerialTurnout extends AbstractTurnout {

    private OakTreeSystemConnectionMemo _memo = null;

    /**
     * Create a Turnout object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in SerialTurnoutManager
     */
    public SerialTurnout(String systemName, String userName, OakTreeSystemConnectionMemo memo) {
        super(systemName, userName);
        // Save system Name
        tSystemName = systemName;
        _memo = memo;
        // Extract the Bit from the name
        tBit = SerialAddress.getBitFromSystemName(systemName, _memo.getSystemPrefix());
    }

    /**
     * Handle a request to change state by sending a turnout command
     */
    @Override
    protected void forwardCommandChangeToLayout(int s) {
        // implementing classes will typically have a function/listener to get
        // updates from the layout, which will then call
        //  public void firePropertyChange(String propertyName,
        //                    Object oldValue,
        //      Object newValue)
        // _once_ if anything has changed state (or set the commanded state directly)

        // sort out states
        if ((s & Turnout.CLOSED) != 0) {
            // first look for the double case, which we can't handle
            if ((s & Turnout.THROWN) != 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN " + s);
                return;
            } else {
                // send a CLOSED command
                sendMessage(true ^ getInverted());
            }
        } else {
            // send a THROWN command
            sendMessage(false ^ getInverted());
        }
    }

    @Override
    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout) {
        if (log.isDebugEnabled()) {
            log.debug("Send command to {} Pushbutton", (_pushButtonLockout ? "Lock" : "Unlock"));
        }
    }

    // data members
    String tSystemName; // System Name of this turnout
    int tBit;          // bit number of turnout control in Serial node

    protected void sendMessage(boolean closed) {
        SerialNode tNode = SerialAddress.getNodeFromSystemName(tSystemName, _memo.getSystemPrefix(),_memo.getTrafficController());
        if (tNode == null) {
            // node does not exist, ignore call
            return;
        }
        tNode.setOutputBit(tBit, closed);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnout.class);

}
