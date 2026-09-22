package com.websbaba.nitigrow.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// Redesign icon set — 24×24 rounded outline glyphs (2px stroke), transcribed
// from the design boards. Stroke colour is a placeholder: always render through
// `Icon(tint = …)`.
// ─────────────────────────────────────────────────────────────────────────────

object NitiIcons {
    val Bell by lazy { outline("Bell", "M6 16v-5a6 6 0 0 1 12 0v5l2 2H4zM10 21h4") }

    val Megaphone by lazy {
        outline("Megaphone", "M4 10v4h3l7 4V6L7 10H4zM17.5 9a4 4 0 0 1 0 6")
    }

    val Contacts by lazy {
        outline(
            "Contacts",
            circle(9f, 8f, 3.2f),
            "M3 20a6 6 0 0 1 12 0M16 5.2a3 3 0 0 1 0 5.6M18 14.4a6 6 0 0 1 3 5.6",
        )
    }

    /** Two-column board — leads pipeline. */
    val Leads by lazy { outline("Leads", "M4 4h6v16H4zM14 4h6v9h-6z") }

    /** ₹ glyph — payment links. */
    val Rupee by lazy { outline("Rupee", "M7 5h10M7 9h10M7 5c6 0 6 8 0 8h-.5l7 7") }

    val Chat by lazy {
        outline("Chat", "M21 12a8 8 0 0 1-11.6 7.1L4 20l1-4.6A8 8 0 1 1 21 12z")
    }

    val ArrowUpRight by lazy { outline("ArrowUpRight", "M7 17L17 7M8 7h9v9") }

    val Clock by lazy { outline("Clock", circle(12f, 12f, 9f), "M12 7v5l3 2") }

    val Send by lazy { outline("Send", "M4 12l16-8-6 16-3-7z") }

    val Home by lazy { outline("Home", "M3 11l9-8 9 8M5 10v10h5v-6h4v6h5V10") }

    val Sliders by lazy {
        outline(
            "Sliders",
            "M4 7h9M17 7h3M4 17h3M11 17h9",
            circle(15f, 7f, 2f),
            circle(9f, 17f, 2f),
        )
    }

    val Warning by lazy {
        outline("Warning", "M12 3l10 18H2zM12 10v5M12 18h.01")
    }

    val Check by lazy { outline("Check", "M5 12.5l4.5 4.5L19 7.5") }

    /** Read-receipt ticks. */
    val DoubleCheck by lazy { outline("DoubleCheck", "M2 13l4 4 8-9M10 15l2 2 9-10") }

    val Pin by lazy { outline("Pin", "M9 4h6l-1 6 3 3H7l3-3zM12 13v8") }

    val Search by lazy { outline("Search", circle(11f, 11f, 6.5f), "M20 20l-4.2-4.2") }

    val Close by lazy { outline("Close", "M6 6l12 12M18 6L6 18") }

    /** Three descending lines — filter / sort. */
    val Filter by lazy { outline("Filter", "M4 6h16M7 12h10M10 18h4") }

    val WifiOff by lazy {
        outline(
            "WifiOff",
            "M2 8.8a15 15 0 0 1 4-2.4M22 8.8a15 15 0 0 0-10-3.8M5 12.9a10 10 0 0 1 3.5-2" +
                "M19 12.9a10 10 0 0 0-3.6-2.1M8.5 16.4a5 5 0 0 1 7 0M12 20v.01M3 3l18 18",
        )
    }

    val Attach by lazy {
        outline("Attach", "M20 11l-8 8a5 5 0 0 1-7-7l9-9a3.5 3.5 0 0 1 5 5l-9 9a2 2 0 0 1-3-3l8-8")
    }

    val Lock by lazy { outline("Lock", "M5 11h14v9H5zM8 11V8a4 4 0 0 1 8 0v3") }

    /** Document with text lines — templates and files. */
    val Document by lazy { outline("Document", "M6 3h9l4 4v14H6zM14 3v5h5M9 13h7M9 17h5") }

    val Bolt by lazy { outline("Bolt", "M13 3L5 14h6l-1 7 8-11h-6z") }

    val ChevronRight by lazy { outline("ChevronRight", "M9 5l7 7-7 7") }

    val Pencil by lazy { outline("Pencil", "M4 20h4L19 9l-4-4L4 16zM13 7l4 4") }

    val Plus by lazy { outline("Plus", "M12 5v14M5 12h14") }

    val BarChart by lazy { outline("BarChart", "M4 20V4M4 20h16M8 16v-5M12 16V8M16 16v-3") }

    val Calendar by lazy {
        outline(
            "Calendar",
            "M5.5 5h13A2.5 2.5 0 0 1 21 7.5v11a2.5 2.5 0 0 1-2.5 2.5h-13A2.5 2.5 0 0 1 3 18.5v-11A2.5 2.5 0 0 1 5.5 5z" +
                "M3 10h18M8 3v4M16 3v4",
        )
    }

    val Info by lazy { outline("Info", circle(12f, 12f, 9f), "M12 11v6M12 7.5v.01") }

    val Person by lazy { outline("Person", circle(12f, 8f, 4f), "M4 20a8 8 0 0 1 16 0") }

    val Mail by lazy {
        outline(
            "Mail",
            "M5.5 5h13A2.5 2.5 0 0 1 21 7.5v9a2.5 2.5 0 0 1-2.5 2.5h-13A2.5 2.5 0 0 1 3 16.5v-9A2.5 2.5 0 0 1 5.5 5z" +
                "M3 7l9 6 9-6",
        )
    }

    val Star by lazy { outline("Star", "M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z") }

    val Refresh by lazy { outline("Refresh", "M20 8a8 8 0 0 0-14-2L4 8M4 4v4h4M4 16a8 8 0 0 0 14 2l2-2M20 20v-4h-4") }

    val Building by lazy {
        outline("Building", roundRect(3f, 7f, 18f, 13f, 2.5f), "M9 7V5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2M3 13h18")
    }

    val Card by lazy { outline("Card", roundRect(3f, 6f, 18f, 13f, 2.5f), "M3 10.5h18M7 15h4") }

    val Gift by lazy {
        outline("Gift", "M4 10h16v10H4zM3 7h18v3H3zM12 7v13M12 7c-1-3-5-3-4 0M12 7c1-3 5-3 4 0")
    }

    val Bag by lazy { outline("Bag", "M5 9h14l-1 11H6zM9 9a3 3 0 0 1 6 0") }

    val Flow by lazy { outline("Flow", "M6 4h5v5H6zM13 15h5v5h-5zM8.5 9v3a3 3 0 0 0 3 3H13") }

    val ListBullets by lazy {
        outline("ListBullets", "M8 6h13M8 12h13M8 18h13M3.5 6h.01M3.5 12h.01M3.5 18h.01")
    }

    val Speaker by lazy { outline("Speaker", "M4 9v6h4l5 4V5L8 9zM16.5 9a4 4 0 0 1 0 6") }

    val Eye by lazy { outline("Eye", "M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12z", circle(12f, 12f, 3f)) }

    val Globe by lazy {
        outline("Globe", circle(12f, 12f, 9f), "M3 12h18M12 3c3 3 3 15 0 18M12 3c-3 3-3 15 0 18")
    }

    val Palette by lazy {
        outline(
            "Palette",
            "M12 3a9 9 0 1 0 0 18c1.5 0 2-1 1.5-2s0-2 1.5-2h2a3 3 0 0 0 3-3A9 9 0 0 0 12 3z",
            circle(8f, 11f, 1f), circle(12f, 8f, 1f), circle(16f, 11f, 1f),
        )
    }

    val Upload by lazy { outline("Upload", "M12 4v11M8 8l4-4 4 4M5 14v6h14v-6") }

    val Trash by lazy { outline("Trash", "M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13M10 11v6M14 11v6") }

    val Logout by lazy { outline("Logout", "M9 4H5v16h4M16 8l4 4-4 4M20 12H9") }

    val Shield by lazy { outline("Shield", "M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z") }

    val Cursor by lazy { outline("Cursor", "M5 3l14 7-6 2-2 6z") }

    val EyeOff by lazy {
        outline("EyeOff", "M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12z", circle(12f, 12f, 3f), "M3 3l18 18")
    }

    val Key by lazy { outline("Key", circle(8f, 15f, 4f), "M11 12l9-9M16 7l3 3") }

    val Fingerprint by lazy {
        outline("Fingerprint", "M12 11v3a5 5 0 0 0 1 3M8 14a4 4 0 0 1 8 0M5 12a7 7 0 0 1 14 0M9 20c1-2 1.5-4 1.5-7")
    }

    val ArrowRight by lazy { outline("ArrowRight", "M9 5l7 7-7 7") }

    val Back by lazy { outline("Back", "M19 12H5M11 6l-6 6 6 6") }

    /** SVG path for a rounded rectangle. */
    private fun roundRect(x: Float, y: Float, w: Float, h: Float, r: Float): String =
        "M${x + r} ${y}h${w - 2 * r}a$r $r 0 0 1 $r $r" +
            "v${h - 2 * r}a$r $r 0 0 1 ${-r} $r" +
            "h${-(w - 2 * r)}a$r $r 0 0 1 ${-r} ${-r}" +
            "v${-(h - 2 * r)}a$r $r 0 0 1 $r ${-r}z"

    /** SVG path for a circle, so glyphs can be a single parsed path list. */
    private fun circle(cx: Float, cy: Float, r: Float): String =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

    private fun outline(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString(paths.joinToString("")).toNodes(),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()
}
