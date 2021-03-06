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
package org.drools.workbench.services.verifier.api.client.index.select;

import java.util.Collection;
import java.util.List;

import org.drools.workbench.services.verifier.api.client.AnalyzerConfigurationMock;
import org.drools.workbench.services.verifier.api.client.index.keys.Key;
import org.drools.workbench.services.verifier.api.client.index.keys.UUIDKey;
import org.drools.workbench.services.verifier.api.client.index.keys.Value;
import org.drools.workbench.services.verifier.api.client.index.matchers.ExactMatcher;
import org.drools.workbench.services.verifier.api.client.maps.KeyDefinition;
import org.drools.workbench.services.verifier.api.client.maps.KeyTreeMap;
import org.drools.workbench.services.verifier.api.client.maps.MultiMap;
import org.drools.workbench.services.verifier.api.client.maps.MultiMapFactory;
import org.drools.workbench.services.verifier.api.client.maps.util.HasKeys;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SelectWithNegativeExactMatcherWhenTheValueIsNotInTheMapTest {

    private Select<Item> select;

    private MultiMap<Value, Item, List<Item>> makeMap() {
        final MultiMap<Value, Item, List<Item>> map = MultiMapFactory.make( false );

        map.put( new Value( 0 ),
                 new Item( 0 ) );
        map.put( new Value( 56 ),
                 new Item( 56 ) );
        map.put( new Value( 100 ),
                 new Item( 100 ) );
        map.put( new Value( 1200 ),
                 new Item( 1200 ) );
        return map;
    }

    private void fill( final KeyTreeMap<Item> itemKeyTreeMap,
                       final Item item ) {
        itemKeyTreeMap.put( item );
    }

    @Before
    public void setUp() throws Exception {
        this.select = new Select<>( makeMap(),
                                    new ExactMatcher( null,
                                                      "cost",
                                                      true ) );
    }

    @Test
    public void testAll() throws Exception {
        final Collection<Item> all = select.all();

        assertEquals( 4, all.size() );
    }

    @Test
    public void testFirst() throws Exception {
        assertEquals( 0,
                      select.first().cost );
    }

    @Test
    public void testLast() throws Exception {
        assertEquals( 1200,
                      select.last().cost );
    }

    private class Item
            implements HasKeys {

        private int cost;
        private UUIDKey uuidKey = new AnalyzerConfigurationMock().getUUID( this );

        public Item( final int cost ) {
            this.cost = cost;
        }

        @Override
        public Key[] keys() {
            return new Key[]{
                    uuidKey,
                    new Key( KeyDefinition.newKeyDefinition().withId( "cost" ).build(),
                             cost )
            };
        }

        @Override
        public UUIDKey getUuidKey() {
            return uuidKey;
        }
    }
}