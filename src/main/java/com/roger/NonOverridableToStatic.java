package com.roger;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Flag;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.emptyList;
import static org.openrewrite.Tree.randomId;

/**
 * Recipe to add the static keyword to methods that are not overridable (private or final)
 * and don't access instance variables.
 *
 * @author Roger Horn
 *
 */
public class NonOverridableToStatic extends Recipe {

    /**
     * Returns a short display name for this recipe
     *
     * @return The display name
     */
    @Override
    public String getDisplayName() {
        return "NonOverridableToStatic";
    }

    /**
     * Returns a description for this recipe
     *
     * @return The description
     */
    @Override
    public String getDescription() {
        return "Convert Non-Overridable Methods without instance access to static methods.";
    }

    @Override
    protected JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new NonOverridableToStaticVisitor();
    }

    /*
    * Main visitor for the recipe.  Visit the method declaration to determine if the static
    * keyword should be added.
    */
    private class NonOverridableToStaticVisitor extends JavaIsoVisitor<ExecutionContext> {

        /*
        * visit the method declaration, use the modifiers for the method and check for instance
        * variable access to see if the static keyword should be added.
        */
        @Override
        public MethodDeclaration visitMethodDeclaration(MethodDeclaration m, ExecutionContext c) {
            m = super.visitMethodDeclaration(m, c);

            ArrayList<J.Modifier> newModifiers = new ArrayList<>();
            Iterator modifiers = m.getModifiers().iterator();
            boolean overridable = true;

            //get all the modifiers and add them to a new list in
            // anticipation of replacing the old list on the method signature
            while( modifiers.hasNext() ){
                J.Modifier modifier = (J.Modifier) modifiers.next();
                newModifiers.add(modifier);

                //first check to see if the method is already 'static', no need to make a change, just return
                if(modifier.getType().equals(J.Modifier.Type.Static)){
                    return m;
                }
                //otherwise check to see if the method is overridable.  If so record it for now
                //as we have to continue first to check all remaining modifiers.
                if(modifier.getType().equals(J.Modifier.Type.Private) || modifier.getType().equals(J.Modifier.Type.Final)){
                    overridable=false;
                }

            }

            //return if the method is overridable since we don't want to add 'static' in this case
            if (overridable){
                return m;
            }

            //if the method is NOT overridable, but accesses instance variables, just return again
            //as we don't want to add 'static' in this case either.
            //
            //send the method body to the find method which will be visited to determine any instance access
            if(m.getBody()!=null) {
                if( FindAnyInstanceVariables.find(m.getBody()).get()){
                    return m;
                }
            }
            // if it's NOT overridable, and it does NOT access instance variables, add the static modifier to the method.
            Space singleSpace = Space.build(" ", emptyList());
            J.Modifier newModifier = new J.Modifier(randomId(), singleSpace, Markers.EMPTY, J.Modifier.Type.Static, emptyList());
            newModifiers.add(1, newModifier);
            m = m.withModifiers(newModifiers);

            return m;
        }
    }

    /*
    * sub-visitor used to find instance variables
    */
    @Value
    @EqualsAndHashCode(callSuper = true)
    private static class FindAnyInstanceVariables extends JavaIsoVisitor<AtomicBoolean> {

        /*
        * static method used to start traversing the AST.  In this case the method body for which we
        * want to find any instance variable access.
        *
        * Create an AtomicBoolean to record if we find any instance access
        */
        static AtomicBoolean find(J j) {
            return new FindAnyInstanceVariables().reduce(j, new AtomicBoolean(false));
        }

        @Override
        public J.VariableDeclarations.NamedVariable visitVariable(J.VariableDeclarations.NamedVariable nv, AtomicBoolean ab) {
            return nv.withInitializer(visitAndCast(nv.getInitializer(), ab));
        }

        /*
        * visit each idetifier, and check to see if it is a field.  If so, see if it is declared
        * at the class level (instance variable) or Method level (local variable)
        */
        @Override
        public J.Identifier visitIdentifier(J.Identifier identifier, AtomicBoolean isInstanceVar) {
            //if we've already found that the method accesses instance data, just return
            if(isInstanceVar.get()){
                return identifier;
            }
            identifier = super.visitIdentifier(identifier, isInstanceVar);

            //if the identifier's field type is null, it's not a field so just continue
            //If it's not null AND it's not static it's an instance var.
            if (identifier.getFieldType()!=null && !identifier.getFieldType().hasFlags(Flag.Static)){
                    isInstanceVar.set(true);
            }

            return identifier;
        }

    }

}
