package com.automattic.simplenote.utils

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Utility class to handle system bar appearance changes in a way that's compatible
 * with Android 15's deprecated APIs for statusBarColor and navigationBarColor.
 */
object SystemBarUtils {
    /**
     * Sets the status bar color in a way that's compatible across Android versions.
     * On Android 15+, this becomes a no-op as the API is deprecated.
     *
     * @param activity The activity whose status bar color should be changed
     * @param color The color to set
     */
    @JvmStatic
    @Suppress("deprecation")
    fun setStatusBarColor(activity: Activity?, @ColorInt color: Int) {
        if (activity == null) return

        val window = activity.getWindow()
        if (window == null) return

        // For Android 15+, we should use edge-to-edge design instead
        // but for backward compatibility, we'll still set the color on older versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.setStatusBarColor(color)
        }
        // On Android 15+, the deprecated setStatusBarColor is ignored
        // Apps should migrate to edge-to-edge design with WindowInsets handling
    }

    /**
     * Sets the appearance of system bars (status bar and navigation bar) to be light or dark.
     * This is the modern way to control system bar appearance.
     *
     * @param activity The activity
     * @param lightStatusBar Whether status bar content should be dark (for light backgrounds)
     * @param lightNavigationBar Whether navigation bar content should be dark (for light backgrounds)
     */
    @JvmStatic
    fun setSystemBarsAppearance(activity: Activity?, lightStatusBar: Boolean, lightNavigationBar: Boolean) {
        if (activity == null) return

        val controller = WindowCompat.getInsetsController(
            activity.getWindow(),
            activity.getWindow().getDecorView()
        )

        controller.isAppearanceLightStatusBars = lightStatusBar
        controller.isAppearanceLightNavigationBars = lightNavigationBar
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
    @JvmStatic
    fun setupEdgeToEdgeWithToolbar(
        activity: Activity,
        rootView: View?,
        toolbar: Toolbar?,
        contentView: View?,
        lightStatusBar: Boolean,
        lightNavigationBar: Boolean
    ) {
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false)

        // Set custom status bar appearance
        setSystemBarsAppearance(activity, lightStatusBar, lightNavigationBar)

        rootView?.let {
            // Apply insets to the root view first - handles horizontal system bars (notches/cutouts)
            ViewCompat.setOnApplyWindowInsetsListener(it, { v: View, windowInsets: WindowInsetsCompat ->
                val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Apply horizontal insets to root view to handle notches/cutouts
                v.setPadding(
                    systemBars.left,
                    0,  // Don't apply top padding here - toolbar will handle it
                    systemBars.right,
                    0 // Don't apply bottom padding here - content view will handle it
                )
                windowInsets
            })
        }

        // Handle toolbar insets - make sure it doesn't overlap with status bar
        toolbar?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it, { v: View, windowInsets: WindowInsetsCompat ->
                val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Apply top margin to toolbar to push it below status bar
                val toolbarParams = v.getLayoutParams() as MarginLayoutParams?
                toolbarParams?.let {
                    it.topMargin = systemBars.top
                    v.setLayoutParams(it)
                }
                windowInsets
            })
        }

        // Handle content view insets - avoid overlap with navigation bar
        if (contentView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                contentView,
                { v: View, windowInsets: WindowInsetsCompat ->
                    val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                    // Apply bottom padding to avoid navigation bar overlap
                    v.setPadding(
                        v.getPaddingLeft(),
                        v.getPaddingTop(),
                        v.getPaddingRight(),
                        systemBars.bottom
                    )
                    WindowInsetsCompat.CONSUMED
                })
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
    @JvmStatic
    fun setupEdgeToEdgeWithAutoTheming(activity: Activity?, rootView: View?, toolbar: Toolbar?, contentView: View?) {
        if (activity == null || rootView == null) return

        // Determine if we're in light or dark theme
        val isLightTheme = isLightTheme(activity)

        // Setup edge-to-edge with appropriate theme
        setupEdgeToEdgeWithToolbar(activity, rootView, toolbar, contentView, isLightTheme, isLightTheme)
    }

    /**
     * Determines if the current theme is light or dark.
     *
     * @param activity The activity to check
     * @return true if light theme, false if dark theme
     */
    private fun isLightTheme(activity: Activity?): Boolean {
        if (activity == null) return true

        val nightModeFlags = activity.getResources().getConfiguration().uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags != Configuration.UI_MODE_NIGHT_YES
    }
}
