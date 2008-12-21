/*
    JSPWiki - a JSP-based WikiWiki clone.

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.    
 */

package com.ecyrd.jspwiki.providers;

import junit.framework.*;
import java.io.*;
import java.util.*;

import org.apache.jspwiki.api.WikiPage;

import com.ecyrd.jspwiki.*;
import com.ecyrd.jspwiki.util.FileUtil;

public class FileSystemProviderTest extends TestCase
{
    FileSystemProvider m_provider;
    FileSystemProvider m_providerUTF8;
    String             m_pagedir;
    Properties props  = new Properties();

    TestEngine         m_engine;

    public FileSystemProviderTest( String s )
    {
        super( s );
    }

    public void setUp()
        throws Exception
    {
        m_pagedir = System.getProperties().getProperty("java.io.tmpdir");

        props.setProperty( PageManager.PROP_PAGEPROVIDER, "FileSystemProvider" );
        props.setProperty( FileSystemProvider.PROP_PAGEDIR, 
                           m_pagedir );

        m_engine = new TestEngine(props);

        m_provider = new FileSystemProvider();

        m_provider.initialize( m_engine, props );
        
        props.setProperty( WikiEngine.PROP_ENCODING, "UTF-8" );
        m_providerUTF8 = new FileSystemProvider();
        m_providerUTF8.initialize( m_engine, props );
    }

    public void tearDown()
    {
        TestEngine.deleteAll( new File(m_pagedir) );
    }

    public void testScandinavianLetters()
        throws Exception
    {
        WikiPage page = m_engine.createPage("\u00c5\u00e4Test");

        m_provider.putPageText( page, "test" );
        
        File resultfile = new File( m_pagedir, "%C5%E4Test.txt" );
        
        assertTrue("No such file", resultfile.exists());
        
        String contents = FileUtil.readContents( new FileInputStream(resultfile),
                                                 "ISO-8859-1" );
        
        assertEquals("Wrong contents", contents, "test");
    }

    public void testScandinavianLettersUTF8()
        throws Exception
    {
        WikiPage page = m_engine.createPage("\u00c5\u00e4Test");

        m_providerUTF8.putPageText( page, "test\u00d6" );

        File resultfile = new File( m_pagedir, "%C3%85%C3%A4Test.txt" );

        assertTrue("No such file", resultfile.exists());

        String contents = FileUtil.readContents( new FileInputStream(resultfile),
                                                 "UTF-8" );

        assertEquals("Wrong contents", contents, "test\u00d6");
    }

    /**
     * This should never happen, but let's check that we're protected anyway.
     * @throws Exception
     */
    public void testSlashesInPageNamesUTF8()
         throws Exception
    {
        WikiPage page = m_engine.createPage("Test/Foobar");

        m_providerUTF8.putPageText( page, "test" );
        
        File resultfile = new File( m_pagedir, "Test%2FFoobar.txt" );
        
        assertTrue("No such file", resultfile.exists());
        
        String contents = FileUtil.readContents( new FileInputStream(resultfile),
                                                 "UTF-8" );
        
        assertEquals("Wrong contents", contents, "test");
    }

    public void testSlashesInPageNames()
         throws Exception
    {
        WikiPage page = m_engine.createPage("Test/Foobar");

        m_provider.putPageText( page, "test" );
   
        File resultfile = new File( m_pagedir, "Test%2FFoobar.txt" );
   
        assertTrue("No such file", resultfile.exists());
   
        String contents = FileUtil.readContents( new FileInputStream(resultfile),
                                                 "ISO-8859-1" );
   
        assertEquals("Wrong contents", contents, "test");
    }

    public void testDotsInBeginning()
       throws Exception
    {
        WikiPage page = m_engine.createPage(".Test");

        m_provider.putPageText( page, "test" );

        File resultfile = new File( m_pagedir, "%2ETest.txt" );

        assertTrue("No such file", resultfile.exists());

        String contents = FileUtil.readContents( new FileInputStream(resultfile), "ISO-8859-1" );

        assertEquals("Wrong contents", contents, "test");
    }
    
    public void testAuthor()
        throws Exception
    {
        try
        {
            WikiPage page = m_engine.createPage("\u00c5\u00e4Test");
            page.setAuthor("Min\u00e4");

            m_provider.putPageText( page, "test" );

            WikiPage page2 = m_provider.getPageInfo( "\u00c5\u00e4Test", 1 );

            assertEquals( "Min\u00e4", page2.getAuthor() );
        }
        finally
        {
            File resultfile = new File( m_pagedir,
                                        "%C5%E4Test.txt" );
            try
            {
                resultfile.delete();
            }
            catch(Exception e) {}

            resultfile = new File( m_pagedir,
                                   "%C5%E4Test.properties" );
            try
            {
                resultfile.delete();
            }
            catch(Exception e) {}
        }
    }

    public void testNonExistantDirectory()
        throws Exception
    {
        String tmpdir = m_pagedir;
        String dirname = "non-existant-directory";

        String newdir = tmpdir + File.separator + dirname;

        Properties pr = new Properties();

        pr.setProperty( FileSystemProvider.PROP_PAGEDIR, 
                           newdir );

        FileSystemProvider test = new FileSystemProvider();

        test.initialize( m_engine, pr );

        File f = new File( newdir );

        assertTrue( "didn't create it", f.exists() );
        assertTrue( "isn't a dir", f.isDirectory() );

        f.delete();
    }

    public void testDirectoryIsFile()
        throws Exception
    {
        File tmpFile = null;

        try
        {
            tmpFile = FileUtil.newTmpFile("foobar"); // Content does not matter.

            Properties pr = new Properties();

            pr.setProperty( FileSystemProvider.PROP_PAGEDIR, 
                               tmpFile.getAbsolutePath() );

            FileSystemProvider test = new FileSystemProvider();

            try
            {
                test.initialize( m_engine, pr );

                fail( "Wiki did not warn about wrong property." );
            }
            catch( IOException e )
            {
                // This is okay.
            }
        }
        finally
        {
            if( tmpFile != null )
            {
                tmpFile.delete();
            }
        }
    }

    public void testDelete()
        throws Exception
    {
        String files = props.getProperty( FileSystemProvider.PROP_PAGEDIR );

        WikiPage p = m_engine.createPage("Test");
        p.setAuthor("AnonymousCoward");
        
        m_provider.putPageText( p, "v1" );

        File f = new File( files, "Test"+FileSystemProvider.FILE_EXT );

        assertTrue( "file does not exist", f.exists() );
        
        f = new File( files, "Test.properties" );
        
        assertTrue( "property file does not exist", f.exists() );
        
        m_provider.deletePage( "Test" );

        f = new File( files, "Test"+FileSystemProvider.FILE_EXT );

        assertFalse( "file exists", f.exists() );
        
        f = new File( files, "Test.properties" );
        
        assertFalse( "properties exist", f.exists() );
    }

    public static Test suite()
    {
        return new TestSuite( FileSystemProviderTest.class );
    }
}
