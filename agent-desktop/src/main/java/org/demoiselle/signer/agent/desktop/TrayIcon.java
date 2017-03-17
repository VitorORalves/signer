package org.demoiselle.signer.agent.desktop;

import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.cert.CertificateException;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.demoiselle.signer.agent.desktop.command.cert.ListCerts;
import org.demoiselle.signer.agent.desktop.command.cert.ListCertsRequest;
import org.demoiselle.signer.agent.desktop.command.cert.ListCertsResponse;
import org.demoiselle.signer.agent.desktop.command.signer.FileSigner;
import org.demoiselle.signer.agent.desktop.command.signer.FileSignerUsingDefaults;
import org.demoiselle.signer.agent.desktop.command.signer.SignerRequest;
import org.demoiselle.signer.agent.desktop.command.signer.SignerResponse;
import org.demoiselle.signer.agent.desktop.ui.ListCertificateData;
import org.demoiselle.signer.agent.desktop.ui.SignatureInfo;
import org.demoiselle.signer.agent.desktop.ui.pdf.SignerPDF;
import org.demoiselle.signer.agent.desktop.web.WSServer;
import org.demoiselle.signer.core.keystore.loader.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrayIcon {

	private static final Logger logger = LoggerFactory.getLogger(TrayIcon.class);

	public TrayIcon() {
		logger.info("java.version: " + System.getProperty("java.version"));
		this.makeTrayIcon();
	}

	public void errorMessage(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
	}

	public void warnMessage(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Set default font to all aplication
	 * 
	 * @param defaultFont
	 */
	private static void setUIFont(javax.swing.plaf.FontUIResource defaultFont) {
		java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				UIManager.put(key, defaultFont);
			}
		}
	}

	public void makeTrayIcon() {
		Runnable runner = new Runnable() {
			public void run() {

				setUIFont(new javax.swing.plaf.FontUIResource(new Font("Arial", Font.PLAIN, 12)));

				if (SystemTray.isSupported()) {
					final SystemTray tray = SystemTray.getSystemTray();
					
					URL urlImagem = getClass().getResource("/icone-linux.png");
					Image image = Toolkit.getDefaultToolkit().getImage(urlImagem);
					PopupMenu popup = new PopupMenu();

					// Variable to enable or disable EE features
					boolean isEnterpriseLicence = false;

					// java -DDESKTOP_LICENCE="XXX" -jar
					// ./agent-desktop-3.0.0-SNAPSHOT.jar
					String enterpriseLicence = System.getProperty("DESKTOP_LICENCE");
					if (enterpriseLicence != null)
						isEnterpriseLicence = true;

					final java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image, "Demoiselle Signer", popup);
					// popup.addSeparator();
					trayIcon.setImageAutoSize(true);
					trayIcon.setToolTip("SERPRO Signer");

					// MenuItem removeTray = new MenuItem("Remove Tray Icon");
					// removeTray.addActionListener(new ActionListener() {
					// public void actionPerformed(ActionEvent e) {
					// tray.remove(trayIcon);
					// }
					// });
					// popup.add(removeTray);
					// popup.addSeparator();

					MenuItem docSigner = new MenuItem("Assinar Arquivo");
					docSigner.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							try {
								signer();
							} catch (Throwable error) {
								logger.error("Erro ao tentar assinar um documento", error);
								logger.error("Erro visível: " + error.getMessage());
								if (error.getMessage().equals("") || error.getMessage() == null)
									errorMessage("Erro", "Um erro inesperado ocorreu, reinicie o aplicativo.");
								else
									errorMessage("Erro", error.getMessage());
							}
						}
					});
					popup.add(docSigner);

					if (isEnterpriseLicence) {
						MenuItem menuPdfSigner = new MenuItem("Assinar PDF (Adobe Acrobat)");
						menuPdfSigner.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {

								try {
									pdfSigner();
								} catch (Throwable error) {
									errorMessage("Erro", error.getMessage());
								}

							}
						});
						popup.add(menuPdfSigner);
					}

					popup.addSeparator();

					MenuItem docValidate = new MenuItem("Validar Assinatura");
					docValidate.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							try {
								validate();
							} catch (Throwable error) {
								errorMessage("Erro", error.getMessage());
							}
						}
					});
					popup.add(docValidate);

					popup.addSeparator();

					// MenuItem newDriver = new MenuItem("Adicionar driver do
					// token");
					// newDriver.addActionListener(new ActionListener() {
					// public void actionPerformed(ActionEvent e) {
					// try {
					// addDriver();
					// } catch (IOException e1) {
					// // TODO Auto-generated catch block
					// e1.printStackTrace();
					// }
					// }
					// });
					// popup.add(newDriver);

					MenuItem closeMenu = new MenuItem("Fechar");
					closeMenu.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							WSServer.getInstance().stop();
							System.exit(0);
						}
					});
					popup.add(closeMenu);

					try {
						tray.add(trayIcon);
					} catch (AWTException e) {
					}
				} else {
					JOptionPane.showMessageDialog(null, "Sistema Operacional não suporta icones na bandeija!");
				}
			}
		};
		EventQueue.invokeLater(runner);
	}

	public void signer() throws IOException {

		FileSignerUsingDefaults signer = new FileSignerUsingDefaults();

		SignerResponse resp = signer.doCommand(new SignerRequest());

		if (resp.getActionCanceled()) {
			JOptionPane.showMessageDialog(null, "Ação cancelada pelo usuário", "Cancelado",
					JOptionPane.INFORMATION_MESSAGE);
		} else if (!resp.getSigned().equals("")) {
			JOptionPane.showMessageDialog(null, "Arquivo de assinatuara disponível em: " + resp.getSigned(), "Sucesso",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "Ocorreu algum erro ao tentar assinar, tente novamente", "Falha",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	public void pdfSigner() throws Throwable {

		String fileName = "";
		String alias;

		ListCertsRequest requestCert = new ListCertsRequest();

		ListCerts ls = new ListCerts();
		ListCertsResponse lr = ls.doCommand(requestCert);
		if (lr.getCertificates().size() > 1) {
			ListCertificateData lcd = new ListCertificateData(lr);
			lcd.init();
			alias = lcd.getAlias();
		} else
			alias = lr.getCertificates().iterator().next().getAlias();

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Pdf", "pdf");
		fileChooser.addChoosableFileFilter(filter);

		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			fileName = selectedFile.getAbsolutePath();
			byte[] signature = new FileSigner().makeSignature(alias, null, fileName);
			SignerPDF pdf = new SignerPDF();
			try {
				pdf.doSigner(fileName, fileName + ".p7s.pdf", signature);

				JOptionPane.showMessageDialog(null, "Arquivo de assinatuara disponível em: " + fileName, "Sucesso",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Falha ao assinar: " + e.getMessage(), "Falha",
						JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	public void validate() throws CertificateException, IOException {
		String fileName = "";
		String signatureFileName = "";

		// Dialog with instructions to validate file
		JLabel label = new JLabel(
				"<html>Este processo de validação de assinatura permite verificar se o arquivo de conteúdo e seu arquivo no formato P7S são válidos. <br/>"
						+ "Para isso é necessário seguir os seguintes passos: <br/>"
						+ "1 - Informe o arquivo do conteúdo <br/>" + "2 - Informe o arquivo P7S <br/>"
						+ "3 - Uma janela informará se o certificado é válido ou não");
		label.setFont(new Font("Arial", Font.PLAIN, 12));

		JOptionPane.showMessageDialog(null, label, "Processo de Validação de Assinatura",
				JOptionPane.INFORMATION_MESSAGE);

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Selecione o Arquivo de Conteúdo");

		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			fileName = selectedFile.getAbsolutePath();

			fileChooser.setDialogTitle("Selecione o Arquivo de Assinatura");
			fileChooser.setFileFilter(new FileFilter() {
				public String getDescription() {
					return "Arquivo da Assinatura (.p7s)";
				}

				public boolean accept(File f) {
					return f.getName().endsWith(".p7s");
				}
			});
			returnValue = fileChooser.showOpenDialog(null);

			if (returnValue == JFileChooser.APPROVE_OPTION) {
				selectedFile = fileChooser.getSelectedFile();
				signatureFileName = selectedFile.getAbsolutePath();
				new SignatureInfo(fileName, signatureFileName).init();
			}
		}

	}

	public static void main(String[] args) throws IOException, InterruptedException {
		new TrayIcon().signer();
	}

	public void addDriver() throws IOException {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Drivers", "so", "dll", "so.1", "so.2");
		fileChooser.addChoosableFileFilter(filter);
		int returnValue = fileChooser.showOpenDialog(null);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			FileWriter prop = new FileWriter(Configuration.getConfigFilePath(), true);
			prop.write("Custom " + selectedFile.getName() + ":" + selectedFile.getAbsolutePath());
			prop.flush();
			prop.close();

		}

	}

}