package org.drools.workbench.screens.guided.dtable.backend.server;

import org.drools.workbench.models.guided.dtable.backend.GuidedDTXMLPersistence;
import org.drools.workbench.models.guided.dtable.shared.model.GuidedDecisionTable52;

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
public class GuidedDecisionTable52MessageBodyReader implements MessageBodyReader<GuidedDecisionTable52> {
    @Override
    public boolean isReadable(
            Class<?> aClass,
            Type type,
            Annotation[] annotations,
            MediaType mediaType)
    {
        return aClass == GuidedDecisionTable52.class;
    }

    @Override
    public GuidedDecisionTable52 readFrom(
            Class<GuidedDecisionTable52> aClass,
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
        return GuidedDTJSONPersistence.getInstance().unmarshal(
            buffer.toString());
    }
}
