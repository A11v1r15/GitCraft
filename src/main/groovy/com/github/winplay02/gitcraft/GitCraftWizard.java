package com.github.winplay02.gitcraft;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.terminal.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.github.winplay02.gitcraft.mappings.MappingFlavour;


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
            gridLayout.setHorizontalSpacing(3);

            // Mappings ComboBox
            contentPanel.addComponent(new Label("Mapping:"));
            ComboBox mappingComboBox = new ComboBox<>
            (Arrays.stream(MappingFlavour.values()).map(Object::toString).collect(Collectors.toList()))
            .setReadOnly(true)
            .setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));
            contentPanel.addComponent(mappingComboBox);

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
            contentPanel.addComponent(checkBoxList.setLayoutData(
                GridLayout.createHorizontallyFilledLayoutData(2)));

            // End buttons
            contentPanel.addComponent(
                new EmptySpace()
                        .setLayoutData(
                                GridLayout.createHorizontallyFilledLayoutData(2)));
            contentPanel.addComponent(
                new Button("Cancel", () -> {
                    cancel[0] = true;
                    window.close();
                }));
            contentPanel.addComponent(
                    new Button("Done", () -> {
                        newArgs.add("--mappings=" + mappingComboBox.getText());
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
                    return null;
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}