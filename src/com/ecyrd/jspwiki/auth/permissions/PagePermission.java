package com.ecyrd.jspwiki.auth.permissions;

import java.security.Permission;
import java.util.Arrays;

import com.ecyrd.jspwiki.WikiPage;

/**
 * <p>Represents a permission to perform an operation on a single page or collection of pages.
 * Permission actions include: 
 * <code>view</code>, <code>edit</code>, <code>comment</code>, <code>upload</code> and 
 * <code>delete</code> and <code>rename</code>.</p>
 * <p>The target of a permission is a single page or collection. Collections
 * may be specified using a wildcard character (*). The wildcard may
 * be a prefix, suffix, or allby itself: for example, *Main, Main* or *.</p>
 * <p>For a given target, certain permissions imply others:
 * <code>delete</code> and <code>rename</code> imply <code>edit</code>; <code>edit</code>
 * implies <code>comment</code>, <code>upload</code>, and <code>view</code>;
 * <code>comment</code> and <code>upload</code> imply <code>view</code>.</p>
 * @author Andrew R. Jaquith
 * @version $Revision: 1.1.2.1 $ $Date: 2005-02-01 02:56:15 $
 */
public class PagePermission extends Permission
{

    private static final String COMMENT_ACTION = "comment";

    private static final String DELETE_ACTION  = "delete";

    private static final String EDIT_ACTION    = "edit";
    
    private static final String GROUP_SUFFIX   = "Group";

    private static final String RENAME_ACTION  = "rename";

    private static final String UPLOAD_ACTION  = "upload";

    private static final String VIEW_ACTION    = "view";

    protected static final int    COMMENT        = 0x4;

    protected static final int    DELETE         = 0x10;

    protected static final int    EDIT           = 0x2;

    protected static final int    RENAME         = 0x20;

    protected static final int    UPLOAD         = 0x8;

    protected static final int    VIEW           = 0x1;
    
    public static final PagePermission COMMENT_ALL    = new PagePermission( COMMENT_ACTION );

    public static final PagePermission DELETE_ALL     = new PagePermission( DELETE_ACTION );

    public static final PagePermission EDIT_ALL       = new PagePermission( EDIT_ACTION );

    public static final PagePermission RENAME_ALL     = new PagePermission( RENAME_ACTION );

    public static final PagePermission UPLOAD_ALL     = new PagePermission( UPLOAD_ACTION );

    public static final PagePermission VIEW_ALL       = new PagePermission( VIEW_ACTION );

    private final String        m_actionString;

    private final int           m_mask;

    private final String        m_target;
    
    /**
     * Convenience constructor that creates a new PagePermission 
     * for all pages (*) and set of actions.
     * @param actions
     */
    public PagePermission( String actions ) {
       this("*", actions); 
    }
    
    /**
     * Creates a new PagePermission for a specified page name and set of actions.
     * @param page
     * @param actions
     */
    public PagePermission( String page, String actions )
    {
        super( page );
        m_target = page;
        String pageActions[] = actions.toLowerCase().split( "," );
        Arrays.sort( pageActions, String.CASE_INSENSITIVE_ORDER );
        m_mask = createMask( actions );
        StringBuffer buffer = new StringBuffer();
        for( int i = 0; i < pageActions.length; i++ )
        {
            buffer.append( pageActions[i] );
            if ( i < ( pageActions.length - 1 ) )
            {
                buffer.append( "," );
            }
        }
        m_actionString = buffer.toString();
    }

    /**
     * Creates a new PagePermission for a specified page and set of actions.
     * @param page
     * @param actions
     */
    public PagePermission( WikiPage page, String actions ) {
        this( page.getName(), actions);
    }

    /**
     * Two PagePermission objects are considered equal if their actions (after
     * normalization) and target are equal.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public final boolean equals( Object obj )
    {
        if ( !( obj instanceof PagePermission ) )
        {
            return false;
        }
        PagePermission p = (PagePermission) obj;
        return ( p.m_mask == m_mask && p.m_target.equals( m_target ) );
    }

    /**
     * Returns the actions for this permission: "view", "edit", "comment",
     * "upload" or "delete." The actions will always be sorted in alphabetic
     * order, and will always appear in lower case.
     * @see java.security.Permission#getActions()
     */
    public final String getActions()
    {
        return m_actionString;
    }

    /**
     * Returns the hash code for this PagePermission.
     * @see java.lang.Object#hashCode()
     */
    public final int hashCode()
    {
        int hash = m_mask;
        String actions = getActions();
        for (int i = 0; i < actions.length(); i++) {
            hash += 13 * actions.hashCode();
        }
        return hash;
    }
    
    /**
     * <p>
     * PagePermission can only imply other PagePermissions; no other permission
     * types are implied.
     * </p>
     * <p>
     * One PagePermission implies another if its actions if two conditions are
     * met:
     * </p>
     * <ol>
     * <li>All of other PagePermission's actions are equal to, or a subset of,
     * those of this permission</li>
     * <li>The other PagePermission's target is equal to, or a subset of, the
     * target specified by this permission. This permission's target is
     * considered a superset of the other if it contains a matching prefix plus
     * a wildcard, or a wildcard followed by a matching suffix.</li>
     * </ol>
     * <p>Note: a significant (hard-coded) exception to the rule occurs
     * with pages ending in "DefaultGroup," because these are group member list pages.
     * For a permission whose target ends with "*", permission for all
     * pages ending in "DefaultGroup" is <em>not</em> implied.</p>
     * @see java.security.Permission#implies(java.security.Permission)
     */
    public final boolean implies( Permission permission )
    {
        // Permission must be a PagePermission
        if ( !( permission instanceof PagePermission ) )
        {
            return false;
        }

        // Build up an "implied mask"
        PagePermission p = (PagePermission) permission;
        int impliedMask = impliedMask( m_mask );
        
        // If actions aren't a proper subset, return false
        if ( ( impliedMask & p.m_mask ) != p.m_mask )
        {
            return false;
        }

        return isSubset( m_target, p.m_target );
    }
    
    /**
     * Prints a human-readable representation of this permission.
     * @see java.lang.Object#toString()
     */
    public final String toString()
    {
        return "(\"" + this.getClass().getName() + "\",\"" + m_target + "\",\"" + getActions() + "\")";
    }

    /**
     * Creates an "implied mask" based on the actions originally
     * assigned: for example, delete implies edit, comment, upload and view.
     * @param mask
     * @return
     */
    protected static final int impliedMask( int mask )
    {
        if ( ( mask & DELETE ) > 0 ) {
            mask |= EDIT;
        }
        if ( ( mask & RENAME ) > 0 ) {
            mask |= EDIT;
        }
        if ( ( mask & EDIT ) > 0 ) {
            mask |= ( COMMENT | UPLOAD );
        }
        if ( ( mask & COMMENT ) > 0 ) {
            mask |= VIEW;
        }
        if ( ( mask & UPLOAD ) > 0 ) {
            mask |= VIEW;
        }
        return mask;
    }

    /**
     * Determines whether one target string is a logical subset of the other.
     * @param superSet the prospective superset
     * @param subSet the prospective subset
     * @return the results of the test, where <code>true</code> indicates
     * that <code>subSet</code> is a subset of <code>superSet</code>
     */
    protected final boolean isSubset(String superSet, String subSet) {
        // If targets are identical, it's a subset
        if ( superSet.equals( subSet ) )
        {
            return true;
        }
                
        // If super is "*", it's a subset unless sub has "DefaultGroup" suffix
        if ( superSet.equals("*") )
        {
            return ( !subSet.endsWith( GROUP_SUFFIX ) );
        }
        
        // If super starts with "*", sub must end with everything after the *
        if ( superSet.startsWith("*"))
        {
            String suffix = superSet.substring(1);
            return subSet.endsWith(suffix);
        }
        
        // If super ends with "*", sub must start with everything before *
        if ( superSet.endsWith("*")) 
        {
            String prefix = superSet.substring(0, superSet.length() - 2);
            if ( !subSet.endsWith( GROUP_SUFFIX ) )
            {
                return subSet.startsWith( prefix );
            }
        }

        return false;
    }

    /**
     * Private method that creates a binary mask based on the actions specified.
     * This is used by {@link #implies(Permission)}.
     * @param actions
     * @return
     */
    protected static final int createMask( String actions )
    {
        if ( actions == null || actions.length() == 0 )
        {
            throw new IllegalArgumentException( "Actions cannot be blank or null" );
        }
        int mask = 0;
        String[] actionList = actions.split( "," );
        for( int i = 0; i < actionList.length; i++ )
        {
            String action = actionList[i];
            if ( action.equalsIgnoreCase( VIEW_ACTION ) )
            {
                mask |= VIEW;
            }
            else if ( action.equalsIgnoreCase( EDIT_ACTION ) )
            {
                mask |= EDIT;
            }
            else if ( action.equalsIgnoreCase( COMMENT_ACTION ) )
            {
                mask |= COMMENT;
            }
            else if ( action.equalsIgnoreCase( UPLOAD_ACTION ) )
            {
                mask |= UPLOAD;
            }
            else if ( action.equalsIgnoreCase( DELETE_ACTION ) )
            {
                mask |= DELETE;
            }
            else if ( action.equalsIgnoreCase( RENAME_ACTION ) )
            {
                mask |= RENAME;
            }
            else
            {
                throw new IllegalArgumentException( "Unrecognized action: " + action );
            }
        }
        return mask;
    }
}