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
package org.apache.wiki.ui.stripes;

import java.lang.reflect.Method;

import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;

import org.apache.wiki.action.ViewActionBean;
import org.apache.wiki.action.WikiActionBean;
import org.apache.wiki.content.MissingParameterException;
import org.apache.wiki.filters.SpamFilter;

/**
 * Stripes Interceptor that ensures that SpamFilter algorithms are applied to
 * events annotated with the {@link SpamProtect} annotation. This class
 * processes form parameters generated by the
 * {@link org.apache.wiki.tags.SpamProtectTag} tag. It fires after the
 * {@link LifecycleStage#HandlerResolution} stage; that is, after ActionBean and
 * event handler resolution, but before parameter binding.
 */
@Intercepts( { LifecycleStage.HandlerResolution } )
public class SpamInterceptor implements Interceptor
{
    /**
     * Validates spam parameters contained in any requests targeting an
     * ActionBean method annotated with the {@link SpamProtect} annotation. This
     * simply delegates to
     * {@link SpamFilter#validateSpamParams(WikiActionBean, String[])} and
     * {@link SpamFilter#validateUTF8Param(WikiActionBean)} in sequence, and
     * returns a {@link RedirectResolution} to the WikiPage
     * <code>SessionExpired</code> if either of these checks fail. If the
     * targeted ActionBean event is not annotated, this method returns
     * <code>null</code>.
     */
    public Resolution intercept( ExecutionContext context ) throws Exception
    {
        // Execute all other interceptors first
        Resolution r = context.proceed();
        if ( r != null )
        {
            return r;
        }

        // Is the target handler protected by a @SpamProtect annotation?
        WikiActionBean actionBean = (WikiActionBean) context.getActionBean();
        Method handler = context.getHandler();
        SpamProtect ann = handler.getAnnotation( SpamProtect.class );
        if( ann == null )
        {
            return null;
        }

        // Validate spam token/trap params
        try
        {
            SpamFilter.validateSpamParams( actionBean, ann.content() );
        }
        catch( MissingParameterException e )
        {
            return new RedirectResolution( ViewActionBean.class, "view" ).addParameter( "page", "SessionExpired" );
        }

        // Validate non-Latin1 param
        try
        {
            SpamFilter.validateUTF8Param( actionBean );
        }
        catch( MissingParameterException e )
        {
            return new RedirectResolution( ViewActionBean.class, "view" ).addParameter( "page", "SessionExpired" );
        }

        return null;
    }

}
