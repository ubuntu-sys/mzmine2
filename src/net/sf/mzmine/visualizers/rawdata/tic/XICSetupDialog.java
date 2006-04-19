/**
 * 
 */
package net.sf.mzmine.visualizers.rawdata.tic;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.mzmine.userinterface.mainwindow.MainWindow;

class XICSetupDialog extends JDialog implements
        ActionListener {

    private JTextField txtXicMZ;
    private JTextField txtXicMZDelta;

    private JButton okBtn;
    private JButton cancelBtn;

    private int exitCode;

    /**
     * Constructor
     */
    public XICSetupDialog(String title,
            double _ricMZ, double _ricMZDelta) {
        super(MainWindow.getInstance(), title, true);
        exitCode = 0;


        // MZ value
        JLabel ricMZLabel = new JLabel("MZ");
        // txtXicMZ= new JTextField(new
        // String(strFormatter.format(_ricMZ)));
        txtXicMZ = new JTextField("" + java.lang.Math.round(_ricMZ * 1000)
                / 1000.0);

        // MZ delta value
        JLabel ricMZDeltaLabel = new JLabel("MZ delta");
        // txtXicMZDelta= new JTextField(new
        // String(strFormatter.format(_ricMZDelta)));
        txtXicMZDelta = new JTextField("" + _ricMZDelta);

        JPanel fields = new JPanel();
        fields.setLayout(new GridLayout(2, 2));
        fields.add(ricMZLabel);
        fields.add(txtXicMZ);
        fields.add(ricMZDeltaLabel);
        fields.add(txtXicMZDelta);

        // Buttons
        JPanel btnPanel = new JPanel();
        okBtn = new JButton("OK");
        cancelBtn = new JButton("Cancel");

        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        okBtn.addActionListener(this);
        cancelBtn.addActionListener(this);

        // Add it
        getContentPane().add(fields, BorderLayout.CENTER);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(MainWindow.getInstance());
        // setPosition(_mainWin.getX()+_mainWin.getWidth()/2,
        // _mainWin.getY()+_mainWin.getHeight()/2);
        /*
         * setSize(512, 256); setResizable( true );
         */
        pack();
    }

    public void actionPerformed(java.awt.event.ActionEvent ae) {
        Object src = ae.getSource();
        if (src == okBtn) {
            exitCode = 1;
            setVisible(false);
        }
        if (src == cancelBtn) {
            exitCode = -1;
            setVisible(false);
        }

    }

    public double getXicMZ() {
        String s = txtXicMZ.getText();
        double d;
        try {
            d = Double.parseDouble(s);
        } catch (NumberFormatException exe) {
            return -1;
        }
        return d;
    }

    public double getXicMZDelta() {
        String s = txtXicMZDelta.getText();
        double d;
        try {
            d = Double.parseDouble(s);
        } catch (NumberFormatException exe) {
            return -1;
        }
        return d;
    }

    public int getExitCode() {
        return exitCode;
    }
}