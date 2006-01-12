package com.ecyrd.jspwiki.web;

import junit.framework.TestCase;
import net.sourceforge.jwebunit.WebTester;

public class CommonTests extends TestCase
{
    protected static final String PASSWORD = "secreto";
    protected WebTester t;
    protected final String m_baseURL;

    public CommonTests( String name, String baseURL )
    {
        super( name );
        t = new WebTester();
        t.getTestContext().setBaseUrl( baseURL );
        m_baseURL = baseURL;
    }

    public void setUp()
    {
    }

    public void testAnonymousView()
    {
        // Start at main, then navigate to About; verify user not logged in
        t.beginAt( "/Wiki.jsp?page=Main" );
        t.assertTextPresent( "You have successfully installed" );
        t.assertLinkPresentWithText( "About" );
        t.clickLinkWithText( "About" );
        t.assertTextPresent( "This Wiki is done using" );
    }
    
    public void testAssertedName()
    {
        // Navigate to Prefs page; set user cookie
        t.beginAt( "/Wiki.jsp?page=Main" );
        t.assertTextNotPresent( "G'day" );
        t.clickLinkWithText( "My Prefs" );
        t.assertTextPresent( "Set your user preferences here." );
        t.assertFormPresent( "setCookie" );
        t.setWorkingForm( "setCookie" );
        t.assertFormNotPresent( "clearCookie" );
        t.assertFormElementPresent( "assertedName" );
        t.assertSubmitButtonPresent( "ok" );
        t.setFormElement( "assertedName", "Don Quixote" );
        t.submit( "ok" );
        t.assertTextPresent( "G'day" );
        t.assertTextPresent( "Don Quixote" );
        t.assertTextPresent( "(not logged in)" );

        // Clear user cookie
        t.clickLinkWithText( "My Prefs" );
        t.assertFormNotPresent( "setCookie" );
        t.setWorkingForm( "clearCookie" );
        t.assertFormPresent( "clearCookie" );
        t.assertSubmitButtonPresent( "ok" );
        t.submit( "ok" );
        t.assertTextNotPresent( "G'day" );
        t.assertTextNotPresent( "Don Quixote" );
        t.assertTextNotPresent( "(not logged in)" );
    }
    
    public void testCreateGroup()
    {
        login();
        String group = "Test" + String.valueOf( System.currentTimeMillis() );
        t.beginAt( "/NewGroup.jsp" );
        t.assertFormPresent( "newGroup" );
        t.setWorkingForm( "newGroup" );
        t.assertFormElementPresent( "members" );
        t.assertSubmitButtonPresent( "ok" );
        t.submit( "ok" );
        
        //Oops! We forgot to name it
        t.assertTextPresent( "Could not create group: Group name may not be blank" );
        t.setFormElement( "name", group );
        t.setFormElement( "members", "" );
        t.submit( "ok" );
        
        //Oops! We forgot to include 
        t.assertTextPresent( "Could not create group: The group must have at least one member." );
        t.setFormElement( "name", group );
        t.setFormElement( "members", "Janne Jalkanen" );
        t.submit( "ok" );
        
        // Verify the group was created 
        t.assertTextNotPresent( "Could not create group" );
        t.gotoPage("/Wiki.jsp?page=Group" + group );
        t.assertTextPresent( "This is a wiki group." );
        
        // Verifiy that anonymous users can't view the page
        t = new WebTester();
        t.getTestContext().setBaseUrl( m_baseURL );
        t.beginAt( "/Wiki.jsp" );
        t.gotoPage("/Wiki.jsp?page=Group" + group );
        t.assertTextPresent( "Please sign in" );
        
        // Log in again and verify we can read it
        login();
        t.gotoPage("/Wiki.jsp?page=Group" + group );
        t.assertTextPresent( "This is a wiki group." );
    }
    
    protected void createProfile()
    {
        createProfile( true );
    }
    
    
    protected void createProfile(boolean withPassword)
    {
        // Navigate to profile tab
        t.beginAt( "/Wiki.jsp?page=Main" );
        t.assertTextNotPresent( "G'day" );
        t.clickLinkWithText( "My Prefs" );
        t.assertFormPresent( "editProfile" );
        t.setWorkingForm( "editProfile" );
        t.assertFormElementPresent( "loginname" );
        t.assertSubmitButtonPresent( "ok" );

        // Create user profile with generated user name
        String suffix = generateSuffix();
        t.setFormElement( "loginname", "pvilla" + suffix );
        t.setFormElement( "wikiname", "PanchoVilla" + suffix );
        t.setFormElement( "fullname", "Pancho Villa" + suffix );
        t.setFormElement( "email", "pvilla@bandito.org" );
        if ( withPassword )
        {
            t.assertFormElementPresent( "password" );
            t.assertFormElementPresent( "password2" );
            t.setFormElement( "password", PASSWORD + suffix );
            t.setFormElement( "password2", PASSWORD + suffix );
        }
        t.submit( "ok" );
    }
    
    protected String generateSuffix()
    {
        return String.valueOf( System.currentTimeMillis() );
    }
    
    public void testLogin()
    {
        // Start at front page; try to log in
        t.beginAt( "/Wiki.jsp?page=Main" );
        t.assertTextNotPresent( "G'day" );
        t.clickLinkWithText( "Log in" );
        t.assertTextPresent( "Please sign in" );
        t.assertFormPresent( "login" );
        t.setWorkingForm( "login" );
        t.assertFormElementPresent( "j_username" );
        t.assertFormElementPresent( "j_password" );
        t.assertSubmitButtonPresent( "action" );
        t.setFormElement( "j_username", "janne" );
        t.setFormElement( "j_password", "myP@5sw0rd" );
        t.submit( "action" );
        t.assertTextNotPresent( "Please sign in" );
        t.assertTextPresent( "G'day" );
        t.assertTextPresent( "Janne" ); // This is a hack: detecting full
                                            // name isn't working (?)
        t.assertTextPresent( "(authenticated)" );
    }

    public void login() 
    {
        // Start at front page; try to log in
        t.beginAt( "/Wiki.jsp?page=Main" );
        t.clickLinkWithText( "Log in" );
        t.setWorkingForm( "login" );
        t.setFormElement( "j_username", "janne" );
        t.setFormElement( "j_password", "myP@5sw0rd" );
        t.submit( "action" );
    }
    
    /**
     * Generates and registers a user with a unique name.
     * @return the generated account name
     */
    protected String newUser()
    {
        String baseURL = t.getTestContext().getBaseUrl();
        String suffix = generateSuffix();
        String user = "pvilla" + suffix;
        t.beginAt( "/UserPreferences.jsp" );
        t.setFormElement( "loginname", user );
        t.setFormElement( "password", PASSWORD );
        t.setFormElement( "password2", PASSWORD );
        t.setFormElement( "wikiname", "PanchoVilla" + suffix );
        t.setFormElement( "fullname", "Pancho Villa" + suffix );
        t.setFormElement( "email", "pvilla@bandito.org" );
        t.submit( "ok" );
        t.assertTextNotPresent( "Could not save profile" );
        t = new WebTester();
        t.getTestContext().setBaseUrl( baseURL );
        return user;
    }
    
}