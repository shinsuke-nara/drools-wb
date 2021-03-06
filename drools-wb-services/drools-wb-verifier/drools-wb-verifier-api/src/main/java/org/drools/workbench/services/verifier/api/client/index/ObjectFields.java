/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.workbench.services.verifier.api.client.index;

import org.drools.workbench.services.verifier.api.client.index.matchers.Matcher;
import org.drools.workbench.services.verifier.api.client.index.select.Listen;
import org.drools.workbench.services.verifier.api.client.index.select.Select;

public class ObjectFields
        extends FieldsBase<ObjectField> {

    public Where<FieldSelector, FieldListen> where( final Matcher matcher ) {
        return new Where<FieldSelector, FieldListen>() {
            @Override
            public FieldSelector select() {
                return new FieldSelector( matcher );
            }

            @Override
            public FieldListen listen() {
                return new FieldListen( matcher );
            }
        };
    }


    public class FieldSelector
            extends Select<ObjectField> {

        public FieldSelector( final Matcher matcher ) {
            super( map.get( matcher.getKeyDefinition() ),
                   matcher );
        }

    }

    public class FieldListen
            extends Listen<ObjectField> {

        public FieldListen( final Matcher matcher ) {
            super( map.get( matcher.getKeyDefinition() ),
                   matcher );
        }
    }

}
