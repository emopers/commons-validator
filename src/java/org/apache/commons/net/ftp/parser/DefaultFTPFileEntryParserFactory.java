package org.apache.commons.net.ftp.parser;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import org.apache.commons.net.ftp.FTPFileEntryParser;

/**
 * This is the default implementation of the 
 * FTPFileEntryParserFactory interface.  This is the 
 * implementation that will be used by
 * org.apache.commons.net.ftp.FTPClient.listFiles()
 * if no other implementation has been specified.
 * 
 * @see org.apache.commons.net.ftp.FTPClient#listFiles
 * @see org.apache.commons.net.ftp.FTPClient#setParserFactory
 */
public class DefaultFTPFileEntryParserFactory 
implements FTPFileEntryParserFactory 
{
    /**
     * This default implementation of the FTPFileEntryParserFactory
     * interface works according to the following logic:
     * First it attempts to interpret the supplied key as a fully
     * qualified classname of a class implementing the
     * FTPFileEntryParser interface.  If that succeeds, a parser 
     * object of this class is instantiated and is returned.
     * <p>
     * If <code>key</code> is not recognized as a fully qualified
     * classname known to the system, this method will then attempt
     * to see whether it <b>contains</b> a string identifying one of
     * the known parsers.  This comparison is <b>case-insensitive</b>.
     * The intent here is where possible, to select as keys strings
     * which are returned by the SYST command on the systems which
     * the corresponding parser successfully parses.  This enables 
     * this factory to be used in the auto-detection system. 
     * <p/>
     * @param key    should be a fully qualified classname corresponding to
     *               a class implementing the FTPFileEntryParser interface<br/>
     *               OR<br/>
     *               a string containing (case-insensitively) one of the
     *               following keywords:
     *               <ul>
     *               <li><code>unix</code></li>
     *               <li><code>windows</code></li>
     *               <li><code>os/2</code></li>
     *               <li><code>vms</code></li>
     *               </ul>
     * 
     * @return the FTPFileEntryParser corresponding to the supplied key.
     * @exception ParserInitializationException
     *                   thrown if for any reason the factory cannot resolve 
     *                   the supplied key into an FTPFileEntryParser.
     * @see FTPFileEntryParser
     */
    public FTPFileEntryParser createFileEntryParser(String key) 
    {
        Class parserClass = null;
        try {
            parserClass = Class.forName(key);
            return (FTPFileEntryParser) parserClass.newInstance();
        } catch (ClassNotFoundException e) {
            String ukey = null;
            if (null != key) {
                ukey = key.toUpperCase();
            }
            if (ukey.indexOf("UNIX") >= 0) {
                return new UnixFTPEntryParser();
            } else if (ukey.indexOf("VMS") >= 0) {
                return new VMSVersioningFTPEntryParser();
            } else if (ukey.indexOf("WINDOWS") >= 0) {
                return new NTFTPEntryParser();
            } else if (ukey.indexOf("OS/2") >= 0) {
                return new OS2FTPEntryParser();
            } else {
                throw new ParserInitializationException(
                    "Unknown parser type: " + key);
            }
        } catch (ClassCastException e) {
            throw new ParserInitializationException(
                parserClass.getName() 
                + " does not implement the interface " 
                + "org.apache.commons.net.ftp.FTPFileEntryParser.", e);
        } catch (Throwable e) {
            throw new ParserInitializationException(
                "Error initializing parser", e);
        }

    }
}
