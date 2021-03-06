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

import com.playonlinux.common.log.LogStream;
import com.playonlinux.framework.ScriptFailureException;
import org.apache.log4j.Logger;
import org.python.core.*;
import org.reflections.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

public class PythonInstaller<T> extends AbstractPythonModule<T> {
    private static final String MAIN_METHOD_NAME = "main";
    private static final String DEFINE_LOGCONTEXT_NAME = "logContext";
    private static final java.lang.String ROLLBACK_METHOD_NAME = "rollback";
    private static final java.lang.String DEFAULT_ROLLBACK_METHOD_NAME = "_defaultRollback";
    private PyObject mainInstance;
    private Logger logger = Logger.getLogger(PythonInstaller.class);

    public PythonInstaller(Interpreter pythonInterpreter, Class<T> type) {
        super(pythonInterpreter, type);
    }

    public PyType getMainClass() {
        return this.getCandidateClasses().get(0);
    }

    private PyObject getMainInstance() {
        if(mainInstance == null) {
            mainInstance = this.getMainClass().__call__();
        }
        return mainInstance;
    }

    public boolean hasMain() {
        return !(this.getCandidateClasses().isEmpty());
    }

    public void runMain(PyObject mainInstance) {
        mainInstance.invoke(MAIN_METHOD_NAME);
    }

    public String extractLogContext() throws ScriptFailureException {
        return (String) extractAttribute(DEFINE_LOGCONTEXT_NAME);
    }

    private void injectAllPythonAttributes() throws ScriptFailureException {
        Class <?> parentClass = ((PyType) ((PyType) getMainClass().getBase()).getBase()).getProxyType();

        Set<Field> fields = ReflectionUtils.getAllFields(parentClass,
                ReflectionUtils.withAnnotation(PythonAttribute.class));

        for(Field field: fields) {
            field.setAccessible(true);
            try {
                field.set(getMainInstance().__tojava__(type), this.extractAttribute(field.getName()));
            } catch (IllegalAccessException e) {
                throw new ScriptFailureException(e);
            }
        }
    }

    public void exec() throws ScriptFailureException {
        if(this.hasMain()) {
            String logContext = this.extractLogContext();
            LogStream logStream = null;

            if(logContext != null) {
                try {
                    logStream = new LogStream(logContext);
                    pythonInterpreter.setOut(logStream);
                } catch (IOException e) {
                    throw new ScriptFailureException(e);
                }
            }

            this.injectAllPythonAttributes();

            try {
                this.runMain(getMainInstance());
            } catch(Exception e) {
                logger.error("The script encountered an error. Rolling back");
                try {
                    getMainInstance().invoke(ROLLBACK_METHOD_NAME);
                } catch (Exception rollbackException) {
                    getMainInstance().invoke(DEFAULT_ROLLBACK_METHOD_NAME);
                    rollbackException.initCause(e);
                    throw rollbackException;
                }
                throw e;
            } finally {
                if(logStream != null) {
                    try {
                        logStream.flush();
                    } catch (IOException e) {
                        logger.warn("Unable to flush script log stream", e);
                    }
                }
            }
        }
    }



    public Object extractAttribute(String attributeToExtract) throws ScriptFailureException {
        PyObject pyLogAttribute;
        try {
            pyLogAttribute = getMainInstance().__getattr__(attributeToExtract);
        } catch (PyException e) {
            logger.info(String.format("The attribute %s was not found. Returning null", attributeToExtract), e);
            return null;
        }
        if (pyLogAttribute instanceof PyMethod) {
            PyObject pyReturn = getMainInstance().invoke(attributeToExtract);
            if (pyReturn != null && !(pyReturn instanceof PyNone)) {
                return pyReturn.__tojava__(Object.class);
            } else {
                return null;
            }
        } else {
            return pyLogAttribute.__tojava__(Object.class);
        }
    }
}
