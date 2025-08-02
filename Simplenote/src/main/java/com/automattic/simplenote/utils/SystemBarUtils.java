package com.automattic.simplenote.utils;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import androidx.annotation.ColorInt;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Utility class to handle system bar appearance changes in a way that's compatible
 * with Android 15's deprecated APIs for statusBarColor and navigationBarColor.
 */
public class SystemBarUtils {

    /**
     * Sets the status bar color in a way that's compatible across Android versions.
     * On Android 15+, this becomes a no-op as the API is deprecated.
     * 
     * @param activity The activity whose status bar color should be changed
     * @param color The color to set
     */
    @SuppressWarnings("deprecation")
    public static void setStatusBarColor(Activity activity, @ColorInt int color) {
        if (activity == null) return;
        
        Window window = activity.getWindow();
        if (window == null) return;
        
        // For Android 15+, we should use edge-to-edge design instead
        // but for backward compatibility, we'll still set the color on older versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.setStatusBarColor(color);
        }
        // On Android 15+, the deprecated setStatusBarColor is ignored
        // Apps should migrate to edge-to-edge design with WindowInsets handling
    }

    /**
     * Sets the navigation bar color in a way that's compatible across Android versions.
     * On Android 15+, this becomes a no-op as the API is deprecated.
     * 
     * @param activity The activity whose navigation bar color should be changed
     * @param color The color to set
     */
    @SuppressWarnings("deprecation")
    public static void setNavigationBarColor(Activity activity, @ColorInt int color) {
        if (activity == null) return;
        
        Window window = activity.getWindow();
        if (window == null) return;
        
        // For Android 15+, we should use edge-to-edge design instead
        // but for backward compatibility, we'll still set the color on older versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.setNavigationBarColor(color);
        }
        // On Android 15+, the deprecated setNavigationBarColor is ignored for gesture navigation
    }

    /**
     * Enables edge-to-edge display for the activity.
     * This is the recommended approach for Android 15+ instead of using deprecated system bar colors.
     * 
     * @param activity The activity to enable edge-to-edge for
     */
    public static void enableEdgeToEdge(Activity activity) {
        if (activity == null) return;
        
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
            activity.getWindow(), 
            activity.getWindow().getDecorView()
        );
        
        if (controller != null) {
            // Make status bar icons dark or light based on background
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }
    }

    /**
     * Sets the appearance of system bars (status bar and navigation bar) to be light or dark.
     * This is the modern way to control system bar appearance.
     * 
     * @param activity The activity
     * @param lightStatusBar Whether status bar content should be dark (for light backgrounds)
     * @param lightNavigationBar Whether navigation bar content should be dark (for light backgrounds)
     */
    public static void setSystemBarsAppearance(Activity activity, boolean lightStatusBar, boolean lightNavigationBar) {
        if (activity == null) return;
        
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
            activity.getWindow(), 
            activity.getWindow().getDecorView()
        );
        
        if (controller != null) {
            controller.setAppearanceLightStatusBars(lightStatusBar);
            controller.setAppearanceLightNavigationBars(lightNavigationBar);
        }
    }

    /**
     * Sets up edge-to-edge display with proper WindowInsets handling for activities with a toolbar.
     * This is a complete solution for activities that have a toolbar and content area that need
     * to respect system bars while using edge-to-edge display.
     * 
     * @param activity The activity to setup
     * @param rootView The root view of the activity layout
     * @param toolbar The toolbar that should be pushed below the status bar
     * @param contentView The main content view that should avoid navigation bar overlap
     */
    public static void setupEdgeToEdgeWithToolbar(Activity activity, View rootView, Toolbar toolbar, View contentView) {
        if (activity == null || rootView == null) return;
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        
        // Set light status bar appearance for better visibility
        setSystemBarsAppearance(activity, true, true);
        
        // Apply insets to the root view first - handles horizontal system bars (notches/cutouts)
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Apply horizontal insets to root view to handle notches/cutouts
            v.setPadding(
                systemBars.left, 
                0, // Don't apply top padding here - toolbar will handle it
                systemBars.right, 
                0  // Don't apply bottom padding here - content view will handle it
            );
            
            // Pass insets to children for specific handling
            return windowInsets;
        });

        // Handle toolbar insets - make sure it doesn't overlap with status bar
        if (toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
                Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                
                // Apply top margin to toolbar to push it below status bar
                ViewGroup.MarginLayoutParams toolbarParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                if (toolbarParams != null) {
                    toolbarParams.topMargin = systemBars.top;
                    v.setLayoutParams(toolbarParams);
                }
                
                // Continue passing insets down
                return windowInsets;
            });
        }

        // Handle content view insets - avoid overlap with navigation bar
        if (contentView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, windowInsets) -> {
                Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                
                // Apply bottom padding to avoid navigation bar overlap
                v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(), 
                    v.getPaddingRight(),
                    systemBars.bottom
                );
                
                // Consume the insets so they don't affect child views unnecessarily
                return WindowInsetsCompat.CONSUMED;
            });
        }
    }

    /**
     * Simplified edge-to-edge setup for activities without a separate toolbar.
     * Applies standard padding to avoid system bar overlaps.
     * 
     * @param activity The activity to setup
     * @param rootView The root view that should respect system bars
     */
    public static void setupEdgeToEdgeSimple(Activity activity, View rootView) {
        if (activity == null || rootView == null) return;
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        
        // Set light status bar appearance for better visibility
        setSystemBarsAppearance(activity, true, true);
        
        // Apply standard system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            v.setPadding(
                systemBars.left,
                systemBars.top, 
                systemBars.right,
                systemBars.bottom
            );
            
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * Sets up edge-to-edge display with custom system bar appearance.
     * 
     * @param activity The activity to setup
     * @param rootView The root view of the activity layout
     * @param toolbar The toolbar that should be pushed below the status bar
     * @param contentView The main content view that should avoid navigation bar overlap
     * @param lightStatusBar Whether status bar content should be dark (for light backgrounds)
     * @param lightNavigationBar Whether navigation bar content should be dark (for light backgrounds)
     */
    public static void setupEdgeToEdgeWithToolbar(Activity activity, View rootView, Toolbar toolbar, View contentView, boolean lightStatusBar, boolean lightNavigationBar) {
        if (activity == null || rootView == null) return;
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        
        // Set custom status bar appearance
        setSystemBarsAppearance(activity, lightStatusBar, lightNavigationBar);
        
        // Apply insets to the root view first - handles horizontal system bars (notches/cutouts)
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Apply horizontal insets to root view to handle notches/cutouts
            v.setPadding(
                systemBars.left, 
                0, // Don't apply top padding here - toolbar will handle it
                systemBars.right, 
                0  // Don't apply bottom padding here - content view will handle it
            );
            
            // Pass insets to children for specific handling
            return windowInsets;
        });

        // Handle toolbar insets - make sure it doesn't overlap with status bar
        if (toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
                Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                
                // Apply top margin to toolbar to push it below status bar
                ViewGroup.MarginLayoutParams toolbarParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                if (toolbarParams != null) {
                    toolbarParams.topMargin = systemBars.top;
                    v.setLayoutParams(toolbarParams);
                }
                
                // Continue passing insets down
                return windowInsets;
            });
        }

        // Handle content view insets - avoid overlap with navigation bar
        if (contentView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, windowInsets) -> {
                Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                
                // Apply bottom padding to avoid navigation bar overlap
                v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(), 
                    v.getPaddingRight(),
                    systemBars.bottom
                );
                
                // Consume the insets so they don't affect child views unnecessarily
                return WindowInsetsCompat.CONSUMED;
            });
        }
    }

    /**
     * Automatically determines the appropriate status bar appearance based on the current theme.
     * Sets up edge-to-edge display with proper theming.
     * 
     * @param activity The activity to setup
     * @param rootView The root view of the activity layout
     * @param toolbar The toolbar that should be pushed below the status bar
     * @param contentView The main content view that should avoid navigation bar overlap
     */
    public static void setupEdgeToEdgeWithAutoTheming(Activity activity, View rootView, Toolbar toolbar, View contentView) {
        if (activity == null || rootView == null) return;
        
        // Determine if we're in light or dark theme
        boolean isLightTheme = isLightTheme(activity);
        
        // Setup edge-to-edge with appropriate theme
        setupEdgeToEdgeWithToolbar(activity, rootView, toolbar, contentView, isLightTheme, isLightTheme);
    }

    /**
     * Determines if the current theme is light or dark.
     * 
     * @param activity The activity to check
     * @return true if light theme, false if dark theme
     */
    private static boolean isLightTheme(Activity activity) {
        if (activity == null) return true;
        
        int nightModeFlags = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags != Configuration.UI_MODE_NIGHT_YES;
    }
}