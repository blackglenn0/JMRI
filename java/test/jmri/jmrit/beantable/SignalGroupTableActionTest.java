package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.InstanceManager;
import jmri.SignalGroup;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for the jmri.jmrit.beantable.SignalGroupTableAction class
 *
 * @author	Egbert Broerse Copyright 2017
 */
public class SignalGroupTableActionTest extends AbstractTableActionBase {

    @Test
    public void testCreate() {
        Assert.assertNotNull(a);
    }

    @Override
    public String getTableFrameName() {
        return Bundle.getMessage("TitleSignalGroupTable");
    }

    @Override
    @Test
    public void testGetClassDescription() {
        Assert.assertEquals("Signal Group Table Action class description", "Signal Group Table", a.getClassDescription());
    }

    /**
     * Check the return value of includeAddButton. The table generated by this
     * action includes an Add Button.
     */
    @Override
    @Test
    public void testIncludeAddButton() {
        Assert.assertTrue("Default include add button", a.includeAddButton());
    }

    @Test
    public void testAdd() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0002)", "VM1");
        // create a Turnout
        Turnout it1 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        // create a signal head
        SignalHead sh = new jmri.implementation.SingleTurnoutSignalHead("IH1",
                new jmri.NamedBeanHandle<>("IT1", it1),
                SignalHead.LUNAR, SignalHead.DARK); // on state + off state
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(sh);

        // open Signal Group Table
        SignalGroupTableAction _sGroupTable;
        _sGroupTable = new SignalGroupTableAction();
        Assert.assertNotNull("found SignalGroupTable frame", _sGroupTable);

        _sGroupTable.addPressed(null);
        JFrame af = JFrameOperator.waitJFrame(Bundle.getMessage("AddSignalGroup"), true, true);
        Assert.assertNotNull("found Add frame", af);

        // create a new signal group
        _sGroupTable._userName.setText("TestGroup");
        Assert.assertEquals("user name", "TestGroup", _sGroupTable._userName.getText());
        _sGroupTable._systemName.setText("R1");
        Assert.assertEquals("system name", "R1", _sGroupTable._systemName.getText());
        _sGroupTable.mainSignalComboBox.setSelectedBeanByName("VM1");
        SignalGroup g = _sGroupTable.checkNamesOK();
        _sGroupTable.setValidSignalMastAspects();
        // add the head to the group:
        g.addSignalHead(sh);

        // NPE when bypassing the GUI to open an Edit Head pane: was fixed by registering sh in SignalHeadManager (line 63)
        // open Edit head pane
        SignalGroupSubTableAction editSignalHead = new SignalGroupSubTableAction();
        editSignalHead.editHead(g, "IH1");
        editSignalHead.cancelSubPressed(null); // close edit head pane

        _sGroupTable.cancelPressed(null); // calling updatePressed() complains about duplicate group name

        // clean up
        JUnitUtil.dispose(af);
        g.dispose();
        _sGroupTable.dispose();
        sh.dispose();
        JUnitUtil.dispose(editSignalHead.addSubFrame);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        a = new SignalGroupTableAction();
    }

    @After
    @Override
    public void tearDown() {
        a = null;
        JUnitUtil.tearDown();
    }
}
