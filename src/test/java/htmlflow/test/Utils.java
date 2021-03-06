/*
 * MIT License
 *
 * Copyright (c) 2014-16, Miguel Gamboa (gamboa.pt)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package htmlflow.test;

import htmlflow.HtmlWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

/**
 * @author Miguel Gamboa
 * Created on 22-01-2016.
 */
public class Utils {

    private Utils() {}

    static Element getRootElement(byte[] input) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setValidating(false);
        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(input));
        return doc.getDocumentElement();
    }

    static <T> Stream<String> html(HtmlWriter<T> view, T model){
        try(
                ByteArrayOutputStream mem = new ByteArrayOutputStream();
                PrintStream out = new PrintStream(mem))
        {
            view.setPrintStream(out).write(model);
            InputStreamReader actual = new InputStreamReader(new ByteArrayInputStream(mem.toByteArray()));
            return new BufferedReader(actual).lines();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Stream<String> loadLines(String path) {
        try{
            InputStream in = TestDivDetails.class
                    .getClassLoader()
                    .getResource(path)
                    .openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            return reader.lines().onClose(asUncheckedRunnable(reader));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Convert a Closeable to a Runnable by converting checked IOException
     * to UncheckedIOException
     */
    private static Runnable asUncheckedRunnable(Closeable c) {
        return () -> {
            try {
                c.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

}
