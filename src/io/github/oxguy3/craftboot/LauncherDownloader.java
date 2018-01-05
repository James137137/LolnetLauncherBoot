/**
 * CraftBoot, a bootstrapper for SKCraft Launcher
 * Copyright (C) 2014 Hayden Schiff <http://oxguy3.github.io> and contributors
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
import java.net.URL;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;

public class LauncherDownloader {

    private final static String FORCEUPDATECHECK = "https://modpack.lolnet.co.nz/forceupdate";

    public LauncherDownloader() {
    }

    /**
     * Returns the URL where the URL of the launcher download can be found
     *
     * @return the URL
     */
    public static String getLauncherUpdateUrl() {
        //TODO should use own launcher (or prompt user for URL)
        return "https://modpack.lolnet.co.nz/latest";
    }

    public static String getLauncherSnapshotUrl() {
        return "https://modpack.lolnet.co.nz/snapshot";
    }

    /**
     * Downloads the launcher to the appropriate directory
     *
     * @return true if successful
     */
    public static boolean downloadLauncher(boolean snapShot) {
        String downloadUrl;
        if (snapShot) {
            downloadUrl = CraftbootUtils.downloadTextFromUrl(getLauncherSnapshotUrl());
        } else {
            downloadUrl = CraftbootUtils.downloadTextFromUrl(getLauncherUpdateUrl());
        }

        if (downloadUrl == null) {
            return false;
        }

        File launcherFolder = new File(LolnetLauncherboot.getDataDir(), "launcher");
        File launcherJar = new File(launcherFolder, Long.toString(System.currentTimeMillis()) + CraftbootUtils.UNPACKED_EXT);

        return Downloader.download(downloadUrl, launcherJar);
    }

    public static boolean downloadLauncher(URL url) {
        if (url == null) {
            return false;
        }
        File launcherFolder = new File(LolnetLauncherboot.getDataDir(), "launcher");
        File launcherJar = new File(launcherFolder, Long.toString(System.currentTimeMillis()) + CraftbootUtils.UNPACKED_EXT);

        return Downloader.download(url.toString(), launcherJar);
    }

    public static void checkForUpdate(boolean justDownloaded) {
        if (forceUpdateDue(justDownloaded)) {
            return;
        }
    }

    private static boolean forceUpdateDue(boolean justDownloaded) {
        Preferences userNodeForPackage = java.util.prefs.Preferences.userRoot();
        String IDString = userNodeForPackage.get("LolnetLauncherForceUpdateCheck", "");
        int ID = 0;
        if (IDString != null && IDString.length() > 0) {
            ID = Integer.parseInt(IDString);
        }
        int newID = Integer.parseInt(CraftbootUtils.downloadTextFromUrl(FORCEUPDATECHECK));
        if (ID < newID || LolnetLauncherboot.updateAnyway) {
            if (!justDownloaded || LolnetLauncherboot.updateAnyway) {
                JOptionPane.showMessageDialog(null, "LolnetLauncher is due for an update", "New launcher Update", JOptionPane.INFORMATION_MESSAGE);
                downloadLauncher(false);
            }
            userNodeForPackage.put("LolnetLauncherForceUpdateCheck", "" + newID);
        }
        return false;
    }
}
