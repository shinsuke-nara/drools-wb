package org.drools.workbench.screens.guided.dtable.backend.server;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import org.drools.workbench.models.guided.dtable.backend.util.GuidedDecisionTableUpgradeHelper1;
import org.drools.workbench.models.guided.dtable.backend.util.GuidedDecisionTableUpgradeHelper2;
import org.drools.workbench.models.guided.dtable.backend.util.GuidedDecisionTableUpgradeHelper3;
import org.drools.workbench.models.guided.dtable.shared.model.*;
import org.drools.workbench.models.guided.dtable.shared.model.legacy.*;

import java.math.BigDecimal;

// TODO: Fix me.
/*
  This is JSON version of GuidedDTXMLPersistence.
  The only difference between GuidedDTXMLPersistence and this class is driver
  instance of XStream.

  This file is created by coping GuidedDTXMLPersistence.
  This is not simple way. We should fix.
 */
@SuppressWarnings("deprecation")
public class GuidedDTJSONPersistence {

    private XStream xt;
    private static final GuidedDecisionTableUpgradeHelper1 upgrader1 = new GuidedDecisionTableUpgradeHelper1();
    private static final GuidedDecisionTableUpgradeHelper2 upgrader2 = new GuidedDecisionTableUpgradeHelper2();
    private static final GuidedDecisionTableUpgradeHelper3 upgrader3 = new GuidedDecisionTableUpgradeHelper3();
    private static final GuidedDTJSONPersistence INSTANCE = new GuidedDTJSONPersistence();

    private GuidedDTJSONPersistence() {
        xt = new XStream( new JettisonMappedXmlDriver() );

        //Legacy model
        xt.alias( "decision-table",
                  GuidedDecisionTable.class );
        xt.alias( "metadata-column",
                  MetadataCol.class );
        xt.alias( "attribute-column",
                  AttributeCol.class );
        xt.alias( "condition-column",
                  ConditionCol.class );
        xt.alias( "set-field-col",
                  ActionSetFieldCol.class );
        xt.alias( "retract-fact-column",
                  ActionRetractFactCol.class );
        xt.alias( "insert-fact-column",
                  ActionInsertFactCol.class );

        //Post 5.2 model
        xt.alias( "decision-table52",
                  GuidedDecisionTable52.class );
        xt.alias( "metadata-column52",
                  MetadataCol52.class );
        xt.alias( "attribute-column52",
                  AttributeCol52.class );
        xt.alias( "condition-column52",
                  ConditionCol52.class );
        xt.alias( "set-field-col52",
                  ActionSetFieldCol52.class );
        xt.alias( "retract-fact-column52",
                  ActionRetractFactCol52.class );
        xt.alias( "insert-fact-column52",
                  ActionInsertFactCol52.class );
        xt.alias( "value",
                  DTCellValue52.class );
        xt.alias( "Pattern52",
        		Pattern52.class );
        
        //See https://issues.jboss.org/browse/GUVNOR-1115
        xt.aliasPackage( "org.drools.guvnor.client",
                         "org.drools.ide.common.client");
                         
        //this is for migrating org.drools.ide.common.client.modeldriven.auditlog.AuditLog to org.drools.workbench.models.datamodel.auditlog.AuditLog
		xt.aliasPackage("org.drools.guvnor.client.modeldriven.dt52.auditlog",
				"org.drools.workbench.models.guided.dtable.shared.auditlog");

	    //this is for migrating org.drools.ide.common.client.modeldriven.dt52.auditlog.DecisionTableAuditLogFilter
		//to org.drools.workbench.models.guided.dtable.shared.auditlog.DecisionTableAuditLogFilter
		xt.aliasPackage("org.drools.guvnor.client.modeldriven.dt52",
				" org.drools.workbench.models.guided.dtable.shared.model");
        
        //All numerical values are historically BigDecimal
        xt.alias( "valueNumeric",
                  Number.class,
                  BigDecimal.class );

        // this is needed for OSGi as XStream needs to be able to load classes from the guided-dtable module
        // and the default classloader for XStream bundle in OSGi does not have access to those classes
        xt.setClassLoader( getClass().getClassLoader() );
    }

    public static GuidedDTJSONPersistence getInstance() {
        return INSTANCE;
    }

    public String marshal( GuidedDecisionTable52 dt ) {
        return xt.toXML( dt );
    }

    public GuidedDecisionTable52 unmarshal( String xml ) {
        if ( xml == null || xml.trim().equals( "" ) ) {
            return new GuidedDecisionTable52();
        }

        //Upgrade DTModel to new class
        Object model = xt.fromXML( xml );
        GuidedDecisionTable52 newDTModel;
        if ( model instanceof GuidedDecisionTable ) {
            GuidedDecisionTable legacyDTModel = (GuidedDecisionTable) model;
            newDTModel = upgrader1.upgrade( legacyDTModel );
        } else {
            newDTModel = (GuidedDecisionTable52) model;
        }

        //Upgrade RowNumber, Salience and Duration data-types are correct
        newDTModel = upgrader2.upgrade( newDTModel );

        //Upgrade Default Values to typed equivalents
        newDTModel = upgrader3.upgrade( newDTModel );

        return newDTModel;
    }

}