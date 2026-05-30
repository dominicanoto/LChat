package com.messenger.client;

import javafx.scene.Parent;
import javafx.scene.control.Button;

public class ThemeService {

    private static boolean darkTheme = false;

    public static void applyTheme(
            Parent root,
            Button themeButton
    ) {

        root.getStyleClass().removeAll(
                "light-theme",
                "dark-theme"
        );

        root.getStyleClass().add(
                darkTheme
                        ? "dark-theme"
                        : "light-theme"
        );

        themeButton.setText(
                darkTheme
                        ? "☀"
                        : "☾"
        );
    }

    public static void toggleTheme(
            Parent root,
            Button themeButton
    ) {

        darkTheme = !darkTheme;

        applyTheme(
                root,
                themeButton
        );
    }
}
