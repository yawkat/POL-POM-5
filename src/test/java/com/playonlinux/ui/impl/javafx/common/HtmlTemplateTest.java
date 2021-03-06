/*
 * Copyright (C) 2015 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.playonlinux.ui.impl.javafx.common;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.MissingFormatArgumentException;

import static org.junit.Assert.*;

public class HtmlTemplateTest {

    private URL testTemplate;

    @Before
    public void setUp() throws IOException {
        File fileTemplate = File.createTempFile("test", "html");
        testTemplate = new URL("file://"+fileTemplate.getAbsolutePath());

        OutputStream outputStream = new FileOutputStream(fileTemplate);

        outputStream.write(
                ("<html>" +
                        "<head>" +
                        "<title>%s</title>" +
                        "</head>" +
                        "<body>" +
                        "Content: %s" +
                        "</bod>" +
                        "</html>").getBytes()
        );

        outputStream.flush();
    }

    @Test
    public void testRender_replaceTwoValues_valuesAreReplaced() throws Exception {
        HtmlTemplate htmlTemplate = new HtmlTemplate(testTemplate);

        String expected = "<html>" +
                "<head>" +
                "<title>Title</title>" +
                "</head>" +
                "<body>" +
                "Content: Content" +
                "</bod>" +
                "</html>";
        assertEquals(expected, htmlTemplate.render("Title", "Content"));
    }

    @Test(expected = MissingFormatArgumentException.class)
    public void testRender_noReplacement_exceptionThrown() throws Exception {
        HtmlTemplate htmlTemplate = new HtmlTemplate(testTemplate);

        String expected = "<html>" +
                "<head>" +
                "<title>Title</title>" +
                "</head>" +
                "<body>" +
                "Content: Content" +
                "</bod>" +
                "</html>";
        assertEquals(expected, htmlTemplate.render());
    }
}