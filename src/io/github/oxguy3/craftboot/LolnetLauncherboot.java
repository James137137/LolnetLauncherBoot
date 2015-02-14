/**
 * CraftBoot, a bootstrapper for SKCraft Launcher Copyright (C) 2014 Hayden
 * Schiff <http://oxguy3.github.io> and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.oxguy3.craftboot;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import lombok.Getter;
import lombok.extern.java.Log;

@Log
public class LolnetLauncherboot {

    @Getter
    private static File dataDir;

    static final String LAUNCHER_CLASS_NAME = "com.skcraft.launcher.Launcher";
    public static final String ForceUpdateCheck = "LolnetLauncherbootstrapInstalled-1.0.1";

    private static String defaultDirectory() {
        String OS = System.getProperty("os.name").toUpperCase();
        if (OS.contains("WIN")) {
            return System.getenv("APPDATA");
        } else if (OS.contains("MAC")) {
            return System.getProperty("user.home") + "/Library/Application "
                    + "Support" + "/";
        } else if (OS.contains("NUX")) {
            return System.getProperty("user.home");
        }
        return System.getProperty("user.dir");
    }

    /**
     * Does most of the everything
     *
     * @param args arguments (not used)
     */
    public static void main(String[] args) {
        File launcher = null;
        boolean downloadLatest = false;
        try {
            launcher = new File(LolnetLauncherboot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (URISyntaxException ex) {
            Logger.getLogger(LolnetLauncherboot.class.getName()).log(Level.SEVERE, null, ex);
        }
        Preferences userNodeForPackage = java.util.prefs.Preferences.userRoot();

        if (userNodeForPackage.get(ForceUpdateCheck, "") == null || !userNodeForPackage.get(ForceUpdateCheck, "").equals("true")) {
            downloadLatest = true;
        }
        userNodeForPackage.put("LolnetLauncherbootstrap", "true");
        userNodeForPackage.put(ForceUpdateCheck, "true");
        if (launcher != null) {
            userNodeForPackage.put("LolnetLauncherbootstrapLocation", launcher.getAbsolutePath());
        }

        System.out.println(userNodeForPackage.get("LolnetLauncherbootstrap", ""));
        String dataLocation = userNodeForPackage.get("LolnetLauncherDataPath", "");
        String getSnapshotVersion = userNodeForPackage.get("DownloadSnapShot", "");

        if (dataLocation == null || dataLocation.length() == 0 || !(new File(dataLocation).exists())) {
            dataLocation = defaultDirectory() + File.separator + "LolnetData/";
        }

        dataDir = new File(dataLocation);
        File launcherDir = new File(dataDir, "launcher");
        if (!dataDir.exists() || !launcherDir.exists()) {
            dataDir.mkdirs();
            launcherDir.mkdir();
        }

        File[] launcherPacks = launcherDir.listFiles();
        if (launcherPacks == null || launcherPacks.length == 0 || downloadLatest) {
            boolean didDownload = new LauncherDownloader().downloadLauncher(false);
            if (!didDownload) {
                log.severe("Failed to download launcher! Shutting down...");
                System.exit(1);
            }
            launcherPacks = launcherDir.listFiles();
        } else if (getSnapshotVersion != null && getSnapshotVersion.equals("true")) {
            userNodeForPackage.put("DownloadSnapShot", "");
            boolean didDownload = new LauncherDownloader().downloadLauncher(true);
            if (!didDownload) {
                log.severe("Failed to download launcher! Shutting down...");
                System.exit(1);
            }
            launcherPacks = launcherDir.listFiles();
        }

        ArrayList<LauncherJar> launcherPackFiles = new ArrayList<LauncherJar>(launcherPacks.length);

        for (int i = 0; i < launcherPacks.length; i++) {
            File f = launcherPacks[i];
            if (f.isFile()) {
                LauncherJar pf = LauncherJar.makePackFile(f);
                if (pf == null) {
                    log.warning("Skipped incorrectly formatted launcher pack file: " + launcherPacks[i].getName());
                } else {
                    launcherPackFiles.add(pf);
                }
            }
        }
        launcherPackFiles.trimToSize();

        // determine which LauncherJar is newest
        LauncherJar newestPackFile = null;
        for (LauncherJar pf : launcherPackFiles) {
            if (newestPackFile == null || pf.getFileName() > newestPackFile.getFileName()) {
                newestPackFile = pf;
            }
        }

        if (newestPackFile == null) {
            log.severe("No valid jar files! Shutting down...");
            System.exit(1);
        }

        if (newestPackFile.isPacked()) {
            boolean didUnpack = false;

            didUnpack = newestPackFile.unpackJar();

            if (!didUnpack) {
                log.severe("Failed to unpack jar file! Shutting down...");
                System.exit(1);
            }
        }
        try {
            runLauncherJar(newestPackFile);
        } catch (Exception e) {
            log.severe("Failed to run launcher jar!");
            e.printStackTrace();
        }
    }

    /**
     * Attempts to run a given LauncherJar
     *
     * @param jar the LauncherJar to run (must not be packed!)
     * @throws ClassNotFoundException if the launcher jar lacks the right class
     * @throws SecurityException see Class.getConstructor()
     * @throws NoSuchMethodException see Class.getConstructor()
     * @throws InvocationTargetException see Constructor.newInstance()
     * @throws IllegalArgumentException see Constructor.newInstance()
     * @throws IllegalAccessException see Constructor.newInstance()
     * @throws InstantiationException see Constructor.newInstance()
     */
    public static void runLauncherJar(LauncherJar jar) throws ClassNotFoundException,
            NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {

        URL[] jarUrls = new URL[1];
        try {
            jarUrls[0] = jar.getFile().toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            log.severe("Could not convert File to URL! Shutting down...");
            System.exit(1);
        }

        ClassLoader loader = URLClassLoader.newInstance(jarUrls);
        try {
            Class<?> launcherClass = loader.loadClass(LAUNCHER_CLASS_NAME);
            Method launcherMethod = launcherClass.getDeclaredMethod("main", Class.forName("[Ljava.lang.String;"));

            String[] args = {};

            launcherMethod.invoke(null, new Object[]{args});
        } catch (Exception e) {
            jar.getFile().deleteOnExit();
            String replaceAll = jar.getFile().getAbsolutePath().replaceAll(".jar", ".jar.pack");
            new File(replaceAll).deleteOnExit();
        }

    }
}
