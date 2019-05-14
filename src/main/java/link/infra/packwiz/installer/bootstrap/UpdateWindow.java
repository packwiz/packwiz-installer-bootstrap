package link.infra.packwiz.installer.bootstrap;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class UpdateWindow {

	private JFrame frmUpdatingPackwizlauncher;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					UpdateWindow window = new UpdateWindow();
					window.frmUpdatingPackwizlauncher.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public UpdateWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmUpdatingPackwizlauncher = new JFrame();
		frmUpdatingPackwizlauncher.setTitle("Updating packwiz-launcher...");
		frmUpdatingPackwizlauncher.setBounds(100, 100, 493, 95);
		frmUpdatingPackwizlauncher.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmUpdatingPackwizlauncher.setLocationRelativeTo(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 25, 10));
		frmUpdatingPackwizlauncher.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		panel.add(progressBar, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(0, 5, 0, 5));
		frmUpdatingPackwizlauncher.getContentPane().add(panel_1, BorderLayout.EAST);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		panel_1.setLayout(gbl_panel_1);
		
		JButton btnOptions = new JButton("Cancel Update");
		btnOptions.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_btnOptions = new GridBagConstraints();
		gbc_btnOptions.gridx = 0;
		gbc_btnOptions.gridy = 0;
		panel_1.add(btnOptions, gbc_btnOptions);
		
		JButton btnCancel = new JButton("Close");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frmUpdatingPackwizlauncher.dispose();
			}
		});
		btnCancel.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 1;
		panel_1.add(btnCancel, gbc_btnCancel);
	}

}
