/*
 * Copyright 2006-2007 The MZmine Development Team
 * 
 * This file is part of MZmine.
 * 
 * MZmine is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.methods.deisotoping.simplegrouper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.mzmine.data.AlignmentResult;
import net.sf.mzmine.data.Parameter;
import net.sf.mzmine.data.ParameterSet;
import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.Parameter.ParameterType;
import net.sf.mzmine.data.impl.SimpleParameter;
import net.sf.mzmine.data.impl.SimpleParameterSet;
import net.sf.mzmine.io.OpenedRawDataFile;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.methods.Method;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.taskcontrol.TaskController;
import net.sf.mzmine.taskcontrol.TaskListener;
import net.sf.mzmine.taskcontrol.TaskSequenceListener;
import net.sf.mzmine.userinterface.Desktop;
import net.sf.mzmine.userinterface.Desktop.MZmineMenu;
import net.sf.mzmine.userinterface.dialogs.ParameterSetupDialog;
import net.sf.mzmine.userinterface.mainwindow.MainWindow;

/**
 * This class implements a simple isotopic peaks grouper method based on
 * searhing for neighbouring peaks from expected locations.
 * 
 * @version 31 March 2006
 */

public class SimpleGrouper implements Method, TaskListener,
        ListSelectionListener, ActionListener {

    public static final Parameter mzTolerance = new SimpleParameter(
            ParameterType.DOUBLE, "M/Z tolerance",
            "Maximum distance in M/Z from the expected location of a peak",
            "Da", new Double(0.05), new Double(0.0), null);

    public static final Parameter rtTolerance = new SimpleParameter(
            ParameterType.DOUBLE, "RT tolerance",
            "Maximum distance in RT from the expected location of a peak",
            "seconds", new Double(5.0), new Double(0.0), null);

    public static final Parameter monotonicShape = new SimpleParameter(
            ParameterType.BOOLEAN,
            "Monotonic shape",
            "If true, then monotonically decreasing height of isotope pattern is required (monoisotopic peak is strongest).",
            new Boolean(true));

    public static final Parameter maximumCharge = new SimpleParameter(
            ParameterType.INTEGER, "Maximum charge", "Maximum charge", "",
            new Double(1), new Double(1), null);

    private ParameterSet parameters;

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private TaskController taskController;
    private Desktop desktop;
    private JMenuItem myMenuItem;

    /**
     * @see net.sf.mzmine.main.MZmineModule#initModule(net.sf.mzmine.main.MZmineCore)
     */
    public void initModule(MZmineCore core) {

        this.taskController = core.getTaskController();
        this.desktop = core.getDesktop();

        parameters = new SimpleParameterSet(new Parameter[] { mzTolerance,
                rtTolerance, monotonicShape, maximumCharge });

        desktop.addMenuSeparator(MZmineMenu.PEAKPICKING);

        myMenuItem = desktop.addMenuItem(MZmineMenu.PEAKPICKING,
                "Simple isotopic peaks grouper", this, null, KeyEvent.VK_S,
                false, false);

        desktop.addSelectionListener(this);

    }


    public void setParameters(SimpleParameterSet parameters) {
        this.parameters = parameters;
    }

    /**
     * @see net.sf.mzmine.methods.Method#runMethod(net.sf.mzmine.data.impl.SimpleParameterSet,
     *      net.sf.mzmine.io.RawDataFile[],
     *      net.sf.mzmine.methods.alignment.AlignmentResult[])
     */
    public void runMethod(OpenedRawDataFile[] dataFiles,
            AlignmentResult[] alignmentResults) {


        logger.finest("Running " + toString());


        for (OpenedRawDataFile dataFile : dataFiles) {
            PeakList currentPeakList = (PeakList) dataFile.getCurrentFile().getData(
                    PeakList.class)[0];
            if (currentPeakList == null)
                continue;
            Task peaklistProcessorTask = new SimpleGrouperTask(
                    dataFile, currentPeakList, parameters);
            taskController.addTask(peaklistProcessorTask, this);
        }

    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {


        OpenedRawDataFile[] dataFiles = desktop.getSelectedDataFiles();

        runMethod(dataFiles, null);

    }

    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {

        OpenedRawDataFile[] dataFiles = desktop.getSelectedDataFiles();

        for (OpenedRawDataFile file : dataFiles) {
            if (file.getCurrentFile().hasData(PeakList.class)) {
                myMenuItem.setEnabled(true);
                return;
            }
        }
        myMenuItem.setEnabled(false);
    }

    public void taskStarted(Task task) {
        // do nothing
    }

    public void taskFinished(Task task) {

        if (task.getStatus() == Task.TaskStatus.FINISHED) {

            Object[] result = (Object[]) task.getResult();
            OpenedRawDataFile dataFile = (OpenedRawDataFile) result[0];
            PeakList peakList = (PeakList) result[1];
            SimpleParameterSet params = (SimpleParameterSet) result[2];

            dataFile.addHistoryEntry(dataFile.getCurrentFile().getFile(), this,
                    params);

            // Add peak list to MZmineProject
            dataFile.getCurrentFile().addData(PeakList.class, peakList);

            // Notify listeners
            desktop.notifySelectionListeners();

        } else if (task.getStatus() == Task.TaskStatus.ERROR) {
            /* Task encountered an error */
            String msg = "Error while deisotoping a file: "
                    + task.getErrorMessage();
            logger.severe(msg);
            desktop.displayErrorMessage(msg);

        }

    }

    /**
     * @see net.sf.mzmine.methods.Method#toString()
     */
    public String toString() {
        return "Simple isotopic peaks grouper";
    }

    /**
     * @see net.sf.mzmine.methods.Method#setupParameters(net.sf.mzmine.data.ParameterSet)
     */
    public ParameterSet setupParameters(ParameterSet current) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see net.sf.mzmine.methods.Method#runMethod(net.sf.mzmine.io.OpenedRawDataFile[],
     *      net.sf.mzmine.data.AlignmentResult[],
     *      net.sf.mzmine.data.ParameterSet,
     *      net.sf.mzmine.taskcontrol.TaskSequenceListener)
     */
    public void runMethod(OpenedRawDataFile[] dataFiles,
            AlignmentResult[] alignmentResults, ParameterSet parameters,
            TaskSequenceListener methodListener) {
        // TODO Auto-generated method stub

    }

    /**
     * @see net.sf.mzmine.main.MZmineModule#getParameterSet()
     */
    public ParameterSet getParameterSet() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see net.sf.mzmine.main.MZmineModule#setParameters(net.sf.mzmine.data.ParameterSet)
     */
    public void setParameters(ParameterSet parameterValues) {
        // TODO Auto-generated method stub

    }

}
