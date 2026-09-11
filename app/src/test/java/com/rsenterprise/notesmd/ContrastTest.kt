package com.rsenterprise.notesmd

import org.junit.Test

class ContrastTest {

    private fun sRGB_to_lin(color: Int): Double {
        val c = color / 255.0
        return if (c <= 0.03928) {
            c / 12.92
        } else {
            Math.pow((c + 0.055) / 1.055, 2.4)
        }
    }

    private fun luminance(r: Int, g: Int, b: Int): Double {
        return 0.2126 * sRGB_to_lin(r) + 0.7152 * sRGB_to_lin(g) + 0.0722 * sRGB_to_lin(b)
    }

    private fun contrast(c1: IntArray, c2: IntArray): Double {
        val l1 = luminance(c1[0], c1[1], c1[2])
        val l2 = luminance(c2[0], c2[1], c2[2])
        val bright = maxOf(l1, l2)
        val dark = minOf(l1, l2)
        return (bright + 0.05) / (dark + 0.05)
    }

    private fun hexToRgb(hexStr: String): IntArray {
        val cleanHex = if (hexStr.startsWith("0xFF")) hexStr.substring(4) else hexStr
        return intArrayOf(
            cleanHex.substring(0, 2).toInt(16),
            cleanHex.substring(2, 4).toInt(16),
            cleanHex.substring(4, 6).toInt(16)
        )
    }

    @Test
    fun runContrastReport() {
        val colors = mapOf(
            "PrimaryLight" to "0xFF0969DA",
            "OnPrimaryLight" to "0xFFFFFFFF",
            "SurfaceLight" to "0xFFFFFFFF",
            "SurfaceContainerLight" to "0xFFF6F8FA",
            "SurfaceVariantLight" to "0xFFEAEFF5",
            "OnSurfaceLight" to "0xFF1F2328",
            "OnSurfaceVariantLight" to "0xFF656D76",
            "ErrorLight" to "0xFFCF222E",
            "OnSurfaceDisabledLight" to "0xFF8C959F",
            "ErrorContainerLight" to "0xFFFFEBE9",
            "OnErrorContainerLight" to "0xFF82071E",
            
            "PrimaryDark" to "0xFF58A6FF",
            "OnPrimaryDark" to "0xFF0D1117",
            "SurfaceDark" to "0xFF0D1117",
            "SurfaceContainerDark" to "0xFF161B22",
            "SurfaceVariantDark" to "0xFF21262D",
            "OnSurfaceDark" to "0xFFC9D1D9",
            "OnSurfaceVariantDark" to "0xFF8B949E",
            "ErrorDark" to "0xFFF85149",
            "OnSurfaceDisabledDark" to "0xFF484F58",
            "ErrorContainerDark" to "0xFF3C1618",
            "OnErrorContainerDark" to "0xFFFFA198",

            "CodeInlineBgLight" to "0xFFEFF1F3",
            "CodeInlineTextLight" to "0xFF0969DA",
            "CodeInlineBgDark" to "0xFF22272E",
            "CodeInlineTextDark" to "0xFF79C0FF"
        )

        val pairs = listOf(
            "OnPrimaryLight" to "PrimaryLight",
            "OnSurfaceLight" to "SurfaceLight",
            "OnSurfaceVariantLight" to "SurfaceLight",
            "OnSurfaceLight" to "SurfaceContainerLight",
            "OnSurfaceVariantLight" to "SurfaceContainerLight",
            "ErrorLight" to "SurfaceLight",
            "OnErrorContainerLight" to "ErrorContainerLight",
            "OnSurfaceDisabledLight" to "SurfaceLight",
            "CodeInlineTextLight" to "CodeInlineBgLight",
            
            "OnPrimaryDark" to "PrimaryDark",
            "OnSurfaceDark" to "SurfaceDark",
            "OnSurfaceVariantDark" to "SurfaceDark",
            "OnSurfaceDark" to "SurfaceContainerDark",
            "OnSurfaceVariantDark" to "SurfaceContainerDark",
            "ErrorDark" to "SurfaceDark",
            "OnErrorContainerDark" to "ErrorContainerDark",
            "OnSurfaceDisabledDark" to "SurfaceDark",
            "CodeInlineTextDark" to "CodeInlineBgDark"
        )

        println("=== CONTRAST REPORT START ===")
        println("| Token Pair | Contrast Ratio | AA Normal Text (4.5:1) | AA Large Text (3.0:1) |")
        println("|---|---|---|---|")
        for ((p1, p2) in pairs) {
            val c1 = hexToRgb(colors[p1]!!)
            val c2 = hexToRgb(colors[p2]!!)
            val cr = contrast(c1, c2)
            val aaNormal = if (cr >= 4.5) "Pass" else "Fail"
            val aaLarge = if (cr >= 3.0) "Pass" else "Fail"
            println(String.format("| %s / %s | %.2f:1 | %s | %s |", p1, p2, cr, aaNormal, aaLarge))
        }
        println("=== CONTRAST REPORT END ===")
    }
}
