package net.coru.kloadgen.loadgen.impl;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;


import java.util.Collections;
import java.util.List;

public class ProtobufSchemaMetadata extends SchemaMetadata {
    private int id;
    private int version;
    private String schemaType;
    private String schema;
    private List<SchemaReference> references;

    public ProtobufSchemaMetadata(int id, int version, String schema) {
        super(id, version, ProtobufSchema.TYPE, Collections.emptyList(), schema);
    }
}
