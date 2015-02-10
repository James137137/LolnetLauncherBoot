package io.github.oxguy3.craftboot;


import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class Downloader extends JFrame {

    public static final void main(String[] args) throws Exception {
        
    }

    static boolean download(String downloadUrl, File launcherJar) {
        String site = downloadUrl;
        String filename = launcherJar.getAbsolutePath();
        JFrame frm = new JFrame("Downloading Launcher");
        JProgressBar current = new JProgressBar(0, 100);
        current.setSize(100, 100);
        current.setValue(43);
        current.setStringPainted(true);
        frm.add(current);
        frm.setVisible(true);
        frm.setLayout(new FlowLayout());
        frm.setSize(200, 100);
        
        frm.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - frm.getSize().width / 2 - 50, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - frm.getSize().height / 2);
        frm.setDefaultCloseOperation(EXIT_ON_CLOSE);
        try {
            URL url = new URL(site);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int filesize = connection.getContentLength();
            float totalDataRead = 0;
            java.io.BufferedInputStream in = new java.io.BufferedInputStream(connection.getInputStream());
            java.io.FileOutputStream fos = new java.io.FileOutputStream(filename);
            java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
            byte[] data = new byte[1024];
            int i = 0;
            while ((i = in.read(data, 0, 1024)) >= 0) {
                totalDataRead = totalDataRead + i;
                bout.write(data, 0, i);
                float Percent = (totalDataRead * 100) / filesize;
                current.setValue((int) Percent);
            }
            bout.close();
            in.close();
            frm.setVisible(false);
            return true;
        } catch (Exception e) {
            javax.swing.JOptionPane.showConfirmDialog((java.awt.Component) null, e.getMessage(), "Error", javax.swing.JOptionPane.DEFAULT_OPTION);
            return false;
        }
        
    }
}
