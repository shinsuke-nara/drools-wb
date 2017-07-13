package org.drools.workbench.screens.guided.dtable.backend.server;

import org.drools.workbench.models.guided.dtable.backend.GuidedDTXMLPersistence;
import org.drools.workbench.models.guided.dtable.shared.model.GuidedDecisionTable52;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces(MediaType.APPLICATION_XML)
public class GuidedDecisionTable52MessageBodyWriter implements MessageBodyWriter<GuidedDecisionTable52> {
    @Override
    public boolean isWriteable(
            Class<?> aClass,
            Type type,
            Annotation[] annotations,
            MediaType mediaType)
    {
        return aClass == GuidedDecisionTable52.class;
    }

    @Override
    public long getSize(
            GuidedDecisionTable52 guidedDecisionTable52,
            Class<?> aClass,
            Type type,
            Annotation[] annotations,
            MediaType mediaType)
    {
        return -1;
    }

    @Override
    public void writeTo(
            GuidedDecisionTable52 guidedDecisionTable52,
            Class<?> aClass,
            Type type,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, Object> multivaluedMap,
            OutputStream outputStream)
        throws IOException, WebApplicationException
    {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        writer.write(
            GuidedDTXMLPersistence.getInstance().marshal(guidedDecisionTable52));
        writer.flush();
    }
}
