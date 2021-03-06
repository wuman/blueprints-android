package com.tinkerpop.blueprints.impls.rexster;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
abstract class RexsterElement implements Element {

    protected final Object id;
    protected final RexsterGraph graph;

    public RexsterElement(final JSONObject rawElement, final RexsterGraph graph) {
        this.id = rawElement.opt(RexsterTokens._ID);
        this.graph = graph;
    }

    public Object getId() {
        return this.id;
    }

    public Set<String> getPropertyKeys() {
        JSONObject rawElement;

        if (this instanceof Vertex)
            rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()));
        else
            rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(this.getId()));

        Set<String> keys = new HashSet<String>();
        Iterator keyIterator = rawElement.keys();
        while (keyIterator.hasNext()) {
            keys.add((String) keyIterator.next());
        }

        keys.remove(RexsterTokens._TYPE);
        keys.remove(RexsterTokens._LABEL);
        keys.remove(RexsterTokens._ID);
        keys.remove(RexsterTokens._OUTE);
        keys.remove(RexsterTokens._INE);
        keys.remove(RexsterTokens._OUTV);
        keys.remove(RexsterTokens._INV);
        return keys;
    }

    public Object getProperty(final String key) {
        JSONObject rawElement;
        if (this instanceof Vertex)
            rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RexsterTokens.REXSTER_SHOW_TYPES_EQUALS_TRUE);
        else
            rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RexsterTokens.REXSTER_SHOW_TYPES_EQUALS_TRUE);

        JSONObject typedProperty = rawElement.optJSONObject(key);
        if (null != typedProperty)
            return RestHelper.typeCast(typedProperty.optString(RexsterTokens.TYPE), typedProperty.opt(RexsterTokens.VALUE));
        else
            return null;
    }

    public void setProperty(final String key, final Object value) {
        if (key.equals(StringFactory.ID))
            throw ExceptionFactory.propertyKeyIdIsReserved();
        if (key.equals(StringFactory.LABEL) && this instanceof Edge)
            throw ExceptionFactory.propertyKeyLabelIsReservedForEdges();
        if (key.equals(StringFactory.EMPTY_STRING))
            throw ExceptionFactory.elementKeyCanNotBeEmpty();

        if (key.startsWith(RexsterTokens.UNDERSCORE))
            throw new RuntimeException("RexsterGraph does not support property keys that start with underscore");

        if (this instanceof Vertex) {
            RestHelper.postResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RestHelper.encode(key) + RexsterTokens.EQUALS + RestHelper.uriCast(value));
        } else {
            RestHelper.postResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RestHelper.encode(key) + RexsterTokens.EQUALS + RestHelper.uriCast(value));
        }
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public Object removeProperty(final String key) {

        Object object = this.getProperty(key);

        if (this instanceof Vertex)
            RestHelper.delete(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RestHelper.encode(key));
        else
            RestHelper.delete(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RestHelper.encode(key));

        return object;
    }

    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

}
