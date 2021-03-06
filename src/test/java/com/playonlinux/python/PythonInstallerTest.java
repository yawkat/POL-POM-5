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

package com.playonlinux.python;

import com.playonlinux.MockContextConfig;
import com.playonlinux.domain.ScriptTemplate;
import com.playonlinux.framework.ScriptFailureException;

import com.playonlinux.injection.AbstractConfigFile;
import com.playonlinux.injection.InjectionException;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class PythonInstallerTest {
    @BeforeClass
    public static void setUp() throws InjectionException {
        AbstractConfigFile testConfigFile = new MockContextConfig();
        testConfigFile.setStrictLoadingPolicy(false);
        testConfigFile.load();
    }

    @Test
    public void testPythonInstaller_DefineLogContextWithMethod_ContextIsSet() throws IOException, ScriptFailureException {
        File temporaryScript = File.createTempFile("testDefineLogContext", "py");
        FileOutputStream fileOutputStream = new FileOutputStream(temporaryScript);

        fileOutputStream.write(("#!/usr/bin/env/python\n" +
                "from com.playonlinux.framework.templates import Installer\n" +
                "\n" +
                "class PlayOnLinuxBashInterpreter(Installer):\n" +
                "    def main(self):\n" +
                "        pass\n" +
                "    def logContext(self):\n" +
                "        return \"Mock Log Context\"\n").getBytes());

        Interpreter interpreter = Interpreter.createInstance();
        interpreter.execfile(temporaryScript.getAbsolutePath());
        PythonInstaller<ScriptTemplate> pythonInstaller = new PythonInstaller<>(interpreter, ScriptTemplate.class);

        assertEquals("Mock Log Context", pythonInstaller.extractLogContext());
    }


    @Test
    public void testPythonInstaller_DefineLogContextWithAttribute_ContextIsSet() throws IOException, ScriptFailureException {
        File temporaryScript = File.createTempFile("testDefineLogContext", "py");
        FileOutputStream fileOutputStream = new FileOutputStream(temporaryScript);

        fileOutputStream.write(("#!/usr/bin/env/python\n" +
                "from com.playonlinux.framework.templates import Installer\n" +
                "\n" +
                "class PlayOnLinuxBashInterpreter(Installer):\n" +
                "   logContext = \"Mock Log Context 2\"\n" +
                "   def main(self):\n" +
                "        pass\n").getBytes());

        Interpreter interpreter = Interpreter.createInstance();
        interpreter.execfile(temporaryScript.getAbsolutePath());
        PythonInstaller<ScriptTemplate> pythonInstaller = new PythonInstaller<>(interpreter, ScriptTemplate.class);

        assertEquals("Mock Log Context 2", pythonInstaller.extractLogContext());
    }


    @Test
    public void testPythonInstaller_DefineVariableAttributes_AttributesAreSet() throws IOException, ScriptFailureException {
        File temporaryOutput = new File("/tmp/testPythonInstaller_DefineVariableAttributes.log");
        temporaryOutput.deleteOnExit();

        if(temporaryOutput.exists()) {
            temporaryOutput.delete();
        }

        File temporaryScript = File.createTempFile("defineVariableAttributes", "py");
        FileOutputStream fileOutputStream = new FileOutputStream(temporaryScript);

        fileOutputStream.write(("from com.playonlinux.framework.templates import MockWineSteamInstaller\n" +
                "\n" +
                "class Example(MockWineSteamInstaller):\n" +
                "    logContext = \"testPythonInstaller_DefineVariableAttributes\"\n" +
                "    title = \"Example with Steam\"\n" +
                "    prefix = \"Prefix\"\n" +
                "    wineversion = \"1.7.34\"\n" +
                "    steamId = 130\n" +
                "    packages = [\"package1\", \"package2\"]\n").getBytes());

        Interpreter interpreter = Interpreter.createInstance();
        interpreter.execfile(temporaryScript.getAbsolutePath());
        PythonInstaller<ScriptTemplate> pythonInstaller = new PythonInstaller<>(interpreter, ScriptTemplate.class);

        pythonInstaller.exec();

        assertEquals("Implementation has to be done, but we have access to prefix (Prefix), " +
                "wineversion (1.7.34), steamId (130) and packages (['package1', 'package2'])." +
                " First package (to check that we have a list: package1\n", FileUtils.readFileToString(temporaryOutput));
    }
}