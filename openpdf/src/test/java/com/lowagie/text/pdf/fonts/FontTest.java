package com.lowagie.text.pdf.fonts;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.RectangleReadOnly;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfLiteral;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Font Font}-related test cases.
 *
 * @author noavarice
 * @since 1.2.7
 */
class FontTest {

    private static final Map<Integer, Predicate<Font>> STYLES_TO_TEST_METHOD = new HashMap<Integer, Predicate<Font>> () {{
        put(Font.NORMAL, f -> !f.isBold() && !f.isItalic() && !f.isStrikethru() && !f.isUnderlined());
        put(Font.BOLD, Font::isBold);
        put(Font.ITALIC, Font::isItalic);
        put(Font.UNDERLINE, Font::isUnderlined);
        put(Font.STRIKETHRU, Font::isStrikethru);
        put(Font.BOLDITALIC, f -> f.isBold() && f.isItalic());
    }};

    private static final String FONT_NAME_WITHOUT_STYLES = "non-existing-font";
    
    private static final String FONT_NAME_WITH_STYLES = "Courier";

    private static final float DEFAULT_FONT_SIZE = 16.0f;

    /**
     * Checks if style property value is preserved during font construction
     * through {@link FontFactory#getFont(String, float, int)} method by getting raw property value.
     *
     * @see Font#getStyle()
     */
    @Test
    void testStyleSettingByValue() {
        FontFactory.registerDirectories();
        for (final int style: STYLES_TO_TEST_METHOD.keySet()) { // TODO: complement tests after adding enum with font styles
            final Font font = FontFactory.getFont(FONT_NAME_WITHOUT_STYLES, DEFAULT_FONT_SIZE, style);
            assertEquals(font.getStyle(), style);
        }
    }

    /**
     * Checks if style property value is preserved during font construction
     * through {@link FontFactory#getFont(String, float, int)} method by testing appropriate predicate.
     *
     * @see Font#isBold()
     * @see Font#isItalic()
     * @see Font#isStrikethru()
     * @see Font#isUnderlined()
     */
    @Test
    void testStyleSettingByPredicate() {
        for (final int style: STYLES_TO_TEST_METHOD.keySet()) {
            final Font font = FontFactory.getFont(FONT_NAME_WITHOUT_STYLES, DEFAULT_FONT_SIZE, style);
            final Predicate<Font> p = STYLES_TO_TEST_METHOD.get(style);
            assertTrue(p.test(font));
        }
    }

    @Test
    void testFontStyleOfStyledFont() {
        for (final int style : STYLES_TO_TEST_METHOD.keySet()) {
            final Font font = FontFactory.getFont(FONT_NAME_WITH_STYLES, DEFAULT_FONT_SIZE, style);

            // For the font Courier, there is no Courier-Underline or Courier-Strikethru font available.
            if (style == Font.UNDERLINE || style == Font.STRIKETHRU) {
                assertEquals(font.getStyle(), style);
            } else {
                assertEquals(Font.NORMAL, font.getStyle());
            }
        }
    }

    /**
     * checks if the stroke width is correctly set after a text in simulated bold font is written
     * @throws Exception
     */
    @Test
    void testBoldSimulationAndStrokeWidth() throws Exception {
        FileOutputStream outputStream = new FileOutputStream("target/resultSimulatedBold.pdf");
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        // set hardcoded documentID to be able to compare the resulting document with the reference
        writer.getInfo().put(PdfName.FILEID, new PdfLiteral("[<1><2>]"));
        document.open();
        document.setPageSize(new RectangleReadOnly(200,70));
        document.newPage();
        PdfContentByte cb = writer.getDirectContentUnder();

        // Overwrite Helvetica bold with the standard Helvetica, to force simulated bold mode
        DefaultFontMapper fontMapper = new DefaultFontMapper();
        java.awt.Font font = new java.awt.Font("Helvetica", java.awt.Font.BOLD, 8);
        DefaultFontMapper.BaseFontParameters p = new DefaultFontMapper.BaseFontParameters("Helvetica");
        fontMapper.putName("Helvetica", p);
        Graphics2D graphics2D =
                cb.createGraphics(document.getPageSize().getWidth(), document.getPageSize().getHeight(), fontMapper);
        // setting the color is important to pass line 484 of PdfGraphics2D
        graphics2D.setColor(Color.BLACK);
        graphics2D.setStroke(new BasicStroke(2f));
        graphics2D.drawLine(10,10,10,50);
        graphics2D.setFont(font);
        graphics2D.drawString("Simulated Bold String", 20,30);
        graphics2D.setStroke(new BasicStroke(2f));
        graphics2D.drawLine(120,10,120,50);

        graphics2D.dispose();
        document.close();
        outputStream.close();

        File original = new File(getClass().getClassLoader().getResource("SimulatedBoldAndStrokeWidth.pdf").getFile());
        File current = new File("target/resultSimulatedBold.pdf");
        assertTrue(FileUtils.contentEquals(original, current));
    }

}
