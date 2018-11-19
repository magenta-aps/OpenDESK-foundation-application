/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.constraints;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.alfresco.repo.dictionary.constraint.AbstractConstraint;
import org.alfresco.repo.dictionary.constraint.RegexConstraint;
import static org.alfresco.repo.dictionary.constraint.RegexConstraint.CONSTRAINT_REGEX_MSG_PREFIX;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 *
 * @author martin
 */
public class UnicodeRegexConstraint extends AbstractConstraint{
    //Reuse regex messages
    public static final String CONSTRAINT_REGEX_NO_MATCH = "d_dictionary.constraint.regex.no_match";
    public static final String CONSTRAINT_REGEX_MATCH = "d_dictionary.constraint.regex.match";
    public static final String CONSTRAINT_REGEX_MSG_PREFIX = "d_dictionary.constraint.regex.error.";
    
    protected String expression;
    protected Pattern patternMatcher;
    protected boolean caseSensitive = true;
    protected boolean requiresMatch = true;
    
    /**
     * {@inheritDoc}
     */
    public String getType()
    {
        return "REGEX";
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("RegexConstraint")
          .append("[ expression=").append(expression)
          .append(", requiresMatch=").append(requiresMatch)
          .append("]");
        return sb.toString();
    }
    
    
/**
     * @return Returns the regular expression similar to the {@link String#matches(java.lang.String)}
     */
    public String getExpression()
    {
        return expression;
    }

    /**
     * Set the regular expression used to evaluate String values
     * @param expression regular expression similar to the {@link String#matches(java.lang.String)} argument
     */
    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    /**
     * @return Returns <tt>true</tt> if the value must match the regular expression
     *      or <tt>false</tt> if the value must not match the regular expression
     */
    public boolean getRequiresMatch()
    {
        return requiresMatch;
    }

    /**
     * Set whether the regular expression must be matched or not
     * 
     * @param requiresMatch Set to <tt>true</tt> if the value must match the regular expression
     *      or <tt>false</tt> if the value must not match the regular expression
     */
    public void setRequiresMatch(boolean requiresMatch)
    {
        this.requiresMatch = requiresMatch;
    }

    
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Specifies if the regex match should be case-sensitive
     * @param caseSensitive
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
    
    

    @Override
    public Map<String, Object> getParameters()
    {
        Map<String, Object> params = new HashMap<String, Object>(2);
        
        params.put("expression", this.expression);
        params.put("requiresMatch", this.requiresMatch);
        
        return params;
    }
    
    @Override
    public void initialize()
    {
        checkPropertyNotNull("expression", expression);
        this.patternMatcher = Pattern.compile(expression, Pattern.UNICODE_CHARACTER_CLASS+(!caseSensitive ? Pattern.UNICODE_CASE : 0));
    }

    @Override
    protected void evaluateSingleValue(Object value)
    {
        // convert the value to a String
        String valueStr = DefaultTypeConverter.INSTANCE.convert(String.class, value);
        Matcher matcher = patternMatcher.matcher(valueStr);
        boolean matches = matcher.matches();
        if (matches != requiresMatch)
        {
            // Look for a message corresponding to this constraint name
            String messageId = CONSTRAINT_REGEX_MSG_PREFIX + getShortName();
            if (I18NUtil.getMessage(messageId, value) != null)
            {
                throw new ConstraintException(messageId, value);
            }
            // Otherwise, fall back to a generic (but unfriendly) message
            else if (requiresMatch)
            {
                throw new ConstraintException(RegexConstraint.CONSTRAINT_REGEX_NO_MATCH, value, expression);
            }
            else
            {
                throw new ConstraintException(RegexConstraint.CONSTRAINT_REGEX_MATCH, value, expression);
            }
        }
    }
    
    
    
}
