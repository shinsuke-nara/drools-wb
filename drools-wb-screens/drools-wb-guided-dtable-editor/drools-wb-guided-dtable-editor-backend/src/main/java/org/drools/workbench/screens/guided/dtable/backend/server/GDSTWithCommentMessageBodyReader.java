package org.drools.workbench.screens.guided.dtable.backend.server;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class GDSTWithCommentMessageBodyReader implements MessageBodyReader<GDSTWithComment> {

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return aClass == GDSTWithComment.class;
    }

    @Override
    public GDSTWithComment readFrom(
            Class<GDSTWithComment> aClass,
            Type type,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> multivaluedMap,
            InputStream inputStream)
        throws IOException, WebApplicationException
    {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream));
        StringBuffer buffer = new StringBuffer();
        for (String line = reader.readLine();
             line != null;
             line = reader.readLine())
        {
            buffer.append(line);
        }

        try {
            JSONObject json = null;
            json = new JSONObject(buffer.toString());
            GDSTWithComment gdst = new GDSTWithComment();
            gdst.author = json.getString("author");
            gdst.comment = json.getString("comment");
            gdst.model = GuidedDTJSONPersistence.getInstance().unmarshal(
                json.getJSONObject("model").toString());
            return gdst;
        } catch (JSONException e) {
            throw new WebApplicationException(e);
        }
    }
}