package com.github.winplay02.gitcraft;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.terminal.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import com.github.winplay02.gitcraft.mappings.MappingFlavour;
import com.github.winplay02.gitcraft.manifest.MinecraftLauncherManifest;
import com.github.winplay02.gitcraft.util.MiscHelper;
import com.github.winplay02.gitcraft.util.SerializationHelper;
import com.github.winplay02.gitcraft.util.GitCraftPaths;

public class GitCraftWizard {
    public static GitCraftConfig Start() {
        boolean[] cancel = new boolean[]{false};
        GitCraftConfig defaultConfig = GitCraftConfig.defaultConfig();
        List<String> newArgs = new ArrayList<String>();
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Screen screen = null;
        try {
            screen = terminalFactory.createScreen();
            screen.startScreen();

            final WindowBasedTextGUI mainGUI = new MultiWindowTextGUI(screen);
            final WindowBasedTextGUI helpGUI = new MultiWindowTextGUI(screen);

            final Window window = new BasicWindow("GitCraft Wizard");
            window.setHints(Arrays.asList(Window.Hint.CENTERED));

            Panel contentPanel = new Panel(new GridLayout(2));
            GridLayout gridLayout = (GridLayout)contentPanel.getLayoutManager();
            gridLayout.setHorizontalSpacing(1);
            Panel comboBoxPanel = new Panel(new GridLayout(2));
            GridLayout gridLayout2 = (GridLayout)comboBoxPanel.getLayoutManager();
            gridLayout2.setHorizontalSpacing(1);

            // Help button
            /*contentPanel.addComponent(
                new Button("Help", () -> {
                    try {
                        String readmeContent = new String(Files.readAllBytes(Paths.get("readme.md")));
                        readmeContent = readmeContent.substring(readmeContent.indexOf("Options:"));
                        readmeContent = readmeContent.substring(0, readmeContent.indexOf("```"));
                        readmeContent = readmeContent.replace("      ", " ");
                        readmeContent = readmeContent.replace("    ", "  ");
                        MessageDialog helpDialog = 
                        new MessageDialogBuilder()
                        .setTitle("Help")
                        .setText(readmeContent)
                        .addButton(MessageDialogButton.OK)
                        .build();
                        helpDialog.showDialog(helpGUI);
                    } catch (IOException e) {
                        new MessageDialogBuilder()
                        .setTitle("Help")
                        .setText("Please run\ngradlew run --args=\"--help\"")
                        .addButton(MessageDialogButton.OK)
                        .build()
                        .showDialog(helpGUI);
                        e.printStackTrace();
                    }
                }).setLayoutData(
                    GridLayout.createHorizontallyEndAlignedLayoutData(2)));*/

            // Mappings ComboBox
            comboBoxPanel.addComponent(new Label("Mapping:"));
            ComboBox mappingComboBox = new ComboBox<>
            (Arrays.stream(MappingFlavour.values()).map(Object::toString).collect(Collectors.toList()))
            .setReadOnly(true)
            .setPreferredSize(new TerminalSize(15, 1));
            comboBoxPanel.addComponent(mappingComboBox);

            // Single version options
            String[] versions = getVersions();

            comboBoxPanel.addComponent(new Label("Min version:"));
            ComboBox minVersionComboBox = new ComboBox<>("")
            .setReadOnly(false)
            .setPreferredSize(new TerminalSize(15, 1));
            comboBoxPanel.addComponent(minVersionComboBox);

            comboBoxPanel.addComponent(new Label("Max version:"));
            ComboBox maxVersionComboBox = new ComboBox<>("")
            .setReadOnly(false)
            .setPreferredSize(new TerminalSize(15, 1));
            comboBoxPanel.addComponent(maxVersionComboBox);

            comboBoxPanel.addComponent(new Label("Min refresh:"));
            ComboBox minRefreshComboBox = new ComboBox<>("")
            .setReadOnly(false)
            .setPreferredSize(new TerminalSize(15, 1));
            comboBoxPanel.addComponent(minRefreshComboBox);

            comboBoxPanel.addComponent(new Label("Max refresh:"));
            ComboBox maxRefreshComboBox = new ComboBox<>("")
            .setReadOnly(false)
            .setPreferredSize(new TerminalSize(15, 1));
            comboBoxPanel.addComponent(maxRefreshComboBox);

            if (versions != null) for (String version : versions) {
                minVersionComboBox.addItem(version);
                maxVersionComboBox.addItem(version);
                minRefreshComboBox.addItem(version);
                maxRefreshComboBox.addItem(version);
            }

            // TODO: Implement logic for handling --exclude-version[=<version>[,<version>]...]
            // TODO: Implement logic for handling --fallback-mappings[=<mapping>[,<mapping>]...]
            // TODO: Implement logic for handling --only-version[=<version>[,<version>]...]
            // TODO: Implement logic for handling --override-repo-target=<path>
            // TODO: Implement logic for handling --refresh-only-version[=<version>[,<version>]...]

            contentPanel.addComponent(comboBoxPanel);

            // Boolean Options
	        CheckBoxList<String> checkBoxList = new CheckBoxList<String>();
            checkBoxList.addItem("no-assets", !defaultConfig.loadAssets);
            checkBoxList.addItem("no-external-assets", !defaultConfig.loadAssetsExtern);
            checkBoxList.addItem("no-verify", !defaultConfig.verifyChecksums);
            checkBoxList.addItem("skip-nonlinear", defaultConfig.skipNonLinear);
            checkBoxList.addItem("no-repo", defaultConfig.noRepo);
            checkBoxList.addItem("no-datapack", !defaultConfig.loadIntegratedDatapack);
            checkBoxList.addItem("no-datagen-snbt", !defaultConfig.readableNbt);
            checkBoxList.addItem("no-datagen-report", !defaultConfig.loadDatagenRegistry);
            checkBoxList.addItem("refresh", defaultConfig.refreshDecompilation);
            checkBoxList.addItem("create-version-branches", defaultConfig.createVersionBranches);
            checkBoxList.addItem("create-stable-version-branches", defaultConfig.createStableVersionBranches);
            checkBoxList.addItem("sort-json", defaultConfig.sortJsonObjects);
            checkBoxList.addItem("only-stable", defaultConfig.onlyStableReleases);
            checkBoxList.addItem("only-snapshot", defaultConfig.onlySnapshots);
            contentPanel.addComponent(checkBoxList);

            // End buttons
            contentPanel.addComponent(
                new EmptySpace()
                        .setLayoutData(
                                GridLayout.createHorizontallyFilledLayoutData(3)));
            contentPanel.addComponent(
                new Button("Cancel", () -> {
                    cancel[0] = true;
                    window.close();
                }));
            contentPanel.addComponent(
                    new Button("Done", () -> {
                        newArgs.add("--mappings=" + mappingComboBox.getText());
                        if(minVersionComboBox.getText() != "")
                            newArgs.add("--min-version=" + minVersionComboBox.getText());
                        if(maxVersionComboBox.getText() != "")
                            newArgs.add("--max-version=" + maxVersionComboBox.getText());
                        if(minRefreshComboBox.getText() != "")
                            newArgs.add("--refresh-min-version=" + minRefreshComboBox.getText());
                        if(maxRefreshComboBox.getText() != "")
                            newArgs.add("--refresh-max-version=" + maxRefreshComboBox.getText());
                        for (String item : checkBoxList.getCheckedItems()) {
                            newArgs.add("--" + item);
                        }
                        window.close();
                    }).setLayoutData(
                        GridLayout.createHorizontallyEndAlignedLayoutData(1)));
            window.setComponent(contentPanel);
            mainGUI.addWindowAndWait(window);
            return cancel[0] ? null : GitCraftCli.handleCliArgs(newArgs.toArray(new String[newArgs.size()]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(screen != null) {
                try {
                    screen.close();
                    if (newArgs.size() == 0) return null;
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    protected static String[] getVersions() {
        Path cachePath = GitCraftPaths.CURRENT_WORKING_DIRECTORY.resolve(String.format("semver-cache-%s.json", "mojang-launcher"));
        TreeMap<String, String> semverCache = null;
        if (Files.exists(cachePath)) {
            try {
                semverCache = SerializationHelper.deserialize(SerializationHelper.fetchAllFromPath(cachePath), SerializationHelper.TYPE_TREE_MAP_STRING_STRING);
            } catch (IOException e) {
                semverCache = new TreeMap<>();
                MiscHelper.println("This is not a fatal error: %s", e);
            }
        } else {
            semverCache = new TreeMap<>();
        }
        List<Map.Entry<String, String>> entryList = new ArrayList<>(semverCache.entrySet());
        Iterator<Map.Entry<String, String>> iterator = entryList.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (entry.getValue().split("\\.")[0] == "0" ||
            Integer.parseInt(entry.getValue().split("\\.")[1].replaceAll("\\D", "")) < 14) {
                iterator.remove();
            }
        }
        entryList.sort(Comparator.comparing(Map.Entry::getValue));
        Collections.reverse(entryList);
        String[] sortedArray = entryList.stream()
                                       .map(Map.Entry::getKey)
                                       .toArray(String[]::new);
        return sortedArray;
    }
}