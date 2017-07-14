/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.workbench.screens.guided.dtable.backend.server;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.*;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Charsets;
import org.drools.workbench.models.datamodel.oracle.PackageDataModelOracle;
import org.drools.workbench.models.datamodel.workitems.PortableWorkDefinition;
import org.drools.workbench.models.guided.dtable.backend.GuidedDTXMLPersistence;
import org.drools.workbench.models.guided.dtable.shared.model.GuidedDecisionTable52;
import org.drools.workbench.screens.guided.dtable.model.GuidedDecisionTableEditorContent;
import org.drools.workbench.screens.guided.dtable.service.GuidedDecisionTableEditorService;
import org.drools.workbench.screens.workitems.service.WorkItemsEditorService;
import org.guvnor.common.services.backend.config.SafeSessionInfo;
import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.guvnor.common.services.backend.util.CommentedOptionFactory;
import org.guvnor.common.services.backend.validation.GenericValidator;
import org.guvnor.common.services.project.model.Package;
import org.guvnor.common.services.shared.metadata.MetadataService;
import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.guvnor.common.services.shared.metadata.model.Overview;
import org.guvnor.common.services.shared.validation.model.ValidationMessage;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.services.backend.service.KieService;
import org.kie.workbench.common.services.backend.source.SourceServices;
import org.kie.workbench.common.services.datamodel.backend.server.DataModelOracleUtilities;
import org.kie.workbench.common.services.datamodel.backend.server.service.DataModelService;
import org.kie.workbench.common.services.datamodel.model.PackageDataModelOracleBaselinePayload;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.ext.editor.commons.service.CopyService;
import org.uberfire.ext.editor.commons.service.DeleteService;
import org.uberfire.ext.editor.commons.service.RenameService;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.FileAlreadyExistsException;
import org.uberfire.java.nio.file.attribute.BasicFileAttributes;
import org.uberfire.rpc.SessionInfo;
import org.uberfire.workbench.events.ResourceOpenedEvent;

import static org.uberfire.backend.server.util.Paths.convert;

@javax.ws.rs.Path("gdst")
@Service
@ApplicationScoped
public class GuidedDecisionTableEditorServiceImpl
        extends KieService<GuidedDecisionTableEditorContent>
        implements GuidedDecisionTableEditorService {

    @Inject
    @Named( "ioStrategy" )
    private IOService ioService;

    @Inject
    private CopyService copyService;

    @Inject
    private DeleteService deleteService;

    @Inject
    private RenameService renameService;

    @Inject
    private Event<ResourceOpenedEvent> resourceOpenedEvent;

    @Inject
    private DataModelService dataModelService;

    @Inject
    private WorkItemsEditorService workItemsService;

    @Inject
    private GenericValidator genericValidator;

    @Inject
    private CommentedOptionFactory commentedOptionFactory;

    private SafeSessionInfo safeSessionInfo;

    public GuidedDecisionTableEditorServiceImpl() {

    }

    @Inject
    public GuidedDecisionTableEditorServiceImpl( final SessionInfo sessionInfo ) {
        safeSessionInfo = new SafeSessionInfo( sessionInfo );
    }

    @Override
    public Path create( final Path context,
                        final String fileName,
                        final GuidedDecisionTable52 content,
                        final String comment ) {
        try {
            final Package pkg = projectService.resolvePackage( context );
            final String packageName = (pkg == null ? null : pkg.getPackageName());
            content.setPackageName( packageName );

            final org.uberfire.java.nio.file.Path nioPath = Paths.convert( context ).resolve( fileName );
            final Path newPath = Paths.convert( nioPath );

            if ( ioService.exists( nioPath ) ) {
                throw new FileAlreadyExistsException( nioPath.toString() );
            }

            ioService.write( nioPath,
                             GuidedDTXMLPersistence.getInstance().marshal( content ),
                             commentedOptionFactory.makeCommentedOption( comment ) );

            return newPath;

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public GuidedDecisionTable52 load( final Path path ) {
        try {
            final String content = ioService.readAllString( Paths.convert( path ) );

            return GuidedDTXMLPersistence.getInstance().unmarshal( content );

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public GuidedDecisionTableEditorContent loadContent( final Path path ) {
        return super.loadContent( path );
    }

    @Override
    protected GuidedDecisionTableEditorContent constructContent( Path path, Overview overview ) {
        final GuidedDecisionTable52 model = load( path );
        final PackageDataModelOracle oracle = dataModelService.getDataModel( path );
        final PackageDataModelOracleBaselinePayload dataModel = new PackageDataModelOracleBaselinePayload();

        //Get FQCN's used by model
        final GuidedDecisionTableModelVisitor visitor = new GuidedDecisionTableModelVisitor( model );
        final Set<String> consumedFQCNs = visitor.getConsumedModelClasses();

        //Get FQCN's used by Globals
        consumedFQCNs.addAll( oracle.getPackageGlobals().values() );

        DataModelOracleUtilities.populateDataModel( oracle,
                                                    dataModel,
                                                    consumedFQCNs );

        final Set<PortableWorkDefinition> workItemDefinitions = workItemsService.loadWorkItemDefinitions( path );

        //Signal opening to interested parties
        resourceOpenedEvent.fire( new ResourceOpenedEvent( path,
                                                           safeSessionInfo ) );

        return new GuidedDecisionTableEditorContent( model,
                                                     workItemDefinitions,
                                                     overview,
                                                     dataModel );
    }

    @Override
    public PackageDataModelOracleBaselinePayload loadDataModel( final Path path ) {
        try {
            final PackageDataModelOracle oracle = dataModelService.getDataModel( path );
            final PackageDataModelOracleBaselinePayload dataModel = new PackageDataModelOracleBaselinePayload();
            //There are no classes to pre-load into the DMO when requesting a new Data Model only
            DataModelOracleUtilities.populateDataModel( oracle,
                                                        dataModel,
                                                        new HashSet<String>() );

            return dataModel;

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public Path save( final Path resource,
                      final GuidedDecisionTable52 model,
                      final Metadata metadata,
                      final String comment ) {
        try {
            final Package pkg = projectService.resolvePackage( resource );
            final String packageName = ( pkg == null ? null : pkg.getPackageName() );
            model.setPackageName( packageName );

            Metadata currentMetadata = metadataService.getMetadata( resource );
            ioService.write( Paths.convert( resource ),
                             GuidedDTXMLPersistence.getInstance().marshal( model ),
                             metadataService.setUpAttributes( resource,
                                                              metadata ),
                             commentedOptionFactory.makeCommentedOption( comment ) );

            fireMetadataSocialEvents( resource, currentMetadata, metadata );
            return resource;

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public void delete( final Path path,
                        final String comment ) {
        try {
            deleteService.delete( path,
                                  comment );

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public Path rename( final Path path,
                        final String newName,
                        final String comment ) {
        try {
            return renameService.rename( path,
                                         newName,
                                         comment );

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public Path copy( final Path path,
                      final String newName,
                      final String comment ) {
        try {
            return copyService.copy( path,
                                     newName,
                                     comment );

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public String toSource(final Path path,
                           final GuidedDecisionTable52 model) {
        return sourceServices.getServiceFor(Paths.convert(path)).getSource(Paths.convert(path), model);
    }

    @Override
    public List<ValidationMessage> validate( final Path path,
                                             final GuidedDecisionTable52 guidedDecisionTable52 ) {
        try {
            final String content = GuidedDTXMLPersistence.getInstance().marshal( guidedDecisionTable52 );

            return genericValidator.validate( path, content );

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @javax.ws.rs.Path("hello/{message}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@PathParam("message") String message) {
        return "hello, " + message;
    }

    @javax.ws.rs.Path("list/{branch}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> list(@PathParam("branch") String branch) {
        List<String> list = new ArrayList();
        list.addAll(getContent(
                ioService.getFileSystem(
                        URI.create("git://" + branch))
                        .getRootDirectories().iterator().next()));
        return list;
    }

    private List<String> getContent(
            org.uberfire.java.nio.file.Path parentPath) {
        List<String> list = new ArrayList();
        for (org.uberfire.java.nio.file.Path path :
                ioService.newDirectoryStream(parentPath)) {
            if (ioService.getFileSystem(path.toUri()).provider().readAttributes(
                    path, BasicFileAttributes.class).isRegularFile()) {
                list.add(path.toString());
            } else {
                list.addAll(getContent(path));
            }
        }
        return list;
    }

    @javax.ws.rs.Path("{path:.*}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public GuidedDecisionTable52 get(
            @PathParam("path") String path) {
        return loadContent(convert(ioService.get(
                            URI.create("git://" + path)))).getModel();
    }

    @javax.ws.rs.Path("{path:.*}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void save(@PathParam("path") String path, GuidedDecisionTable52 model) {
        save(convert(ioService.get(URI.create("git://" + path))), model, null, null);
    }
}
