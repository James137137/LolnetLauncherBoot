package io.github.oxguy3.craftboot;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author James
 */
public class LolnetLookAndFeel {

    File dataDir;
    String type;

    LolnetLookAndFeel(File dataDir) {

        this.dataDir = dataDir;
        this.type = type;
        Preferences userNodeForPackage = java.util.prefs.Preferences.userRoot();
        if (userNodeForPackage.get("LolnetLauncherSkin", "") == null) {
            userNodeForPackage.put("LolnetLauncherSkin", "default");
            return;
        }

        downloadDefaults();
        setupLookAndFeel(new File(dataDir.getAbsolutePath() + File.separator + "themes" + File.separator + userNodeForPackage.get("LolnetLauncherSkin", "") + ".loltheme"));
    }

    public void downloadDefaults() {

        File dir = new File(dataDir, "themes");
        if (!dir.exists()) {
            dir.mkdir();
        }
        for (String theme : getThemes()) {
            try {
                URL website = new URL("https://www.lolnet.co.nz/modpack/themes/" + theme);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());

                FileOutputStream fos = new FileOutputStream(dataDir.getAbsolutePath() + File.separator + "themes" + File.separator + theme);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (MalformedURLException ex) {
                Logger.getLogger(LolnetLookAndFeel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LolnetLookAndFeel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(LolnetLookAndFeel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private static List<String> getThemes() {
        List<String> publicList = new ArrayList<String>();
        try {
            URL url = new URL("https://www.lolnet.co.nz/modpack/" + "listthemes.php");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            while ((line = rd.readLine()) != null) {
                String[] split = line.split("~~");
                for (String string : split) {
                    if (string.length() >= 2) {
                        publicList.add(string);
                    }
                }
            }
            wr.close();
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
            return publicList;
        }

        return publicList;
    }

    public Properties loadParams(File f) {
        if (f == null || !f.exists()) {
            return null;
        }
        Properties props = new Properties();
        InputStream is = null;

        // First try loading from the current directory
        try {
            is = new FileInputStream(f);
        } catch (Exception e) {
            return null;
        }

        try {
            if (is == null) {
                // Try loading from classpath
                is = getClass().getResourceAsStream("server.properties");
            }

            // Try loading properties from the file (if found)
            props.load(is);
        } catch (Exception e) {
        }
        return props;
    }

    public void saveParamChanges(Properties props) {
        try {
            File f = new File(dataDir + File.separator + "SkinLookAndFeel.properties");
            OutputStream out = new FileOutputStream(f);
            props.store(out, "This is an optional header comment string");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupLookAndFeel(File file) {

        Properties props = loadParams(file);
        if (props == null) {
            return;
        }

        // Set your theme
        com.jtattoo.plaf.noire.NoireLookAndFeel.setCurrentTheme(props);

        try {
            // select the look and feel

            UIManager.setLookAndFeel(com.jtattoo.plaf.noire.NoireLookAndFeel.class.getName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LolnetLookAndFeel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(LolnetLookAndFeel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(LolnetLookAndFeel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(LolnetLookAndFeel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
