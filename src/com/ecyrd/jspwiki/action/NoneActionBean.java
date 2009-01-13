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

package com.ecyrd.jspwiki.action;

import com.ecyrd.jspwiki.ui.stripes.WikiRequestContext;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;

/**
 * Represents a dummy WikiContext that doesn't bind to a URL, and doesn't
 * contain any embedded logic. When the NoneActionBean class is passed as a
 * parameter to a method that produces an URL, the resulting URL does not
 * prepend anything before the page.
 * 
 * @author Andrew Jaquith
 */
public class NoneActionBean extends AbstractPageActionBean
{
    @DefaultHandler
    @HandlesEvent( "none" )
    @WikiRequestContext( "none" )
    public Resolution view()
    {
        return null;
    }
}
